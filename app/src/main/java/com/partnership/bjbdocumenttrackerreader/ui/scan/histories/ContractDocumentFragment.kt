package com.partnership.bjbdocumenttrackerreader.ui.scan.histories

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.databinding.FragmentContractDocumentBinding
import com.partnership.bjbdocumenttrackerreader.ui.adapter.HistoriesPagingAdapter
import com.partnership.bjbdocumenttrackerreader.ui.scan.StockOpnameViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ContractDocumentFragment : Fragment() {

    private var _binding: FragmentContractDocumentBinding? = null
    private val binding get() = _binding!!
    private val adapter = HistoriesPagingAdapter()
    private val viewModel: StockOpnameViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContractDocumentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvDocumentContract.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ContractDocumentFragment.adapter
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.historiesDocumentContract.collectLatest {
                adapter.submitData(it)
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
