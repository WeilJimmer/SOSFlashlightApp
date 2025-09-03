package org.wbftw.weil.sos_flashlight

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.wbftw.weil.sos_flashlight.utils.Misc

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class MiscTest {

    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("org.wbftw.weil.sos_flashlight.dev", appContext.packageName)
    }

    @Test
    fun testColorConvertion() {
        val red = "#FFFF0000"
        val redInt = Misc.colorHex2ColorInt(red)
        assertEquals(-65536, redInt)
        val redHex = Misc.colorInt2ColorHex(redInt)
        assertEquals(red, redHex)
        System.out.println("Red: $red -> $redInt -> $redHex")
    }

}