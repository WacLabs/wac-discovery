package com.wac.wacdiscovery

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Configuration for network device discovery.
 *
 * @property protocols Which discovery protocols to use (default: both SSDP and mDNS).
 * @property timeout Total scan duration before the flow completes.
 * @property filter Post-discovery filter — only matching devices are emitted.
 * @property resolveDeviceInfo If true, auto-fetches UPnP device description for each device.
 * @property resolveTimeout Timeout for each device info HTTP fetch.
 * @property ssdpSearchTarget SSDP search target (ST header). See [SsdpSearchTargets].
 * @property ssdpMx Maximum wait time for SSDP responses (seconds).
 * @property mdnsServiceTypes mDNS service types to browse. See [MdnsServiceTypes].
 */
data class DiscoveryConfig(
    val protocols: Set<DiscoveryProtocol> = setOf(DiscoveryProtocol.SSDP, DiscoveryProtocol.MDNS),
    val timeout: Duration = 10.seconds,
    val filter: DeviceFilter = DeviceFilter.None,
    val resolveDeviceInfo: Boolean = false,
    val resolveTimeout: Duration = 5.seconds,
    val ssdpSearchTarget: String = SsdpSearchTargets.ALL,
    val ssdpMx: Int = 3,
    val mdnsServiceTypes: List<String> = listOf(MdnsServiceTypes.ALL),
)
