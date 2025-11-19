package com.partnership.bjbdocumenttrackerreader.ui.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.partnership.bjbdocumenttrackerreader.MainActivity
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.databinding.FragmentStockOpnameBinding
import com.partnership.bjbdocumenttrackerreader.reader.BeepSoundManager
import com.partnership.bjbdocumenttrackerreader.reader.RFIDManager
import com.partnership.bjbdocumenttrackerreader.reader.ReaderKeyEventHandler
import com.partnership.bjbdocumenttrackerreader.ui.adapter.SoDocumentAdapter
import com.partnership.bjbdocumenttrackerreader.ui.home.DashboardViewModel
import com.partnership.bjbdocumenttrackerreader.util.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StockOpnameFragment : Fragment(), ReaderKeyEventHandler {

    private val stockOpnameViewModel: StockOpnameViewModel by activityViewModels()
    private val dashboardViewModel: DashboardViewModel by activityViewModels()
    private var loadingDialog: AlertDialog? = null
    private var isDocument: Boolean = true

    @Inject
    lateinit var reader: RFIDManager
    private var _binding: FragmentStockOpnameBinding? = null
    private var message = ""
    private val binding get() = _binding!!
    private lateinit var assetAdapter: SoDocumentAdapter

    @Inject
    lateinit var soundManager: BeepSoundManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStockOpnameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        dashboardViewModel.isDocumentSelected.observe(viewLifecycleOwner) {
            setupToolbar(it)
            isDocument = it
            if (stockOpnameViewModel.listBulkDocument.value == null) {
                lifecycleScope.launch {
                    stockOpnameViewModel.getBulkDocument(it)
                }
            }
        }
        stockOpnameViewModel.soundBeep.observe(viewLifecycleOwner) {
            if (it) {
                soundManager.playBeep()
            }
        }

        observeListDocument()
        binding.lyFound.setOnClickListener {
            stockOpnameViewModel.setIsThereFilter(true)
        }

        binding.lyTotal.setOnClickListener {
            stockOpnameViewModel.setIsThereFilter(null)
        }

        binding.lyNotFound.setOnClickListener {
            stockOpnameViewModel.setIsThereFilter(false)
        }

        binding.btScan.setOnClickListener {
            toggleScan()
        }

        stockOpnameViewModel.isScanning.observe(viewLifecycleOwner){
            if(it){
                binding.lyTimer.visibility = View.VISIBLE
                stockOpnameViewModel.elapsedTime.observe(viewLifecycleOwner){timer ->
                    binding.tvTimer.text = timer
                }
            }else{
                binding.lyTimer.visibility = View.GONE
            }
        }


        binding.btSend.setOnClickListener {
            if (reader.isInventorying() == true) {
                Toast.makeText(
                    requireActivity(),
                    "Hentikan scanning terlebih dahulu",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Konfirmasi Kirim")
                    .setMessage(message)
                    .setPositiveButton("Kirim") { dialog, _ ->
                        stockOpnameViewModel.postStockOpnameChunked(isDocument = isDocument, chunkSize = 500)
                        lifecycleScope.launch {
                            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                                stockOpnameViewModel.progress.collect {p ->
                                    updateLoadingProgress(
                                        current = p.current,
                                        total = p.total
                                    )
                                }
                            }
                        }
                        dialog.dismiss()
                        showLoadingDialog("Kirim data stock opname...", false)
                    }
                    .setNegativeButton("Periksa Kembali") { dialog, _ ->
                        dialog.dismiss()
                        Utils.dismissLoading()
                    }
                    .show()
            }


        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Cek kondisi, misal sedang scanning
                    if (reader.isInventorying() == true) {
                        Toast.makeText(
                            requireContext(),
                            "Hentikan scan terlebih dahulu!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // Tampilkan dialog konfirmasi
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Keluar Halaman?")
                            .setMessage("Stock Opname masih berjalan,jika anda keluar maka progress stock opname akan di hapus. apakah kamu yakin ingin keluar?")
                            .setPositiveButton("Ya") { _, _ ->
                                stockOpnameViewModel.clearScannedTags()
                                stockOpnameViewModel.clearBulkDocument()
                                findNavController().navigateUp()

                            }
                            .setNegativeButton("Batal") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    }
                }
            })


        lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                stockOpnameViewModel.assetStatusInfo.collect { (detected, total) ->
                    binding.tvTotal.text = total.toString()
                    binding.tvDetected.text = detected.toString()
                    val missing = total - detected
                    binding.tvUndetected.text = missing.toString()
                    message = if (total - detected == 0) {
                        "Kirim data stock opname?"
                    } else {
                        "Terdapat $missing barang yang tidak ditemukan. Apakah Anda ingin memeriksa kembali atau tetap mengirim data stock opname?"
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                stockOpnameViewModel.uploadResult.collect { result ->
                    result ?: return@collect
                    val msg = when (result) {
                        is ResultWrapper.Success -> "Stock Opname Berhasil Dikirim"
                        is ResultWrapper.ErrorResponse -> "Gagal (server): ${result.error}"
                        is ResultWrapper.NetworkError -> "Gagal (jaringan): ${result.error}"
                        is ResultWrapper.Error -> "Error: ${result.error}"
                        ResultWrapper.Loading -> "Loading..."
                    }
                    dismissLoadingDialog()
                    stockOpnameViewModel.clearScannedTags()
                    stockOpnameViewModel.clearBulkDocument()
                    stockOpnameViewModel.clearResultStockOpname()
                    findNavController().popBackStack()
                    Toast.makeText(requireActivity(), msg, Toast.LENGTH_SHORT).show()
                }
            }
        }

        lifecycleScope.launch {
            stockOpnameViewModel.pagedAssets.collectLatest { pagingData ->
                assetAdapter.submitData(pagingData)
            }
        }
    }

    private fun observeListDocument() {
        stockOpnameViewModel.listBulkDocument.observe(viewLifecycleOwner) {
            when (it) {
                is ResultWrapper.Error<*> -> {
                    dismissLoadingDialog()
                    Toast.makeText(requireActivity(), it.error, Toast.LENGTH_SHORT).show()
                }

                is ResultWrapper.ErrorResponse<*> -> {
                    dismissLoadingDialog()
                    Toast.makeText(requireActivity(), it.error, Toast.LENGTH_SHORT).show()
                }

                ResultWrapper.Loading -> {
                    showLoadingDialog()
                }

                is ResultWrapper.NetworkError<*> -> {
                    dismissLoadingDialog()
                    Toast.makeText(
                        requireActivity(),
                        "Terjadi kesalahan pada jaringan, Harap periksa jaringan",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is ResultWrapper.Success -> {
                    stockOpnameViewModel.cacheAllValidEpcs()
                    dismissLoadingDialog()
                    Toast.makeText(
                        requireActivity(),
                        "Sinkronisasi data berhasil",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                null -> {

                }
            }
        }
    }

    private fun showLoadingDialog(
        message: String = "Sinkronisasi data",
        indeterminate: Boolean = true,
        max: Int = 100
    ) {
        if (loadingDialog == null) {
            loadingDialog = MaterialAlertDialogBuilder(requireContext())
                .setView(R.layout.dialog_loading)
                .setCancelable(false)
                .create()
        }
        loadingDialog?.show()
        loadingDialog?.findViewById<TextView>(R.id.tvLoadingMessage)?.text = message
        loadingDialog?.findViewById<ProgressBar>(R.id.progressCircular)?.apply {
            isIndeterminate = indeterminate
            this.max = max
            progress = 0
        }
    }

    private fun updateLoadingProgress(current: Int, total: Int) {
        val percent = if (total == 0) 0 else ((current * 100f) / total).toInt()
        loadingDialog?.findViewById<ProgressBar>(R.id.progressCircular)?.apply {
            isIndeterminate = false
            progress = percent
        }
        loadingDialog?.findViewById<TextView>(R.id.tvLoadingMessage)?.text =
            "Mengirim data"
    }

    private fun dismissLoadingDialog() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    private fun setupRecyclerView() {
        assetAdapter = SoDocumentAdapter() {

        }
        binding.rvEpc.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = assetAdapter
        }
    }

    private fun setupToolbar(isDocument: Boolean = true) {
        val activity = (activity as MainActivity)
        activity.setSupportActionBar(binding.toolbarScan)
        activity.setupActionBarWithNavController(findNavController())

        binding.toolbarScan.setTitle(if (isDocument) "Scan Dokumen" else "Scan Agunan")

        binding.toolbarScan.setNavigationIcon(R.drawable.arrow_back_ios_24px)
        binding.toolbarScan.setNavigationOnClickListener {
            // Tampilkan dialog konfirmasi
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Keluar Halaman?")
                .setMessage("Stock Opname masih berjalan,jika anda keluar maka progress stock opname akan di hapus. apakah kamu yakin ingin keluar?")
                .setPositiveButton("Ya") { _, _ ->
                    stockOpnameViewModel.clearScannedTags()
                    stockOpnameViewModel.clearBulkDocument()
                    findNavController().navigateUp()
                }
                .setNegativeButton("Batal") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun toggleScan(){
        val isScanning = reader.isInventorying() == true


        if (!isScanning) {
            // Mulai scan
            stockOpnameViewModel.startScan()
            binding.btScan.apply {
                text = "STOP"
                setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            }
        } else {
            // Hentikan scan
            stockOpnameViewModel.stopScan()
            binding.btScan.apply {
                text = "SCAN"
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.md_theme_yellow))
                setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_primary))
            }
        }
    }

    override fun myOnKeyDown() = toggleScan()

    override fun myOnKeyUp() {
    }
}
