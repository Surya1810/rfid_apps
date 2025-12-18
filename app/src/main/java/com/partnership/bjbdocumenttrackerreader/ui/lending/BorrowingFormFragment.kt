package com.partnership.bjbdocumenttrackerreader.ui.lending

import android.app.DatePickerDialog
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
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.partnership.bjbdocumenttrackerreader.MainActivity
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.data.model.Document
import com.partnership.bjbdocumenttrackerreader.databinding.FragmentBorrowingFormBinding
import com.partnership.bjbdocumenttrackerreader.util.Utils
import kotlinx.coroutines.launch
import java.util.Calendar

class BorrowingFormFragment : Fragment() {

    private var _binding: FragmentBorrowingFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BorrowingViewModel by activityViewModels()

    private lateinit var selectedDocument: Document

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBorrowingFormBinding.inflate(inflater, container, false)
        setupToolbar()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.documentSelected.observe(viewLifecycleOwner) { document ->
            if (document != null) {
                setDetailDocument(document)
                selectedDocument = document
                binding.lyPickDocument.visibility = View.GONE
                binding.borrowDocument.visibility = View.VISIBLE
            } else {
                //kalau mau pick dokumennya
                binding.lyPickDocument.visibility = View.VISIBLE
            }
        }

        binding.cardView6.setOnClickListener {
            findNavController().navigate(R.id.action_borrowingFormFragment_to_searchDocumentFragment)
            viewModel.setIsCreateBorrow(true)
        }

        binding.cardSignatureBorrowing.setOnClickListener {
            if (binding.edBorrower.text.isNullOrEmpty()){
                Toast.makeText(requireContext(), "Mohon isi nama peminjam", Toast.LENGTH_SHORT).show()
            }else{
                findNavController().navigate(R.id.action_borrowingFormFragment_to_signaturePadFragment)
                viewModel.setIsCreateBorrow(true)
            }
        }


        binding.edDateReturn.setOnClickListener {
                showDatePicker()
        }

        viewModel.signatureBorrowed.observe(viewLifecycleOwner) {
            if (it == null){
                binding.tvBorrowerName.text = "Ketuk tanda untuk tanda tangan"
            }else{
                Glide.with(binding.root.context)
                    .load(it)
                    .placeholder(R.drawable.logo_bjb)
                    .into(binding.imgSignatureBorrowing)
                binding.tvBorrowerName.text = binding.edBorrower.text
            }
        }

        binding.btnSubmit.setOnClickListener {
            if (viewModel.signatureBorrowed.value == null) {
                Toast.makeText(
                    requireContext(),
                    "Tanda tangan belum ditandatangani",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Konfirmasi Peminjaman")
                .setMessage("Apakah Anda yakin ingin meminjam dokumen ini?")
                .setNegativeButton("Batal") { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton("Ya, Pinjam") { _, _ ->
                    lifecycleScope.launch {
                        val result = viewModel.borrowDocument(
                            selectedDocument.id,
                            binding.edBorrower.text.toString(),
                            binding.edDateReturn.text.toString(),
                            viewModel.signatureBorrowed.value!!
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
                                // kalau mau, tampilkan loading
                            }

                            is ResultWrapper.Success -> {
                                Toast.makeText(
                                    requireContext(),
                                    "Berhasil meminjam dokumen",
                                    Toast.LENGTH_LONG
                                ).show()
                                findNavController().popBackStack()
                            }
                        }
                    }
                }
                .show()
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                    viewModel.clearSignature()
                    viewModel.clearDocument()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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


    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format(
                    "%04d-%02d-%02d",
                    selectedYear,
                    selectedMonth + 1,
                    selectedDay
                )
                binding.edDateReturn.setText(formattedDate)
            },
            year,
            month,
            day
        )

        // optional: cegah pilih tanggal kemarin
        datePicker.datePicker.minDate = System.currentTimeMillis()

        datePicker.show()
    }

    private fun setupToolbar() {
        val activity = (activity as MainActivity)
        activity.setSupportActionBar(binding.toolbar)
        activity.setupActionBarWithNavController(findNavController())
        binding.toolbar.setNavigationIcon(R.drawable.arrow_back_ios_24px)
        binding.toolbar.title = "Peminjaman Dokumen"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
            viewModel.clearDocument()
        }
    }

}
