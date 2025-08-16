package org.wbftw.weil.sos_flashlight.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Context.CAMERA_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.widget.RemoteViews
import android.widget.Toast
import org.wbftw.weil.sos_flashlight.R

class FlashlightWidgetProvider : AppWidgetProvider() {

    companion object {
        private var isFlashlightOn = false
        private const val ACTION_TOGGLE_FLASHLIGHT = "org.wbftw.weil.sos_flashlight.TOGGLE_FLASHLIGHT"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_TOGGLE_FLASHLIGHT) {
            toggleFlashlight(context)

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, FlashlightWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.flashlight_widget)

        val iconResource = if (isFlashlightOn) {
            R.drawable.light_on
        } else {
            R.drawable.light_off
        }
        views.setImageViewResource(R.id.flashlight_button, iconResource)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, FlashlightWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE_FLASHLIGHT
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.flashlight_button, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun toggleFlashlight(context: Context) {
        val newValue = !isFlashlightOn
        light(context, newValue)
        isFlashlightOn = newValue
    }

    private fun light(context: Context, isOn: Boolean) {

        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            Toast.makeText(context, "No flashlight detected!", Toast.LENGTH_SHORT).show()
            return
        }

        val cameraManager = context.getSystemService(CAMERA_SERVICE) as CameraManager
        var cameraId : String? = null
        try {
            cameraId = cameraManager.cameraIdList.firstOrNull {
                cameraManager.getCameraCharacteristics(it).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            cameraId?.let {
                cameraManager.setTorchMode(it, isOn)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}