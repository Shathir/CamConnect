package com.outdu.camconnect.services

import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.UUID
import java.util.regex.Pattern

data class OnvifDevice(
    val ipAddress: String,
    val endpointUrls: List<String>,
    val deviceType: String? = null,
    val scopes: List<String> = emptyList()
)

fun discoverOnvifDevices(callback: ((List<OnvifDevice>) -> Unit)? = null) {
    val multicastAddr = InetAddress.getByName("239.255.255.250")
    val port = 3702
    val messageId = "uuid:${UUID.randomUUID()}"

    val probe = """
        <?xml version="1.0" encoding="utf-8"?>
        <e:Envelope xmlns:e="http://www.w3.org/2003/05/soap-envelope"
                    xmlns:w="http://schemas.xmlsoap.org/ws/2004/08/addressing"
                    xmlns:d="http://schemas.xmlsoap.org/ws/2005/04/discovery">
          <e:Header>
            <w:MessageID>$messageId</w:MessageID>
            <w:To>urn:schemas-xmlsoap-org:ws:2005:04:discovery</w:To>
            <w:Action>http://schemas.xmlsoap.org/ws/2005/04/discovery/Probe</w:Action>
          </e:Header>
          <e:Body>
            <d:Probe/>
          </e:Body>
        </e:Envelope>
    """.trimIndent().toByteArray()

    Thread {
        val discoveredDevices = mutableListOf<OnvifDevice>()
        val discoveredIPs = mutableSetOf<String>() // To avoid duplicates
        val allResponses = mutableListOf<String>() // Track all responses for debugging
        
        try {
            DatagramSocket().use { socket ->
                socket.soTimeout = 3000 // Increased timeout to 10 seconds
                socket.reuseAddress = true
                
                // Get local network info for debugging
                val localAddress = socket.localAddress
                Log.d("OnvifService", "🌐 Local address: ${localAddress.hostAddress}")
                Log.d("OnvifService", "🔌 Local port: ${socket.localPort}")
                
                // Send probe
                val packet = DatagramPacket(probe, probe.size, multicastAddr, port)
                socket.send(packet)


                // Receive responses
                val buf = ByteArray(8192)
                var responseCount = 0
                try {
                    while (true) {
                        val resp = DatagramPacket(buf, buf.size)
                        socket.receive(resp)
                        responseCount++
                        val responseXml = String(resp.data, 0, resp.length)
                        val deviceIP = resp.address.hostAddress
                        val devicePort = resp.port
                        
                        allResponses.add("$deviceIP:$devicePort")
                        
                        Log.d("OnvifService", "📥 Response #$responseCount from $deviceIP:$devicePort")
                        
                        // Parse the response to extract endpoint URLs
                        val device = parseOnvifResponse(deviceIP, responseXml)
                        if (device != null && !discoveredIPs.contains(deviceIP)) {
                            discoveredDevices.add(device)
                            discoveredIPs.add(deviceIP)
                            
                            Log.d("OnvifService", "✅ ONVIF Device discovered:")
                            Log.d("OnvifService", "   📍 IP Address: ${device.ipAddress}")
                            Log.d("OnvifService", "   🔗 Endpoints: ${device.endpointUrls}")
                            Log.d("OnvifService", "   📱 Device Type: ${device.deviceType ?: "Unknown"}")
                            Log.d("OnvifService", "   🏷️ Scopes: ${device.scopes}")
                            

                        }
                    }
                } catch (e: Exception) {
                    Log.d("OnvifService", "⏹️ Discovery finished: ${e.message}")
                    Log.d("OnvifService", "📊 Total responses received: $responseCount")
                    Log.d("OnvifService", "📊 Total devices discovered: ${discoveredDevices.size}")
                    Log.d("OnvifService", "📋 All responding IPs: ${allResponses.joinToString(", ")}")
                    

                    
                    // Log summary of all discovered devices
                    if (discoveredDevices.isNotEmpty()) {
                        Log.d("OnvifService", "🎯 DISCOVERY SUMMARY:")
                        discoveredDevices.forEachIndexed { index, device ->
                            Log.d("OnvifService", "  Device ${index + 1}:")
                            Log.d("OnvifService", "    IP: ${device.ipAddress}")
                            Log.d("OnvifService", "    Endpoints: ${device.endpointUrls.joinToString(", ")}")
                            Log.d("OnvifService", "    Type: ${device.deviceType ?: "Unknown"}")
                        }
                    } else {
                        Log.d("OnvifService", "❌ No ONVIF devices found on the network")
                    }
                    
                    // Call the callback with discovered devices
                    callback?.invoke(discoveredDevices)
                }
            }
        } catch (e: Exception) {
            Log.e("OnvifService", "💥 Discovery failed with error: ${e.message}")
            Log.e("OnvifService", "Stack trace: ", e)
            callback?.invoke(emptyList())
        }
    }.start()
}

private fun parseOnvifResponse(ipAddress: String, xml: String): OnvifDevice? {
    try {
        // Extract XAddrs (endpoint URLs)
        val xAddrsPattern = Pattern.compile("<d:XAddrs>(.*?)</d:XAddrs>", Pattern.DOTALL)
        val xAddrsMatcher = xAddrsPattern.matcher(xml)
        val endpointUrls = mutableListOf<String>()
        
        if (xAddrsMatcher.find()) {
            val xAddrsContent = xAddrsMatcher.group(1)?.trim()
            xAddrsContent?.split("\\s+".toRegex())?.forEach { url ->
                if (url.isNotBlank() && (url.startsWith("http://") || url.startsWith("https://"))) {
                    endpointUrls.add(url.trim())
                }
            }
        }
        
        // Extract device types
        val typesPattern = Pattern.compile("<d:Types>(.*?)</d:Types>", Pattern.DOTALL)
        val typesMatcher = typesPattern.matcher(xml)
        var deviceType: String? = null
        
        if (typesMatcher.find()) {
            deviceType = typesMatcher.group(1)?.trim()
        }
        
        // Extract scopes
        val scopesPattern = Pattern.compile("<d:Scopes>(.*?)</d:Scopes>", Pattern.DOTALL)
        val scopesMatcher = scopesPattern.matcher(xml)
        val scopes = mutableListOf<String>()
        
        if (scopesMatcher.find()) {
            val scopesContent = scopesMatcher.group(1)?.trim()
            scopesContent?.split("\\s+".toRegex())?.forEach { scope ->
                if (scope.isNotBlank()) {
                    scopes.add(scope.trim())
                }
            }
        }
        
        // Return device only if we found at least one endpoint URL
        return if (endpointUrls.isNotEmpty()) {
            OnvifDevice(ipAddress, endpointUrls, deviceType, scopes)
        } else {
            Log.w("OnvifService", "⚠️ No endpoint URLs found for device at $ipAddress")
            null
        }
        
    } catch (e: Exception) {
        Log.e("OnvifService", "❌ Error parsing ONVIF response from $ipAddress: ${e.message}")
        return null
    }
}
