package com.partnership.bjbdocumenttrackerreader.ui.scan

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.partnership.bjbdocumenttrackerreader.MainActivity
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.databinding.FragmentScanBinding
import com.partnership.bjbdocumenttrackerreader.reader.RFIDManager
import com.partnership.bjbdocumenttrackerreader.ui.adapter.TagAdapter
import com.partnership.bjbdocumenttrackerreader.ui.home.DashboardViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class ScanFragment : Fragment() {

    private val viewModel: StockOpnameViewModel by activityViewModels()
    private val dashboardViewModel: DashboardViewModel by activityViewModels()
    private var isDocumentSelected: Boolean = true
    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var reader: RFIDManager

    private lateinit var tagAdapter: TagAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        // Inisialisasi adapter & RecyclerView
        tagAdapter = TagAdapter()
        binding.rvTag.adapter = tagAdapter

        // Observasi data tag dari ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.scannedTags.collect { tags ->
                tagAdapter.submitList(tags)
                binding.tvTagScanned.text = "${tags.size} Items"
            }
        }

        // Tombol start/stop scan
        binding.btnStartScan.setOnClickListener {
            var isScanning = reader.isInventorying()
            if (isScanning == true) {
                viewModel.startScan()
                binding.btnStartScan.apply {
                    text = "STOP SCAN"
                    setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                    setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                }
                binding.fabValidate.visibility = View.VISIBLE
            } else {
                viewModel.stopScan()
                binding.btnStartScan.apply {
                    text = "START SCAN"
                    setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.md_theme_primary))
                    setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                }
                binding.fabValidate.visibility = View.GONE
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.assetStatusInfo.collect { (_, total) ->
                    binding.tvTotal.text = total.toString()
                    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    val currentDate = dateFormat.format(Date())
                    binding.tvDate.text = currentDate
                }
            }
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
                            viewModel.clearScannedTags()
                        }
                        .setNegativeButton("Batal") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        })
        binding.rvTag.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    // Scroll ke bawah → sembunyikan FAB
                    binding.fabValidate.shrink()
                    binding.fabValidate.hide()
                } else if (dy < 0) {
                    // Scroll ke atas → tampilkan FAB
                    binding.fabValidate.show()
                    binding.fabValidate.extend()
                }
            }
        })




        dashboardViewModel.isDocumentSelected.observe(viewLifecycleOwner) { isDocument ->
            isDocumentSelected = isDocument
            setupToolbar()

            lifecycleScope.launch {
                when (val result = viewModel.getBulkDocument(isDocument)) {
                    is ResultWrapper.Loading -> {
                        showLoadingDialog("Sinkronisasi data")
                    }

                    is ResultWrapper.Success -> {
                        dismissLoadingDialog()
                        Toast.makeText(requireContext(), "Sinkronisasi Data Berhasil", Toast.LENGTH_SHORT).show()
                    }

                    is ResultWrapper.Error -> {
                        dismissLoadingDialog()
                        Toast.makeText(requireActivity(), result.error, Toast.LENGTH_SHORT).show()
                    }

                    is ResultWrapper.ErrorResponse -> {
                        dismissLoadingDialog()
                        Toast.makeText(requireActivity(), result.error, Toast.LENGTH_SHORT).show()
                    }

                    is ResultWrapper.NetworkError -> {
                        dismissLoadingDialog()
                        Toast.makeText(requireActivity(), "Terjadi kesalahan pada jaringan, Harap periksa jaringan", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.fabValidate.setOnClickListener {
            // Buat dialog builder
            val dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_loading_validate, null, false)

            val loadingDialog = MaterialAlertDialogBuilder(requireContext())
                .setCancelable(false)
                .setView(dialogView)
                .create()

            // Tampilkan dialog
            loadingDialog.show()

            viewModel.validateAllTags {
                loadingDialog.dismiss()

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Validasi Selesai")
                    .setMessage("Tag RFID telah divalidasi dengan database.")
                    .setPositiveButton("OK"){ dialog, _ ->
                        findNavController().navigate(R.id.action_scanFragment_to_stockOpnameFragment)
                        dialog.dismiss()
                    }
                    .show()
            }
        }



    }
    private var loadingDialog: AlertDialog? = null

    private fun showLoadingDialog(message: String = "Sinkronisasi data") {
        if (loadingDialog == null) {
            loadingDialog = MaterialAlertDialogBuilder(requireContext())
                .setView(R.layout.dialog_loading) // layout custom dengan ProgressBar
                .setCancelable(false)
                .create()
        }
        loadingDialog?.show()
        loadingDialog?.findViewById<TextView>(R.id.tvLoadingMessage)?.text = message
    }

    private fun dismissLoadingDialog() {
        loadingDialog?.dismiss()
    }

    private fun setupToolbar() {
        val activity = (activity as MainActivity)
        activity.setSupportActionBar(binding.toolbarScan)
        activity.setupActionBarWithNavController(findNavController())

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
                        viewModel.clearScannedTags()
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
    }
}

