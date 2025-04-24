package com.partnership.bjbdocumenttrackerreader.ui.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.partnership.bjbdocumenttrackerreader.MainActivity
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
        setupToolbar()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchViewModel.searchDocumentEpc.observe(viewLifecycleOwner){
            epc = it.rfid
        }
        binding.btnStartScanRadar.setOnClickListener {
            reader.setPower(30)
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

    private fun setupToolbar() {
        val activity = (activity as MainActivity)
        activity.setSupportActionBar(binding.toolbarScan)
        activity.setupActionBarWithNavController(findNavController())

        binding.toolbarScan.setNavigationIcon(R.drawable.arrow_back_ios_24px)
        binding.toolbarScan.setNavigationOnClickListener {
            searchViewModel.clearFilterReader()
            searchViewModel.setNotFound()
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
