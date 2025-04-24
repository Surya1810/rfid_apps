package com.partnership.bjbdocumenttrackerreader.ui.scan

import android.content.ContentValues.TAG
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.partnership.bjbdocumenttrackerreader.MainActivity
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.data.model.TagInfo
import com.partnership.bjbdocumenttrackerreader.databinding.FragmentScanBinding
import com.partnership.bjbdocumenttrackerreader.reader.BeepSoundManager
import com.partnership.bjbdocumenttrackerreader.reader.RFIDManager
import com.partnership.bjbdocumenttrackerreader.reader.ReaderKeyEventHandler
import com.partnership.bjbdocumenttrackerreader.ui.adapter.DocumentAdapter
import com.partnership.bjbdocumenttrackerreader.ui.adapter.SoDocumentAdapter
import com.partnership.bjbdocumenttrackerreader.ui.home.DashboardViewModel
import com.partnership.bjbdocumenttrackerreader.util.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class ScanFragment : Fragment(), ReaderKeyEventHandler {

    private val stockOpnameViewModel: StockOpnameViewModel by viewModels()
    private val dashboardViewModel : DashboardViewModel by activityViewModels()

    @Inject lateinit var soundManager: BeepSoundManager
    private var isDocument : Boolean = true
    @Inject lateinit var reader: RFIDManager
    private var _binding: FragmentScanBinding? = null
    private var message = ""
    private val binding get() = _binding!!
    private lateinit var assetAdapter : SoDocumentAdapter
    private var epcList = mutableListOf<TagInfo>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        setupToolbar()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpMenu()
        setupRecyclerView()

        binding.btScan.setOnClickListener {
            val isScanning = reader.isInventorying()
            if (isScanning != null) {
                scanTag(isScanning)
            }
        }

        dashboardViewModel.isDocumentSelected.observe(viewLifecycleOwner){
            isDocument = it
            lifecycleScope.launch {
                when(val result = stockOpnameViewModel.getBulkDocument(it)){
                    is ResultWrapper.Error -> {
                        Toast.makeText(requireActivity(), result.error, Toast.LENGTH_SHORT).show()
                    }
                    is ResultWrapper.ErrorResponse -> {
                        Toast.makeText(requireActivity(), result.error, Toast.LENGTH_SHORT).show()
                    }
                    ResultWrapper.Loading -> {

                    }
                    is ResultWrapper.NetworkError -> {
                        Toast.makeText(requireActivity(), "Terjadi kesalahan pada jaringan, Harap periksa jaringan", Toast.LENGTH_SHORT).show()
                    }
                    is ResultWrapper.Success -> {
                        stockOpnameViewModel.cacheAllValidEpcs()
                    }
                }
            }
        }

        binding.btSend.setOnClickListener {
            lifecycleScope.launch {
                when(val result = stockOpnameViewModel.postStockOpname(isDocument)){
                    is ResultWrapper.Error -> {
                        Toast.makeText(requireActivity(), result.error, Toast.LENGTH_SHORT).show()
                    }
                    is ResultWrapper.ErrorResponse -> {
                        Toast.makeText(requireActivity(), result.error, Toast.LENGTH_SHORT).show()
                    }
                    ResultWrapper.Loading -> {

                    }
                    is ResultWrapper.NetworkError -> {
                        Toast.makeText(requireActivity(), "Terjadi kesalahan pada jaringan, Harap periksa jaringan", Toast.LENGTH_SHORT).show()
                    }
                    is ResultWrapper.Success -> {
                        Toast.makeText(requireActivity(), "Stock Opname Berhasil", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                }
            }
        }

        stockOpnameViewModel.elapsedTime.observe(viewLifecycleOwner){
            binding.tvTimeScanning.text = it
        }

        stockOpnameViewModel.soundBeep.observe(viewLifecycleOwner){
            if (it){
                soundManager.playBeep()
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                stockOpnameViewModel.assetStatusInfo.collect { (detected, total) ->
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
        stockOpnameViewModel.observeScannedTags()

        lifecycleScope.launch {
            stockOpnameViewModel.pagedAssets.collectLatest {pagingData ->
                assetAdapter.submitData(pagingData)
            }
        }
    }

    private fun setupRecyclerView() {
        assetAdapter = SoDocumentAdapter(){

        }
        binding.rvEpc.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = assetAdapter
        }
    }

    private fun setUpMenu() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.reader_setting, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.readerSetting -> {
                        val isScanning = reader.isInventorying()
                        if (isScanning == true) {
                            Toast.makeText(
                                requireActivity(),
                                "Hentikan Scan Terlebih Dahulu!",
                                Toast.LENGTH_SHORT
                            ).show()
                            false
                        } else {
                            findNavController().navigate(R.id.action_scanFragment_to_settingReaderFragment)
                            true
                        }
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun scanTag(isScanning: Boolean) {
        if (isScanning) {
            stockOpnameViewModel.stopReadTag()
            stockOpnameViewModel.setSoundToFalse()
            binding.btScan.text = "START"
            binding.btScan.setBackgroundColor(resources.getColor(R.color.md_theme_yellow))
            binding.btSend.isEnabled = true
            binding.btSend.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.md_theme_yellow))
            binding.tvTimeScanning.text = "00:00"
        } else {
            stockOpnameViewModel.readTagAuto2()
            binding.btScan.text = "STOP"
            binding.btScan.setBackgroundColor(Color.RED)
            binding.btSend.isEnabled = false
            binding.btSend.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.md_theme_outlineVariant))
        }
    }

    private fun setupToolbar() {
        val activity = (activity as MainActivity)
        activity.setSupportActionBar(binding.toolbarScan)
        activity.setupActionBarWithNavController(findNavController())

        binding.toolbarScan.setNavigationIcon(R.drawable.arrow_back_ios_24px)
        binding.toolbarScan.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        epcList.clear()
    }

    override fun myOnKeyDown() {
        val isScanning = reader.isInventorying()
        if (isScanning != null) {
            scanTag(isScanning)
        }
    }

    override fun myOnKeyUp() {

    }
}
