package com.partnership.bjbdocumenttrackerreader.ui.lending

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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.partnership.bjbdocumenttrackerreader.MainActivity
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.data.model.Document
import com.partnership.bjbdocumenttrackerreader.databinding.FragmentDetailBorrowedDocumentBinding
import com.partnership.bjbdocumenttrackerreader.ui.adapter.HistoryBorrowedAdapter
import com.partnership.bjbdocumenttrackerreader.util.Utils
import kotlinx.coroutines.launch


class DetailBorrowedDocumentFragment : Fragment() {

    private var _binding: FragmentDetailBorrowedDocumentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BorrowingViewModel by activityViewModels()
    private lateinit var selectedDocument: Document
    private lateinit var adapter: HistoryBorrowedAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBorrowedDocumentBinding.inflate(inflater, container, false)
        setUpMenu()
        setupToolbar()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.documentSelected.observe(viewLifecycleOwner) {
            if(it != null){
                setDetailDocument(it)
                if(it.isBorrowed){
                    viewModel.getDetailBorrowed(it.id)
                    observeDetailBorrowDocument()
                    binding.lyBorrowed.visibility = View.VISIBLE
                    binding.lyNotBorrowed.visibility = View.GONE
                }else{
                    viewModel.getHistoryBorrow(it.id)
                    binding.lyBorrowed.visibility = View.GONE
                    binding.lyNotBorrowed.visibility = View.VISIBLE
                    setupRecyclerView()
                    observeViewModel()
                }
                selectedDocument = it
            }
        }


        viewModel.signatureReturned.observe(viewLifecycleOwner) {
            if (it != null) {
                Glide.with(binding.root.context)
                    .load(it)
                    .placeholder(R.drawable.logo_bjb)
                    .into(binding.imgSignatureReturning)
            }
        }

        binding.btnSubmit.setOnClickListener {
            if (viewModel.signatureReturned.value == null) {
                Toast.makeText(
                    requireContext(),
                    "Mohon tanda tangan pengembalian terlebih dahulu",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Konfirmasi Pengembalian")
                .setMessage("Apakah Anda yakin ingin mengembalikan dokumen ini?")
                .setNegativeButton("Batal") { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton("Ya, Kembalikan") { _, _ ->
                    lifecycleScope.launch {
                        val result = viewModel.returnDocument(
                            selectedDocument.id,
                            viewModel.signatureReturned.value!!
                        )

                        when (result) {
                            is ResultWrapper.Error<*>,
                            is ResultWrapper.ErrorResponse<*> -> {
                                Toast.makeText(
                                    requireContext(),
                                    "Terjadi kesalahan hubungi admin",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            is ResultWrapper.NetworkError<*> -> {
                                Toast.makeText(
                                    requireContext(),
                                    "Koneksi tidak stabil atau tidak terhubung",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            ResultWrapper.Loading -> {
                                // kalau mau, pasang loading dialog di sini
                            }

                            is ResultWrapper.Success -> {
                                viewModel.clearSignature()
                                Toast.makeText(
                                    requireContext(),
                                    "Berhasil mengembalikan dokumen",
                                    Toast.LENGTH_LONG
                                ).show()
                                findNavController().popBackStack()
                            }
                        }
                    }
                }
                .show()
        }


        binding.cardSignatureReturning.setOnClickListener {
            findNavController().navigate(R.id.action_detailBorrowedDocumentFragment_to_signaturePadFragment)
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                    viewModel.clearSignature()
                }
            })
    }

    fun observeDetailBorrowDocument() {
        viewModel.detailBorrowed.observe(viewLifecycleOwner) {
            when (it) {
                is ResultWrapper.Error -> {
                    Toast.makeText(
                        requireContext(),
                        "Terjadi kesalahan hubungi admin",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("tes", "observeDetailBorrowDocument: ${it.error}")
                }

                is ResultWrapper.ErrorResponse -> {
                    Toast.makeText(requireContext(), "error : ${it.error}", Toast.LENGTH_SHORT)
                        .show()
                }

                ResultWrapper.Loading -> {

                }

                is ResultWrapper.NetworkError -> {
                    Toast.makeText(
                        requireContext(),
                        "Koneksi tidak stabil atau tidak terhubung",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is ResultWrapper.Success -> {
                    val data = it.data.data?.borrowed
                    if (data != null) {
                        binding.tvBorrowerName.text = data.borrowerName
                        binding.tvReturnName.text = data.borrowerName
                        binding.tvBorrowedDate.text = Utils.formatDate(data.borrowedAt)
                        binding.tvReturningDate.text = Utils.formatDate(data.estimatedReturnDate)
                        Glide.with(binding.root.context)
                            .load(data.firstSignature)
                            .placeholder(R.drawable.logo_bjb)
                            .into(binding.imgSignatureBorrowing)
                        binding.tvBorrowedName.text = data.borrowerName
                    }
                }

                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null // biar gak bocor memori, kita anak baik
    }

    fun setDetailDocument(item: Document) {
        binding.tvNameDocument.text = item.name
        if (item.noRef == null) {
            binding.tvNoRef.visibility = View.GONE
        } else {
            binding.tvNoRef.visibility = View.VISIBLE
            binding.tvNoRef.text = "No.Ref : ${item.noRef}"
        }
        binding.tvRfid.text = "RFID : ${item.rfid}"
        if (item.cif == null) {
            binding.tvNoCif.visibility = View.GONE
        } else {
            binding.tvNoCif.visibility = View.VISIBLE
            binding.tvNoCif.text = "CIF : ${item.cif}"
        }
        binding.tvNoDoc.text = "No.Doc : ${item.noDoc}"
        binding.tvBaris.text = item.location?.row
        binding.tvBox.text = item.location?.box
        binding.tvRak.text = item.location?.rack
        if (item.segment.isNullOrEmpty()) {
            binding.lySegment.visibility = View.GONE
        } else {
            binding.tvSegment.text = item.segment
        }
        if (item.isThere) {
            binding.tvState.text = "Ditemukan"
            binding.tvState.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.accent_good
                )
            )
            binding.lyState.setCardBackgroundColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.state_good
                )
            )
        } else {
            binding.tvState.text = "Tidak Ditemukan"
            binding.tvState.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.accent_bad
                )
            )
            binding.lyState.setCardBackgroundColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.state_bad
                )
            )
        }
        if (item.isBorrowed) {
            binding.lyStateLending.visibility = View.VISIBLE
            binding.tvStateLending.text = "Dipinjam"
            binding.tvStateLending.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.md_theme_background
                )
            )
            binding.lyStateLending.setCardBackgroundColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.md_theme_yellow
                )
            )
        } else {
            binding.lyStateLending.visibility = View.GONE
        }
    }

    private fun setUpMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.history_menu, menu)
            }

            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
                val filterItem = menu.findItem(R.id.history)
                val visible = viewModel.documentSelected.value?.isBorrowed
                filterItem.isVisible = visible ?: false
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.history -> {
                        findNavController().navigate(R.id.action_detailBorrowedDocumentFragment_to_historyBorrowedFragment)
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupToolbar() {
        val activity = (activity as MainActivity)
        activity.setSupportActionBar(binding.toolbar)
        activity.setupActionBarWithNavController(findNavController())
        binding.toolbar.setNavigationIcon(R.drawable.arrow_back_ios_24px)
        binding.toolbar.title = "Dokumen Dipinjam"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
            viewModel.clearSignature()
        }
    }

    private fun setupRecyclerView() {
        adapter = HistoryBorrowedAdapter()
        binding.rvHistoryBorrowed.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistoryBorrowed.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.historyBorrow.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ResultWrapper.Loading -> {
                    // loading state, pasrah dulu
                }
                is ResultWrapper.Success -> {
                    val data = result.data.data?.borrowed ?: emptyList()
                    if (data.isEmpty()){
                        binding.rvHistoryBorrowed.visibility = View.GONE
                        binding.tvInformationNotFound.visibility = View.VISIBLE
                    }else{
                        binding.rvHistoryBorrowed.visibility = View.VISIBLE
                        binding.tvInformationNotFound.visibility = View.GONE
                        adapter.submitList(data)
                    }
                }
                is ResultWrapper.Error -> {
                    // tampilkan error, hidup memang keras
                }

                else -> {}
            }
        }
    }
}
