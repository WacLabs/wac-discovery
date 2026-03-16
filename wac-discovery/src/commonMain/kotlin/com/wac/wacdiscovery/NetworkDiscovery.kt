package com.wac.wacdiscovery

import kotlinx.coroutines.flow.Flow

/**
 * Unified network device discovery API.
 *
 * Discovers devices on the local network using SSDP and/or mDNS protocols.
 * Results are emitted as a [Flow] of [DiscoveredDevice], with built-in
 * deduplication and optional post-filtering.
 *
 * Usage:
 * ```kotlin
 * val discovery = NetworkDiscovery()
 *
 * // Discover all devices
 * discovery.discover().collect { device ->
 *     println("${device.name} at ${device.address}:${device.port}")
 * }
 *
 * // Discover Samsung TVs only
 * discovery.discover(
 *     DiscoveryConfig(filter = DeviceFilters.SAMSUNG_TV)
 * ).collect { println(it.name) }
 *
 * // Custom filter
 * val filter = DeviceFilter.byKeyword("my-brand") or DeviceFilters.SAMSUNG
 * discovery.discover(
 *     DiscoveryConfig(filter = filter)
 * ).collect { println(it.name) }
 *
 * discovery.close()
 * ```
 */
expect class NetworkDiscovery() {
    /**
     * Starts discovery and emits matching devices as a [Flow].
     *
     * The flow completes after [DiscoveryConfig.timeout].
     * Duplicate devices (same address:port) are automatically filtered.
     *
     * @param config Discovery configuration with protocol, filter, and timeout settings.
     * @return A cold [Flow] of discovered [DiscoveredDevice]s.
     */
    fun discover(config: DiscoveryConfig = DiscoveryConfig()): Flow<DiscoveredDevice>

    /**
     * Releases resources held by internal discovery engines.
     */
    fun close()
}
