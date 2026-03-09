package com.outdu.camconnect.ui.models

import org.junit.Assert.*
import org.junit.Test

class CountryCodeTest {

    @Test
    fun `test CountryCode displayName format`() {
        val country = CountryCode("US", "United States")
        assertEquals("US - United States", country.displayName)
    }

    @Test
    fun `test VALID_CODES contains expected countries`() {
        assertTrue("Should contain US", CountryCode.VALID_CODES.any { it.code == "US" })
        assertTrue("Should contain IN", CountryCode.VALID_CODES.any { it.code == "IN" })
        assertTrue("Should contain GB", CountryCode.VALID_CODES.any { it.code == "GB" })
        assertTrue("Should contain DE", CountryCode.VALID_CODES.any { it.code == "DE" })
        assertTrue("Should contain FR", CountryCode.VALID_CODES.any { it.code == "FR" })
        assertTrue("Should contain JP", CountryCode.VALID_CODES.any { it.code == "JP" })
        assertTrue("Should contain AU", CountryCode.VALID_CODES.any { it.code == "AU" })
        assertTrue("Should contain CA", CountryCode.VALID_CODES.any { it.code == "CA" })
    }

    @Test
    fun `test VALID_CODES has correct count`() {
        assertEquals("Should have 250 country codes", 250, CountryCode.VALID_CODES.size)
    }

    @Test
    fun `test fromCode returns correct country`() {
        val country = CountryCode.fromCode("US")
        assertNotNull("Should find US", country)
        assertEquals("US", country?.code)
        assertEquals("United States", country?.name)
    }

    @Test
    fun `test fromCode is case insensitive`() {
        val upperCase = CountryCode.fromCode("US")
        val lowerCase = CountryCode.fromCode("us")
        val mixedCase = CountryCode.fromCode("Us")
        
        assertNotNull("Should find uppercase", upperCase)
        assertNotNull("Should find lowercase", lowerCase)
        assertNotNull("Should find mixed case", mixedCase)
        
        assertEquals("All should return same code", upperCase?.code, lowerCase?.code)
        assertEquals("All should return same code", upperCase?.code, mixedCase?.code)
    }

    @Test
    fun `test fromCode returns null for invalid code`() {
        val country = CountryCode.fromCode("XX")
        assertNull("Should return null for invalid code", country)
    }

    @Test
    fun `test isValid returns true for valid codes`() {
        assertTrue("US should be valid", CountryCode.isValid("US"))
        assertTrue("IN should be valid", CountryCode.isValid("IN"))
        assertTrue("GB should be valid", CountryCode.isValid("GB"))
        assertTrue("us should be valid (case insensitive)", CountryCode.isValid("us"))
    }

    @Test
    fun `test isValid returns false for invalid codes`() {
        assertFalse("XX should be invalid", CountryCode.isValid("XX"))
        assertFalse("ZZZ should be invalid", CountryCode.isValid("ZZZ"))
        assertFalse("Empty should be invalid", CountryCode.isValid(""))
        assertFalse("Single char should be invalid", CountryCode.isValid("U"))
    }

    @Test
    fun `test all country codes are exactly 2 characters`() {
        CountryCode.VALID_CODES.forEach { country ->
            assertEquals("Country code ${country.code} should be 2 characters", 2, country.code.length)
        }
    }

    @Test
    fun `test all country codes are uppercase`() {
        CountryCode.VALID_CODES.forEach { country ->
            assertEquals("Country code ${country.code} should be uppercase", 
                country.code.uppercase(), country.code)
        }
    }

    @Test
    fun `test no duplicate country codes`() {
        val codes = CountryCode.VALID_CODES.map { it.code }
        val uniqueCodes = codes.toSet()
        assertEquals("Should have no duplicate codes", codes.size, uniqueCodes.size)
    }

    @Test
    fun `test all country names are non-empty`() {
        CountryCode.VALID_CODES.forEach { country ->
            assertTrue("Country name should not be empty for ${country.code}", 
                country.name.isNotEmpty())
        }
    }

    @Test
    fun `test specific country mappings`() {
        val testCases = mapOf(
            "AF" to "Afghanistan",
            "US" to "United States",
            "IN" to "India",
            "GB" to "United Kingdom",
            "DE" to "Germany",
            "FR" to "France",
            "JP" to "Japan",
            "AU" to "Australia",
            "CA" to "Canada",
            "ZW" to "Zimbabwe"
        )
        
        testCases.forEach { (code, expectedName) ->
            val country = CountryCode.fromCode(code)
            assertNotNull("Should find country $code", country)
            assertEquals("Name should match for $code", expectedName, country?.name)
        }
    }
}
