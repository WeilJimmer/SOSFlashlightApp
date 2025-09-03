package org.wbftw.weil.sos_flashlight.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import org.wbftw.weil.sos_flashlight.R
import org.wbftw.weil.sos_flashlight.ui.activity.MainActivity
import org.wbftw.weil.sos_flashlight.utils.Misc
import org.wbftw.weil.sos_flashlight.utils.MorseCodeUtils
import org.wbftw.weil.sos_flashlight.SOSFlashlightApp
import org.wbftw.weil.sos_flashlight.config.PreferenceValueConst
import org.wbftw.weil.sos_flashlight.databinding.FragmentSettingsBinding
import org.wbftw.weil.sos_flashlight.ui.activity.BaseActivity
import org.wbftw.weil.sos_flashlight.ui.viewmodel.SettingsViewModel

/**
 * [SettingsFragment] is used to manage the settings of the SOS Flashlight application.
 */
class SettingsFragment : Fragment() {

    val TAG = "SettingsFragment"

    private var _binding: FragmentSettingsBinding? = null
    private val viewModel: SettingsViewModel by lazy {
        ViewModelProvider(this).get(SettingsViewModel::class.java)
    }

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val app = requireContext().applicationContext as SOSFlashlightApp
        readConfig(app)
        setUIHandle()
        setUIEvent()
        setButtonEvent()
    }

    /**
     * Reads the configuration settings from the application context and initializes the ViewModel.
     */
    private fun readConfig(app: SOSFlashlightApp) {
        viewModel.textClear.value = app.defaultMessage
        viewModel.interval.value = app.defaultIntervalShortMs
        viewModel.screenColor.value = app.defaultScreenColor
        viewModel.flashlightOn.value = app.defaultFlashlightOn
        viewModel.screenFlicker.value = app.defaultScreenFlicker
        viewModel.soundOn.value = app.defaultSoundOn
        viewModel.vibrateOn.value = app.defaultVibrateOn
    }

    /**
     * Sets up the UI elements with the current values from the ViewModel.
     */
    private fun setUIHandle(){
        binding.editTextClear.setText(viewModel.textClear.value)
        viewModel.textClear.observe(viewLifecycleOwner) { clearText ->
            generateCode(clearText)
        }

        binding.editTextShortInterval.setText(viewModel.interval.value?.toString() ?: PreferenceValueConst.SETTING_DEFAULT_INTERVAL_SHORT_MS_VALUE.toString())

        binding.editTextScreenColor.setText(viewModel.screenColor.value)

        viewModel.textCode.observe(viewLifecycleOwner) { code ->
            binding.editTextCode.text = code
        }

        viewModel.flashlightOn.observe(viewLifecycleOwner) { isOn ->
            binding.checkBoxFlashlight.isChecked = isOn
        }

        viewModel.screenFlicker.observe(viewLifecycleOwner) { isOn ->
            binding.checkBoxScreen.isChecked = isOn
        }

        viewModel.soundOn.observe(viewLifecycleOwner) { isOn ->
            binding.checkBoxSound.isChecked = isOn
        }

        viewModel.vibrateOn.observe(viewLifecycleOwner) { isOn ->
            binding.checkBoxVibrate.isChecked = isOn
        }
    }

    /**
     * Sets up the event listeners for UI elements to handle user input and save settings.
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setUIEvent(){
        binding.editTextClear.addTextChangedListener {
            val newString = it.toString()
            if (newString.isNotEmpty() && newString != viewModel.textClear.value) {
                viewModel.textClear.value = newString
                saveSettings(1)
            }
        }

        binding.editTextShortInterval.addTextChangedListener {
            val input = it.toString()
            if (input.isNotEmpty()) {
                try {
                    val interval = input.toLong()
                    if (interval >= 10 && interval <= 1000) {
                        viewModel.interval.value = interval
                    }
                } catch (e: NumberFormatException) {
                    Log.e(TAG, "Invalid interval input: $input", e)
                    viewModel.interval.value = PreferenceValueConst.SETTING_DEFAULT_INTERVAL_SHORT_MS_VALUE
                }
                saveSettings(2)
            }
        }

        binding.editTextScreenColor.addTextChangedListener {
            val input = it.toString()
            if (input.isNotEmpty() && Misc.isValidColorHex(input)) {
                viewModel.screenColor.value = input
                saveSettings(7)
            }
        }

        binding.editTextShortInterval.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.editTextShortInterval.setText(viewModel.interval.value?.toString() ?: PreferenceValueConst.SETTING_DEFAULT_INTERVAL_SHORT_MS_VALUE.toString())
            }
        }

        binding.checkBoxFlashlight.setOnCheckedChangeListener { _, isChecked ->
            viewModel.flashlightOn.value = isChecked
            saveSettings(3)
        }

        binding.checkBoxScreen.setOnCheckedChangeListener { _, isChecked ->
            viewModel.screenFlicker.value = isChecked
            saveSettings(4)
        }

        binding.checkBoxSound.setOnCheckedChangeListener { _, isChecked ->
            viewModel.soundOn.value = isChecked
            saveSettings(5)
        }

        binding.checkBoxVibrate.setOnCheckedChangeListener { _, isChecked ->
            viewModel.vibrateOn.value = isChecked
            saveSettings(6)
        }
    }

    /**
     * Sets up the click listeners for the buttons to change the interval values and reset settings.
     */
    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    private fun setButtonEvent() {
        binding.editTextScreenColor.setOnTouchListener { v, e ->
            when(e.action){
                android.view.MotionEvent.ACTION_UP -> showColorPickerDialog()
            }
            true
        }
        binding.buttonSlow.setOnClickListener {
            binding.editTextShortInterval.setText("350")
        }
        binding.buttonMedium.setOnClickListener {
            binding.editTextShortInterval.setText("250")
        }
        binding.buttonFast.setOnClickListener {
            binding.editTextShortInterval.setText("150")
        }
        binding.buttonReset.setOnClickListener {
            binding.editTextClear.setText(PreferenceValueConst.SETTING_DEFAULT_MESSAGE_VALUE)
            binding.editTextShortInterval.setText(PreferenceValueConst.SETTING_DEFAULT_INTERVAL_SHORT_MS_VALUE.toString())
            binding.editTextScreenColor.setText(PreferenceValueConst.SETTING_DEFAULT_SCREEN_COLOR_VALUE)
            binding.checkBoxFlashlight.isChecked = PreferenceValueConst.SETTING_DEFAULT_FLASHLIGHT_ON_VALUE
            binding.checkBoxScreen.isChecked = PreferenceValueConst.SETTING_DEFAULT_SCREEN_FLICKER_VALUE
            binding.checkBoxSound.isChecked = PreferenceValueConst.SETTING_DEFAULT_SOUND_ON_VALUE
            binding.checkBoxVibrate.isChecked = PreferenceValueConst.SETTING_DEFAULT_VIBRATE_ON_VALUE
        }
        binding.buttonSave.setOnClickListener {
            saveSettings()
            BaseActivity.toastShort(requireContext(), getString(R.string.code_save_successful))
            Log.d(TAG, "Settings saved successfully.")
        }
    }

    private fun showColorPickerDialog(colorHex: String = "#FFFF0000"){
        val composeView = ComposeView(requireContext()).apply {
            setContent {
                val controller = rememberColorPickerController()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HsvColorPicker(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .padding(16.dp),
                        controller = controller,
                        initialColor = Color(Misc.colorHex2ColorInt(binding.editTextScreenColor.text.toString())),
                        onColorChanged = { colorEnvelope ->
                            val hexColor = "#${colorEnvelope.hexCode}".uppercase()
                            binding.editTextScreenColor.setText(hexColor)
                            Log.d(TAG, "Selected color: $hexColor")
                        }
                    )
                    BrightnessSlider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .height(35.dp),
                        controller = controller,
                    )
                }
            }
        }
        val ab = AlertDialog.Builder(requireContext())
            .setTitle("Select Screen Color")
            .setView(composeView)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
        ab.show()
    }

    private fun generateCode(text: String){
        val morseCode = MorseCodeUtils.Companion.encodeWordToMorseCode(text)
        if (viewModel.textCode.value != morseCode){
            viewModel.textCode.value = morseCode
        }
        Log.d(TAG, "Generated Morse code: $morseCode for text: $text")
    }

    private fun saveSettings(trigger: Int = -1) {
        Log.v(TAG, "Saving settings. Trigger: $trigger")
        val appContext = requireContext().applicationContext as SOSFlashlightApp
        appContext.defaultMessage = viewModel.textClear.value ?: PreferenceValueConst.SETTING_DEFAULT_MESSAGE_VALUE
        appContext.defaultIntervalShortMs = viewModel.interval.value ?: PreferenceValueConst.SETTING_DEFAULT_INTERVAL_SHORT_MS_VALUE
        appContext.defaultScreenColor = viewModel.screenColor.value ?: PreferenceValueConst.SETTING_DEFAULT_SCREEN_COLOR_VALUE
        appContext.defaultFlashlightOn = viewModel.flashlightOn.value ?: PreferenceValueConst.SETTING_DEFAULT_FLASHLIGHT_ON_VALUE
        appContext.defaultScreenFlicker = viewModel.screenFlicker.value ?: PreferenceValueConst.SETTING_DEFAULT_SCREEN_FLICKER_VALUE
        appContext.defaultSoundOn = viewModel.soundOn.value ?: PreferenceValueConst.SETTING_DEFAULT_SOUND_ON_VALUE
        appContext.defaultVibrateOn = viewModel.vibrateOn.value ?: PreferenceValueConst.SETTING_DEFAULT_VIBRATE_ON_VALUE
        Misc.Companion.saveSettings(appContext)
        refreshConfig()
    }

    fun refreshConfig(){
        if (activity is MainActivity) {
            (activity as MainActivity).sendConfigReload()
        } else {
            Log.w(TAG, "Activity is not MainActivity, cannot update settings.")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}