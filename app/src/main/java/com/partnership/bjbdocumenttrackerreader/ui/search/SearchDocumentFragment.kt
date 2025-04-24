package com.partnership.bjbdocumenttrackerreader.ui.search

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.partnership.bjbdocumenttrackerreader.MainActivity
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.data.model.Document
import com.partnership.bjbdocumenttrackerreader.databinding.FragmentSearchDocumentBinding
import com.partnership.bjbdocumenttrackerreader.reader.RFIDManager
import com.partnership.bjbdocumenttrackerreader.ui.adapter.SearchAdapter
import com.partnership.bjbdocumenttrackerreader.ui.home.DashboardViewModel
import com.partnership.bjbdocumenttrackerreader.ui.scan.StockOpnameViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class SearchDocumentFragment : Fragment(){
    private val stockOpnameViewModel : StockOpnameViewModel by activityViewModels()
    private val dashboardViewModel : DashboardViewModel by activityViewModels()
    private val searchingViewModel: SearchViewModel by activityViewModels()
    private var _binding: FragmentSearchDocumentBinding? = null
    private var isDocument by Delegates.notNull<Boolean>()
    private lateinit var searchAdapter: SearchAdapter
    private val binding get() = _binding!!

    @Inject lateinit var reader: RFIDManager
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchDocumentBinding.inflate(inflater, container, false)
        setupToolbar()
        setUpMenu()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dashboardViewModel.isDocumentSelected.observe(viewLifecycleOwner){
            isDocument = it
            stockOpnameViewModel.getSearchDocument(it)
        }

        searchAdapter = SearchAdapter(){document ->
            searchingViewModel.setDataToSearchingDocument(document)
            findNavController().navigate(R.id.action_searchDocumentFragment_to_searchingDocumentFragment)
        }

        stockOpnameViewModel.listSearch.observe(viewLifecycleOwner) {
            when (it) {
                is ResultWrapper.Error<*> -> {
                    // Tampilkan error
                    Toast.makeText(requireContext(), "Terjadi kesalahan: ${it.error}", Toast.LENGTH_SHORT).show()
                }
                is ResultWrapper.ErrorResponse<*> -> {
                    // Error dari server, bisa tampilkan errorMessage
                    Toast.makeText(requireContext(), "Response error: ${it.error}", Toast.LENGTH_SHORT).show()
                }
                ResultWrapper.Loading -> {

                }
                is ResultWrapper.NetworkError<*> -> {
                    // Tampilkan error jaringan
                    Toast.makeText(requireContext(), "Tidak ada koneksi", Toast.LENGTH_SHORT).show()
                }
                is ResultWrapper.Success -> {
                    val data = it.data
                    searchAdapter.submitList(data.data?.documents)
                }
            }
        }

        binding.rvDocument.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpMenu() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.search_menu, menu)

                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem.actionView as SearchView

                searchView.apply {
                    queryHint = "Ketik nama, kode, atau RFID barang"

                    val searchEditText =
                        findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
                    searchEditText.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.md_theme_onPrimary
                        )
                    )
                    searchEditText.setHintTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.md_theme_onPrimary
                        )
                    )

                    val searchCloseIcon =
                        findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
                    val searchBackIcon =
                        findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon)
                    searchCloseIcon.setColorFilter(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.md_theme_onPrimary
                        ), PorterDuff.Mode.SRC_IN
                    )
                    searchBackIcon.setColorFilter(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.md_theme_onPrimary
                        ), PorterDuff.Mode.SRC_IN
                    )
                    searchBackIcon.setImageResource(R.drawable.arrow_back_ios_24px)

                    setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            if (query != null) {
                                stockOpnameViewModel.getSearchDocument(isDocument,query)
                            }
                            return true
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {
                            return true
                        }
                    })
                }
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
                            findNavController().navigate(R.id.action_searchDocumentFragment_to_settingReaderFragment)
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
            findNavController().navigateUp()
        }
    }
}
