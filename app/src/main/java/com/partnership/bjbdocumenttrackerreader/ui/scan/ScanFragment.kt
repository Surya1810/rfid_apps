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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.partnership.bjbdocumenttrackerreader.MainActivity
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.databinding.FragmentScanBinding
import com.partnership.bjbdocumenttrackerreader.reader.BeepSoundManager
import com.partnership.bjbdocumenttrackerreader.reader.RFIDManager
import com.partnership.bjbdocumenttrackerreader.reader.ReaderKeyEventHandler
import com.partnership.bjbdocumenttrackerreader.ui.adapter.TagAdapter
import com.partnership.bjbdocumenttrackerreader.ui.home.DashboardViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class ScanFragment : Fragment(), ReaderKeyEventHandler {

    private val viewModel: StockOpnameViewModel by activityViewModels()

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!
    @Inject
    lateinit var soundManager: BeepSoundManager
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
        binding.rvTag.layoutManager = LinearLayoutManager(requireActivity())

        // Observasi data tag dari ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.scannedTags.collect { tags ->
                tagAdapter.submitList(tags)
                binding.tvTagScanned.text = "${tags.size} Items"
            }
        }

        // Tombol start/stop scan
        binding.btnStartScan.setOnClickListener {
            startScan()
        }

        lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.assetStatusInfo.collect { (_, total) ->
                    binding.tvTotal.text = total.toString()
                    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                    val currentDate = dateFormat.format(Date())
                    binding.tvDate.text = "Tanggal $currentDate"

                }
            }
        }

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
        viewModel.soundBeep.observe(viewLifecycleOwner) {
            if (it) {
                soundManager.playBeep()
            }
        }

        binding.fabValidate.setOnClickListener {
            val isScanning = reader.isInventorying() == true
            if (isScanning) {
                Toast.makeText(requireContext(), "Hentikan scan terlebih dahulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else{
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
                            findNavController().navigateUp()
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        }
    }

    private fun startScan(){
        val isScanning = reader.isInventorying() == true

        if (!isScanning) {
            // Mulai scan
            viewModel.startScan()
            binding.btnStartScan.apply {
                text = "STOP SCAN"
                setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            }
            binding.fabValidate.hide()
        } else {
            // Hentikan scan
            viewModel.stopScan()
            binding.btnStartScan.apply {
                text = "START SCAN"
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.md_theme_primary))
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            }
            binding.fabValidate.show()
        }
    }

    private fun setupToolbar() {
        val activity = (activity as MainActivity)
        activity.setSupportActionBar(binding.toolbarScan)
        activity.setupActionBarWithNavController(findNavController())

        binding.toolbarScan.setNavigationIcon(R.drawable.arrow_back_ios_24px)
        binding.toolbarScan.setNavigationOnClickListener {
            if (reader.isInventorying() == true) {
                Toast.makeText(requireContext(), "Hentikan scan terlebih dahulu!", Toast.LENGTH_SHORT).show()
            }else{
                findNavController().navigateUp()
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun myOnKeyDown() {
        startScan()
    }

    override fun myOnKeyUp() {

    }
}

