package com.partnership.bjbdocumenttrackerreader.ui.book

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.partnership.bjbdocumenttrackerreader.databinding.FragmentPdfViewBinding
import com.partnership.bjbdocumenttrackerreader.ui.adapter.VideoTutorialAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ManualBookFragment : Fragment() {
    private var _binding: FragmentPdfViewBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: VideoTutorialAdapter
    private val manualViewModel : ManualViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPdfViewBinding.inflate(inflater, container, false)
        manualViewModel.getListTutorial()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = VideoTutorialAdapter()
        manualViewModel.videoList.observe(viewLifecycleOwner){
            adapter.submitList(it)
        }
        binding.rvTutorial.adapter = adapter
    }
}

