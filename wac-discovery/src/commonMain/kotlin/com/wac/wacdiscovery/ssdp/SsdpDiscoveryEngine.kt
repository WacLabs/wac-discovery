package com.wac.wacdiscovery.ssdp

import com.wac.wacdiscovery.DiscoveredDevice
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

/**
 * Internal SSDP discovery engine with platform-specific implementations.
 */
internal expect class SsdpDiscoveryEngine() {
    fun discover(config: SsdpConfig, timeout: Duration): Flow<DiscoveredDevice>
    fun close()
}
