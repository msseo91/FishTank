package com.marine.fishtank

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule

private const val TAG = "FishTankTest"

@RunWith(AndroidJUnit4::class)
class FishTankTest {
    private val appContext by lazy { InstrumentationRegistry.getInstrumentation().targetContext }

    @get:Rule
    val composeTestRule = createComposeRule()


    @Test
    fun brightnessTest() {
    }
}