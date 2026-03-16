package com.wac.wacdiscovery.ssdp

internal const val SSDP_MULTICAST_ADDRESS = "239.255.255.250"
internal const val SSDP_PORT = 1900

/**
 * Internal SSDP configuration derived from [com.wac.wacdiscovery.DiscoveryConfig].
 */
internal data class SsdpConfig(
    val searchTarget: String = "ssdp:all",
    val mx: Int = 3,
    val multicastAddress: String = SSDP_MULTICAST_ADDRESS,
    val port: Int = SSDP_PORT,
)
