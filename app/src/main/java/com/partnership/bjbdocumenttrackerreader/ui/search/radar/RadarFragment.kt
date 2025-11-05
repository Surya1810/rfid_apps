package com.partnership.bjbdocumenttrackerreader.ui.search.radar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.partnership.bjbdocumenttrackerreader.MainActivity
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.data.model.Document
import com.partnership.bjbdocumenttrackerreader.databinding.FragmentRadarBinding
import com.partnership.bjbdocumenttrackerreader.reader.BeepSoundManager
import com.partnership.bjbdocumenttrackerreader.ui.scan.StockOpnameViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.getValue

@AndroidEntryPoint
class RadarFragment : Fragment() {
    @Inject
    lateinit var soundManager: BeepSoundManager
    private var _binding: FragmentRadarBinding? = null
    private val binding get() = _binding!!
    private val stockOpnameViewModel: StockOpnameViewModel by activityViewModels()
    private val radarViewModel: RadarViewModel by viewModels()
    private lateinit var epcToSearch: String
    private lateinit var document: Document
    private var showAllTag = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRadarBinding.inflate(inflater, container, false)
        setupToolbar()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stockOpnameViewModel.searchDocumentEpc.observe(viewLifecycleOwner){
            binding.tvEpc.text = it.rfid
            binding.tvItemName.text = it.name
            document = it
            epcToSearch = it.rfid
        }

        binding.swShowAllTag.setOnCheckedChangeListener { _, isChecked ->
            showAllTag = isChecked
            // refresh radar view dengan setting baru
            val currentData = radarViewModel.radarData.value
            if (currentData != null && ::epcToSearch.isInitialized) {
                binding.radarView.bindingData(currentData, epcToSearch, showAllTag)
            }
        }

        radarViewModel.radarData.observe(viewLifecycleOwner) {
            binding.radarView.bindingData(
                TagList = it,
                targetTag = epcToSearch,
                showAllTag = showAllTag
            )
        }

        radarViewModel.beep.observe(viewLifecycleOwner){
            if(it){
                soundManager.playBeep()
            }
        }

        binding.btnScanSearching.setOnClickListener {
            if(radarViewModel.isScanning.value == true){
                radarViewModel.stopRadar()
                binding.swShowAllTag.isEnabled = true
                binding.btnScanSearching.text = "Mulai"
                binding.btnScanSearching.setBackgroundColor(resources.getColor(R.color.md_theme_primary))
                binding.radarView.clearPanel()
                binding.radarView.stopRadar()
            }else{
                binding.radarView.clearPanel()
                radarViewModel.startRadar(if (showAllTag) "" else epcToSearch)
                binding.radarView.startRadar()
                binding.swShowAllTag.isEnabled = false
                binding.btnScanSearching.text = "Berhenti"
                binding.btnScanSearching.setBackgroundColor(resources.getColor(R.color.md_theme_error))
            }
        }

        radarViewModel.angle.observe(viewLifecycleOwner){
            binding.radarView.setRotation(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun setupToolbar() {
        val activity = (activity as MainActivity)
        activity.setSupportActionBar(binding.toolbar)
        activity.setupActionBarWithNavController(findNavController())

        binding.toolbar.setNavigationIcon(R.drawable.arrow_back_ios_24px)
        binding.toolbar.setNavigationOnClickListener {
            if (radarViewModel.isScanning.value == true){
                Toast.makeText(requireContext(), "Hentikan locating terlebih dahulu", Toast.LENGTH_SHORT).show()
            }else{
                findNavController().navigateUp()
            }
        }
    }
}


