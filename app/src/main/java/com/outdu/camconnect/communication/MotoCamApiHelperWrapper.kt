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

    private suspend fun <T> withSocketClient(block: suspend (MotocamSocketClient) -> T): T {
        val client = MotocamSocketClient()
        return try {
            client.init()
            block(client)
        } finally {
            client.destroy()
        }
    }

    private suspend fun <T> sendCommand(
        reqCmd: IntArray,
        parse: (IntArray, Int) -> T
    ): T = withSocketClient { client ->
        val res = IntArray(MAX_BYTES)
        val len = client.sendCmd(reqCmd, res)
        parse(res, len)
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

}
