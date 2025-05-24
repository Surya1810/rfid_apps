package com.partnership.bjbdocumenttrackerreader.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.reader.RFIDManager
import com.partnership.bjbdocumenttrackerreader.ui.scan.StockOpnameViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SplashScreenFragment : Fragment() {

    @Inject
    lateinit var reader: RFIDManager
    private val viewModel : StockOpnameViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initReaderIfNeeded()
        if (reader.getCurrentPower() != 30){
            reader.setPower(30)
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash_screen, container, false)

    }
    private fun initReaderIfNeeded() {
        if (viewModel.isReaderInit.value == false) {
            viewModel.initReader(requireActivity())
        }

        viewModel.messageReader.observe(viewLifecycleOwner) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.splashScreenFragment, true)
                .build()
            findNavController().navigate(R.id.action_splashScreenFragment_to_homeFragment, null, navOptions)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as AppCompatActivity).supportActionBar?.show()
    }

}