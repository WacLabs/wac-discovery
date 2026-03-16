package com.wac.wacdiscovery.ssdp

import com.wac.wacdiscovery.DiscoveredDevice
import com.wac.wacdiscovery.DiscoveryProtocol

/**
 * Utilities for building SSDP M-SEARCH requests and parsing responses.
 */
internal object SsdpMessage {

    fun buildMSearchRequest(config: SsdpConfig): String = buildString {
        append("M-SEARCH * HTTP/1.1\r\n")
        append("HOST: ${config.multicastAddress}:${config.port}\r\n")
        append("MAN: \"ssdp:discover\"\r\n")
        append("MX: ${config.mx}\r\n")
        append("ST: ${config.searchTarget}\r\n")
        append("\r\n")
    }

    fun parseResponse(response: String, address: String): DiscoveredDevice? {
        val headers = parseHeaders(response)
        if (headers.isEmpty()) return null

        val location = headers["LOCATION"] ?: return null
        val usn = headers["USN"] ?: return null

        // Extract port from location URL
        val port = extractPort(location)

        // Build a useful name from SERVER or USN
        val name = headers["SERVER"]
            ?: usn.substringBefore("::")
            ?: "Unknown SSDP Device"

        return DiscoveredDevice(
            name = name,
            address = address,
            port = port,
            protocol = DiscoveryProtocol.SSDP,
            properties = headers + mapOf(
                "USN" to usn,
                "LOCATION" to location,
            ),
            rawData = response,
        )
    }

    internal fun parseHeaders(response: String): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        response.lineSequence()
            .drop(1) // Skip status line
            .forEach { line ->
                val trimmed = line.trim()
                if (trimmed.isEmpty()) return@forEach
                val colonIndex = trimmed.indexOf(':')
                if (colonIndex > 0) {
                    val key = trimmed.substring(0, colonIndex).trim().uppercase()
                    val value = trimmed.substring(colonIndex + 1).trim()
                    headers[key] = value
                }
            }
        return headers
    }

    private fun extractPort(location: String): Int {
        return try {
            // location = "http://192.168.50.195:8001/api/v2/"
            val afterScheme = location.substringAfter("://")
            val hostPort = afterScheme.substringBefore("/")
            val portStr = hostPort.substringAfter(":", "0")
            portStr.toIntOrNull() ?: 0
        } catch (_: Exception) {
            0
        }
    }
}
