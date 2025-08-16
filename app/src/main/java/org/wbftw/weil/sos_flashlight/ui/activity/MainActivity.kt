package org.wbftw.weil.sos_flashlight.ui.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import org.wbftw.weil.sos_flashlight.R
import org.wbftw.weil.sos_flashlight.SOSFlashlightApp
import org.wbftw.weil.sos_flashlight.databinding.ActivityMainBinding
import org.wbftw.weil.sos_flashlight.services.SOSFlashlightService
import org.wbftw.weil.sos_flashlight.ui.fragment.MainFragment
import org.wbftw.weil.sos_flashlight.ui.viewmodel.MainActivityViewModel
import org.wbftw.weil.sos_flashlight.utils.BrightControl
import org.wbftw.weil.sos_flashlight.utils.Misc
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : BaseActivity() {

    val TAG = "MainActivity"

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var app: SOSFlashlightApp? = null
    private var localBroadcastManager: LocalBroadcastManager? = null
    private var isSendingMessageRunning: AtomicBoolean = AtomicBoolean(false)
    private var rootLayout: ViewGroup? = null
    private val activityViewModel by viewModels<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app = application as SOSFlashlightApp
        BrightControl.setContext(applicationContext)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val controller: WindowInsetsControllerCompat = WindowCompat.getInsetsController(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.navigationBars())
        controller.hide(WindowInsetsCompat.Type.statusBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        rootLayout = binding.root
        navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            activityViewModel.currentDestination.value = destination.id
            Log.d(TAG, "Current destination changed to: ${destination.label} (${destination.id})")
        }
        checkFlashlight()

        requestNotificationPermission()

        init()
    }

    private fun init(){
        Misc.Companion.initSettings(application as SOSFlashlightApp)
    }

    override fun onResume() {
        super.onResume()
        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        val sosIntentFilter = IntentFilter().apply {
            addAction(SOSFlashlightService.Companion.ACTION_SOS_SIGNAL)
            addAction(SOSFlashlightService.Companion.ACTION_SOS_FINISHED)
        }
        localBroadcastManager?.registerReceiver(sosSignalReceiver, sosIntentFilter)
    }

    override fun onPause() {
        super.onPause()
        localBroadcastManager?.unregisterReceiver(sosSignalReceiver)
    }

    private val sosSignalReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                SOSFlashlightService.Companion.ACTION_SOS_SIGNAL -> {
                    val isLightOn = intent.getBooleanExtra(SOSFlashlightService.Companion.EXTRA_LIGHT_STATE, false)
                    val signalChar = intent.getCharExtra(SOSFlashlightService.Companion.EXTRA_MESSAGE, ' ')
                    updateScreenColor(isLightOn)
                    updateSignalChar(signalChar)
                    isSendingMessageRunning.set(true)
                }
                SOSFlashlightService.Companion.ACTION_SOS_FINISHED -> {
                    resetScreen()
                    isSendingMessageRunning.set(false)
                }
            }
        }
    }


    private fun updateScreenColor(isLightOn: Boolean) {
        if (app?.defaultScreenFlicker != true){
            Log.v(TAG, "Screen flicker is disabled, not updating screen color.")
            return
        }
        if (isLightOn) {
            rootLayout?.setBackgroundColor(Color.RED)
            //set action bar color
            supportActionBar?.setBackgroundDrawable(ContextCompat.getDrawable(this, R.color.red))
        } else {
            rootLayout?.setBackgroundColor(Color.BLACK)
            supportActionBar?.setBackgroundDrawable(ContextCompat.getDrawable(this, R.color.black))
        }
        BrightControl.setBrightness(this, 255) // Set brightness to maximum when SOS is active
    }

    private fun updateSignalChar(signalChar: Char) {
        Log.d(TAG, "Updating signal character: $signalChar")
        val fragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)?.childFragmentManager?.fragments?.firstOrNull()
        if (fragment is MainFragment) {
            fragment.updateSignalChar(signalChar)
        }
    }

    private fun resetScreen() {
        Log.d(TAG, "Resetting screen color to default")
        val isDarkMode = (getResources().configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        if (isDarkMode) {
            rootLayout?.setBackgroundColor(ContextCompat.getColor(this, android.R.color.background_dark))
            supportActionBar?.setBackgroundDrawable(ContextCompat.getDrawable(this, R.color.black))
        } else {
            rootLayout?.setBackgroundColor(ContextCompat.getColor(this, android.R.color.background_light))
            supportActionBar?.setBackgroundDrawable(ContextCompat.getDrawable(this, R.color.white))
        }
        BrightControl.setBrightness(this, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE) // Reset brightness to system default
    }


    fun toggleSendingMessage() {
        if (!isSendingMessageRunning.get()) {
            startSendMessageService()
        } else {
            stopSendMessageService()
        }
    }

    private fun startSendMessageService() {
        requestNotificationPermission()

        val intent = Intent(this, SOSFlashlightService::class.java).apply {
            action = SOSFlashlightService.Companion.ACTION_START_SOS
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        isSendingMessageRunning.set(true)
    }

    fun sendConfigReload() {
        Log.d(TAG, "Sending config reload")
        val intent = Intent(this, SOSFlashlightService::class.java).apply{
            action = SOSFlashlightService.Companion.ACTION_REFRESH_CONFIG
        }
        startService(intent)
    }

    private fun stopSendMessageService() {
        val intent = Intent(this, SOSFlashlightService::class.java).apply {
            action = SOSFlashlightService.Companion.ACTION_STOP_SOS
        }
        startService(intent)
        isSendingMessageRunning.set(false)
    }


    private fun checkFlashlight() {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            toastLong(getString(R.string.code_feature_no_flashlight_message))
            return
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /**
     * Navigates to a fragment with the specified target ID.
     * @param target The ID of the target fragment to navigate to.
     * @param label An optional label for the target fragment, used for logging and user feedback.
     */
    fun navigateToFragment(target: Int, label: String? = null) {
        Log.d(TAG, "Navigating to fragment with ID: $target, label: $label")
        if (activityViewModel.currentDestination.value == target) {
            Log.d(TAG, "Already in target fragment, no need to navigate")
            toastShort("Already in $label fragment")
        } else {
            Log.d(TAG, "Navigating to target fragment")
            navController.navigate(target)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sos -> {
                navigateToFragment(R.id.FirstFragment, "SOS")
                true
            }
            R.id.action_screen_light -> {
                navigateToFragment(R.id.ScreenLightFragment, "Screen Light")
                true
            }
            R.id.action_settings -> {
                navigateToFragment(R.id.SettingsFragment, "Settings")
                true
            }
            R.id.action_message_encoder -> {
                navigateToFragment(R.id.CodeReaderFragment, "Message Encoder")
                true
            }
            R.id.action_copyright -> {
                navigateToFragment(R.id.CopyrightFragment, "About")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}