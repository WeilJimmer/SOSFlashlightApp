package org.wbftw.weil.sos_flashlight.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import org.wbftw.weil.sos_flashlight.databinding.FragmentScreenLightBinding
import org.wbftw.weil.sos_flashlight.ui.viewmodel.MainActivityViewModel
import org.wbftw.weil.sos_flashlight.utils.BrightControl

/**
 * [ScreenLightFragment] is used to display a full-screen light screen.
 */
class ScreenLightFragment : Fragment() {

    private var visible: Boolean = false
    private var fullscreenContent: View? = null
    private var _binding: FragmentScreenLightBinding? = null

    private val binding get() = _binding!!
    private val viewModel by activityViewModels<MainActivityViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentScreenLightBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        BrightControl.setContext(requireActivity().applicationContext)
        BrightControl.setBrightness(requireActivity(), 255)

        fullscreenContent = binding.fullscreenContent
        fullscreenContent?.setOnClickListener { toggle() }

        hide()

    }

    override fun onResume() {
        super.onResume()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    override fun onPause() {
        super.onPause()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        show()
    }

    override fun onDestroy() {
        super.onDestroy()
        fullscreenContent = null
    }

    private fun toggle() {
        if (visible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        visible = false
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }

    @Suppress("InlinedApi")
    private fun show() {
        visible = true
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        BrightControl.setBrightness(requireActivity(), WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE) // Reset brightness to system default
    }

}