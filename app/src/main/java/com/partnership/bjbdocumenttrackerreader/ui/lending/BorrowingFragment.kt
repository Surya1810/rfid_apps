package com.partnership.bjbdocumenttrackerreader.ui.lending

import android.graphics.PorterDuff
import android.os.Bundle
import androidx.fragment.app.Fragment
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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
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
import com.partnership.bjbdocumenttrackerreader.data.model.GetListSegments
import com.partnership.bjbdocumenttrackerreader.databinding.FragmentBorrowingBinding
import com.partnership.bjbdocumenttrackerreader.ui.adapter.SearchAdapter
import com.partnership.bjbdocumenttrackerreader.ui.adapter.SegmentFilterAdapter
import com.partnership.bjbdocumenttrackerreader.ui.scan.StockOpnameViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class BorrowingFragment : Fragment() {
    private lateinit var searchAdapter: SearchAdapter
    private var _binding: FragmentBorrowingBinding? = null
    private val binding get() = _binding!!
    private var filterDialog: BottomSheetDialog? = null
    private val stockOpnameViewModel: StockOpnameViewModel by viewModels()
    private val borrowViewModel : BorrowingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBorrowingBinding.inflate(inflater, container, false)
        stockOpnameViewModel.setSelectedStatus("borrowed")
        setUpMenu()
        setupToolbar()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchAdapter = SearchAdapter { document ->
            borrowViewModel.setDocument(document)
            borrowViewModel.setIsCreateBorrow(false)
            findNavController().navigate(R.id.action_borrowingFragment_to_detailBorrowedDocumentFragment)
        }
        stockOpnameViewModel.searchWithCurrentFilter(true)

        binding.fabLending.setOnClickListener {
            borrowViewModel.setIsCreateBorrow(true)
            borrowViewModel.clearDocument()
            findNavController().navigate(R.id.action_borrowingFragment_to_borrowingFormFragment)
        }
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

        stockOpnameViewModel.listSearch.observe(viewLifecycleOwner) {
            when (it) {
                is ResultWrapper.Error<*> -> {
                    binding.loading.visibility = View.GONE
                    binding.rvDocumentSearch.visibility = View.VISIBLE
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
                    binding.rvDocumentSearch.visibility = View.GONE
                }
                is ResultWrapper.NetworkError<*> -> {
                    binding.loading.visibility = View.GONE
                    binding.rvDocumentSearch.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), "Tidak ada koneksi", Toast.LENGTH_SHORT).show()
                }

                is ResultWrapper.Success -> {
                    binding.loading.visibility = View.GONE
                    binding.rvDocumentSearch.visibility = View.VISIBLE
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
        binding.rvDocumentSearch.apply {
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
                            stockOpnameViewModel.searchWithCurrentFilter(true, query)
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
                filterItem.isVisible = true
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
        activity.setSupportActionBar(binding.toolbarSearch)
        activity.setupActionBarWithNavController(findNavController())
        binding.toolbarSearch.setNavigationIcon(R.drawable.arrow_back_ios_24px)
        binding.toolbarSearch.title ="Peminjaman Dokumen"
        binding.toolbarSearch.setNavigationOnClickListener {
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
        tvTitle.text = "Filter Segmen"
        rvSegments.layoutManager = LinearLayoutManager(requireContext())
        rvSegments.adapter = SegmentFilterAdapter(
            segments = segments,
            initialSelectedSegment = currentSelectedSegment
        ) { selectedValueForApi: String? ->
            stockOpnameViewModel.setSelectedSegment(selectedValueForApi)
            dialog.dismiss()
        }

        val chipGroupStatus = view.findViewById<com.google.android.material.chip.ChipGroup>(R.id.chipGroupStatus)

        stockOpnameViewModel.selectedStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                null -> chipGroupStatus.check(R.id.chipAll)
                "borrowed" -> chipGroupStatus.check(R.id.chipDipinjam)
                "not_borrowed" -> chipGroupStatus.check(R.id.chipTidakDipinjam)
            }
        }

        chipGroupStatus.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener

            when (checkedIds.first()) {
                R.id.chipAll -> {
                    // ambil semua data
                    stockOpnameViewModel.setSelectedStatus(null)
                }

                R.id.chipDipinjam -> {
                    // hanya yang dipinjam
                    stockOpnameViewModel.setSelectedStatus("borrowed")
                }

                R.id.chipTidakDipinjam -> {
                    // hanya yang tidak dipinjam
                    stockOpnameViewModel.setSelectedStatus("not_borrowed")
                }
            }
        }


        val btnApply = view.findViewById<TextView>(R.id.btnApply)
        btnApply.setOnClickListener {
            stockOpnameViewModel.searchWithCurrentFilter(true)
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
        filterDialog = dialog
    }
}
