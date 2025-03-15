package com.partnership.bjbdocumenttrackerreader.ui.home

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.data.model.GetDashboard
import com.partnership.bjbdocumenttrackerreader.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btScan.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_scanFragment)
        }

        if (viewModel.dataDashboard.value == null) {
            viewModel.getDashboard()
        }

        viewModel.dataDashboard.observe(viewLifecycleOwner) {
            when (it) {
                is ResultWrapper.Error -> {
                    Snackbar.make(binding.root, "Terjadi masalah harap hubungi Admin", Snackbar.LENGTH_LONG).show()
                    Log.e(TAG, "uploadData: ${it.error}")
                }
                is ResultWrapper.ErrorResponse -> {
                    Snackbar.make(binding.root, it.error, Snackbar.LENGTH_LONG).show()
                    Log.e(TAG, "uploadData: ${it.error}")
                }
                ResultWrapper.Loading -> {
                    // Tambahkan UI loading jika perlu
                }
                is ResultWrapper.Success -> {
                    it.data.data?.let { data ->
                        setData(data)
                        Log.e(TAG, "onViewCreated: $data")
                    }
                }
                is ResultWrapper.NetworkError -> {
                    Snackbar.make(binding.root, "Jaringan bermasalah, Harap mendekat ke wifi", Snackbar.LENGTH_LONG)
                        .setAction("Baik") {}
                        .show()
                    Log.e(TAG, "uploadData: ${it.error}")
                }
            }
        }
    }


    fun setData(dashboardData: GetDashboard){
        binding.tvLts.text = dashboardData.overview.lastTimeScan
        binding.tvTotalData.text = dashboardData.overview.totalData.toString()
        binding.tvTotalNilai.text = dashboardData.overview.totalvalue.toString()
        binding.tvTotalDocument.text = dashboardData.totalDocuments.toString()
        binding.tvTotalAgunan.text = dashboardData.totalAgunan.toString()
        binding.tvValueLost.text = dashboardData.dashboard.valueLostDocument.toString()
        binding.tvFound.text = dashboardData.dashboard.totalDocumentsFound.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
