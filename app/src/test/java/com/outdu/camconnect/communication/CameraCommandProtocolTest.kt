package com.outdu.camconnect.communication

import org.junit.Assert.*
import org.junit.Test

/**
 * Comprehensive test suite for CameraCommandProtocol
 * Tests command builders, CRC validation, response parsing, and enum parameter validation
 */
class CameraCommandProtocolTest {

    // ========== Enum Tests ==========

    @Test
    fun `test Header enum values are correct`() {
        assertEquals(1, CameraCommandProtocol.Header.SET.getVal())
        assertEquals(2, CameraCommandProtocol.Header.GET.getVal())
        assertEquals(3, CameraCommandProtocol.Header.ACK.getVal())
        assertEquals(4, CameraCommandProtocol.Header.RESPONSE.getVal())
    }

    @Test
    fun `test Commands enum values are correct`() {
        assertEquals(1, CameraCommandProtocol.Commands.STREAMING.getVal())
        assertEquals(2, CameraCommandProtocol.Commands.NETWORK.getVal())
        assertEquals(3, CameraCommandProtocol.Commands.CONFIG.getVal())
        assertEquals(4, CameraCommandProtocol.Commands.IMAGE.getVal())
        assertEquals(5, CameraCommandProtocol.Commands.AUDIO.getVal())
        assertEquals(6, CameraCommandProtocol.Commands.SYSTEM.getVal())
    }

    @Test
    fun `test ZOOM enum get by value`() {
        assertEquals(CameraCommandProtocol.ZOOM.X1, CameraCommandProtocol.ZOOM.get(1))
        assertEquals(CameraCommandProtocol.ZOOM.X2, CameraCommandProtocol.ZOOM.get(2))
        assertEquals(CameraCommandProtocol.ZOOM.X3, CameraCommandProtocol.ZOOM.get(3))
        assertEquals(CameraCommandProtocol.ZOOM.X4, CameraCommandProtocol.ZOOM.get(4))
        assertNull(CameraCommandProtocol.ZOOM.get(99))
    }

    @Test
    fun `test ZOOM enum get by display value`() {
        assertEquals(CameraCommandProtocol.ZOOM.X1, CameraCommandProtocol.ZOOM.get("x1"))
        assertEquals(CameraCommandProtocol.ZOOM.X2, CameraCommandProtocol.ZOOM.get("x2"))
        assertEquals(CameraCommandProtocol.ZOOM.X3, CameraCommandProtocol.ZOOM.get("x3"))
        assertEquals(CameraCommandProtocol.ZOOM.X4, CameraCommandProtocol.ZOOM.get("x4"))
        assertNull(CameraCommandProtocol.ZOOM.get("invalid"))
    }

    @Test
    fun `test ZOOM enum getAllValues`() {
        val values = CameraCommandProtocol.ZOOM.getAllValues()
        assertEquals(4, values.size)
        assertTrue(values.contains("x1"))
        assertTrue(values.contains("x2"))
        assertTrue(values.contains("x3"))
        assertTrue(values.contains("x4"))
    }

    @Test
    fun `test ROTATION enum values`() {
        assertEquals(1, CameraCommandProtocol.ROTATION.R0.getVal())
        assertEquals(2, CameraCommandProtocol.ROTATION.R90.getVal())
        assertEquals(3, CameraCommandProtocol.ROTATION.R180.getVal())
        assertEquals(4, CameraCommandProtocol.ROTATION.R270.getVal())
    }

    @Test
    fun `test ROTATION enum get by display value`() {
        assertEquals(CameraCommandProtocol.ROTATION.R0, CameraCommandProtocol.ROTATION.get("0"))
        assertEquals(CameraCommandProtocol.ROTATION.R90, CameraCommandProtocol.ROTATION.get("90"))
        assertEquals(CameraCommandProtocol.ROTATION.R180, CameraCommandProtocol.ROTATION.get("180"))
        assertEquals(CameraCommandProtocol.ROTATION.R270, CameraCommandProtocol.ROTATION.get("270"))
    }

    @Test
    fun `test IRCUTFILTER enum values`() {
        assertEquals(0, CameraCommandProtocol.IRCUTFILTER.OFF.getVal())
        assertEquals(1, CameraCommandProtocol.IRCUTFILTER.ON.getVal())
        assertEquals("Off", CameraCommandProtocol.IRCUTFILTER.OFF.getDisplayVal())
        assertEquals("On", CameraCommandProtocol.IRCUTFILTER.ON.getDisplayVal())
    }

    @Test
    fun `test TILT enum has all values`() {
        val values = CameraCommandProtocol.TILT.getAllValues()
        assertEquals(6, values.size)
        assertEquals("0", CameraCommandProtocol.TILT.T0.getDisplayVal())
        assertEquals("5", CameraCommandProtocol.TILT.T5.getDisplayVal())
    }

    @Test
    fun `test RESOLUTION enum values`() {
        assertEquals(0, CameraCommandProtocol.RESOLUTION.MODE0.getVal())
        assertEquals(1, CameraCommandProtocol.RESOLUTION.MODE1.getVal())
        assertEquals("mode0", CameraCommandProtocol.RESOLUTION.MODE0.getDisplayVal())
        assertEquals("mode1", CameraCommandProtocol.RESOLUTION.MODE1.getDisplayVal())
    }

    // ========== Command Builder Tests ==========

    @Test
    fun `test startStreamCmd builds correct command`() {
        val cmd = CameraCommandProtocol.startStreamCmd()
        
        assertEquals(4, cmd.size)
        assertEquals(CameraCommandProtocol.Header.SET.getVal(), cmd[0])
        assertEquals(CameraCommandProtocol.Commands.STREAMING.getVal(), cmd[1])
        assertEquals(CameraCommandProtocol.StreamingSubCommands.START.getVal(), cmd[2])
        assertEquals(0, cmd[3])
    }

    @Test
    fun `test stopStreamCmd builds correct command`() {
        val cmd = CameraCommandProtocol.stopStreamCmd()
        
        assertEquals(4, cmd.size)
        assertEquals(CameraCommandProtocol.Header.SET.getVal(), cmd[0])
        assertEquals(CameraCommandProtocol.Commands.STREAMING.getVal(), cmd[1])
        assertEquals(CameraCommandProtocol.StreamingSubCommands.STOP.getVal(), cmd[2])
        assertEquals(0, cmd[3])
    }

    @Test
    fun `test getFactoryConfigCmd builds correct command`() {
        val cmd = CameraCommandProtocol.getFactoryConfigCmd()
        
        assertEquals(4, cmd.size)
        assertEquals(CameraCommandProtocol.Header.GET.getVal(), cmd[0])
        assertEquals(CameraCommandProtocol.Commands.CONFIG.getVal(), cmd[1])
        assertEquals(CameraCommandProtocol.ConfigGetSubCommands.Factory.getVal(), cmd[2])
        assertEquals(0, cmd[3])
    }

    @Test
    fun `test getDefaultConfigCmd builds correct command`() {
        val cmd = CameraCommandProtocol.getDefaultConfigCmd()
        
        assertEquals(4, cmd.size)
        assertEquals(CameraCommandProtocol.Header.GET.getVal(), cmd[0])
        assertEquals(CameraCommandProtocol.Commands.CONFIG.getVal(), cmd[1])
        assertEquals(CameraCommandProtocol.ConfigGetSubCommands.Default.getVal(), cmd[2])
        assertEquals(0, cmd[3])
    }

    @Test
    fun `test getCurrentConfigCmd builds correct command`() {
        val cmd = CameraCommandProtocol.getCurrentConfigCmd()
        
        assertEquals(4, cmd.size)
        assertEquals(CameraCommandProtocol.Header.GET.getVal(), cmd[0])
        assertEquals(CameraCommandProtocol.Commands.CONFIG.getVal(), cmd[1])
        assertEquals(CameraCommandProtocol.ConfigGetSubCommands.Current.getVal(), cmd[2])
        assertEquals(0, cmd[3])
    }

    @Test
    fun `test setImgZoomCmd with valid zoom value`() {
        val cmd = CameraCommandProtocol.setImgZoomCmd("x2")
        
        assertEquals(5, cmd.size)
        assertEquals(CameraCommandProtocol.Header.SET.getVal(), cmd[0])
        assertEquals(CameraCommandProtocol.Commands.IMAGE.getVal(), cmd[1])
        assertEquals(CameraCommandProtocol.ImageSubCommands.ZOOM.getVal(), cmd[2])
        assertEquals(2, cmd[3]) // x2 has value 2
        assertEquals(0, cmd[4])
    }

    @Test
    fun `test setImgZoomCmd with all zoom values`() {
        val zoomValues = listOf("x1", "x2", "x3", "x4")
        val expectedValues = listOf(1, 2, 3, 4)
        
        zoomValues.forEachIndexed { index, zoom ->
            val cmd = CameraCommandProtocol.setImgZoomCmd(zoom)
            assertEquals(expectedValues[index], cmd[3])
        }
    }

    @Test
    fun `test setImgZoomCmd with invalid zoom throws exception`() {
        try {
            CameraCommandProtocol.setImgZoomCmd("invalid")
            fail("Should throw exception for invalid zoom")
        } catch (e: Exception) {
            assertTrue(e.message!!.contains("Invalid zoom value"))
        }
    }

    @Test
    fun `test setImgRotationCmd with valid rotation`() {
        val cmd = CameraCommandProtocol.setImgRotationCmd("90")
        
        assertEquals(5, cmd.size)
        assertEquals(CameraCommandProtocol.Header.SET.getVal(), cmd[0])
        assertEquals(CameraCommandProtocol.Commands.IMAGE.getVal(), cmd[1])
        assertEquals(CameraCommandProtocol.ImageSubCommands.ROTATION.getVal(), cmd[2])
        assertEquals(2, cmd[3]) // 90 degrees has value 2
        assertEquals(0, cmd[4])
    }

    @Test
    fun `test setImgRotationCmd with all rotation values`() {
        val rotations = listOf("0", "90", "180", "270")
        val expectedValues = listOf(1, 2, 3, 4)
        
        rotations.forEachIndexed { index, rotation ->
            val cmd = CameraCommandProtocol.setImgRotationCmd(rotation)
            assertEquals(expectedValues[index], cmd[3])
        }
    }

    @Test
    fun `test setImgRotationCmd with invalid rotation throws exception`() {
        try {
            CameraCommandProtocol.setImgRotationCmd("45")
            fail("Should throw exception for invalid rotation")
        } catch (e: Exception) {
            assertTrue(e.message!!.contains("Invalid rotation value"))
        }
    }

    @Test
    fun `test setImgResolutionCmd with valid resolution`() {
        val cmd = CameraCommandProtocol.setImgResolutionCmd("mode0")
        
        assertEquals(5, cmd.size)
        assertEquals(CameraCommandProtocol.Header.SET.getVal(), cmd[0])
        assertEquals(CameraCommandProtocol.Commands.IMAGE.getVal(), cmd[1])
        assertEquals(CameraCommandProtocol.ImageSubCommands.RESOLUTION.getVal(), cmd[2])
        assertEquals(0, cmd[3])
        assertEquals(0, cmd[4])
    }

    @Test
    fun `test setImgTiltCmd with valid tilt`() {
        val cmd = CameraCommandProtocol.setImgTiltCmd("3")
        
        assertEquals(5, cmd.size)
        assertEquals(CameraCommandProtocol.Header.SET.getVal(), cmd[0])
        assertEquals(CameraCommandProtocol.Commands.IMAGE.getVal(), cmd[1])
        assertEquals(CameraCommandProtocol.ImageSubCommands.TILT.getVal(), cmd[2])
        assertEquals(3, cmd[3])
        assertEquals(0, cmd[4])
    }

    @Test
    fun `test setImgIRCutFilterCmd with valid filter`() {
        val cmdOn = CameraCommandProtocol.setImgIRCutFilterCmd("On")
        
        assertEquals(5, cmdOn.size)
        assertEquals(CameraCommandProtocol.Header.SET.getVal(), cmdOn[0])
        assertEquals(CameraCommandProtocol.Commands.IMAGE.getVal(), cmdOn[1])
        assertEquals(CameraCommandProtocol.ImageSubCommands.IRCUTFILTER.getVal(), cmdOn[2])
        assertEquals(1, cmdOn[3])
        assertEquals(0, cmdOn[4])
        
        val cmdOff = CameraCommandProtocol.setImgIRCutFilterCmd("Off")
        assertEquals(0, cmdOff[3])
    }

    @Test
    fun `test setImgIRBrightnessCmd with valid brightness`() {
        val cmd = CameraCommandProtocol.setImgIRBrightnessCmd(128)
        
        assertEquals(5, cmd.size)
        assertEquals(CameraCommandProtocol.Header.SET.getVal(), cmd[0])
        assertEquals(CameraCommandProtocol.Commands.IMAGE.getVal(), cmd[1])
        assertEquals(CameraCommandProtocol.ImageSubCommands.IRBRIGHTNESS.getVal(), cmd[2])
        assertEquals(128, cmd[3])
        assertEquals(0, cmd[4])
    }

    @Test
    fun `test setImgIRBrightnessCmd with boundary values`() {
        // Test minimum
        val cmdMin = CameraCommandProtocol.setImgIRBrightnessCmd(0)
        assertEquals(0, cmdMin[3])
        
        // Test maximum
        val cmdMax = CameraCommandProtocol.setImgIRBrightnessCmd(255)
        assertEquals(255, cmdMax[3])
    }

    @Test
    fun `test setImgIRBrightnessCmd with invalid brightness throws exception`() {
        try {
            CameraCommandProtocol.setImgIRBrightnessCmd(-1)
            fail("Should throw exception for negative brightness")
        } catch (e: Exception) {
            assertTrue(e.message!!.contains("must be between 0 and 255"))
        }
        
        try {
            CameraCommandProtocol.setImgIRBrightnessCmd(256)
            fail("Should throw exception for brightness > 255")
        } catch (e: Exception) {
            assertTrue(e.message!!.contains("must be between 0 and 255"))
        }
    }

    @Test
    fun `test shutdownCmd builds correct command`() {
        val cmd = CameraCommandProtocol.shutdownCmd()
        
        assertEquals(4, cmd.size)
        assertEquals(CameraCommandProtocol.Header.SET.getVal(), cmd[0])
        assertEquals(CameraCommandProtocol.Commands.SYSTEM.getVal(), cmd[1])
        assertEquals(CameraCommandProtocol.SystemSubCommands.SHUTDOWN.getVal(), cmd[2])
        assertEquals(0, cmd[3])
    }

    @Test
    fun `test getWifiStateCmd builds correct command`() {
        val cmd = CameraCommandProtocol.getWifiStateCmd()
        
        assertEquals(4, cmd.size)
        assertEquals(CameraCommandProtocol.Header.GET.getVal(), cmd[0])
        assertEquals(CameraCommandProtocol.Commands.NETWORK.getVal(), cmd[1])
        assertEquals(CameraCommandProtocol.NetworkSubCommands.WifiState.getVal(), cmd[2])
        assertEquals(0, cmd[3])
    }

    // ========== Response Parsing Tests ==========

    @Test
    fun `test parseSetCommandResponse with valid ACK response`() {
        val response = intArrayOf(
            CameraCommandProtocol.Header.ACK.getVal(),
            CameraCommandProtocol.Commands.STREAMING.getVal(),
            CameraCommandProtocol.StreamingSubCommands.START.getVal(),
            1
        )
        
        val result = CameraCommandProtocol.startStreamCmdResponseParse(response, 4)
        assertTrue("Should parse valid ACK response", result)
    }

    @Test
    fun `test parseSetCommandResponse with invalid header`() {
        val response = intArrayOf(
            CameraCommandProtocol.Header.RESPONSE.getVal(), // Wrong header
            CameraCommandProtocol.Commands.STREAMING.getVal(),
            CameraCommandProtocol.StreamingSubCommands.START.getVal(),
            1
        )
        
        val result = CameraCommandProtocol.startStreamCmdResponseParse(response, 4)
        assertFalse("Should reject wrong header", result)
    }

    @Test
    fun `test parseSetCommandResponse with wrong command`() {
        val response = intArrayOf(
            CameraCommandProtocol.Header.ACK.getVal(),
            CameraCommandProtocol.Commands.IMAGE.getVal(), // Wrong command
            CameraCommandProtocol.StreamingSubCommands.START.getVal(),
            1
        )
        
        val result = CameraCommandProtocol.startStreamCmdResponseParse(response, 4)
        assertFalse("Should reject wrong command", result)
    }

    @Test
    fun `test parseSetCommandResponse with wrong subcommand`() {
        val response = intArrayOf(
            CameraCommandProtocol.Header.ACK.getVal(),
            CameraCommandProtocol.Commands.STREAMING.getVal(),
            CameraCommandProtocol.StreamingSubCommands.STOP.getVal(), // Wrong subcommand
            1
        )
        
        val result = CameraCommandProtocol.startStreamCmdResponseParse(response, 4)
        assertFalse("Should reject wrong subcommand", result)
    }

    @Test
    fun `test parseSetCommandResponse with response too short`() {
        val response = intArrayOf(1, 2, 3) // Only 3 bytes
        
        try {
            CameraCommandProtocol.startStreamCmdResponseParse(response, 3)
            fail("Should throw exception for short response")
        } catch (e: Exception) {
            assertTrue(e.message!!.contains("Response too short"))
        }
    }

    @Test
    fun `test parseConfigResponse with valid data`() {
        val response = intArrayOf(
            CameraCommandProtocol.Header.RESPONSE.getVal(),
            CameraCommandProtocol.Commands.CONFIG.getVal(),
            CameraCommandProtocol.ConfigGetSubCommands.Current.getVal(),
            0,
            2, // ZOOM x2
            3, // ROTATION 180
            1, // IRCUTFILTER On
            128 // IRBRIGHTNESS 128
        )
        
        val config = CameraCommandProtocol.getCurrentConfigCmdResponseParse(response, 8)
        
        assertNotNull("Config should not be null", config)
        assertEquals("Should parse zoom", "x2", config["ZOOM"])
        assertEquals("Should parse rotation", "180", config["ROTATION"])
        assertEquals("Should parse IR cut filter", "On", config["IRCUTFILTER"])
        assertEquals("Should parse IR brightness", 128, config["IRBRIGHTNESS"])
        assertEquals("Should include response length", 8, config["response_length"])
        assertTrue("Should include timestamp", config.containsKey("timestamp"))
    }

    @Test
    fun `test parseConfigResponse with minimal data`() {
        val response = intArrayOf(
            CameraCommandProtocol.Header.RESPONSE.getVal(),
            CameraCommandProtocol.Commands.CONFIG.getVal(),
            CameraCommandProtocol.ConfigGetSubCommands.Factory.getVal(),
            0,
            1, // ZOOM x1
            1, // ROTATION 0
            0, // IRCUTFILTER Off
            0  // IRBRIGHTNESS 0
        )
        
        val config = CameraCommandProtocol.getFactoryConfigCmdResponseParse(response, 8)
        
        assertEquals("x1", config["ZOOM"])
        assertEquals("0", config["ROTATION"])
        assertEquals("Off", config["IRCUTFILTER"])
        assertEquals(0, config["IRBRIGHTNESS"])
    }

    @Test
    fun `test parseConfigResponse with invalid enum values`() {
        val response = intArrayOf(
            CameraCommandProtocol.Header.RESPONSE.getVal(),
            CameraCommandProtocol.Commands.CONFIG.getVal(),
            CameraCommandProtocol.ConfigGetSubCommands.Current.getVal(),
            0,
            99, // Invalid ZOOM
            99, // Invalid ROTATION
            99, // Invalid IRCUTFILTER
            128
        )
        
        val config = CameraCommandProtocol.getCurrentConfigCmdResponseParse(response, 8)
        
        // Invalid enum values should not be added to config
        assertFalse("Should not include invalid zoom", config.containsKey("ZOOM"))
        assertFalse("Should not include invalid rotation", config.containsKey("ROTATION"))
        assertFalse("Should not include invalid IR cut filter", config.containsKey("IRCUTFILTER"))
        // But IR brightness should still be there (it's an int, not enum)
        assertEquals(128, config["IRBRIGHTNESS"])
    }

    @Test
    fun `test getWifiStateCmdResponseParse with valid state`() {
        val responseHotspot = intArrayOf(
            CameraCommandProtocol.Header.RESPONSE.getVal(),
            CameraCommandProtocol.Commands.NETWORK.getVal(),
            CameraCommandProtocol.NetworkSubCommands.WifiState.getVal(),
            1 // WifiHotspot
        )
        
        val state = CameraCommandProtocol.getWifiStateCmdResponseParse(responseHotspot, 4)
        assertEquals(CameraCommandProtocol.WifiState.WifiHotspot, state)
    }

    @Test
    fun `test getWifiStateCmdResponseParse with invalid header`() {
        val response = intArrayOf(
            CameraCommandProtocol.Header.ACK.getVal(), // Wrong header
            CameraCommandProtocol.Commands.NETWORK.getVal(),
            CameraCommandProtocol.NetworkSubCommands.WifiState.getVal(),
            1
        )
        
        try {
            CameraCommandProtocol.getWifiStateCmdResponseParse(response, 4)
            fail("Should throw exception for invalid header")
        } catch (e: Exception) {
            assertTrue(e.message!!.contains("Invalid response header"))
        }
    }

    @Test
    fun `test getWifiStateCmdResponseParse with invalid state value`() {
        val response = intArrayOf(
            CameraCommandProtocol.Header.RESPONSE.getVal(),
            CameraCommandProtocol.Commands.NETWORK.getVal(),
            CameraCommandProtocol.NetworkSubCommands.WifiState.getVal(),
            99 // Invalid state
        )
        
        try {
            CameraCommandProtocol.getWifiStateCmdResponseParse(response, 4)
            fail("Should throw exception for unknown state")
        } catch (e: Exception) {
            assertTrue(e.message!!.contains("Unknown WiFi state value"))
        }
    }

    @Test
    fun `test getWifiStateCmdResponseParse with short response`() {
        val response = intArrayOf(1, 2, 3) // Only 3 bytes
        
        try {
            CameraCommandProtocol.getWifiStateCmdResponseParse(response, 3)
            fail("Should throw exception for short response")
        } catch (e: Exception) {
            assertTrue(e.message!!.contains("Invalid response length"))
        }
    }

    // ========== Command Validation Tests ==========

    @Test
    fun `test validateCommand with valid command`() {
        val cmd = CameraCommandProtocol.startStreamCmd()
        val result = CameraCommandProtocol.validateCommand(cmd)
        
        assertTrue("Should validate correct command", result.isSuccess)
    }

    @Test
    fun `test validateCommand with empty command`() {
        val result = CameraCommandProtocol.validateCommand(intArrayOf())
        
        assertTrue("Should fail for empty command", result.isFailure)
        assertTrue(result.exceptionOrNull()?.message!!.contains("cannot be empty"))
    }

    @Test
    fun `test validateCommand with short command`() {
        val result = CameraCommandProtocol.validateCommand(intArrayOf(1, 2, 3))
        
        assertTrue("Should fail for short command", result.isFailure)
        assertTrue(result.exceptionOrNull()?.message!!.contains("at least 4 bytes"))
    }

    @Test
    fun `test validateCommand with invalid header`() {
        val cmd = intArrayOf(99, 1, 1, 0) // Invalid header
        val result = CameraCommandProtocol.validateCommand(cmd)
        
        assertTrue("Should fail for invalid header", result.isFailure)
        assertTrue(result.exceptionOrNull()?.message!!.contains("Invalid header"))
    }

    @Test
    fun `test validateCommand with invalid command type`() {
        val cmd = intArrayOf(1, 99, 1, 0) // Invalid command type
        val result = CameraCommandProtocol.validateCommand(cmd)
        
        assertTrue("Should fail for invalid command type", result.isFailure)
        assertTrue(result.exceptionOrNull()?.message!!.contains("Invalid command type"))
    }

    // ========== Helper Method Tests ==========

    @Test
    fun `test getAvailableValues for ZOOM`() {
        val values = CameraCommandProtocol.getAvailableValues("ZOOM")
        
        assertEquals(4, values.size)
        assertTrue(values.contains("x1"))
        assertTrue(values.contains("x2"))
        assertTrue(values.contains("x3"))
        assertTrue(values.contains("x4"))
    }

    @Test
    fun `test getAvailableValues for ROTATION`() {
        val values = CameraCommandProtocol.getAvailableValues("ROTATION")
        
        assertEquals(4, values.size)
        assertTrue(values.contains("0"))
        assertTrue(values.contains("90"))
        assertTrue(values.contains("180"))
        assertTrue(values.contains("270"))
    }

    @Test
    fun `test getAvailableValues for IRCUTFILTER`() {
        val values = CameraCommandProtocol.getAvailableValues("IRCUTFILTER")
        
        assertEquals(2, values.size)
        assertTrue(values.contains("Off"))
        assertTrue(values.contains("On"))
    }

    @Test
    fun `test getAvailableValues case insensitive`() {
        val valuesUpper = CameraCommandProtocol.getAvailableValues("ZOOM")
        val valuesLower = CameraCommandProtocol.getAvailableValues("zoom")
        val valuesMixed = CameraCommandProtocol.getAvailableValues("ZoOm")
        
        assertEquals(valuesUpper, valuesLower)
        assertEquals(valuesUpper, valuesMixed)
    }

    @Test
    fun `test getAvailableValues for unknown parameter`() {
        val values = CameraCommandProtocol.getAvailableValues("UNKNOWN")
        
        assertTrue("Should return empty list for unknown parameter", values.isEmpty())
    }

    @Test
    fun `test describeCommand with valid command`() {
        val cmd = CameraCommandProtocol.startStreamCmd()
        val description = CameraCommandProtocol.describeCommand(cmd)
        
        assertTrue(description.contains("SET"))
        assertTrue(description.contains("STREAMING"))
        assertTrue(description.contains("subcommand"))
    }

    @Test
    fun `test describeCommand with short command`() {
        val description = CameraCommandProtocol.describeCommand(intArrayOf(1, 2))
        
        assertEquals("Invalid command", description)
    }

    @Test
    fun `test describeCommand with unknown values`() {
        val cmd = intArrayOf(99, 99, 1, 0)
        val description = CameraCommandProtocol.describeCommand(cmd)
        
        assertTrue(description.contains("Unknown"))
    }

    // ========== Edge Cases ==========

    @Test
    fun `test all zoom commands build correctly`() {
        val zoomValues = listOf("x1", "x2", "x3", "x4")
        
        zoomValues.forEach { zoom ->
            val cmd = CameraCommandProtocol.setImgZoomCmd(zoom)
            assertNotNull("Command should not be null", cmd)
            assertEquals(5, cmd.size)
            assertEquals(CameraCommandProtocol.Header.SET.getVal(), cmd[0])
        }
    }

    @Test
    fun `test all rotation commands build correctly`() {
        val rotations = listOf("0", "90", "180", "270")
        
        rotations.forEach { rotation ->
            val cmd = CameraCommandProtocol.setImgRotationCmd(rotation)
            assertNotNull("Command should not be null", cmd)
            assertEquals(5, cmd.size)
            assertEquals(CameraCommandProtocol.Header.SET.getVal(), cmd[0])
        }
    }

    @Test
    fun `test IR brightness boundary values`() {
        // Test 0
        val cmd0 = CameraCommandProtocol.setImgIRBrightnessCmd(0)
        assertEquals(0, cmd0[3])
        
        // Test 255
        val cmd255 = CameraCommandProtocol.setImgIRBrightnessCmd(255)
        assertEquals(255, cmd255[3])
        
        // Test mid-range
        val cmd128 = CameraCommandProtocol.setImgIRBrightnessCmd(128)
        assertEquals(128, cmd128[3])
    }

    @Test
    fun `test response parsing with all SET command types`() {
        val testCases = listOf(
            Triple(CameraCommandProtocol.Commands.STREAMING.getVal(), 
                   CameraCommandProtocol.StreamingSubCommands.START.getVal(),
                   CameraCommandProtocol::startStreamCmdResponseParse),
            Triple(CameraCommandProtocol.Commands.STREAMING.getVal(), 
                   CameraCommandProtocol.StreamingSubCommands.STOP.getVal(),
                   CameraCommandProtocol::stopStreamCmdResponseParse),
            Triple(CameraCommandProtocol.Commands.IMAGE.getVal(), 
                   CameraCommandProtocol.ImageSubCommands.ZOOM.getVal(),
                   CameraCommandProtocol::setImgZoomCmdResponseParse)
        )
        
        testCases.forEach { (command, subCommand, parser) ->
            val response = intArrayOf(
                CameraCommandProtocol.Header.ACK.getVal(),
                command,
                subCommand,
                1
            )
            
            val result = parser(response, 4)
            assertTrue("Should parse valid response for command $command/$subCommand", result)
        }
    }

    @Test
    fun `test config response with all valid enum combinations`() {
        // Test all combinations of valid enum values
        val zooms = listOf(1, 2, 3, 4)
        val rotations = listOf(1, 2, 3, 4)
        val filters = listOf(0, 1)
        
        zooms.forEach { zoom ->
            rotations.forEach { rotation ->
                filters.forEach { filter ->
                    val response = intArrayOf(
                        CameraCommandProtocol.Header.RESPONSE.getVal(),
                        CameraCommandProtocol.Commands.CONFIG.getVal(),
                        CameraCommandProtocol.ConfigGetSubCommands.Current.getVal(),
                        0,
                        zoom,
                        rotation,
                        filter,
                        100
                    )
                    
                    val config = CameraCommandProtocol.getCurrentConfigCmdResponseParse(response, 8)
                    assertNotNull("Config should be parsed", config)
                    assertTrue("Should have ZOOM", config.containsKey("ZOOM"))
                    assertTrue("Should have ROTATION", config.containsKey("ROTATION"))
                    assertTrue("Should have IRCUTFILTER", config.containsKey("IRCUTFILTER"))
                }
            }
        }
    }

    @Test
    fun `test getWifiCountryCodeCmd builds correct command`() {
        val command = CameraCommandProtocol.getWifiCountryCodeCmd()
        
        assertEquals("Command should have 4 elements", 4, command.size)
        assertEquals("Header should be GET", CameraCommandProtocol.Header.GET.getVal(), command[0])
        assertEquals("Command should be NETWORK", CameraCommandProtocol.Commands.NETWORK.getVal(), command[1])
        assertEquals("SubCommand should be WifiCountryCode", CameraCommandProtocol.NetworkSubCommands.WifiCountryCode.getVal(), command[2])
        assertEquals("Data length should be 0", 0, command[3])
    }

    @Test
    fun `test setWifiCountryCodeCmd builds correct command with valid country code`() {
        val command = CameraCommandProtocol.setWifiCountryCodeCmd("US")
        
        assertEquals("Command should have 7 elements", 7, command.size)
        assertEquals("Header should be SET", CameraCommandProtocol.Header.SET.getVal(), command[0])
        assertEquals("Command should be NETWORK", CameraCommandProtocol.Commands.NETWORK.getVal(), command[1])
        assertEquals("SubCommand should be WifiCountryCode", CameraCommandProtocol.NetworkSubCommands.WifiCountryCode.getVal(), command[2])
        assertEquals("Data length should be 3", 3, command[3])
        assertEquals("Country code length should be 2", 2, command[4])
        assertEquals("First character should be U", 'U'.code, command[5])
        assertEquals("Second character should be S", 'S'.code, command[6])
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test setWifiCountryCodeCmd throws exception for invalid length`() {
        CameraCommandProtocol.setWifiCountryCodeCmd("USA")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test setWifiCountryCodeCmd throws exception for single character`() {
        CameraCommandProtocol.setWifiCountryCodeCmd("U")
    }

    @Test
    fun `test getWifiCountryCodeCmdResponseParse with success response`() {
        val response = intArrayOf(
            CameraCommandProtocol.Header.RESPONSE.getVal(),
            CameraCommandProtocol.Commands.NETWORK.getVal(),
            CameraCommandProtocol.NetworkSubCommands.WifiCountryCode.getVal(),
            3,
            0,
            2,
            'I'.code,
            'N'.code
        )
        
        val countryCode = CameraCommandProtocol.getWifiCountryCodeCmdResponseParse(response, 8)
        assertEquals("Country code should be IN", "IN", countryCode)
    }

    @Test
    fun `test getWifiCountryCodeCmdResponseParse with different country codes`() {
        val testCases = listOf("US", "GB", "IN", "DE", "FR", "JP", "AU", "CA")
        
        testCases.forEach { expectedCode ->
            val response = intArrayOf(
                CameraCommandProtocol.Header.RESPONSE.getVal(),
                CameraCommandProtocol.Commands.NETWORK.getVal(),
                CameraCommandProtocol.NetworkSubCommands.WifiCountryCode.getVal(),
                3,
                0,
                2,
                expectedCode[0].code,
                expectedCode[1].code
            )
            
            val countryCode = CameraCommandProtocol.getWifiCountryCodeCmdResponseParse(response, 8)
            assertEquals("Country code should be $expectedCode", expectedCode, countryCode)
        }
    }

    @Test(expected = Exception::class)
    fun `test getWifiCountryCodeCmdResponseParse throws exception for error response`() {
        val response = intArrayOf(
            CameraCommandProtocol.Header.RESPONSE.getVal(),
            CameraCommandProtocol.Commands.NETWORK.getVal(),
            CameraCommandProtocol.NetworkSubCommands.WifiCountryCode.getVal(),
            2,
            1,
            -5
        )
        
        CameraCommandProtocol.getWifiCountryCodeCmdResponseParse(response, 6)
    }

    @Test(expected = Exception::class)
    fun `test getWifiCountryCodeCmdResponseParse throws exception for invalid length`() {
        val response = intArrayOf(
            CameraCommandProtocol.Header.RESPONSE.getVal(),
            CameraCommandProtocol.Commands.NETWORK.getVal(),
            CameraCommandProtocol.NetworkSubCommands.WifiCountryCode.getVal(),
            2
        )
        
        CameraCommandProtocol.getWifiCountryCodeCmdResponseParse(response, 4)
    }

    @Test(expected = Exception::class)
    fun `test getWifiCountryCodeCmdResponseParse throws exception for invalid country code length`() {
        val response = intArrayOf(
            CameraCommandProtocol.Header.RESPONSE.getVal(),
            CameraCommandProtocol.Commands.NETWORK.getVal(),
            CameraCommandProtocol.NetworkSubCommands.WifiCountryCode.getVal(),
            4,
            0,
            3,
            'U'.code,
            'S'.code,
            'A'.code
        )
        
        CameraCommandProtocol.getWifiCountryCodeCmdResponseParse(response, 9)
    }

    @Test
    fun `test setWifiCountryCodeCmdResponseParse with success response`() {
        val response = intArrayOf(
            CameraCommandProtocol.Header.ACK.getVal(),
            CameraCommandProtocol.Commands.NETWORK.getVal(),
            CameraCommandProtocol.NetworkSubCommands.WifiCountryCode.getVal(),
            1
        )
        
        val result = CameraCommandProtocol.setWifiCountryCodeCmdResponseParse(response, 4)
        assertTrue("Should return true for success response", result)
    }

    @Test(expected = Exception::class)
    fun `test setWifiCountryCodeCmdResponseParse throws exception for invalid header`() {
        val response = intArrayOf(
            CameraCommandProtocol.Header.RESPONSE.getVal(),
            CameraCommandProtocol.Commands.NETWORK.getVal(),
            CameraCommandProtocol.NetworkSubCommands.WifiCountryCode.getVal(),
            1
        )
        
        CameraCommandProtocol.setWifiCountryCodeCmdResponseParse(response, 4)
    }
}
