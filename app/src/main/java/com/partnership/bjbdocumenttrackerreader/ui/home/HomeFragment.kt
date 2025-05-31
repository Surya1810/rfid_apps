package com.partnership.bjbdocumenttrackerreader.ui.home

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.data.model.GetDashboard
import com.partnership.bjbdocumenttrackerreader.databinding.FragmentHomeBinding
import com.partnership.bjbdocumenttrackerreader.ui.adapter.DocumentAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.strHome.setOnRefreshListener {
            viewModel.getDashboard()
            setRefreshing(true)
        }

        binding.btScan.setOnClickListener {
            showDialog()
        }

        if (viewModel.dataDashboard.value == null) {
            viewModel.getDashboard()
            setRefreshing(true)
        }

        binding.btSearchAgunan.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchDocumentFragment)
            viewModel.setIsDocument(false)
        }

        binding.btSearchDocument.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchDocumentFragment)
            viewModel.setIsDocument(true)
        }

        viewModel.dataDashboard.observe(viewLifecycleOwner) {
            setRefreshing(false)
            when (it) {
                is ResultWrapper.Error -> {
                    setRefreshing(false)
                    Snackbar.make(binding.root, "Terjadi masalah harap hubungi Admin", Snackbar.LENGTH_LONG).show()
                    Log.e(TAG, "uploadData: ${it.error}")
                }
                is ResultWrapper.ErrorResponse -> {
                    setRefreshing(false)
                    Snackbar.make(binding.root, it.error, Snackbar.LENGTH_LONG).show()
                    Log.e(TAG, "uploadData: ${it.error}")
                }
                ResultWrapper.Loading -> {
                    setRefreshing(true)
                }
                is ResultWrapper.Success -> {
                    setRefreshing(false)

                    it.data.data?.let { data ->
                        setData(data)
                    }
                }
                is ResultWrapper.NetworkError -> {
                    setRefreshing(false)
                    Snackbar.make(binding.root, "Jaringan bermasalah, Harap mendekat ke wifi", Snackbar.LENGTH_LONG)
                        .setAction("Baik") {}
                        .show()
                    Log.e(TAG, "uploadData: ${it.error}")
                }
            }
        }
    }


    private fun formatToRupiah(value: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return format.format(value).replace(",00", "").replace("Rp", "Rp")
    }

    private fun showDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_select_type, null)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setTitle("Pilih Tipe Dokumen")
            .create()

        dialog.show() // tampilkan dulu, baru findViewById dari dialog

        val btnDocument = dialog.findViewById<Button>(R.id.btnDocument)
        val btnAgunan = dialog.findViewById<Button>(R.id.btnAgunan)

        btnDocument?.setOnClickListener {
            viewModel.setIsDocument(true)
            findNavController().navigate(R.id.action_homeFragment_to_stockOpnameFragment)
            dialog.dismiss()
        }

        btnAgunan?.setOnClickListener {
            viewModel.setIsDocument(false)
            findNavController().navigate(R.id.action_homeFragment_to_stockOpnameFragment)
            dialog.dismiss()
        }
    }



    private fun setData(dashboardData: GetDashboard){
        binding.tvLts.text = dashboardData.overview.lastTimeScan
        binding.tvTotalData.text = dashboardData.overview.totalData.toString()
        binding.tvTotalNilai.text = formatToRupiah(dashboardData.overview.totalvalue)
        binding.tvTotalDocument.text = dashboardData.totalDocuments.toString()
        binding.tvTotalAgunan.text = dashboardData.totalAgunan.toString()
        binding.tvLost.text = dashboardData.dashboard.totalDocumentsLost.toString()
        binding.tvValueLost.text = formatToRupiah(dashboardData.dashboard.valueLostDocument)
        binding.tvFound.text = dashboardData.dashboard.totalDocumentsFound.toString()
    }

    private fun setRefreshing(isRefreshing: Boolean) {
        binding.strHome.isRefreshing = isRefreshing
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
