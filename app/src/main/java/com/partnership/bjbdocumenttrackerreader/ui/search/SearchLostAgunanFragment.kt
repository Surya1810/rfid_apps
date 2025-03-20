package com.partnership.bjbdocumenttrackerreader.ui.search

import android.content.ContentValues.TAG
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.data.model.ItemStatus
import com.partnership.bjbdocumenttrackerreader.databinding.FragmentSearchAgunanLostBinding
import com.partnership.bjbdocumenttrackerreader.databinding.FragmentSearchDocumentLostBinding
import com.partnership.bjbdocumenttrackerreader.ui.adapter.EpcLostAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchLostAgunanFragment : Fragment() {
    private var isScanning = false
    private val rfidViewModel : SearchViewModel by viewModels()
    private lateinit var adapter: EpcLostAdapter
    private var listLost = mutableListOf<ItemStatus>()
    private var _binding: FragmentSearchAgunanLostBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchAgunanLostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rfidViewModel.isScanning.observe(viewLifecycleOwner){
            isScanning = it
        }
        adapter = EpcLostAdapter()
        binding.rvEpc.adapter = adapter
        binding.rvEpc.layoutManager = LinearLayoutManager(requireContext())
        if (rfidViewModel.getLostEpc.value == null){
            rfidViewModel.getLostEpc()
        }
        var listLost = (19..31).map { number ->
            val epc = "020100" + number.toString().padStart(2, '0')
            ItemStatus(epc = epc, isThere = false)
        }
        rfidViewModel.lostCount.observe(viewLifecycleOwner){
            binding.tvTagLost.text = it.toString()
        }
        rfidViewModel.foundCount.observe(viewLifecycleOwner){
            binding.tvTagFound.text = it.toString()
        }
        rfidViewModel.getLostEpc.observe(viewLifecycleOwner){
            when (it) {
                is ResultWrapper.Error -> {
                    Snackbar.make(
                        binding.root,
                        "Terjadi masalah harap hubungi Admin",
                        Snackbar.LENGTH_LONG
                    )
                        .show()
                    Log.e(TAG, "uploadData: ${it.error}")
                }

                is ResultWrapper.ErrorResponse -> {
                    Snackbar.make(binding.root, it.error, Snackbar.LENGTH_LONG)
                        .show()
                    Log.e(TAG, "uploadData: ${it.error}")
                }

                ResultWrapper.Loading -> {

                }

                is ResultWrapper.Success -> {
                    listLost = it.data.data!!
                    rfidViewModel.addLostTag(listLost)
                }

                is ResultWrapper.NetworkError -> {
                    Snackbar.make(
                        binding.root,
                        "Jaringan bermasalah, Harap mendekat ke wifi",
                        Snackbar.LENGTH_LONG
                    )
                        .setAction("Baik") {

                        }
                        .show()
                    Log.e(TAG, "uploadData: ${it.error}")
                }
            }
        }

        rfidViewModel.displayedList.observe(viewLifecycleOwner){
            adapter.submitList(it)
            listLost = it
        }

        binding.btSend.setOnClickListener {
            rfidViewModel.sendDataLost(listLost,false)
        }

        binding.btScan.setOnClickListener {
            scanTag(isScanning)
        }
        rfidViewModel.elapsedTime.observe(viewLifecycleOwner){
            binding.tvTime.text = it
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun scanTag(isScanning: Boolean) {
        if (isScanning) {
            rfidViewModel.stopReadTag()
            binding.btScan.text = "START"
            binding.btScan.setBackgroundColor(resources.getColor(R.color.md_theme_yellow))
            binding.btSend.isEnabled = true
            binding.btSend.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.md_theme_yellow))
            binding.tvTime.text = "00:00"
        } else {
            rfidViewModel.readTagAuto()
            binding.btScan.text = "STOP"
            binding.btScan.setBackgroundColor(Color.RED)
            binding.btSend.isEnabled = false
            binding.btSend.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.md_theme_outlineVariant))
        }
    }
}
