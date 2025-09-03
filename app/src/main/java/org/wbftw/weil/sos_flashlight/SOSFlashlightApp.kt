package org.wbftw.weil.sos_flashlight

import android.app.Application
import org.wbftw.weil.sos_flashlight.config.PreferenceValueConst

class SOSFlashlightApp : Application() {

    val TAG = "SOSFlashlightApp"

    var defaultIntervalShortMs: Long = PreferenceValueConst.SETTING_DEFAULT_INTERVAL_SHORT_MS_VALUE
    var defaultMessage: String = PreferenceValueConst.SETTING_DEFAULT_MESSAGE_VALUE
    var defaultScreenColor: String = PreferenceValueConst.SETTING_DEFAULT_SCREEN_COLOR_VALUE
    var defaultFlashlightOn: Boolean = PreferenceValueConst.SETTING_DEFAULT_FLASHLIGHT_ON_VALUE
    var defaultScreenFlicker: Boolean = PreferenceValueConst.SETTING_DEFAULT_SCREEN_FLICKER_VALUE
    var defaultSoundOn: Boolean = PreferenceValueConst.SETTING_DEFAULT_SOUND_ON_VALUE
    var defaultVibrateOn: Boolean = PreferenceValueConst.SETTING_DEFAULT_VIBRATE_ON_VALUE

    override fun onCreate() {
        super.onCreate()
    }
}