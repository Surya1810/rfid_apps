package com.partnership.bjbdocumenttrackerreader.ui.book

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.partnership.bjbdocumenttrackerreader.MainActivity
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.databinding.FragmentManualBinding


class ManualFragment : Fragment() {

    private var _binding: FragmentManualBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManualBinding.inflate(inflater, container, false)
        setupToolbar()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btBook.setOnClickListener {
            val url = "http://192.168.0.101:5000/template/manual_book.pdf"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = url.toUri()
            }
            requireActivity().startActivity(intent)
        }
        binding.btVideo.setOnClickListener {
            findNavController().navigate(R.id.action_manualFragment_to_manualBookFragment)
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

        binding.toolbar.setTitle("Panduan Aplikasi")

        binding.toolbar.setNavigationIcon(R.drawable.arrow_back_ios_24px)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }
}
