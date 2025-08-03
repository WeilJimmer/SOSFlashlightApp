package org.wbftw.weil.sos_flashlight.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.wbftw.weil.sos_flashlight.utils.MorseCodeUtils
import org.wbftw.weil.sos_flashlight.R
import org.wbftw.weil.sos_flashlight.SOSFlashlightApp
import org.wbftw.weil.sos_flashlight.objs.TonePlayer
import org.wbftw.weil.sos_flashlight.config.PreferenceValueConst
import org.wbftw.weil.sos_flashlight.ui.activity.MainActivity
import java.util.concurrent.atomic.AtomicBoolean

class SOSFlashlightService : Service() {

    private var cameraManager: CameraManager? = null
    private var cameraId: String? = null
    private var vibratorManager: Vibrator? = null
    private var tonePlayer: TonePlayer? = null

    private var isRunning = AtomicBoolean(false)
    private var sosThread: Thread? = null

    private lateinit var localBroadcastManager: LocalBroadcastManager

    private var sosFlashlightApp: SOSFlashlightApp? = null

    private var SHORT_MS = PreferenceValueConst.Companion.SETTING_DEFAULT_INTERVAL_SHORT_MS_VALUE
    private var LONG_MS = SHORT_MS * 3
    private var INTERVAL_MS = SHORT_MS * 3
    private var WORD_INTERVAL_MS = SHORT_MS * 7

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            Toast.makeText(this, "No flashlight detected!", Toast.LENGTH_SHORT).show()
        }

        refreshConfig()
        initVibrator()
        initCamera()
        initSound()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SOS -> startSendingMessage()
            ACTION_STOP_SOS -> stopSendingMessage()
            ACTION_REFRESH_CONFIG -> refreshConfig()
            else -> {
                Log.w(TAG, "Received unknown action: ${intent?.action}")
            }
        }

        return START_STICKY
    }

    private fun refreshConfig() {
        // 重新讀取配置
        sosFlashlightApp = application as SOSFlashlightApp
        SHORT_MS = sosFlashlightApp?.defaultIntervalShortMs ?: PreferenceValueConst.Companion.SETTING_DEFAULT_INTERVAL_SHORT_MS_VALUE
        LONG_MS = SHORT_MS * 3
        INTERVAL_MS = SHORT_MS * 3
        WORD_INTERVAL_MS = SHORT_MS * 7
        Log.d(TAG, "Configuration refreshed: SHORT_MS=$SHORT_MS, LONG_MS=$LONG_MS, INTERVAL_MS=$INTERVAL_MS, WORD_INTERVAL_MS=$WORD_INTERVAL_MS")
    }

    private fun startSendingMessage() {
        if (isRunning.get()) {
            return
        }

        createNotificationChannel()

        val stopIntent = Intent(this, SOSFlashlightService::class.java).apply {
            action = ACTION_STOP_SOS
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val openAppIntent = Intent(this, MainActivity::class.java)
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.code_notification_sos_signal_title))
            .setContentText(getString(R.string.code_notification_sos_signal_description))
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setColor(Color.RED)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.code_stop), stopPendingIntent)
            .setContentIntent(openAppPendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        }

        isRunning.set(true)
        startSOSPattern()
    }

    private fun stopSendingMessage() {
        Log.d(TAG, "Stopping SOS service")
        isRunning.set(false)
        sosThread?.interrupt()
        sosThread = null
        sendFinishedBroadcast()
        releaseCamera()
        stopForeground(true)
        stopSelf()
    }

    // 發送信號廣播
    private fun sendSignalBroadcast(isLightOn: Boolean, char: Char) {
        val intent = Intent(ACTION_SOS_SIGNAL)
        intent.putExtra(EXTRA_LIGHT_STATE, isLightOn)
        intent.putExtra(EXTRA_MESSAGE, char)
        localBroadcastManager.sendBroadcast(intent)
    }

    private fun sendFinishedBroadcast() {
        val intent = Intent(ACTION_SOS_FINISHED)
        localBroadcastManager.sendBroadcast(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SOS閃光燈服務",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "用於SOS緊急信號的通知渠道"
                enableLights(true)
                lightColor = Color.RED
            }

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun initVibrator() {
        vibratorManager = getSystemService(VIBRATOR_SERVICE) as Vibrator
        vibratorManager?.let {
            if (it.hasVibrator()){
                Log.d(TAG, "Vibrator initialized successfully.")
            } else {
                Log.w(TAG, "This device does not have a vibrator.")
                vibratorManager = null
            }
        }
    }

    private fun initSound() {
        tonePlayer = TonePlayer(this, TonePlayer.Companion.DEFAULT_SOUND_TYPE_750)
    }

    private fun initCamera() {

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            Toast.makeText(this, "No flashlight detected!", Toast.LENGTH_SHORT).show()
            return
        }

        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        try {
            cameraId = cameraManager?.cameraIdList[0] // 通常第一個相機是後置相機
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun turnOnFlash() {
        if (sosFlashlightApp?.defaultFlashlightOn != true) {
            Log.v(TAG, "Flashlight is disabled in settings, not turning on.")
            return
        }
        try {
            cameraId?.let {
                cameraManager?.setTorchMode(it, true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun turnOffFlash() {
        if (sosFlashlightApp?.defaultFlashlightOn != true) {
            Log.v(TAG, "Flashlight is disabled in settings, not turning off.")
            return
        }
        try {
            cameraId?.let {
                cameraManager?.setTorchMode(it, false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playSound(t: Long) {
        if (sosFlashlightApp?.defaultSoundOn != true) {
            Log.v(TAG, "Sound is disabled in settings, not playing sound.")
            return
        }
        try{
            tonePlayer?.playTone(t)
        }catch (e: Exception) {
            Log.e(TAG, "Error playing sound: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun stopSound() {
        try {
            tonePlayer?.stopTone()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping sound: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun vibrate(t: Long) {
        if (sosFlashlightApp?.defaultVibrateOn != true) {
            Log.v(TAG, "Vibration is disabled in settings, not vibrating.")
            return
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibratorManager?.vibrate(VibrationEffect.createOneShot(t, VibrationEffect.EFFECT_HEAVY_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibratorManager?.vibrate(t)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error vibrating: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun stopVibrate() {
        try {
            vibratorManager?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping vibration: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun signalOn(t: Long) {
        Log.d(TAG, "Sending signal on for $t ms")
        var char = '.'
        if (t >= LONG_MS) {
            char = '-'
        }
        sendSignalBroadcast(true, char)
        turnOnFlash()
        playSound(t)
        vibrate(t)
        Thread.sleep(t)
    }

    private fun signalOff(t: Long) {
        Log.d(TAG, "Sending signal off for $t ms")
        var char = ' '
        if (t >= INTERVAL_MS) {
            char = '/'
        }
        sendSignalBroadcast(false, char)
        turnOffFlash()
        stopSound()
        stopVibrate()
        Thread.sleep(t)
    }

    private fun dot(){
        Log.d(TAG, "Sending dot signal")
        signalOn(SHORT_MS)
        signalOff(SHORT_MS)
    }

    private fun dash() {
        Log.d(TAG, "Sending dash signal")
        signalOn(LONG_MS)
        signalOff(SHORT_MS)
    }

    private fun space() {
        Log.d(TAG, "Sending space signal")
        signalOff(INTERVAL_MS)
    }

    private fun wordSpace() {
        Log.d(TAG, "Sending word space signal")
        signalOff(WORD_INTERVAL_MS)
    }

    private fun isStopFlagSet(): Boolean {
        return !isRunning.get()
    }

    private fun startSOSPattern() {
        sosThread = Thread {
            try {
                while (isRunning.get()) {
                    MorseCodeUtils.Companion.runMoseCode(
                        moseCode= MorseCodeUtils.Companion.encodeWordToMorseCode(sosFlashlightApp?.defaultMessage ?: PreferenceValueConst.Companion.SETTING_DEFAULT_MESSAGE_VALUE ),
                        dot = { dot() },
                        dash = { dash() },
                        space = { space() },
                        wordSpace = { wordSpace() },
                        stopFlag = { return@runMoseCode isStopFlagSet() }
                    )
                }
            } catch (e: InterruptedException) {

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                signalOff(1)
                sendFinishedBroadcast()
            }
        }
        sosThread?.start()
    }

    private fun releaseSound() {
        try {
            tonePlayer?.release()
            tonePlayer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing sound player: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun releaseCamera() {
        turnOffFlash()
        cameraManager = null
    }

    override fun onDestroy() {
        signalOff(1)
        isRunning.set(false)
        sosThread?.interrupt()
        releaseSound()
        releaseCamera()
        sendFinishedBroadcast()
        super.onDestroy()
    }

    companion object {
        const val TAG = "SOSFlashlightService"
        const val ACTION_START_SOS = "org.wbftw.weil.sos_flashlight.START_SOS"
        const val ACTION_STOP_SOS = "org.wbftw.weil.sos_flashlight.STOP_SOS"
        const val ACTION_REFRESH_CONFIG = "org.wbftw.weil.sos_flashlight.REFRESH_CONFIG"
        const val ACTION_SOS_SIGNAL = "org.wbftw.weil.sos_flashlight.SOS_SIGNAL"
        const val ACTION_SOS_FINISHED = "org.wbftw.weil.sos_flashlight.SOS_FINISHED"
        const val EXTRA_MESSAGE = "org.wbftw.weil.sos_flashlight.MESSAGE"
        const val EXTRA_LIGHT_STATE = "org.wbftw.weil.sos_flashlight.LIGHT_STATE"
        const val CHANNEL_ID = "SOSFlashlightChannel"
        const val NOTIFICATION_ID = 1001
    }
}