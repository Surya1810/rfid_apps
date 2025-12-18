package com.partnership.bjbdocumenttrackerreader.ui.home

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.data.model.GetDashboard
import com.partnership.bjbdocumenttrackerreader.databinding.FragmentDashboardBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.strHome.setOnRefreshListener {
            viewModel.getDashboard()
            setRefreshing(true)
        }

        binding.cardStock.setOnClickListener {
            showDialog()
        }

        if (viewModel.dataDashboard.value == null) {
            viewModel.getDashboard()
            setRefreshing(true)
        }


        binding.cardSearch.setOnClickListener {
            showDialogSearch()
        }

        binding.cardHistorySo.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_historiesSoFragment)
        }

        binding.cardGuide.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_manualFragment)
        }

        binding.cardBorrow.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_borrowingFragment)
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

    fun formatToRupiah(value: Double): String {
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

    private fun showDialogSearch() {
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
            findNavController().navigate(R.id.action_homeFragment_to_searchDocumentFragment)
            dialog.dismiss()
        }

        btnAgunan?.setOnClickListener {
            viewModel.setIsDocument(false)
            findNavController().navigate(R.id.action_homeFragment_to_searchDocumentFragment)
            dialog.dismiss()
        }
    }

    private fun setData(dashboardData: GetDashboard){
        binding.tvLastUpdate.text = "Data diperbaharui pada ${dashboardData.overview.lastTimeScan}"
        binding.tvTotalDocument.text = dashboardData.overview.totalData.toString()
        binding.tvTotalNilai.text = formatToRupiah(dashboardData.overview.totalValue)
        binding.tvTotalDocument.text = dashboardData.overview.totalData.toString()
        binding.tvDocumentCount.text = dashboardData.totalDocuments.toString()
        binding.tvAgunanCount.text = dashboardData.totalAgunan.toString()
        binding.tvBorrowCount.text = dashboardData.totalBorrowedDocuments.toString()
        binding.tvLastScanStatDocument.text = "${dashboardData.lastStockOpname.document.totalFound}/${dashboardData.lastStockOpname.document.totalItems} Scanned"
        binding.tvLastScanStatAgunan.text = "${dashboardData.lastStockOpname.agunan.totalFound}/${dashboardData.lastStockOpname.agunan.totalItems} Scanned"
        binding.tvSoDateContract.text = dashboardData.lastStockOpname.document.lastTimeScan ?: "Tidak diketahui"
        binding.tvSoDateAgunan.text = dashboardData.lastStockOpname.agunan.lastTimeScan ?: "Tidak diketahui"
    }

    private fun setRefreshing(isRefreshing: Boolean) {
        binding.strHome.isRefreshing = isRefreshing
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
