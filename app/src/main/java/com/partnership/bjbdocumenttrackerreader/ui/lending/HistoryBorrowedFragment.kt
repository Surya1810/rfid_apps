package com.partnership.bjbdocumenttrackerreader.ui.lending

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.partnership.bjbdocumenttrackerreader.MainActivity
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.databinding.FragmentHistoryBorrowedBinding
import com.partnership.bjbdocumenttrackerreader.ui.adapter.HistoryBorrowedAdapter
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HistoryBorrowedFragment : Fragment() {

    private var _binding: FragmentHistoryBorrowedBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BorrowingViewModel by activityViewModels()
    private lateinit var adapter: HistoryBorrowedAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBorrowedBinding.inflate(inflater, container, false)
        setupToolbar()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

       viewModel.documentSelected.observe(viewLifecycleOwner){
           viewModel.getHistoryBorrow(idDocument = it.id)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun setupToolbar() {
        val activity = (activity as MainActivity)
        activity.setSupportActionBar(binding.toolbar)
        activity.setupActionBarWithNavController(findNavController())

        binding.toolbar.setTitle("Riwayat Peminjaman")

        binding.toolbar.setNavigationIcon(R.drawable.arrow_back_ios_24px)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }
}
