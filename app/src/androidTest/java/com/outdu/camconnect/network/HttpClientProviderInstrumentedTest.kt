package com.outdu.camconnect.network

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith

/**
 * Instrumented tests for HttpClientProvider.
 * Verifies lazy client initialization on Android.
 */
@RunWith(AndroidJUnit4::class)
class HttpClientProviderInstrumentedTest {

    @Test
    fun client_isNotNull_afterAccess() {
        val client = HttpClientProvider.client
        assertNotNull(client)
    }

    @Test
    fun client_multipleAccess_returnsSameInstance() {
        val first = HttpClientProvider.client
        val second = HttpClientProvider.client
        assertNotNull(first)
        assertNotNull(second)
        assertTrue(first === second)
    }
}
