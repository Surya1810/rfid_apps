package com.partnership.bjbdocumenttrackerreader.ui.search

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.data.model.DocumentDetail
import com.partnership.bjbdocumenttrackerreader.databinding.FragmentSingleSearchDocumentBinding
import com.partnership.bjbdocumenttrackerreader.ui.adapter.SearchAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SingleSearchDocumentFragment : Fragment() {
    private var listener: OnDocumentItemClickListener? = null
    private val searchViewModel: SearchViewModel by activityViewModels()
    private lateinit var adapter: SearchAdapter
    private var _binding: FragmentSingleSearchDocumentBinding? = null
    private val binding get() = _binding!!
    val dummyDocuments = listOf(
        DocumentDetail(
            rfidNumber = "02010021",
            cif = "CIF123456",
            namaNasabah = "Budi Santoso"
        ),
        DocumentDetail(
            rfidNumber = "02010020",
            cif = "CIF654321",
            namaNasabah = "Siti Aminah"
        ),
        DocumentDetail(
            rfidNumber = "02010022",
            cif = "CIF789012",
            namaNasabah = "Andi Nugroho"
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSingleSearchDocumentBinding.inflate(inflater, container, false)
        return binding.root
    }

    interface OnDocumentItemClickListener {
        fun onDocumentItemClicked(item: DocumentDetail)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as? OnDocumentItemClickListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchInput()
        observeSearchResult()
        //adapter.submitList(dummyDocuments)
    }

    private fun setupRecyclerView() {
        adapter = SearchAdapter { document ->
          searchViewModel.setDataDocumentSearch(document)
            findNavController().navigate(R.id.action_searchDocumentFragment_to_searchingDocumentFragment)
        }

        binding.rvSearchResult.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SingleSearchDocumentFragment.adapter
        }
    }

    private fun setupSearchInput() {
        binding.etSearch.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val keyword = textView.text.toString().trim()
                if (keyword.isNotEmpty()) {
                    searchViewModel.getListSearchDocument(keyword)
                }
                true
            } else {
                false
            }
        }
    }

    private fun observeSearchResult() {
        searchViewModel.listSearchDocument.observe(viewLifecycleOwner) { result ->
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
                    adapter.submitList(result.data.data)
                }
                is ResultWrapper.NetworkError -> {
                    Snackbar.make(binding.root, "Jaringan bermasalah, Harap mendekat ke wifi", Snackbar.LENGTH_LONG)
                        .setAction("Baik") {}
                        .show()
                    Log.e(TAG, "uploadData: ${result.error}")
                }
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

