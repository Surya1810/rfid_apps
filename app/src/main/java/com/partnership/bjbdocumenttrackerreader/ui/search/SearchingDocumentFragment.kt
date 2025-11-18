package com.partnership.bjbdocumenttrackerreader.ui.search

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
import androidx.activity.OnBackPressedCallback
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.partnership.bjbdocumenttrackerreader.MainActivity
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.data.model.Document
import com.partnership.bjbdocumenttrackerreader.data.model.PostLostDocument
import com.partnership.bjbdocumenttrackerreader.databinding.FragmentSearchingDocumentBinding
import com.partnership.bjbdocumenttrackerreader.reader.BeepSoundManager
import com.partnership.bjbdocumenttrackerreader.ui.scan.StockOpnameViewModel
import com.partnership.bjbdocumenttrackerreader.util.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchingDocumentFragment : Fragment() {
    private var isScanning = false
    @Inject lateinit var soundManager: BeepSoundManager

    private val searchViewModel : SearchViewModel by viewModels()
    private val stockOpnameViewModel: StockOpnameViewModel by activityViewModels()
    private var _binding: FragmentSearchingDocumentBinding? = null
    private val binding get() = _binding!!
    private lateinit var epcToSearch: String
    private var isFound: Boolean = false
    private lateinit var document: Document

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchingDocumentBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setUpMenu()

        searchViewModel.isFound.observe(viewLifecycleOwner) {
            isFound = it
            if (it) {
                if (it) {
                    binding.tvState.text = "Ditemukan"
                    binding.tvState.setTextColor(ContextCompat.getColor(binding.root.context, R.color.accent_good))
                    binding.lyState.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.state_good))
                } else {
                    binding.tvState.text = "Tidak Ditemukan"
                    binding.tvState.setTextColor(ContextCompat.getColor(binding.root.context, R.color.accent_bad))
                    binding.lyState.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.state_bad))
                }
                updateScanButtonUI(isScanning = false)
                if (it){
                    binding.lyLocating.visibility = View.VISIBLE
                }else{
                    binding.lyLocating.visibility = View.GONE
                }
            }
        }

        searchViewModel.soundBeep.observe (viewLifecycleOwner){
            if (it){
                soundManager.playBeep()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Cek kondisi, misal sedang scanning
                    if (searchViewModel.isScanning.value == true) {
                        Toast.makeText(
                            requireContext(),
                            "Hentikan scan terlebih dahulu!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                       findNavController().navigateUp()
                    }
                }
            })

        stockOpnameViewModel.searchDocumentEpc.observe(viewLifecycleOwner){
            setDataToView(it)
            document =it
            epcToSearch = it.rfid
        }

        searchViewModel.isScanning.observe(viewLifecycleOwner){
            isScanning = it
        }

        binding.btScan.setOnClickListener {
            if (isScanning) {
                searchViewModel.stopReadTag()
                updateScanButtonUI(false)
            } else {
                searchViewModel.searchSingleTag(epcToSearch)
                updateScanButtonUI(true)
            }
        }

        binding.btLocating.setOnClickListener {
            findNavController().navigate(R.id.action_searchingDocumentFragment_to_locatingFragment)
        }
        /*binding.btSearchRadar.setOnClickListener {
            findNavController().navigate(R.id.action_searchingDocumentFragment_to_radarFragment)
        }*/
        binding.btSend.setOnClickListener {
            Utils.showLoading(requireContext())
            // kirim data
            lifecycleScope.launch {
                when(val result = searchViewModel.postLostDocument(PostLostDocument(document.rfid,isFound))){
                    is ResultWrapper.Error -> {
                        Utils.dismissLoading()
                        Toast.makeText(requireContext(), "Terjadi kesalahan: ${result.error}", Toast.LENGTH_SHORT).show()
                    }
                    is ResultWrapper.ErrorResponse<*> -> {
                        Utils.dismissLoading()
                        Toast.makeText(requireContext(), "Response error: ${result.error}", Toast.LENGTH_SHORT).show()
                    }
                    ResultWrapper.Loading -> {

                    }
                    is ResultWrapper.NetworkError -> {
                        Utils.dismissLoading()
                        Toast.makeText(requireActivity(), "Terjadi kesalahan pada jaringan, Harap periksa jaringan", Toast.LENGTH_SHORT).show()
                    }
                    is ResultWrapper.Success -> {
                        Utils.dismissLoading()
                        Toast.makeText(requireContext(), "Berhasil di update", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }

        searchViewModel.postMessage.observe(viewLifecycleOwner){result ->
            Utils.dismissLoading()
            when (result) {
                is ResultWrapper.Error -> {
                    Snackbar.make(binding.root, "Terjadi masalah harap hubungi Admin", Snackbar.LENGTH_LONG).show()
                    Log.e(TAG, "uploadData: ${result.error}")
                }
                is ResultWrapper.ErrorResponse -> {
                    Snackbar.make(binding.root, result.error, Snackbar.LENGTH_LONG).show()
                    Log.e(TAG, "uploadData: ${result.error}")
                }
                ResultWrapper.Loading -> {
                    // Tambahkan UI loading jika perlu
                }
                is ResultWrapper.Success -> {
                    Snackbar.make(binding.root, "Berhasil di update", Snackbar.LENGTH_LONG)
                        .setAction("Baik") {}
                        .show()
                }
                is ResultWrapper.NetworkError -> {
                    Snackbar.make(binding.root, "Jaringan bermasalah, Harap mendekat ke wifi", Snackbar.LENGTH_LONG)
                        .setAction("Baik") {}
                        .show()
                }
            }
        }


    }

    private fun setDataToView(document: Document){
        binding.apply {
            binding.tvNameDocument.text = document.name
            binding.tvCIF.text = document.cif
            binding.tvRfid.text = document.rfid
            binding.tvBaris.text = document.location?.row
            binding.tvBox.text = document.location?.box
            binding.tvRak.text = document.location?.rack
            if (document.segment.isNullOrEmpty()){
                binding.lySegment.visibility = View.GONE
            }else{
                binding.tvSegment.text = document.segment
            }
            if (isFound) {
                binding.tvState.text = "Ditemukan"
                binding.tvState.setTextColor(ContextCompat.getColor(binding.root.context, R.color.accent_good))
                binding.lyState.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.state_good))
            } else {
                binding.tvState.text = "Tidak Ditemukan"
                binding.tvState.setTextColor(ContextCompat.getColor(binding.root.context, R.color.accent_bad))
                binding.lyState.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.state_bad))
            }
        }
    }

    private fun updateScanButtonUI(isScanning: Boolean) {
        if (isScanning) {
            binding.btScan.text = "STOP"
            binding.btScan.setBackgroundColor(Color.RED)
            binding.btSend.isEnabled = false
            binding.btSend.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.md_theme_outlineVariant))
        } else {
            binding.btScan.text = "START"
            binding.btScan.setBackgroundColor(resources.getColor(R.color.md_theme_yellow))
            binding.btSend.isEnabled = true
            binding.btSend.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.md_theme_yellow))
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
                        val isScanning = searchViewModel.isScanning.value ?: false
                        if (isScanning) {
                            Toast.makeText(
                                requireActivity(),
                                "Hentikan Scan Terlebih Dahulu!",
                                Toast.LENGTH_SHORT
                            ).show()
                            false
                        } else {
                            findNavController().navigate(R.id.action_searchingDocumentFragment_to_settingReaderFragment)
                            true
                        }
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupToolbar() {
        val activity = (activity as MainActivity)
        activity.setSupportActionBar(binding.toolbarScan)
        activity.setupActionBarWithNavController(findNavController())

        binding.toolbarScan.setNavigationIcon(R.drawable.arrow_back_ios_24px)
        binding.toolbarScan.setNavigationOnClickListener {
            if (searchViewModel.isScanning.value == true) {
                Toast.makeText(requireContext(), "Hentikan Scan Terlebih Dahulu!", Toast.LENGTH_SHORT).show()
            }else{
                findNavController().navigateUp()
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}