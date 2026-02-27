package dev.gopes.hinducalendar.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generateProfile() {
        baselineProfileRule.collect(
            packageName = "dev.gopes.hinducalendar",
        ) {
            // Cold start
            pressHome()
            startActivityAndWait()

            // Scroll the main screen
            device.waitForIdle()
        }
    }
}
