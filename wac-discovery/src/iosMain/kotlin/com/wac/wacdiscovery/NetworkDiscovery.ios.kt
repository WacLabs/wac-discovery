package com.wac.wacdiscovery

import com.wac.wacdiscovery.mdns.MdnsDiscoveryEngine
import com.wac.wacdiscovery.ssdp.SsdpConfig
import com.wac.wacdiscovery.ssdp.SsdpDiscoveryEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

actual class NetworkDiscovery actual constructor() {

    private val ssdpEngine = SsdpDiscoveryEngine()
    private val mdnsEngine = MdnsDiscoveryEngine()

    actual fun discover(config: DiscoveryConfig): Flow<DiscoveredDevice> = channelFlow {
        val seen = mutableSetOf<String>()

        if (DiscoveryProtocol.SSDP in config.protocols) {
            launch {
                val ssdpConfig = SsdpConfig(
                    searchTarget = config.ssdpSearchTarget,
                    mx = config.ssdpMx,
                )
                ssdpEngine.discover(ssdpConfig, config.timeout).collect { device ->
                    if (seen.add(device.uniqueKey)) {
                        val resolved = if (config.resolveDeviceInfo) {
                            device.resolveDeviceInfo(config.resolveTimeout.inWholeMilliseconds.toInt())
                        } else device
                        send(resolved)
                    }
                }
            }
        }

        if (DiscoveryProtocol.MDNS in config.protocols) {
            launch {
                mdnsEngine.discover(config.mdnsServiceTypes, config.timeout).collect { device ->
                    if (seen.add(device.uniqueKey)) send(device)
                }
            }
        }
    }.filter { config.filter.matches(it) }

    actual fun close() {
        ssdpEngine.close()
        mdnsEngine.close()
    }
}
