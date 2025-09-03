package org.wbftw.weil.sos_flashlight.ui.activity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.wbftw.weil.sos_flashlight.R
import org.wbftw.weil.sos_flashlight.services.SOSFlashlightService

open class BaseActivity : AppCompatActivity() {

    private val TAG = "BaseActivity"
    private val CAMERA_PERMISSION_REQUEST_CODE = 1001
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1002

    companion object {

        fun toastShort(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        fun toastLong(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }

    }

    fun toastShort(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun toastLong(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    /**
     * Requests camera permission if not already granted.
     * If permission is granted, executes the provided setup function.
     * @param setup The function to execute if permission is granted.
     */
    fun requestCameraPermission(setup: () -> Unit) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
            toastLong(getString(R.string.code_flashlight_permission_message))
        } else {
            Log.v(TAG, "Camera permission already granted")
            setup()
        }
    }

    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_REQUEST_CODE)
            }else{
                Log.v(TAG, "Notification permission already granted")
            }
        }
    }

}