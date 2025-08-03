package org.wbftw.weil.sos_flashlight

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import org.wbftw.weil.sos_flashlight.config.PreferenceValueConst
import org.wbftw.weil.sos_flashlight.databinding.FragmentSettingsBinding

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
                    if (interval >= 10 && interval <= 10000) {
                        viewModel.interval.value = interval
                    }
                } catch (e: NumberFormatException) {
                    Log.e(TAG, "Invalid interval input: $input", e)
                    viewModel.interval.value = PreferenceValueConst.SETTING_DEFAULT_INTERVAL_SHORT_MS_VALUE
                }
                saveSettings(2)
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
    @SuppressLint("SetTextI18n")
    private fun setButtonEvent() {
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
            binding.checkBoxFlashlight.isChecked = PreferenceValueConst.SETTING_DEFAULT_FLASHLIGHT_ON_VALUE
            binding.checkBoxScreen.isChecked = PreferenceValueConst.SETTING_DEFAULT_SCREEN_FLICKER_VALUE
            binding.checkBoxSound.isChecked = PreferenceValueConst.SETTING_DEFAULT_SOUND_ON_VALUE
            binding.checkBoxVibrate.isChecked = PreferenceValueConst.SETTING_DEFAULT_VIBRATE_ON_VALUE
        }
        binding.buttonSave.setOnClickListener {
            saveSettings()
            Log.d(TAG, "Settings saved successfully.")
        }
    }

    private fun generateCode(text: String){
        val morseCode = MorseCodeUtils.encodeWordToMorseCode(text)
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
        appContext.defaultFlashlightOn = viewModel.flashlightOn.value ?: PreferenceValueConst.SETTING_DEFAULT_FLASHLIGHT_ON_VALUE
        appContext.defaultScreenFlicker = viewModel.screenFlicker.value ?: PreferenceValueConst.SETTING_DEFAULT_SCREEN_FLICKER_VALUE
        appContext.defaultSoundOn = viewModel.soundOn.value ?: PreferenceValueConst.SETTING_DEFAULT_SOUND_ON_VALUE
        appContext.defaultVibrateOn = viewModel.vibrateOn.value ?: PreferenceValueConst.SETTING_DEFAULT_VIBRATE_ON_VALUE
        Misc.saveSettings(appContext)
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