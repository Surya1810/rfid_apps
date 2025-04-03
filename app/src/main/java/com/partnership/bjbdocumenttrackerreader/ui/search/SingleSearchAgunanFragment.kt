package com.partnership.bjbdocumenttrackerreader.ui.search

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.data.model.DetailAgunan
import com.partnership.bjbdocumenttrackerreader.data.model.DocumentDetail
import com.partnership.bjbdocumenttrackerreader.databinding.FragmentSingleSearchAgunanBinding
import com.partnership.bjbdocumenttrackerreader.ui.adapter.SearchAdapter
import com.partnership.bjbdocumenttrackerreader.ui.adapter.SearchAgunanAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SingleSearchAgunanFragment : Fragment() {

    private var listener: OnAgunanItemClickListener? = null
    private val searchViewModel: SearchViewModel by activityViewModels()
    private lateinit var adapter: SearchAgunanAdapter
    private var _binding: FragmentSingleSearchAgunanBinding? = null
    private val binding get() = _binding!!
    interface OnAgunanItemClickListener {
        fun onAgunanItemClicked(item: DetailAgunan)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as? OnAgunanItemClickListener
            ?: activity as? OnAgunanItemClickListener

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSingleSearchAgunanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchInput()
        observeSearchResult()
        //adapter.submitList(dummyList)
    }



    private fun setupRecyclerView() {
        adapter = SearchAgunanAdapter { agunan ->
            listener?.onAgunanItemClicked(agunan)
            findNavController().navigate(R.id.action_searchAgunanFragment_to_searchingAgunanFragment)
            Log.d("SingleSearchAgunan", "Item clicked: ${agunan.rfidNumber}")
            searchViewModel.setDataAgunanSearch(agunan)
        }

        binding.rvSearchResult.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SingleSearchAgunanFragment.adapter
        }
    }

    private fun setupSearchInput() {
        binding.etSearch.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val keyword = textView.text.toString().trim()
                if (keyword.isNotEmpty()) {
                    searchViewModel.getListSearchAgunan(keyword)
                    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(textView.windowToken, 0)
                }
                true
            } else {
                false
            }
        }
    }

    private fun observeSearchResult() {
        searchViewModel.listSearchAgunan.observe(viewLifecycleOwner) { result ->

            when (result) {
                is ResultWrapper.Loading -> {
                    binding.progressBarSearch.visibility = View.VISIBLE
                }
                is ResultWrapper.Success -> {
                    binding.progressBarSearch.visibility = View.GONE
                    adapter.submitList(result.data.data)
                }
                is ResultWrapper.Error -> {
                    binding.progressBarSearch.visibility = View.GONE
                    Snackbar.make(binding.root, "Terjadi masalah harap hubungi Admin", Snackbar.LENGTH_LONG).show()
                    Log.e(TAG, "uploadData: ${result.error}")
                }
                is ResultWrapper.ErrorResponse -> {
                    binding.progressBarSearch.visibility = View.GONE
                    Snackbar.make(binding.root, result.error, Snackbar.LENGTH_LONG).show()
                    Log.e(TAG, "uploadData: ${result.error}")
                }
                is ResultWrapper.NetworkError -> {
                    binding.progressBarSearch.visibility = View.GONE
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
