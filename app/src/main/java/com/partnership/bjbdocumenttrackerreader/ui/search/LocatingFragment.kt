package com.partnership.bjbdocumenttrackerreader.ui.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.databinding.FragmentLocatingBinding
import com.partnership.bjbdocumenttrackerreader.reader.BeepSoundManager
import com.partnership.bjbdocumenttrackerreader.reader.RFIDManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LocatingFragment : Fragment() {

    @Inject
    lateinit var soundManager: BeepSoundManager

    private val searchViewModel : SearchViewModel by activityViewModels()
    @Inject lateinit var reader : RFIDManager

    private var epc = ""

    private var _binding: FragmentLocatingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocatingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchViewModel.searchDocumentEpc.observe(viewLifecycleOwner){
            epc = it.rfid
        }
        binding.btnStartScanRadar.setOnClickListener {
            reader.startLocatingTag(requireContext(),epc){value,valid ->
                binding.llChart.setData(value)
                if (valid){
                    soundManager.playBeep()
                }
            }
            binding.btnStartScanRadar.isEnabled = false
        }
        binding.btnStopScanRadar.setOnClickListener {
            reader.stopLocating()
            binding.btnStartScanRadar.isEnabled = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
