package com.partnership.bjbdocumenttrackerreader.ui.scan

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.databinding.FragmentSettingReaderBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingReaderFragment : Fragment() {
    private val viewModel : StockOpnameViewModel by activityViewModels()
    private var _binding: FragmentSettingReaderBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingReaderBinding.inflate(inflater, container, false)
        binding.toolbarScan.setNavigationIcon(R.drawable.arrow_back_ios_24px)
        binding.toolbarScan.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.sliderPower.value = viewModel.getCurrentPower()?.toFloat()!!
        binding.sliderPower.addOnChangeListener{silder,value,fromUser ->
            if (fromUser){
                viewModel.setPowerReader(value.toInt())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
