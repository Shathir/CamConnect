package com.outdu.camconnect.communication

import java.util.HashMap

/**
 * camera command protocol with type-safe enums and command builders.
 * Provides a comprehensive API for camera communication protocol.
 */
object CameraCommandProtocol {

    // Protocol headers and command types
    enum class Header(val value: Int) {
        SET(1), GET(2), ACK(3), RESPONSE(4);
        
        fun getVal(): Int = value
    }

    enum class Commands(val value: Int) {
        STREAMING(1), NETWORK(2), CONFIG(3), IMAGE(4), AUDIO(5), SYSTEM(6);
        
        fun getVal(): Int = value
    }

    enum class StreamingSubCommands(val value: Int) {
        START(1), STOP(2);
        
        fun getVal(): Int = value
    }

    enum class ImageSubCommands(val value: Int) {
        ZOOM(1), ROTATION(2), IRCUTFILTER(3), IRBRIGHTNESS(4), DAYMODE(5),
        RESOLUTION(6), MIRROR(7), FLIP(8), TILT(9), WDR(10), EIS(11), GYROREADER(12), MISC(13);
        
        fun getVal(): Int = value
    }

    enum class ConfigSetSubCommands(val value: Int) {
        DefaultToFactory(9), DefaultToCurrent(11), CurrentToFactory(13), CurrentToDefault(14);
        
        fun getVal(): Int = value
    }

    enum class ConfigGetSubCommands(val value: Int) {
        Factory(4), Default(8), Current(12);
        
        fun getVal(): Int = value
    }

    enum class SystemSubCommands(val value: Int) {
        SHUTDOWN(1);
        
        fun getVal(): Int = value
    }

    enum class NetworkSubCommands(val value: Int) {
        WifiHotspot(1), WifiClient(2), WifiState(3);
        
        fun getVal(): Int = value
    }

    // Camera control enums with validation
    enum class ZOOM(val value: Int, val displayValue: String) {
        X1(1, "x1"), X2(2, "x2"), X3(3, "x3"), X4(4, "x4");
        
        fun getVal(): Int = value
        fun getDisplayVal(): String = displayValue
        
        companion object {
            fun get(v: Int): ZOOM? = values().find { it.value == v }
            fun get(v: String): ZOOM? = values().find { it.displayValue == v }
            fun getKey(): String = "ZOOM"
            fun getAllValues(): List<String> = values().map { it.displayValue }
        }
    }

    enum class ROTATION(val value: Int, val displayValue: String) {
        R0(1, "0"), R90(2, "90"), R180(3, "180"), R270(4, "270");
        
        fun getVal(): Int = value
        fun getDisplayVal(): String = displayValue
        
        companion object {
            fun get(v: Int): ROTATION? = values().find { it.value == v }
            fun get(v: String): ROTATION? = values().find { it.displayValue == v }
            fun getKey(): String = "ROTATION"
            fun getAllValues(): List<String> = values().map { it.displayValue }
        }
    }

    enum class RESOLUTION(val value: Int, val displayValue: String) {
        MODE0(0, "mode0"), MODE1(1, "mode1");
        
        fun getVal(): Int = value
        fun getDisplayVal(): String = displayValue
        
        companion object {
            fun get(v: Int): RESOLUTION? = values().find { it.value == v }
            fun get(v: String): RESOLUTION? = values().find { it.displayValue == v }
            fun getKey(): String = "RESOLUTION"
            fun getAllValues(): List<String> = values().map { it.displayValue }
        }
    }

    enum class TILT(val value: Int, val displayValue: String) {
        T0(0, "0"), T1(1, "1"), T2(2, "2"), T3(3, "3"), T4(4, "4"), T5(5, "5");
        
        fun getVal(): Int = value
        fun getDisplayVal(): String = displayValue
        
        companion object {
            fun get(v: Int): TILT? = values().find { it.value == v }
            fun get(v: String): TILT? = values().find { it.displayValue == v }
            fun getKey(): String = "TILT"
            fun getAllValues(): List<String> = values().map { it.displayValue }
        }
    }

    enum class IRCUTFILTER(val value: Int, val displayValue: String) {
        OFF(0, "Off"), ON(1, "On");
        
        fun getVal(): Int = value
        fun getDisplayVal(): String = displayValue
        
        companion object {
            fun get(v: Int): IRCUTFILTER? = values().find { it.value == v }
            fun get(v: String): IRCUTFILTER? = values().find { it.displayValue == v }
            fun getKey(): String = "IRCUTFILTER"
            fun getAllValues(): List<String> = values().map { it.displayValue }
        }
    }

    enum class WifiState(val value: Int) {
        WifiHotspot(1), WifiClient(2);
        
        fun getVal(): Int = value
    }

    // Command building methods
    fun startStreamCmd(): IntArray {
        return intArrayOf(
            Header.SET.getVal(),
            Commands.STREAMING.getVal(),
            StreamingSubCommands.START.getVal(),
            0
        )
    }

    fun stopStreamCmd(): IntArray {
        return intArrayOf(
            Header.SET.getVal(),
            Commands.STREAMING.getVal(),
            StreamingSubCommands.STOP.getVal(),
            0
        )
    }

    fun getFactoryConfigCmd(): IntArray {
        return intArrayOf(
            Header.GET.getVal(),
            Commands.CONFIG.getVal(),
            ConfigGetSubCommands.Factory.getVal(),
            0
        )
    }

    fun getDefaultConfigCmd(): IntArray {
        return intArrayOf(
            Header.GET.getVal(),
            Commands.CONFIG.getVal(),
            ConfigGetSubCommands.Default.getVal(),
            0
        )
    }

    fun getCurrentConfigCmd(): IntArray {
        return intArrayOf(
            Header.GET.getVal(),
            Commands.CONFIG.getVal(),
            ConfigGetSubCommands.Current.getVal(),
            0
        )
    }

    @Throws(Exception::class)
    fun setImgZoomCmd(zoomVal: String): IntArray {
        val zoom = ZOOM.get(zoomVal) ?: throw Exception("Invalid zoom value: $zoomVal")
        return intArrayOf(
            Header.SET.getVal(),
            Commands.IMAGE.getVal(),
            ImageSubCommands.ZOOM.getVal(),
            zoom.getVal(),
            0
        )
    }

    @Throws(Exception::class)
    fun setImgRotationCmd(rotationVal: String): IntArray {
        val rotation = ROTATION.get(rotationVal) ?: throw Exception("Invalid rotation value: $rotationVal")
        return intArrayOf(
            Header.SET.getVal(),
            Commands.IMAGE.getVal(),
            ImageSubCommands.ROTATION.getVal(),
            rotation.getVal(),
            0
        )
    }

    @Throws(Exception::class)
    fun setImgResolutionCmd(resolutionVal: String): IntArray {
        val resolution = RESOLUTION.get(resolutionVal) ?: throw Exception("Invalid resolution value: $resolutionVal")
        return intArrayOf(
            Header.SET.getVal(),
            Commands.IMAGE.getVal(),
            ImageSubCommands.RESOLUTION.getVal(),
            resolution.getVal(),
            0
        )
    }

    @Throws(Exception::class)
    fun setImgTiltCmd(tiltVal: String): IntArray {
        val tilt = TILT.get(tiltVal) ?: throw Exception("Invalid tilt value: $tiltVal")
        return intArrayOf(
            Header.SET.getVal(),
            Commands.IMAGE.getVal(),
            ImageSubCommands.TILT.getVal(),
            tilt.getVal(),
            0
        )
    }

    @Throws(Exception::class)
    fun setImgIRCutFilterCmd(ircutfilterVal: String): IntArray {
        val irCutFilter = IRCUTFILTER.get(ircutfilterVal) ?: throw Exception("Invalid IR cut filter value: $ircutfilterVal")
        return intArrayOf(
            Header.SET.getVal(),
            Commands.IMAGE.getVal(),
            ImageSubCommands.IRCUTFILTER.getVal(),
            irCutFilter.getVal(),
            0
        )
    }

    @Throws(Exception::class)
    fun setImgIRBrightnessCmd(brightnessVal: Int): IntArray {
        if (brightnessVal !in 0..255) {
            throw Exception("IR brightness must be between 0 and 255, got: $brightnessVal")
        }
        return intArrayOf(
            Header.SET.getVal(),
            Commands.IMAGE.getVal(),
            ImageSubCommands.IRBRIGHTNESS.getVal(),
            brightnessVal,
            0
        )
    }

    fun shutdownCmd(): IntArray {
        return intArrayOf(
            Header.SET.getVal(),
            Commands.SYSTEM.getVal(),
            SystemSubCommands.SHUTDOWN.getVal(),
            0
        )
    }

    fun getWifiStateCmd(): IntArray {
        return intArrayOf(
            Header.GET.getVal(),
            Commands.NETWORK.getVal(),
            NetworkSubCommands.WifiState.getVal(),
            0
        )
    }

    // Response parsing methods
    @Throws(Exception::class)
    fun startStreamCmdResponseParse(response: IntArray, length: Int): Boolean {
        return parseSetCommandResponse(response, length, Commands.STREAMING.getVal(), StreamingSubCommands.START.getVal())
    }

    @Throws(Exception::class)
    fun stopStreamCmdResponseParse(response: IntArray, length: Int): Boolean {
        return parseSetCommandResponse(response, length, Commands.STREAMING.getVal(), StreamingSubCommands.STOP.getVal())
    }

    @Throws(Exception::class)
    fun setImgZoomCmdResponseParse(response: IntArray, length: Int): Boolean {
        return parseSetCommandResponse(response, length, Commands.IMAGE.getVal(), ImageSubCommands.ZOOM.getVal())
    }

    @Throws(Exception::class)
    fun setImgRotationCmdResponseParse(response: IntArray, length: Int): Boolean {
        return parseSetCommandResponse(response, length, Commands.IMAGE.getVal(), ImageSubCommands.ROTATION.getVal())
    }

    @Throws(Exception::class)
    fun setImgResolutionCmdResponseParse(response: IntArray, length: Int): Boolean {
        return parseSetCommandResponse(response, length, Commands.IMAGE.getVal(), ImageSubCommands.RESOLUTION.getVal())
    }

    @Throws(Exception::class)
    fun setImgTiltCmdResponseParse(response: IntArray, length: Int): Boolean {
        return parseSetCommandResponse(response, length, Commands.IMAGE.getVal(), ImageSubCommands.TILT.getVal())
    }

    @Throws(Exception::class)
    fun setImgIRCutFilterCmdResponseParse(response: IntArray, length: Int): Boolean {
        return parseSetCommandResponse(response, length, Commands.IMAGE.getVal(), ImageSubCommands.IRCUTFILTER.getVal())
    }

    @Throws(Exception::class)
    fun setImgIRBrightnessCmdResponseParse(response: IntArray, length: Int): Boolean {
        return parseSetCommandResponse(response, length, Commands.IMAGE.getVal(), ImageSubCommands.IRBRIGHTNESS.getVal())
    }

    @Throws(Exception::class)
    fun shutdownCmdResponseParse(response: IntArray, length: Int): Boolean {
        return parseSetCommandResponse(response, length, Commands.SYSTEM.getVal(), SystemSubCommands.SHUTDOWN.getVal())
    }

    @Throws(Exception::class)
    fun getFactoryConfigCmdResponseParse(response: IntArray, length: Int): Map<String, Any> {
        return parseConfigResponse(response, length)
    }

    @Throws(Exception::class)
    fun getDefaultConfigCmdResponseParse(response: IntArray, length: Int): Map<String, Any> {
        return parseConfigResponse(response, length)
    }

    @Throws(Exception::class)
    fun getCurrentConfigCmdResponseParse(response: IntArray, length: Int): Map<String, Any> {
        return parseConfigResponse(response, length)
    }

    @Throws(Exception::class)
    fun getWifiStateCmdResponseParse(response: IntArray, length: Int): WifiState {
        if (length < 4) {
            throw Exception("Invalid response length for WiFi state: $length")
        }
        
        if (response[0] != Header.RESPONSE.getVal()) {
            throw Exception("Invalid response header for WiFi state: ${response[0]}")
        }
        
        val stateValue = response[3]
        return WifiState.values().find { it.value == stateValue } 
            ?: throw Exception("Unknown WiFi state value: $stateValue")
    }

    // Helper methods for response parsing
    private fun parseSetCommandResponse(response: IntArray, length: Int, expectedCommand: Int, expectedSubCommand: Int): Boolean {
        if (length < 4) {
            throw Exception("Response too short: $length bytes")
        }
        
        return response[0] == Header.ACK.getVal() &&
                response[1] == expectedCommand &&
                response[2] == expectedSubCommand &&
                response[3] == 1
    }

    private fun parseConfigResponse(response: IntArray, length: Int): Map<String, Any> {
        val config = HashMap<String, Any>()
        
        if (length >= 8 && response[0] == Header.RESPONSE.getVal()) {
            // Parse basic configuration properties
            val zoom = ZOOM.get(response[4])
            val rotation = ROTATION.get(response[5])
            val irCutFilter = IRCUTFILTER.get(response[6])
            val irBrightness = response[7]
            
            zoom?.let { config[ZOOM.getKey()] = it.getDisplayVal() }
            rotation?.let { config[ROTATION.getKey()] = it.getDisplayVal() }
            irCutFilter?.let { config[IRCUTFILTER.getKey()] = it.getDisplayVal() }
            config["IRBRIGHTNESS"] = irBrightness
            
            // Add metadata
            config["response_length"] = length
            config["timestamp"] = System.currentTimeMillis()
        }
        
        return config
    }

    /**
     * Validates command structure before sending
     */
    fun validateCommand(command: IntArray): Result<Unit> {
        if (command.isEmpty()) {
            return Result.failure(Exception("Command cannot be empty"))
        }
        
        if (command.size < 4) {
            return Result.failure(Exception("Command must have at least 4 bytes"))
        }
        
        val header = Header.values().find { it.value == command[0] }
        if (header == null) {
            return Result.failure(Exception("Invalid header: ${command[0]}"))
        }
        
        val commandType = Commands.values().find { it.value == command[1] }
        if (commandType == null) {
            return Result.failure(Exception("Invalid command type: ${command[1]}"))
        }
        
        return Result.success(Unit)
    }

    /**
     * Gets available values for a specific parameter type
     */
    fun getAvailableValues(parameterType: String): List<String> {
        return when (parameterType.uppercase()) {
            "ZOOM" -> ZOOM.getAllValues()
            "ROTATION" -> ROTATION.getAllValues()
            "RESOLUTION" -> RESOLUTION.getAllValues()
            "TILT" -> TILT.getAllValues()
            "IRCUTFILTER" -> IRCUTFILTER.getAllValues()
            else -> emptyList()
        }
    }

    /**
     * Creates a human-readable description of a command
     */
    fun describeCommand(command: IntArray): String {
        if (command.size < 3) return "Invalid command"
        
        val header = Header.values().find { it.value == command[0] }?.name ?: "Unknown"
        val commandType = Commands.values().find { it.value == command[1] }?.name ?: "Unknown"
        val subCommand = command[2]
        
        return "$header $commandType (subcommand: $subCommand)"
    }
} 