package com.wac.wacdiscovery.mdns

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import com.wac.wacdiscovery.DiscoveredDevice
import com.wac.wacdiscovery.DiscoveryProtocol
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlin.time.Duration

internal actual class MdnsDiscoveryEngine actual constructor() {

    private var nsdManager: NsdManager? = null
    private val activeListeners = mutableListOf<NsdManager.DiscoveryListener>()

    actual fun discover(serviceTypes: List<String>, timeout: Duration): Flow<DiscoveredDevice> = callbackFlow {
        val seenDevices = mutableSetOf<String>()

        val context = MdnsContextHolder.appContext
        if (context == null) {
            channel.close()
            return@callbackFlow
        }

        nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager

        for (serviceType in serviceTypes) {
            val listener = object : NsdManager.DiscoveryListener {
                override fun onDiscoveryStarted(regType: String) { }

                override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                    nsdManager?.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                        override fun onResolveFailed(si: NsdServiceInfo, errorCode: Int) { }

                        override fun onServiceResolved(si: NsdServiceInfo) {
                            val address = si.host?.hostAddress ?: return
                            val props = mutableMapOf<String, String>()
                            props["SERVICE_TYPE"] = si.serviceType
                            props["SERVICE_NAME"] = si.serviceName
                            si.attributes?.forEach { (key, value) ->
                                props[key] = value?.decodeToString() ?: ""
                            }

                            val rawData = buildString {
                                appendLine("SERVICE: ${si.serviceName}")
                                appendLine("TYPE: ${si.serviceType}")
                                appendLine("HOST: ${si.host}")
                                appendLine("PORT: ${si.port}")
                                props.forEach { (k, v) -> appendLine("$k=$v") }
                            }

                            val device = DiscoveredDevice(
                                name = si.serviceName,
                                address = address,
                                port = si.port,
                                protocol = DiscoveryProtocol.MDNS,
                                properties = props.toMap(),
                                rawData = rawData,
                            )
                            if (seenDevices.add(device.uniqueKey)) {
                                trySend(device)
                            }
                        }
                    })
                }

                override fun onServiceLost(serviceInfo: NsdServiceInfo) { }
                override fun onDiscoveryStopped(serviceType: String) { }
                override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) { }
                override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) { }
            }

            activeListeners.add(listener)
            val type = if (serviceType.startsWith("_")) "$serviceType." else serviceType
            nsdManager?.discoverServices(type, NsdManager.PROTOCOL_DNS_SD, listener)
        }

        launch {
            delay(timeout)
            channel.close()
        }

        awaitClose {
            for (listener in activeListeners) {
                try { nsdManager?.stopServiceDiscovery(listener) } catch (_: Exception) { }
            }
            activeListeners.clear()
        }
    }.flowOn(Dispatchers.IO)

    actual fun close() {
        for (listener in activeListeners) {
            try { nsdManager?.stopServiceDiscovery(listener) } catch (_: Exception) { }
        }
        activeListeners.clear()
    }
}

/**
 * Holds Android application context for mDNS discovery.
 *
 * Initialize in your Application class or Activity:
 * ```kotlin
 * MdnsContextHolder.init(applicationContext)
 * ```
 */
object MdnsContextHolder {
    internal var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }
}
