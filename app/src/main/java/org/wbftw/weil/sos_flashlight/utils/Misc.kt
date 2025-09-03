package org.wbftw.weil.sos_flashlight.utils

import android.content.Context
import android.graphics.Color
import androidx.core.content.edit
import org.wbftw.weil.sos_flashlight.SOSFlashlightApp
import org.wbftw.weil.sos_flashlight.config.ConfigConst
import org.wbftw.weil.sos_flashlight.config.PreferenceKeyConst
import androidx.core.graphics.toColorInt

/**
 * Utility class for managing settings in the SOS Flashlight application.
 */
class Misc {

    companion object {

        fun setSettingConfig(context: Context, key: String, value: Any) {
            val sharedPreferences = context.getSharedPreferences(ConfigConst.PREFERENCE_KEY_SETTING, Context.MODE_PRIVATE)
            sharedPreferences.edit {
                when (value) {
                    is Boolean -> putBoolean(key, value)
                    is Int -> putInt(key, value)
                    is Float -> putFloat(key, value)
                    is Long -> putLong(key, value)
                    is String -> putString(key, value)
                }
            }
        }

        fun getSettingConfig(context: Context, key: String, defaultValue: Any?): Any? {
            val sharedPreferences = context.getSharedPreferences(ConfigConst.PREFERENCE_KEY_SETTING, Context.MODE_PRIVATE)
            if (defaultValue != null){
                when (defaultValue) {
                    is Boolean -> return sharedPreferences.getBoolean(key, defaultValue)
                    is Int -> return sharedPreferences.getInt(key, defaultValue)
                    is Float -> return sharedPreferences.getFloat(key, defaultValue)
                    is Long -> return sharedPreferences.getLong(key, defaultValue)
                    is String -> return sharedPreferences.getString(key, defaultValue)
                }
            }
            when (sharedPreferences.all[key]) {
                is Boolean -> return sharedPreferences.getBoolean(key, false)
                is Int -> return sharedPreferences.getInt(key, 0)
                is Float -> return sharedPreferences.getFloat(key, 0f)
                is Long -> return sharedPreferences.getLong(key, 0L)
                is String -> return sharedPreferences.getString(key, null)
            }
            return null
        }

        fun initSettings(app: SOSFlashlightApp) {
            app.defaultIntervalShortMs = getSettingConfig(app, PreferenceKeyConst.SETTING_INTERVAL_SHORT_MS_LONG, app.defaultIntervalShortMs) as Long
            app.defaultMessage = getSettingConfig(app, PreferenceKeyConst.SETTING_MESSAGE_STRING, app.defaultMessage) as String
            app.defaultScreenColor = getSettingConfig(app, PreferenceKeyConst.SETTING_SCREEN_COLOR_STRING, app.defaultScreenColor) as String
            app.defaultFlashlightOn = getSettingConfig(app, PreferenceKeyConst.SETTING_FLASHLIGHT_ON_BOOLEAN, app.defaultFlashlightOn) as Boolean
            app.defaultScreenFlicker = getSettingConfig(app, PreferenceKeyConst.SETTING_SCREEN_LIGHT_BOOLEAN, app.defaultScreenFlicker) as Boolean
            app.defaultSoundOn = getSettingConfig(app, PreferenceKeyConst.SETTING_SOUND_ON_BOOLEAN, app.defaultSoundOn) as Boolean
            app.defaultVibrateOn = getSettingConfig(app, PreferenceKeyConst.SETTING_VIBRATE_ON_BOOLEAN, app.defaultVibrateOn) as Boolean
        }

        fun saveSettings(app: SOSFlashlightApp) {
            setSettingConfig(app, PreferenceKeyConst.SETTING_INTERVAL_SHORT_MS_LONG, app.defaultIntervalShortMs)
            setSettingConfig(app, PreferenceKeyConst.SETTING_MESSAGE_STRING, app.defaultMessage)
            setSettingConfig(app, PreferenceKeyConst.SETTING_SCREEN_COLOR_STRING, app.defaultScreenColor)
            setSettingConfig(app, PreferenceKeyConst.SETTING_FLASHLIGHT_ON_BOOLEAN, app.defaultFlashlightOn)
            setSettingConfig(app, PreferenceKeyConst.SETTING_SCREEN_LIGHT_BOOLEAN, app.defaultScreenFlicker)
            setSettingConfig(app, PreferenceKeyConst.SETTING_SOUND_ON_BOOLEAN, app.defaultSoundOn)
            setSettingConfig(app, PreferenceKeyConst.SETTING_VIBRATE_ON_BOOLEAN, app.defaultVibrateOn)
        }

        fun colorHex2ColorInt(colorHex: String): Int {
            return try {
                colorHex.toColorInt()
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                Color.WHITE
            }
        }

        fun colorInt2ColorHex(colorInt: Int): String {
            return String.format("#FF%06X", 0xFFFFFF and colorInt)
        }

        fun isValidColorHex(colorHex: String): Boolean {
            return try {
                colorHex.toColorInt()
                true
            } catch (e: IllegalArgumentException) {
                false
            }
        }

    }

}