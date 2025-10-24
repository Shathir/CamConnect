package com.outdu.camconnect

import org.junit.Test
import org.junit.Assert.*

/**
 * Simple working test to verify the testing setup
 */
class SimpleTest {
    
    @Test
    fun `basic math test should pass`() {
        // Given
        val a = 2
        val b = 3
        
        // When
        val result = a + b
        
        // Then
        assertEquals(5, result)
    }
    
    @Test
    fun `string test should pass`() {
        // Given
        val input = "Hello"
        
        // When
        val result = input.uppercase()
        
        // Then
        assertEquals("HELLO", result)
    }
    
    @Test
    fun `list test should pass`() {
        // Given
        val list = listOf(1, 2, 3, 4, 5)
        
        // When
        val sum = list.sum()
        
        // Then
        assertEquals(15, sum)
    }
}
