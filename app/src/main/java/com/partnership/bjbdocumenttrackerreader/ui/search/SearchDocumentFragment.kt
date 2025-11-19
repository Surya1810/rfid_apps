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
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.partnership.bjbdocumenttrackerreader.MainActivity
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.data.model.Document
import com.partnership.bjbdocumenttrackerreader.data.model.GetListSegments
import com.partnership.bjbdocumenttrackerreader.databinding.FragmentSearchDocumentBinding
import com.partnership.bjbdocumenttrackerreader.reader.RFIDManager
import com.partnership.bjbdocumenttrackerreader.ui.adapter.SearchAdapter
import com.partnership.bjbdocumenttrackerreader.ui.adapter.SegmentFilterAdapter
import com.partnership.bjbdocumenttrackerreader.ui.home.DashboardViewModel
import com.partnership.bjbdocumenttrackerreader.ui.scan.StockOpnameViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class SearchDocumentFragment : Fragment() {

    private val stockOpnameViewModel: StockOpnameViewModel by activityViewModels()
    private val dashboardViewModel: DashboardViewModel by activityViewModels()
    private var _binding: FragmentSearchDocumentBinding? = null
    private var isDocument by Delegates.notNull<Boolean>()
    private lateinit var searchAdapter: SearchAdapter
    private val binding get() = _binding!!

    private var filterDialog: BottomSheetDialog? = null

    @Inject
    lateinit var reader: RFIDManager

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

        dashboardViewModel.isDocumentSelected.observe(viewLifecycleOwner) {
            isDocument = it
            stockOpnameViewModel.getSearchDocument(it)
        }

        searchAdapter = SearchAdapter { document ->
            stockOpnameViewModel.setDataToSearchingDocument(document)
            findNavController().navigate(R.id.action_searchDocumentFragment_to_searchingDocumentFragment)
        }

        stockOpnameViewModel.listSearch.observe(viewLifecycleOwner) {
            when (it) {
                is ResultWrapper.Error<*> -> {
                    binding.loading.visibility = View.GONE
                    binding.rvDocument.visibility = View.VISIBLE
                    Toast.makeText(
                        requireContext(),
                        "Terjadi kesalahan: ${it.error}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is ResultWrapper.ErrorResponse<*> -> {
                    binding.loading.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Response error: ${it.error}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                ResultWrapper.Loading -> {
                    binding.tvInformation.visibility = View.GONE
                    binding.loading.visibility = View.VISIBLE
                    binding.rvDocument.visibility = View.GONE
                }
                is ResultWrapper.NetworkError<*> -> {
                    binding.loading.visibility = View.GONE
                    binding.rvDocument.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), "Tidak ada koneksi", Toast.LENGTH_SHORT).show()
                }

                is ResultWrapper.Success -> {
                    binding.loading.visibility = View.GONE
                    binding.rvDocument.visibility = View.VISIBLE
                    val data = it.data
                    if (data.data?.documents?.isEmpty() == true) {
                        binding.tvInformation.visibility = View.VISIBLE
                        searchAdapter.submitList(emptyList())
                    } else {
                        searchAdapter.submitList(data.data?.documents)
                        binding.tvInformation.visibility = View.GONE
                    }
                }

                null -> {
                    searchAdapter.submitList(emptyList())
                    binding.tvInformation.visibility = View.VISIBLE
                }
            }
        }
        binding.rvDocument.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchAdapter
        }

        // Ambil list filter segment dari API dan simpan di ViewModel
        lifecycleScope.launch {
            when (val result = stockOpnameViewModel.getListFilterSegment()) {
                is ResultWrapper.Error<*> -> {
                    Toast.makeText(
                        requireContext(),
                        "Terjadi kesalahan: ${result.error}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is ResultWrapper.ErrorResponse<*> -> {
                    Toast.makeText(
                        requireContext(),
                        "Response error: ${result.error}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                ResultWrapper.Loading -> {}
                is ResultWrapper.NetworkError<*> -> {
                    Toast.makeText(requireContext(), result.error, Toast.LENGTH_SHORT).show()
                }

                is ResultWrapper.Success -> {
                    val data = result.data
                    stockOpnameViewModel.setListSegment(data.data ?: emptyList())
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        filterDialog?.dismiss()
        filterDialog = null
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stockOpnameViewModel.clearSearch()
    }

    private fun setUpMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.search_menu, menu)

                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem.actionView as SearchView

                searchView.apply {
                    queryHint = "Ketik nama, kode, atau RFID Dokumen"

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
                                stockOpnameViewModel.getSearchDocument(isDocument, query)
                            }
                            return true
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {
                            return true
                        }
                    })
                }
            }

            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
                val filterItem = menu.findItem(R.id.action_filter)
                val isDocument = dashboardViewModel.isDocumentSelected.value == true
                filterItem.isVisible = isDocument
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_filter -> {
                        showFilterBottomSheet()
                        true
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
        val isDocument = dashboardViewModel.isDocumentSelected.value
        binding.toolbarScan.title = if (isDocument == true) "Cari Dokumen" else "Cari Agunan"
        binding.toolbarScan.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun showFilterBottomSheet() {
        val segments: List<GetListSegments> =
            stockOpnameViewModel.listSegment.value ?: emptyList()

        if (segments.isEmpty()) {
            Toast.makeText(requireContext(), "Data segmen belum tersedia", Toast.LENGTH_SHORT)
                .show()
            return
        }

        // Ambil pilihan terakhir dari ViewModel (null = Semua)
        val currentSelectedSegment: String? = stockOpnameViewModel.selectedSegment.value

        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.sheet_filter_segment, null)

        dialog.setOnShowListener { dialogInterface ->
            val d = dialogInterface as BottomSheetDialog
            val bottomSheet =
                d.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)

            bottomSheet?.post {
                val behavior = BottomSheetBehavior.from(bottomSheet)

                // Biar tinggi ngikutin konten
                bottomSheet.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT

                // Hilangkan state collapsed/peek
                behavior.peekHeight = 0
                behavior.skipCollapsed = true
                behavior.isFitToContents = true

                // Paksa langsung expanded setelah layout selesai
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }


        val rvSegments = view.findViewById<RecyclerView>(R.id.rvSegments)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)

        tvTitle.text = if (isDocument) "Filter Segmen Dokumen" else "Filter Segmen Agunan"

        rvSegments.layoutManager = LinearLayoutManager(requireContext())
        rvSegments.adapter = SegmentFilterAdapter(
            segments = segments,
            initialSelectedSegment = currentSelectedSegment
        ) { selectedValueForApi: String? ->
            stockOpnameViewModel.setSelectedSegment(selectedValueForApi)
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
        filterDialog = dialog
    }
}
