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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.data.model.ItemStatus
import com.partnership.bjbdocumenttrackerreader.databinding.FragmentSearchDocumentLostBinding
import com.partnership.bjbdocumenttrackerreader.reader.BeepSoundManager
import com.partnership.bjbdocumenttrackerreader.ui.adapter.EpcLostAdapter
import com.partnership.bjbdocumenttrackerreader.util.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchLostDocumentFragment : Fragment() {

    @Inject  lateinit var soundManager: BeepSoundManager
    private var isScanning = false
    private val rfidViewModel : SearchViewModel by viewModels()
    private lateinit var adapter: EpcLostAdapter
    private var listLost = mutableListOf<ItemStatus>()
    private var _binding: FragmentSearchDocumentLostBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchDocumentLostBinding.inflate(inflater, container, false)
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
        if (rfidViewModel.getLostEpcDocument.value == null){
            rfidViewModel.getLostEpc(true)
        }
        var listLost = (19..31).map { number ->
            val epc = "020100" + number.toString().padStart(2, '0')
            ItemStatus(epc = epc, isThere = false)
        }
        rfidViewModel.lostCount.observe(viewLifecycleOwner){
            binding.tvTagLost.text = it.toString()
        }

        rfidViewModel.soundBeep.observe(viewLifecycleOwner){
            if (it){
                soundManager.playBeep()
            }
        }
        rfidViewModel.foundCount.observe(viewLifecycleOwner){
            binding.tvTagFound.text = it.toString()
        }
        rfidViewModel.getLostEpcDocument.observe(viewLifecycleOwner){
            when (it) {
                is ResultWrapper.Error -> {
                    Utils.dismissLoading()
                    Snackbar.make(
                        binding.root,
                        "Terjadi masalah harap hubungi Admin",
                        Snackbar.LENGTH_LONG
                    )
                        .show()
                    Log.e(TAG, "uploadData: ${it.error}")
                }

                is ResultWrapper.ErrorResponse -> {
                    Utils.dismissLoading()
                    Snackbar.make(binding.root, it.error, Snackbar.LENGTH_LONG)
                        .show()
                    Log.e(TAG, "uploadData: ${it.error}")
                }

                ResultWrapper.Loading -> {

                }

                is ResultWrapper.Success -> {
                    Utils.dismissLoading()
                    listLost = it.data.data!!
                    rfidViewModel.addLostTag(listLost)
                }

                is ResultWrapper.NetworkError -> {
                    Utils.dismissLoading()
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

        binding.btSend.setOnClickListener {
            rfidViewModel.sendDataLost(listLost,true)
            rfidViewModel.getLostEpc(true)
            Utils.showLoading(requireContext())
        }

        rfidViewModel.displayedList.observe(viewLifecycleOwner){
            listLost = it
            adapter.submitList(it)

        }

        rfidViewModel.postMessage.observe(viewLifecycleOwner){
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
                    Snackbar.make(binding.root, "Berhasil Diupdate", Snackbar.LENGTH_LONG)
                        .show()
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
