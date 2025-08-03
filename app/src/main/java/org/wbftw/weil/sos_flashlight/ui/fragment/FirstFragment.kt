package org.wbftw.weil.sos_flashlight.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.wbftw.weil.sos_flashlight.ui.activity.MainActivity
import org.wbftw.weil.sos_flashlight.databinding.FragmentFirstBinding

/**
 * [FirstFragment] is used to display the main interface of the SOS Flashlight application.
 * It allows users to toggle the sending of messages and displays the current signal character.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.powerButton.setOnClickListener {
            activity?.let { activity ->
                if (activity is MainActivity) {
                    activity.toggleSendingMessage()
                }
            }
        }
    }

    fun updateSignalChar(char: Char){
        binding.textView.text = char.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}