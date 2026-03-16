package com.wac.wacdiscovery.mdns

import com.wac.wacdiscovery.DiscoveredDevice
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

/**
 * Internal mDNS discovery engine with platform-specific implementations.
 */
internal expect class MdnsDiscoveryEngine() {
    fun discover(serviceTypes: List<String>, timeout: Duration): Flow<DiscoveredDevice>
    fun close()
}
