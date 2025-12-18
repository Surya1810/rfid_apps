package com.partnership.bjbdocumenttrackerreader.ui.lending

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.github.gcacace.signaturepad.views.SignaturePad
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.databinding.FragmentSignaturePadBinding
import com.partnership.bjbdocumenttrackerreader.util.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class SignaturePadFragment : Fragment() {

    private var _binding: FragmentSignaturePadBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BorrowingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignaturePadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvClear.setOnClickListener {
            binding.signaturePad.clear()
        }

        binding.signaturePad.setOnSignedListener(object :
            SignaturePad.OnSignedListener {

            override fun onStartSigning() {
                // user mulai gambar → sembunyikan teks
                binding.tvInformation.visibility = View.GONE
            }

            override fun onSigned() {
                // sudah ada tanda tangan → pastikan hilang
                binding.tvInformation.visibility = View.GONE
            }

            override fun onClear() {
                // di-clear → balikin teks
                binding.tvInformation.visibility = View.VISIBLE
            }
        })

        binding.tvDone.setOnClickListener {
            if (binding.signaturePad.isEmpty) return@setOnClickListener

            Utils.showLoading(requireContext(), "Menyimpan tanda tangan...")

            lifecycleScope.launch {
                try {
                    // kerja berat pindah ke background
                    val file = withContext(Dispatchers.IO) {
                        val bitmap = binding.signaturePad.transparentSignatureBitmap
                        bitmap.toFile(requireContext())
                    }

                    if (viewModel.isCreteBorrow.value == true) {
                        viewModel.setSignatureBorrowed(file)
                    } else {
                        viewModel.setSignatureReturned(file)
                    }

                    viewModel.setIsCreateBorrow(false)

                    Utils.dismissLoading()
                    findNavController().popBackStack()

                } catch (e: Exception) {
                    Utils.dismissLoading()
                    Toast.makeText(
                        requireContext(),
                        "Gagal menyimpan tanda tangan",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    }
    override fun onResume() {
        super.onResume()
        requireActivity().requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }
    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        _binding = null
    }

}

fun Bitmap.toFile(context: Context): File {
    val file = File(
        context.cacheDir,
        "signature_${System.currentTimeMillis()}.png"
    )

    FileOutputStream(file).use { out ->
        compress(Bitmap.CompressFormat.PNG, 100, out)
    }

    return file
}

