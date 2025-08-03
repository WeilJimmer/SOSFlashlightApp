package org.wbftw.weil.sos_flashlight.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.wbftw.weil.sos_flashlight.config.PreferenceValueConst

class SettingsViewModel : ViewModel() {
    val textCode = MutableLiveData<String>().apply {
        value = "... --- .../" // Default value for SOS in Morse code
    }
    val textClear = MutableLiveData<String>().apply {
        value = PreferenceValueConst.SETTING_DEFAULT_MESSAGE_VALUE // Default value for SOS in clear text
    }
    val interval = MutableLiveData<Long>().apply {
        value = PreferenceValueConst.SETTING_DEFAULT_INTERVAL_SHORT_MS_VALUE // Default interval for Morse code transmission
    }
    val flashlightOn = MutableLiveData<Boolean>().apply {
        value = PreferenceValueConst.SETTING_DEFAULT_FLASHLIGHT_ON_VALUE // Default flashlight on setting
    }
    val screenFlicker = MutableLiveData<Boolean>().apply {
        value = PreferenceValueConst.SETTING_DEFAULT_SCREEN_FLICKER_VALUE // Default screen flicker setting
    }
    val soundOn = MutableLiveData<Boolean>().apply {
        value = PreferenceValueConst.SETTING_DEFAULT_SOUND_ON_VALUE // Default sound on setting
    }
    val vibrateOn = MutableLiveData<Boolean>().apply {
        value = PreferenceValueConst.SETTING_DEFAULT_VIBRATE_ON_VALUE // Default vibrate on setting
    }

}