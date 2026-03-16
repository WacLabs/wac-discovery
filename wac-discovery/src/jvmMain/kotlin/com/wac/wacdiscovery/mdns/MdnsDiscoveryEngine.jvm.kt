package com.wac.wacdiscovery.mdns

import com.wac.wacdiscovery.DiscoveredDevice
import com.wac.wacdiscovery.DiscoveryProtocol
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.net.NetworkInterface
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceListener
import kotlin.time.Duration

internal actual class MdnsDiscoveryEngine actual constructor() {

    private val jmdnsInstances = mutableListOf<JmDNS>()

    actual fun discover(serviceTypes: List<String>, timeout: Duration): Flow<DiscoveredDevice> = callbackFlow {
        val seenDevices = mutableSetOf<String>()

        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val ni = interfaces.nextElement()
            if (!ni.isUp || ni.isLoopback) continue
            for (addr in ni.inetAddresses.toList()) {
                if (addr.isLoopbackAddress || addr !is java.net.Inet4Address) continue
                try {
                    val jmdns = JmDNS.create(addr, addr.hostAddress)
                    jmdnsInstances.add(jmdns)

                    val listener = object : ServiceListener {
                        override fun serviceAdded(event: ServiceEvent) {
                            jmdns.requestServiceInfo(event.type, event.name, 3000)
                        }

                        override fun serviceResolved(event: ServiceEvent) {
                            val info = event.info ?: return
                            val addresses = info.inet4Addresses
                            if (addresses.isEmpty()) return

                            val props = mutableMapOf<String, String>()
                            val propEnum = info.propertyNames
                            while (propEnum.hasMoreElements()) {
                                val propName = propEnum.nextElement()
                                val propValue = info.getPropertyString(propName)
                                if (propValue != null) props[propName] = propValue
                            }
                            props["SERVICE_TYPE"] = info.type
                            props["SERVICE_NAME"] = info.name

                            // Build raw data string from all properties
                            val rawData = buildString {
                                appendLine("SERVICE: ${info.qualifiedName}")
                                appendLine("TYPE: ${info.type}")
                                appendLine("PORT: ${info.port}")
                                props.forEach { (k, v) -> appendLine("$k=$v") }
                            }

                            for (address in addresses) {
                                val hostAddr = address.hostAddress ?: continue
                                val device = DiscoveredDevice(
                                    name = info.name,
                                    address = hostAddr,
                                    port = info.port,
                                    protocol = DiscoveryProtocol.MDNS,
                                    properties = props.toMap(),
                                    rawData = rawData,
                                )
                                if (seenDevices.add(device.uniqueKey)) {
                                    trySend(device)
                                }
                            }
                        }

                        override fun serviceRemoved(event: ServiceEvent) { }
                    }

                    for (serviceType in serviceTypes) {
                        val type = if (serviceType.endsWith(".local.")) serviceType
                            else "$serviceType.local."
                        jmdns.addServiceListener(type, listener)
                    }
                } catch (_: Exception) { }
            }
        }

        launch {
            delay(timeout)
            channel.close()
        }

        awaitClose {
            for (jmdns in jmdnsInstances) {
                try { jmdns.close() } catch (_: Exception) { }
            }
            jmdnsInstances.clear()
        }
    }.flowOn(Dispatchers.IO)

    actual fun close() {
        for (jmdns in jmdnsInstances) {
            try { jmdns.close() } catch (_: Exception) { }
        }
        jmdnsInstances.clear()
    }
}
