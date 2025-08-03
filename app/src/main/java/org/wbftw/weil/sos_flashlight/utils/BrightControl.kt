package org.wbftw.weil.sos_flashlight.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.provider.Settings


@SuppressLint("StaticFieldLeak")
object BrightControl {

    private var mContext: Context? = null

    fun setContext(context: Context) {
        mContext = context.applicationContext
    }

    fun getInstance(): BrightControl? {
        return this
    }

    /**
     * Sets the screen brightness for the given activity.
     * @param activity The activity for which to set the brightness.
     * @param brightness The brightness level, from 0 to 255.
     */
    fun setBrightness(activity: Activity, brightness: Int) {
        val lp = activity.window.attributes
        lp.screenBrightness = brightness * (1f / 255f)
        activity.window.attributes = lp
    }

    /**
     * Sets the screen brightness for the given activity.
     * @param activity The activity for which to set the brightness.
     * @param brightness The brightness level, from 0.0 to 1.0.
     */
    fun setBrightness(activity: Activity, brightness: Float) {
        val lp = activity.window.attributes
        lp.screenBrightness = brightness
        activity.window.attributes = lp
    }

    fun stopAutoBrightness() {
        mContext?.let {
            val resolver = it.contentResolver
            Settings.System.putInt(
                resolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            val uri = Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS)
            resolver.notifyChange(uri, null)
        }
    }

    fun getScreenBrightness(): Int {
        var nowBrightnessValue = 0
        mContext?.let {
            try {
                val resolver = it.contentResolver
                nowBrightnessValue = Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return nowBrightnessValue
    }

    fun setBrightnessMode(mode: Int) {
        mContext?.let{
            Settings.System.putInt(
                it.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                mode
            )
        }
    }

    fun getBrightnessMode(): Int {
        try {
            mContext?.let{
                return Settings.System.getInt(
                    it.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE
                )
            }
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
        }
        return Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
    }

}