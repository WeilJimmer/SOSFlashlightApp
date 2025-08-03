package org.wbftw.weil.sos_flashlight.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.wbftw.weil.sos_flashlight.BuildConfig
import org.wbftw.weil.sos_flashlight.R
import org.wbftw.weil.sos_flashlight.databinding.FragmentAboutBinding

/**
 * [AboutFragment] is used to display the copyright information and provide navigation options.
 */
class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root

    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textviewVersion.text = "Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

        binding.buttonClick.setOnClickListener {
            findNavController().navigate(R.id.FirstFragment)
        }

        binding.buttonSource.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                "https://github.com/WeilJimmer/SOSFlashlightApp.git".toUri()
            )
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}