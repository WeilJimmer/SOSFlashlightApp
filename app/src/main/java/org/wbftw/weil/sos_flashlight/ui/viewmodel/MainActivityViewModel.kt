package org.wbftw.weil.sos_flashlight.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel : ViewModel() {

    val currentDestination = MutableLiveData<Int>().apply { value = -1 }

}