package com.partnership.bjbdocumenttrackerreader.ui.scan

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.partnership.bjbdocumenttrackerreader.MainActivity
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.data.model.TagInfo
import com.partnership.bjbdocumenttrackerreader.databinding.FragmentScanBinding
import com.partnership.bjbdocumenttrackerreader.ui.adapter.EpcAdapter
import com.partnership.bjbdocumenttrackerreader.ui.adapter.EpcViewPagerAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScanFragment : Fragment() {
    private val viewModel : RFIDViewModel by viewModels()
    // Deklarasi objek binding
    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!
    private lateinit var epcAdapter: EpcAdapter
    private var epcList = mutableListOf<TagInfo>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Menginisialisasi binding
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        val activity = (activity as MainActivity)
        activity.setSupportActionBar(binding.toolbarScan)
        activity.setupActionBarWithNavController(findNavController())
        binding.toolbarScan.setNavigationIcon(R.drawable.arrow_back_ios_24px)
        binding.toolbarScan.setNavigationOnClickListener {
            findNavController().navigateUp()
        }


        // Kembalikan root view dari binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.isReaderInit.observe(viewLifecycleOwner){ isInited ->
            viewModel.messageReader.observe(viewLifecycleOwner){
                val message = it
                binding.btScan.isEnabled = isInited
                binding.btSend.isEnabled = isInited
                if(isInited){
                    Snackbar.make(requireView(),message,Snackbar.LENGTH_LONG).show()
                }else{
                    Snackbar.make(requireView(),message,Snackbar.LENGTH_LONG).show()
                }
            }
        }
        setUpMenu()
        setupRecyclerView()

        viewModel.tagList.observe(viewLifecycleOwner){tagList ->
            if (tagList.size == 0){
                binding.btClear.isEnabled = false
            }else{
                binding.btClear.isEnabled = true
                epcList = tagList
                epcAdapter.updateData(epcList)
                binding.tvTotalTagNumber.text = tagList.size.toString()
            }
        }

        binding.btClear.setOnClickListener {
            viewModel.clearTagList()
            binding.tvTotalTagNumber.text = "0"
            epcList.clear()
            epcAdapter.updateData(epcList)
        }

        viewModel.isScanning.observe(viewLifecycleOwner){isScanning ->
            if (isScanning){
                binding.btScan.text = "STOP"
                binding.btScan.setBackgroundColor(Color.RED)
            }else{
                binding.btScan.text = "START"
                binding.btScan.setBackgroundColor(resources.getColor(R.color.md_theme_yellow))
            }
            binding.btScan.setOnClickListener {
                if (isScanning){
                    viewModel.stopReadTag()
                }else{
                    viewModel.readTagAuto()
                }
            }
        }

        viewModel.elapsedTime.observe(viewLifecycleOwner){
            binding.tvTime.text = it
        }
    }

    private fun setupRecyclerView() {
        epcAdapter = EpcAdapter(epcList)
        binding.rvEpc.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = epcAdapter
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel.initReader(context)

    }

    fun setUpMenu(){
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider{
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.reader_setting, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when(menuItem.itemId) {
                    R.id.readerSetting ->{
                        findNavController().navigate(R.id.action_scanFragment_to_settingReaderFragment)
                        true
                    }

                    else -> false
                }
            }

        },viewLifecycleOwner, Lifecycle.State.RESUMED
            )
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
