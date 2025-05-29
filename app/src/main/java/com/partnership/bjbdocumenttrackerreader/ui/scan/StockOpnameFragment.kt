package com.partnership.bjbdocumenttrackerreader.ui.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
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
import com.partnership.bjbdocumenttrackerreader.data.model.TagInfo
import com.partnership.bjbdocumenttrackerreader.databinding.FragmentStockOpnameBinding
import com.partnership.bjbdocumenttrackerreader.reader.BeepSoundManager
import com.partnership.bjbdocumenttrackerreader.reader.RFIDManager
import com.partnership.bjbdocumenttrackerreader.ui.adapter.SoDocumentAdapter
import com.partnership.bjbdocumenttrackerreader.ui.home.DashboardViewModel
import com.partnership.bjbdocumenttrackerreader.util.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StockOpnameFragment : Fragment() {

    private val stockOpnameViewModel: StockOpnameViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by activityViewModels()

    @Inject
    lateinit var soundManager: BeepSoundManager
    private var isDocument: Boolean = true
    @Inject
    lateinit var reader: RFIDManager
    private var _binding: FragmentStockOpnameBinding? = null
    private var message = ""
    private val binding get() = _binding!!
    private lateinit var assetAdapter: SoDocumentAdapter
    private var epcList = mutableListOf<TagInfo>()

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

        binding.lyFound.setOnClickListener {
            stockOpnameViewModel.setIsThereFilter(true)
        }

        binding.lyTotal.setOnClickListener {
            stockOpnameViewModel.setIsThereFilter(null)
        }

        binding.lyNotFound.setOnClickListener {
            stockOpnameViewModel.setIsThereFilter(false)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Cek kondisi, misal sedang scanning
                if (reader.isInventorying() == true) {
                    Toast.makeText(requireContext(), "Hentikan scan terlebih dahulu!", Toast.LENGTH_SHORT).show()
                } else {
                    // Tampilkan dialog konfirmasi
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Keluar Halaman?")
                        .setMessage("Stock Opname masih berjalan,jika anda keluar maka progress stock opname akan di hapus. apakah kamu yakin ingin keluar?")
                        .setPositiveButton("Ya") { _, _ ->
                            findNavController().navigateUp()
                        }
                        .setNegativeButton("Batal") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        })

        binding.btSend.setOnClickListener {
            if (reader.isInventorying() == true){
                Toast.makeText(requireActivity(), "Hentikan scanning terlebih dahulu", Toast.LENGTH_SHORT).show()
            }else{
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Konfirmasi Kirim")
                    .setMessage(message)
                    .setPositiveButton("Kirim") { dialog, _ ->
                        Utils.showLoading(requireContext())
                        lifecycleScope.launch {
                            when (val result = stockOpnameViewModel.postStockOpname(isDocument)) {
                                is ResultWrapper.Error -> {
                                    Utils.dismissLoading()
                                    Toast.makeText(requireActivity(), result.error, Toast.LENGTH_SHORT)
                                        .show()
                                }

                                is ResultWrapper.ErrorResponse -> {
                                    Utils.dismissLoading()
                                    Toast.makeText(requireActivity(), result.error, Toast.LENGTH_SHORT)
                                        .show()
                                }

                                ResultWrapper.Loading -> {

                                }

                                is ResultWrapper.NetworkError -> {
                                    Utils.dismissLoading()
                                    Toast.makeText(
                                        requireActivity(),
                                        "Terjadi kesalahan pada jaringan, Harap periksa jaringan",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                is ResultWrapper.Success -> {
                                    Utils.dismissLoading()
                                    Toast.makeText(
                                        requireActivity(),
                                        "Stock Opname Berhasil",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    findNavController().navigateUp()
                                }
                            }
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton("Periksa Kembali") { dialog, _ ->
                        dialog.dismiss()
                        Utils.dismissLoading()
                    }
                    .show()
            }


        }

        stockOpnameViewModel.soundBeep.observe(viewLifecycleOwner) {
            if (it) {
                soundManager.playBeep()
            }
        }
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
            stockOpnameViewModel.pagedAssets.collectLatest { pagingData ->
                assetAdapter.submitData(pagingData)
            }
        }
    }

    private fun setupRecyclerView() {
        assetAdapter = SoDocumentAdapter() {

        }
        binding.rvEpc.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = assetAdapter
        }
    }

    private fun setupToolbar() {
        val activity = (activity as MainActivity)
        activity.setSupportActionBar(binding.toolbarScan)
        activity.setupActionBarWithNavController(findNavController())

        binding.toolbarScan.setTitle(if (isDocument) "Stock Opname Dokumen" else "Stock Opname Agunan")

        binding.toolbarScan.setNavigationIcon(R.drawable.arrow_back_ios_24px)
        binding.toolbarScan.setNavigationOnClickListener {
            if (reader.isInventorying() == true) {
                Toast.makeText(requireContext(), "Hentikan scan terlebih dahulu!", Toast.LENGTH_SHORT).show()
            } else {
                // Tampilkan dialog konfirmasi
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Keluar Halaman?")
                    .setMessage("Stock Opname masih berjalan,jika anda keluar maka progress stock opname akan di hapus. apakah kamu yakin ingin keluar?")
                    .setPositiveButton("Ya") { _, _ ->
                        findNavController().navigateUp()
                    }
                    .setNegativeButton("Batal") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        epcList.clear()
    }

}
