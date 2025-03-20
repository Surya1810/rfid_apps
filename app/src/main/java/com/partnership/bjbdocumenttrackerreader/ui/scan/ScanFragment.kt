package com.partnership.bjbdocumenttrackerreader.ui.scan

import android.content.ContentValues.TAG
import android.content.Context
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
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
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
import com.partnership.bjbdocumenttrackerreader.reader.ReaderKeyEventHandler
import com.partnership.bjbdocumenttrackerreader.ui.adapter.EpcAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ScanFragment : Fragment(), ReaderKeyEventHandler {
    private val rfidViewModel: RFIDViewModel by activityViewModels()
    private val scanViewModel: ScanViewModel by viewModels()

    // Deklarasi objek binding
    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!
    private lateinit var epcAdapter: EpcAdapter
    private var isDocumentSelected: Boolean = true
    private var epcList = mutableListOf<TagInfo>()
    private var isScanning: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inisialisasi binding
        _binding = FragmentScanBinding.inflate(inflater, container, false)

        setupToolbar()
        setupToggleButton()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rfidViewModel.initReader(requireActivity())
        setUpMenu()
        setupRecyclerView()

        rfidViewModel.isScanning.observe(viewLifecycleOwner){
            isScanning = it
        }

        binding.btScan.setOnClickListener {
            scanTag(isScanning)
        }

        binding.btClear.setOnClickListener {
            clearTagList(isScanning)
        }

        binding.btSend.setOnClickListener {
            if (epcList.size >= 1) {
                uploadData()
            } else {
                Toast.makeText(requireActivity(), "List Kosong! Scan Terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
            rfidViewModel.elapsedTime.observe(viewLifecycleOwner) {
                binding.tvTime.text = it
            }
        }

        rfidViewModel.tagList.observe(viewLifecycleOwner) { tagList ->
            epcList = tagList
            binding.tvTotalTagNumber.text = tagList.size.toString()
            rfidViewModel.isScanning.observe(viewLifecycleOwner) {
                if (it) {
                    epcAdapter.updateData(epcList)
                }
            }
        }

        rfidViewModel.elapsedTime.observe(viewLifecycleOwner){
            binding.tvTime.text = it
        }
    }

    private fun setupRecyclerView() {
        epcAdapter = EpcAdapter(epcList)
        binding.rvEpc.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = epcAdapter
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
                        val isScanning = rfidViewModel.isScanning.value ?: false
                        if (isScanning) {
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

    private fun setButtonState(
        button: MaterialButton,
        isActive: Boolean,
        primaryColor: Int,
        defaultColor: Int
    ) {
        if (isActive) {
            button.setBackgroundColor(primaryColor)
            button.setTextColor(Color.WHITE)
        } else {
            button.setBackgroundColor(defaultColor)
            button.setTextColor(Color.BLACK)
        }
    }

    private fun uploadData() {
        scanViewModel.uploadData(epcList, isDocumentSelected)
        scanViewModel.resultUploadData.observe(viewLifecycleOwner) {
            when (it) {
                is ResultWrapper.Error -> {
                    Snackbar.make(
                        binding.root,
                        "Terjadi masalah harap hubungi Admin",
                        Snackbar.LENGTH_LONG
                    )
                        .show()
                    Log.e(TAG, "uploadData: ${it.error}")
                }

                is ResultWrapper.ErrorResponse -> {
                    Snackbar.make(binding.root, it.error, Snackbar.LENGTH_LONG)
                        .show()
                    Log.e(TAG, "uploadData: ${it.error}")
                }

                ResultWrapper.Loading -> {

                }

                is ResultWrapper.Success -> {
                    it.data.message?.let { it1 ->
                        Snackbar.make(binding.root, it1, Snackbar.LENGTH_LONG)
                            .show()
                    }
                }

                is ResultWrapper.NetworkError -> {
                    Snackbar.make(
                        binding.root,
                        "Jaringan bermasalah, Harap mendekat ke wifi",
                        Snackbar.LENGTH_LONG
                    )
                        .setAction("Baik") {

                        }
                        .show()
                    Log.e(TAG, "uploadData: ${it.error}")
                }
            }
        }
    }

    private fun scanTag(isScanning: Boolean) {
        if (isScanning) {
            rfidViewModel.stopReadTag()
            binding.btScan.text = "START"
            binding.btScan.setBackgroundColor(resources.getColor(R.color.md_theme_yellow))
            binding.btClear.isEnabled = true
            binding.btClear.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.md_theme_yellow))
            binding.btSend.isEnabled = true
            binding.btSend.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.md_theme_yellow))
            binding.tvTime.text = "00:00"
        } else {
            rfidViewModel.readTagAuto()
            binding.btScan.text = "STOP"
            binding.btScan.setBackgroundColor(Color.RED)
            binding.btClear.isEnabled = false
            binding.btClear.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.md_theme_outlineVariant))
            binding.btSend.isEnabled = false
            binding.btSend.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.md_theme_outlineVariant))
        }
    }

    private fun clearTagList(isScanning: Boolean){
        if (isScanning){
            Toast.makeText(
                requireActivity(),
                "Hentikan scan terlebih dahulu",
                Toast.LENGTH_SHORT
            ).show()
        }else{
            rfidViewModel.clearTagList()
            binding.tvTotalTagNumber.text = "0"
            epcList.clear()
            epcAdapter.updateData(epcList)
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

    private fun setupToggleButton() {
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.md_theme_primary)
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.md_theme_outlineVariant)

        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isDocumentSelected = checkedId == binding.btnDocument.id

                setButtonState(binding.btnDocument, isDocumentSelected, primaryColor, defaultColor)
                setButtonState(binding.btnAgunan, !isDocumentSelected, primaryColor, defaultColor)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun myOnKeyDown() {
        scanTag(isScanning)
    }

    override fun myOnKeyUp() {

    }
}
