package com.outdu.camconnect.network

import io.ktor.client.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for HttpClientProvider
 * Tests singleton pattern and client configuration
 */
class HttpClientProviderTest {

    @Test
    fun `test singleton pattern returns same instance`() {
        val client1 = HttpClientProvider.client
        val client2 = HttpClientProvider.client
        
        assertSame("Should return same singleton instance", client1, client2)
    }

    @Test
    fun `test client is not null`() {
        val client = HttpClientProvider.client
        
        assertNotNull("Client should not be null", client)
    }

    @Test
    fun `test client is HttpClient instance`() {
        val client = HttpClientProvider.client
        
        assertTrue("Client should be HttpClient instance", client is HttpClient)
    }

    @Test
    fun `test client engine is not null`() {
        val client = HttpClientProvider.client
        val engine = client.engine
        
        assertNotNull("Engine should not be null", engine)
    }

    @Test
    fun `test client configuration is consistent across calls`() {
        val client1 = HttpClientProvider.client
        val client2 = HttpClientProvider.client
        
        // Both should have the same configuration
        assertEquals("Engine should be same", client1.engine, client2.engine)
    }

    @Test
    fun `test client can be accessed from multiple threads`() {
        val results = mutableListOf<HttpClient>()
        val threads = List(5) { index ->
            Thread {
                val client = HttpClientProvider.client
                synchronized(results) {
                    results.add(client)
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        // All threads should get the same instance
        assertEquals("Should have 5 results", 5, results.size)
        val firstClient = results[0]
        results.forEach { client ->
            assertSame("All threads should get same instance", firstClient, client)
        }
    }

    @Test
    fun `test lazy initialization only happens once`() {
        // First access
        val client1 = HttpClientProvider.client
        assertNotNull("First access should initialize client", client1)
        
        // Subsequent accesses should return same instance
        val client2 = HttpClientProvider.client
        val client3 = HttpClientProvider.client
        
        assertSame("Second access should return same instance", client1, client2)
        assertSame("Third access should return same instance", client1, client3)
    }

    @Test
    fun `test client has engine configured`() {
        val client = HttpClientProvider.client
        val engine = client.engine
        
        assertNotNull("Engine should not be null", engine)
        assertNotNull("Engine config should exist", engine.config)
    }

    @Test
    fun `test multiple accesses return same client`() {
        val clients = (1..10).map { HttpClientProvider.client }
        
        // All should be the same instance
        val firstClient = clients.first()
        clients.forEach { client ->
            assertSame("All accesses should return same instance", firstClient, client)
        }
    }

    @Test
    fun `test client is properly initialized`() {
        val client = HttpClientProvider.client
        
        // Verify basic properties are accessible
        assertNotNull("Client should have engine", client.engine)
        assertNotNull("Client should have engine config", client.engine.config)
    }

    @Test
    fun `test singleton is thread-safe`() {
        val clients = mutableSetOf<HttpClient>()
        val threads = (1..10).map {
            Thread {
                val client = HttpClientProvider.client
                synchronized(clients) {
                    clients.add(client)
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        // Should only have one unique instance
        assertEquals("Should only have one unique client instance", 1, clients.size)
    }

    @Test
    fun `test client does not throw on initialization`() {
        try {
            val client = HttpClientProvider.client
            assertNotNull("Client should be initialized", client)
        } catch (e: Exception) {
            fail("Client initialization should not throw: ${e.message}")
        }
    }
}
