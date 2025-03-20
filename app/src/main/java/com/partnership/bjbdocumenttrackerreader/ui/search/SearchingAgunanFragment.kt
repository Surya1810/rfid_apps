package com.partnership.bjbdocumenttrackerreader.ui.search

import android.content.ContentValues.TAG
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.snackbar.Snackbar
import com.partnership.bjbdocumenttrackerreader.MainActivity
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.data.model.DetailAgunan
import com.partnership.bjbdocumenttrackerreader.databinding.FragmentSearchingAgunanBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchingAgunanFragment : Fragment() {

    private var isScanning = false
    private val searchViewModel : SearchViewModel by activityViewModels()
    private var _binding: FragmentSearchingAgunanBinding? = null
    private val binding get() = _binding!!
    private lateinit var epcToSearch: String
    private var isFound: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchingAgunanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpMenu()
        setupToolbar()
        searchViewModel.searchAgunanEpc.observe(viewLifecycleOwner){
            setData(it)
            searchViewModel.setEpcFilter(it.rfidNumber)
            epcToSearch = it.rfidNumber
        }

        searchViewModel.isScanning.observe(viewLifecycleOwner){
            isScanning = it
        }

        binding.btScan.setOnClickListener {
            if (isScanning) {
                searchViewModel.stopReadTag()
                searchViewModel.clearFilterReader()
                updateScanButtonUI(false)
            } else {
                searchViewModel.searchSingleTag(epcToSearch)
                updateScanButtonUI(true)
            }
        }

        searchViewModel.postMessage.observe(viewLifecycleOwner){result ->
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
                    Log.e(TAG, "uploadData: ${result.error}")
                }
            }
        }

        binding.btSend.setOnClickListener {
            searchViewModel.sendDataSearch(epcToSearch,false)
        }

        searchViewModel.isFound.observe(viewLifecycleOwner) {
            isFound = it
            if (it) {
                binding.tvStatusItem.text = "Agunan Ditemukan"

                // Stop scanning via ViewModel
                searchViewModel.stopReadTag()
                searchViewModel.clearFilterReader()

                // Update UI ke kondisi START lagi
                updateScanButtonUI(isScanning = false)

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

    private fun setData(detailAgunan: DetailAgunan){
        binding.apply {
            tvAgunanNumber.text = detailAgunan.nomorAgunan
            tvEpc.text = detailAgunan.rfidNumber
            tvTypeAgunan.text = detailAgunan.typeAgunan
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
                            findNavController().navigate(R.id.action_searchingAgunanFragment_to_settingReaderFragment)
                            true
                        }
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
