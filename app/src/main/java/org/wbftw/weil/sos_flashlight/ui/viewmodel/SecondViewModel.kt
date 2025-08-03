package org.wbftw.weil.sos_flashlight.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.wbftw.weil.sos_flashlight.config.PreferenceValueConst

class SecondViewModel : ViewModel() {
    val textCode = MutableLiveData<String>().apply {
        value = "... --- ... /... --- ... /" // Default value for SOS in Morse code
    }
    val textClear = MutableLiveData<String>().apply {
        value = "SOS SOS " // Default value for SOS in clear text
    }
    val interval = MutableLiveData<Long>().apply {
        value = PreferenceValueConst.SETTING_DEFAULT_INTERVAL_SHORT_MS_VALUE // Default interval for Morse code transmission
    }

    fun addClearText(char: Char) {
        textClear.postValue(
            textClear.value?.plus(char) ?: char.toString()
        )
    }

    fun addMorseCode(char: Char) {
        textCode.postValue(
            textCode.value?.plus(char) ?: char.toString()
        )
    }

}