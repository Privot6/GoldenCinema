package com.example.goldencinema

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Testy instrumentalne, uruchamiane na urządzeniu z Androidem.
 *
 * Zobacz [dokumentację testowania](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Kontekst testowanej aplikacji
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.goldencinema", appContext.packageName)
    }
}