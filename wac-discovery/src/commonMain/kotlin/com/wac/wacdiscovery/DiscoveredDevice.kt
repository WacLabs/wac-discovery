package com.wac.wacdiscovery

/**
 * Represents a device discovered on the local network via SSDP or mDNS.
 *
 * @property name Human-readable device/service name.
 * @property address IP address of the device.
 * @property port Port number of the service.
 * @property protocol The discovery protocol that found this device.
 * @property properties Parsed metadata — SSDP headers or mDNS TXT records.
 * @property rawData Raw response data from the device for custom parsing.
 * @property deviceInfo Rich device info from UPnP description (null until resolved).
 */
data class DiscoveredDevice(
    val name: String,
    val address: String,
    val port: Int = 0,
    val protocol: DiscoveryProtocol,
    val properties: Map<String, String> = emptyMap(),
    val rawData: String = "",
    val deviceInfo: DeviceInfo? = null,
) {
    /** Unique key for deduplication: address + port combination. */
    val uniqueKey: String get() = "$address:$port"

    /**
     * Resolves rich device info by fetching the UPnP device description XML.
     *
     * For SSDP devices, fetches from the `LOCATION` header URL.
     * Returns a **new** [DiscoveredDevice] with [deviceInfo] populated.
     *
     * @param timeoutMs HTTP fetch timeout in milliseconds (default: 5000).
     *
     * ```kotlin
     * val resolved = device.resolveDeviceInfo()
     * println(resolved.deviceInfo?.friendlyName) // "Living Room TV"
     * ```
     */
    suspend fun resolveDeviceInfo(timeoutMs: Int = 5000): DiscoveredDevice {
        if (deviceInfo != null) return this

        val locationUrl = properties["LOCATION"] ?: return this
        val xml = HttpFetcher.fetch(locationUrl, timeoutMs)
        if (xml.isEmpty()) return this

        val baseUrl = locationUrl.substringBeforeLast("/")
        val info = DeviceDescriptionParser.parse(xml, baseUrl)
        return copy(deviceInfo = info)
    }
}
