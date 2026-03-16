package com.wac.wacdiscovery.example

import com.wac.wacdiscovery.DeviceFilters
import com.wac.wacdiscovery.DiscoveryConfig
import com.wac.wacdiscovery.DiscoveryProtocol
import com.wac.wacdiscovery.MdnsServiceTypes
import com.wac.wacdiscovery.NetworkDiscovery
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.seconds

fun main() = runBlocking {
    val discovery = NetworkDiscovery()

    println("=" .repeat(70))
    println("  KMM Discovery — Scanning with Device Info Resolution")
    println("=" .repeat(70))

    // ── Scan: All devices with rich device info ──────────────────────
    println("\n🔍 Discovering all devices with UPnP info resolution — 15s\n")
    var count = 0
    discovery.discover(
        DiscoveryConfig(
            timeout = 15.seconds,
            resolveDeviceInfo = true,
            mdnsServiceTypes = listOf(
                MdnsServiceTypes.SAMSUNG_SMART_VIEW,
                MdnsServiceTypes.GOOGLE_CAST,
                MdnsServiceTypes.HTTP,
            ),
        ),
    ).collect { device ->
        count++
        val proto = if (device.protocol == DiscoveryProtocol.SSDP) "SSDP" else "mDNS"
        println("  [$proto] #$count ${device.name}")
        println("         address:  ${device.address}:${device.port}")

        device.deviceInfo?.let { info ->
            if (info.friendlyName.isNotEmpty())
                println("         📺 name:  ${info.friendlyName}")
            if (info.manufacturer.isNotEmpty())
                println("         🏭 maker: ${info.manufacturer}")
            if (info.modelName.isNotEmpty())
                println("         📱 model: ${info.modelName} ${info.modelNumber}")
            if (info.serialNumber.isNotEmpty())
                println("         🔑 serial: ${info.serialNumber}")
            if (info.macAddress.isNotEmpty())
                println("         🔗 MAC:   ${info.macAddress}")
            if (info.iconUrl.isNotEmpty())
                println("         🖼️  icon:  ${info.iconUrl}")
            if (info.deviceType.isNotEmpty())
                println("         📦 type:  ${info.deviceType}")
        } ?: println("         ℹ️  (no UPnP device info available)")

        println()
    }
    println("📊 Total devices found: $count")

    discovery.close()
    println("\n✅ Done!")
}
