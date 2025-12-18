package com.partnership.bjbdocumenttrackerreader.ui.scan.histories

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.partnership.bjbdocumenttrackerreader.MainActivity
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.databinding.FragmentHistoriesSoBinding
import com.partnership.bjbdocumenttrackerreader.ui.adapter.HistoriesPagerAdapter
import com.partnership.bjbdocumenttrackerreader.ui.scan.StockOpnameViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale


@AndroidEntryPoint
class HistoriesSoFragment : Fragment() {

    private var _binding: FragmentHistoriesSoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StockOpnameViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoriesSoBinding.inflate(inflater, container, false)
        setupToolbar()
        viewModel.fetchLostInfo()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPager()
        observeViewModel()
    }

    private fun setupPager() {
        val adapter = HistoriesPagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> {
                    "Dokumen Kontrak"
                }
                else -> {
                    "Agunan"
                }
            }
        }.attach()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.lostInfo.collectLatest {
                binding.tvTotalAgunan.text = it?.countAgunan.toString()
                binding.tvTotalContract.text = it?.countDocument.toString()
                binding.tvTotalLostValue.text = formatToRupiah(it?.value ?: 0.0)
                val total = it?.countAgunan?.plus(it.countDocument)
                binding.tvTotalLost.text = total.toString()
            }
        }
    }
    private fun setupToolbar() {
        val activity = (activity as MainActivity)
        activity.setSupportActionBar(binding.toolbar)
        activity.setupActionBarWithNavController(findNavController())

        binding.toolbar.setTitle("Riwayat Stock Opname")

        binding.toolbar.setNavigationIcon(R.drawable.arrow_back_ios_24px)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    fun formatToRupiah(value: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return format.format(value).replace(",00", "").replace("Rp", "Rp")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
