package com.wac.wacdiscovery

/**
 * Protocol used for device discovery.
 */
enum class DiscoveryProtocol {
    /** Simple Service Discovery Protocol (UPnP, port 1900) */
    SSDP,
    /** Multicast DNS / Bonjour (Zeroconf, port 5353) */
    MDNS,
}
