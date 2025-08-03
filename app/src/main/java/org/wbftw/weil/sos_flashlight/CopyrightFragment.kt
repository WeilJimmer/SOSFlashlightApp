package org.wbftw.weil.sos_flashlight

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.wbftw.weil.sos_flashlight.databinding.FragmentCopyrightBinding

/**
 * CopyrightFragment is used to display the copyright information and provide navigation options.
 */
class CopyrightFragment : Fragment() {

    private var _binding: FragmentCopyrightBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCopyrightBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonClick.setOnClickListener {
            findNavController().navigate(R.id.FirstFragment)
        }

        binding.buttonSource.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW,"https://github.com/WeilJimmer/SOSFlashlighApp.git".toUri())
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}