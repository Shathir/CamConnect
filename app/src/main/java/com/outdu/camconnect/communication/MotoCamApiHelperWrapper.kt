package com.outdu.camconnect.communication

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.FileReader
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.Socket

object MotocamAPIHelperWrapper {

    private const val TAG = "MotocamAPIHelperWrapper"
    private const val MAX_BYTES = 255

    private var deviceIpAddress: String = "192.168.2.1"

    /** Test-only: override client creation for unit tests (e.g. with MockEngine). */
    @Volatile
    var clientFactory: (() -> MotocamSocketClient)? = null
        internal set

    fun setClientFactory(factory: (() -> MotocamSocketClient)?) {
        clientFactory = factory
    }
    private const val MOTOCAM_CLIENT_SOCKET_PORT = 9000
    const val MOTOCAM_SERVER_SOCKET_PORT = 9002

    suspend fun findDevice() {
        Log.i(TAG, "findDevice")
        try {
            withContext(Dispatchers.IO) {
                BufferedReader(FileReader("/proc/net/arp")).use { reader ->
                    reader.lineSequence()
                        .mapNotNull { line ->
                            val tokens = line.split(" +".toRegex())
                            if (tokens.size >= 4) {
                                val ip = tokens[0].trim()
                                val mac = tokens[3].trim()
                                if (ip != "IP" && mac != "00:00:00:00:00:00" &&
                                    isDeviceReachable(ip, MOTOCAM_CLIENT_SOCKET_PORT)
                                ) {
                                    ip
                                } else null
                            } else null
                        }
                        .firstOrNull()?.let {
                            deviceIpAddress = it
                            Log.i(TAG, "Found device: $it")
                        }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in findDevice", e)
        }
    }

    private fun isDeviceReachable(ip: String, port: Int): Boolean {
        return try {
            Socket().use { it.connect(InetSocketAddress(ip, port), 300) }
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun getConfig(type: String): Map<String, Any>? = when (type) {
        "Factory" -> getFactoryConfig()
        "Default" -> getDefaultConfig()
        "Current" -> getCurrentConfig()
        else -> null
    }

    private suspend fun <T> withSocketClient(cameraIp: String? = null, block: suspend (MotocamSocketClient) -> T): T {
        val client = clientFactory?.invoke() ?: MotocamSocketClient()
        return try {
            val targetIp = cameraIp ?: deviceIpAddress
            client.init(targetIp)
            block(client)
        } finally {
            client.destroy()
        }
    }

    private suspend fun <T> sendCommand(
        reqCmd: IntArray,
        parse: (IntArray, Int) -> T,
        cameraIp: String? = null
    ): T = withSocketClient(cameraIp) { client ->
        val reqWithCrcForLog = reqCmd.withCalculatedCrc()
        Log.i(
            TAG,
            "SEND ip=${client.getCameraIp()} ${describePacket(reqCmd, reqCmd.size)} hex=${reqWithCrcForLog.toHexString()}"
        )
        val res = IntArray(MAX_BYTES)
        val len = client.sendCmd(reqCmd, res)

        Log.i(
            TAG,
            "RECV ip=${client.getCameraIp()} ${describePacket(res, len)} hex=${res.toHexString(len)}"
        )
        try {
            parse(res, len)
        } catch (e: Exception) {
            Log.e(
                TAG,
                "PARSE_FAIL ip=${client.getCameraIp()} ${describePacket(res, len)} hex=${res.toHexString(len)}",
                e
            )
            throw e
        }
    }

    private fun IntArray.toHexString(len: Int = this.size): String {
        val safeLen = len.coerceIn(0, this.size)
        return (0 until safeLen).joinToString(" ") { idx ->
            "0x" + (this[idx] and 0xFF).toString(16).padStart(2, '0').uppercase()
        }
    }

    private fun IntArray.withCalculatedCrc(): IntArray {
        if (this.isEmpty()) return this
        val out = this.copyOf()
        val sum = out.dropLast(1).sumOf { it and 0xFF } and 0xFF
        val crc = ((sum xor 0xFF) + 1) and 0xFF
        out[out.size - 1] = crc
        return out
    }

    private fun describePacket(packet: IntArray, len: Int): String {
        if (len < 4) return "len=$len <too short>"
        val headerVal = packet[0]
        val cmdVal = packet[1]
        val subVal = packet[2]
        val dataLen = packet[3]

        val headerName = MotocamAPIHelper.Header.values()
            .firstOrNull { it.getVal() == headerVal }
            ?.name ?: "H?$headerVal"

        val cmdName = MotocamAPIHelper.Commands.values()
            .firstOrNull { it.getVal() == cmdVal }
            ?.name ?: "C?$cmdVal"

        val subName = when (cmdVal) {
            MotocamAPIHelper.Commands.SYSTEM.getVal() -> MotocamAPIHelper.SystemSubCommands.values()
                .firstOrNull { it.getVal() == subVal }
                ?.name ?: "S?$subVal"
            MotocamAPIHelper.Commands.CONFIG.getVal() -> "SUB=$subVal"
            MotocamAPIHelper.Commands.NETWORK.getVal() -> "SUB=$subVal"
            MotocamAPIHelper.Commands.IMAGE.getVal() -> "SUB=$subVal"
            MotocamAPIHelper.Commands.AUDIO.getVal() -> "SUB=$subVal"
            MotocamAPIHelper.Commands.STREAMING.getVal() -> "SUB=$subVal"
            else -> "SUB=$subVal"
        }

        return "hdr=$headerName cmd=$cmdName sub=$subName dataLen=$dataLen"
    }

    suspend fun getFactoryConfig() = sendCommand(
        MotocamAPIHelper.getFactoryConfigCmd(),
        MotocamAPIHelper::getFactoryConfigCmdResponseParse
    )

    suspend fun getDefaultConfig() = sendCommand(
        MotocamAPIHelper.getDefaultConfigCmd(),
        MotocamAPIHelper::getDefaultConfigCmdResponseParse
    )

    suspend fun getCurrentConfig() = sendCommand(
        MotocamAPIHelper.getCurrentConfigCmd(),
        MotocamAPIHelper::getCurrentConfigCmdResponseParse
    )

    suspend fun setIrBrightness(value: Int) = sendCommand(
        MotocamAPIHelper.setImgIRBrightnessCmd(value),
        MotocamAPIHelper::setImgIRBrightnessCmdResponseParse
    )

    suspend fun setImgZoom(zoom: MotocamAPIHelper.ZOOM) = sendCommand(
        MotocamAPIHelper.setImgZoomCmd(zoom.displayVal),
        MotocamAPIHelper::setImgZoomCmdResponseParse
    )

    suspend fun setImgResolution(res: MotocamAPIHelper.RESOLUTION) = sendCommand(
        MotocamAPIHelper.setImgResolutionCmd(res.displayVal),
        MotocamAPIHelper::setImgResolutionCmdResponseParse
    )

    suspend fun setImgTilt(tilt: MotocamAPIHelper.TILT) = sendCommand(
        MotocamAPIHelper.setImgTiltCmd(tilt.displayVal),
        MotocamAPIHelper::setImgTiltCmdResponseParse
    )

    suspend fun setImgRotation(rotation: MotocamAPIHelper.ROTATION) = sendCommand(
        MotocamAPIHelper.setImgRotationCmd(rotation.displayVal),
        MotocamAPIHelper::setImgRotationCmdResponseParse
    )

    suspend fun setIrCutFilter(filter: MotocamAPIHelper.IRCUTFILTER) = sendCommand(
        MotocamAPIHelper.setImgIRCutFilterCmd(filter.displayVal),
        MotocamAPIHelper::setImgIRCutFilterCmdResponseParse
    )

    suspend fun setMirror(value: MotocamAPIHelper.MIRROR) = sendCommand(
        MotocamAPIHelper.setMirrorCmd(value.displayVal),
        MotocamAPIHelper::setMirrorCmdResponseParse
    )

    suspend fun setFlip(value: MotocamAPIHelper.FLIP) = sendCommand(
        MotocamAPIHelper.setFlipCmd(value.displayVal),
        MotocamAPIHelper::setFlipCmdResponseParse
    )

    suspend fun setWdr(value: MotocamAPIHelper.WDR) = sendCommand(
        MotocamAPIHelper.setWdrCmd(value.displayVal),
        MotocamAPIHelper::setWdrCmdResponseParse
    )

    suspend fun setEis(value: MotocamAPIHelper.EIS) = sendCommand(
        MotocamAPIHelper.setEisCmd(value.displayVal),
        MotocamAPIHelper::setEisCmdResponseParse
    )

    suspend fun setMisc(value: Int) = sendCommand(
        MotocamAPIHelper.setMiscCmd(value),
        MotocamAPIHelper::setMiscCmdResponseParse
    )

    suspend fun setDayMode(mode: MotocamAPIHelper.DAYMODE) = sendCommand(
        MotocamAPIHelper.setImgDayModeCmd(mode.displayVal),
        MotocamAPIHelper::setImgDayModeCmdResponseParse
    )

    suspend fun setGyroReader(reader: MotocamAPIHelper.GYROREADER) = sendCommand(
        MotocamAPIHelper.setImgGyroReaderCmd(reader.displayVal),
        MotocamAPIHelper::setImgGyroReaderCmdResponseParse
    )

    suspend fun setMic(mic: MotocamAPIHelper.MIC) = sendCommand(
        MotocamAPIHelper.setAudioMicCmd(mic.displayVal),
        MotocamAPIHelper::setAudioMicCmdResponseParse
    )

    suspend fun setWifiHotspot(
        ssid: String, encryptionType: String, key: String, ip: String, mask: String
    ) = sendCommand(
        MotocamAPIHelper.setWifiHotspotCmd(ssid, encryptionType, key, ip, mask),
        MotocamAPIHelper::setWifiHotspotCmdResponseParse
    )

    suspend fun setWifiClient(
        ssid: String, encryptionType: String, key: String, ip: String, mask: String
    ) = sendCommand(
        MotocamAPIHelper.setWifiClientCmd(ssid, encryptionType, key, ip, mask),
        MotocamAPIHelper::setWifiClientCmdResponseParse
    )

    suspend fun getWifiState() = sendCommand(
        MotocamAPIHelper.getWifiStateCmd(),
        MotocamAPIHelper::getWifiStateCmdResponseParse
    )

    suspend fun getWifiHotspotConfig() = sendCommand(
        MotocamAPIHelper.getWifiHotspotCmd(),
        MotocamAPIHelper::getWifiHotspotCmdResponseParse
    )

    suspend fun getWifiClientConfig() = sendCommand(
        MotocamAPIHelper.getWifiClientCmd(),
        MotocamAPIHelper::getWifiClientCmdResponseParse
    )

    suspend fun getEthernetConfig() = sendCommand(
        MotocamAPIHelper.getEthernetCmd(),
        MotocamAPIHelper::getEthernetCmdResponseParse
    )

    suspend fun setDefaultToFactory() = sendCommand(
        MotocamAPIHelper.setDefaultToFactoryCmd(),
        MotocamAPIHelper::setDefaultToFactoryCmdResponseParse
    )

    suspend fun setDefaultToCurrent() = sendCommand(
        MotocamAPIHelper.setDefaultToCurrentCmd(),
        MotocamAPIHelper::setDefaultToCurrentCmdResponseParse
    )

    suspend fun setCurrentToFactory() = sendCommand(
        MotocamAPIHelper.setCurrentToFactoryCmd(),
        MotocamAPIHelper::setCurrentToFactoryCmdResponseParse
    )

    suspend fun setCurrentToDefault() = sendCommand(
        MotocamAPIHelper.setCurrentToDefaultCmd(),
        MotocamAPIHelper::setCurrentToDefaultCmdResponseParse
    )

    suspend fun shutdownCamera() = sendCommand(
        MotocamAPIHelper.shutdownCmd(),
        MotocamAPIHelper::shutdownCmdResponseParse
    )

    suspend fun configReset(date: String) = sendCommand(
        MotocamAPIHelper.configResetCmd(date),
        MotocamAPIHelper::configResetCmdResponseParse
    )

    suspend fun setTime(epochTime: Long, cameraIp: String? = null) = sendCommand(
        MotocamAPIHelper.setTimeCmd(epochTime),
        MotocamAPIHelper::setTimeCmdResponseParse,
        cameraIp
    )

    suspend fun setUserDob(dob: String) = sendCommand(
        MotocamAPIHelper.setUserDobCmd(dob),
        MotocamAPIHelper::setUserDobCmdResponseParse
    )
    
    suspend fun resetLoginPin(pin: String, dob: String) = sendCommand(
        MotocamAPIHelper.resetLoginPinCmd(pin, dob),
        MotocamAPIHelper::resetLoginPinCmdResponseParse
    )
    
    /**
     * Reset login PIN on specific camera (for viewer flow)
     */
    suspend fun resetLoginPin(pin: String, dob: String, cameraIp: String) = sendCommand(
        MotocamAPIHelper.resetLoginPinCmd(pin, dob),
        MotocamAPIHelper::resetLoginPinCmdResponseParse,
        cameraIp
    )

    suspend fun startStream() = sendCommand(
        MotocamAPIHelper.startStreamCmd(),
        MotocamAPIHelper::startStreamCmdResponseParse
    )

    suspend fun stopStream() = sendCommand(
        MotocamAPIHelper.stopStreamCmd(),
        MotocamAPIHelper::stopStreamCmdResponseParse
    )

    fun uploadFile(fileName: String, input: InputStream) {
        if (deviceIpAddress.isNotBlank()) {
            val ftpUploader = FTPUploader(deviceIpAddress, "root", "ota")
            ftpUploader.uploadFile(input, fileName, "")
            ftpUploader.disconnect()
            Log.i(TAG, "Upload complete: $fileName")
        }
    }

    suspend fun getHealthStatus(): HealthStatus = sendCommand(
        MotocamAPIHelper.getHealthCheckCmd(),
        MotocamAPIHelper::parseHealthCheckResponse
    )

    /**
     * Health check on a specific camera IP (used by auto-reconnect flow).
     */
    suspend fun getHealthStatus(cameraIp: String): HealthStatus = sendCommand(
        MotocamAPIHelper.getHealthCheckCmd(),
        MotocamAPIHelper::parseHealthCheckResponse,
        cameraIp
    )

    suspend fun getStreamConfiguration(): StreamConfiguration = sendCommand(
        MotocamAPIHelper.getStreamConfigurationCmd(),
        MotocamAPIHelper::parseStreamConfigurationResponse
    )

    suspend fun getFirmwareVersion(): String = sendCommand(
        MotocamAPIHelper.getFirmwareCmd(),
        MotocamAPIHelper::parseFirmwareResponse
    )

    suspend fun setOtaUpdate(): Boolean = sendCommand(
        MotocamAPIHelper.setOtaUpdateCmd(),
        MotocamAPIHelper::parseOtaUpdateResponse
    )

    suspend fun getOtaUpdate(): String = sendCommand(
        MotocamAPIHelper.getOtaUpdateCmd(),
        MotocamAPIHelper::parseGetOtaUpdateResponse
    )

    suspend fun getDeviceMode(): String = sendCommand(
        MotocamAPIHelper.getDeviceModeCmd(),
        MotocamAPIHelper::parseDeviceModeResponse
    )


    // Viewer Flow specific functions with camera IP parameter
    
    /**
     * Start stream on specific camera (for viewer flow)
     */
    suspend fun startStream(cameraIp: String) = sendCommand(
        MotocamAPIHelper.startStreamCmd(),
        MotocamAPIHelper::startStreamCmdResponseParse,
        cameraIp
    )
    
    /**
     * Stop stream on specific camera (for viewer flow)
     */
    suspend fun stopStream(cameraIp: String) = sendCommand(
        MotocamAPIHelper.stopStreamCmd(),
        MotocamAPIHelper::stopStreamCmdResponseParse,
        cameraIp
    )
    
    /**
     * Get current config from specific camera (for viewer flow)
     */
    suspend fun getCurrentConfig(cameraIp: String) = sendCommand(
        MotocamAPIHelper.getCurrentConfigCmd(),
        MotocamAPIHelper::getCurrentConfigCmdResponseParse,
        cameraIp
    )
    
    /**
     * Set device IP address for API calls
     */
    fun setDeviceIpAddress(ipAddress: String) {
        deviceIpAddress = ipAddress
        Log.i(TAG, "Device IP address set to: $ipAddress")
    }
    
    /**
     * Get current device IP address
     */
    fun getDeviceIpAddress(): String = deviceIpAddress

}
