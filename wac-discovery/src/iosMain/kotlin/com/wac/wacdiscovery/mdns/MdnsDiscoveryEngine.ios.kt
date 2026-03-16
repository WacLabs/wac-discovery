@file:OptIn(ExperimentalForeignApi::class)

package com.wac.wacdiscovery.mdns

import com.wac.wacdiscovery.DiscoveredDevice
import com.wac.wacdiscovery.DiscoveryProtocol
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import platform.Foundation.NSData
import platform.Foundation.NSNetService
import platform.Foundation.NSNetServiceBrowser
import platform.Foundation.NSNetServiceBrowserDelegateProtocol
import platform.Foundation.NSNetServiceDelegateProtocol
import platform.Foundation.NSRunLoop
import platform.Foundation.NSRunLoopCommonModes
import platform.Foundation.NSRunLoop.Companion.mainRunLoop
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.darwin.NSObject
import kotlin.time.Duration

internal actual class MdnsDiscoveryEngine actual constructor() {

    private val browsers = mutableListOf<NSNetServiceBrowser>()
    private val pendingServices = mutableListOf<NSNetService>()
    private val browserDelegates = mutableListOf<NSNetServiceBrowserDelegateProtocol>()
    private val serviceDelegates = mutableMapOf<String, NSNetServiceDelegateProtocol>()

    actual fun discover(serviceTypes: List<String>, timeout: Duration): Flow<DiscoveredDevice> = callbackFlow {
        val seenDevices = mutableSetOf<String>()

        for (serviceType in serviceTypes) {
            val type = if (serviceType.endsWith(".")) serviceType
                else "$serviceType."

            val browser = NSNetServiceBrowser()

            val findCallback: (NSNetServiceBrowser, NSNetService, Boolean) -> Unit = { _, service, _ ->
                pendingServices.add(service)
                val serviceDelegate = object : NSObject(), NSNetServiceDelegateProtocol {
                    override fun netServiceDidResolveAddress(sender: NSNetService) {
                        val addresses = sender.addresses ?: return
                        if (addresses.isEmpty()) return

                        val props = mutableMapOf<String, String>()
                        props["SERVICE_TYPE"] = sender.type
                        props["SERVICE_NAME"] = sender.name

                        sender.TXTRecordData()?.let { txtData ->
                            val dict = NSNetService.dictionaryFromTXTRecordData(txtData)
                            dict.forEach { (key, value) ->
                                val keyStr = key as? String ?: return@forEach
                                val valueData = value as? NSData
                                val valueStr = if (valueData != null) {
                                    NSString.create(
                                        data = valueData,
                                        encoding = NSUTF8StringEncoding,
                                    )?.toString() ?: ""
                                } else ""
                                props[keyStr] = valueStr
                            }
                        }

                        val rawData = buildString {
                            appendLine("SERVICE: ${sender.name}")
                            appendLine("TYPE: ${sender.type}")
                            appendLine("PORT: ${sender.port}")
                            props.forEach { (k, v) -> appendLine("$k=$v") }
                        }

                        val hostName = sender.hostName?.removeSuffix(".") ?: return
                        val device = DiscoveredDevice(
                            name = sender.name,
                            address = hostName,
                            port = sender.port.toInt(),
                            protocol = DiscoveryProtocol.MDNS,
                            properties = props.toMap(),
                            rawData = rawData,
                        )
                        if (seenDevices.add(device.uniqueKey)) {
                            trySend(device)
                        }
                        pendingServices.remove(sender)
                        serviceDelegates.remove(serviceKey(sender))
                    }

                    override fun netService(
                        sender: NSNetService,
                        didNotResolve: Map<Any?, *>,
                    ) {
                        pendingServices.remove(sender)
                        serviceDelegates.remove(serviceKey(sender))
                    }
                }
                serviceDelegates[serviceKey(service)] = serviceDelegate
                service.delegate = serviceDelegate
                service.resolveWithTimeout(5.0)
            }

            val browserDelegate = object : NSObject(), NSNetServiceBrowserDelegateProtocol {
                override fun netServiceBrowser(
                    browser: NSNetServiceBrowser,
                    didFindService: NSNetService,
                    moreComing: Boolean,
                ) {
                    findCallback(browser, didFindService, moreComing)
                }

                override fun netServiceBrowserDidStopSearch(browser: NSNetServiceBrowser) { }

                override fun netServiceBrowser(
                    browser: NSNetServiceBrowser,
                    didNotSearch: Map<Any?, *>,
                ) { }
            }

            browserDelegates.add(browserDelegate)
            browser.delegate = browserDelegate
            browser.scheduleInRunLoop(mainRunLoop, forMode = NSRunLoopCommonModes)
            browser.searchForServicesOfType(type, inDomain = "local.")
            browsers.add(browser)
        }

        launch {
            delay(timeout)
            channel.close()
        }

        awaitClose {
            for (b in browsers) {
                b.stop()
                b.delegate = null
            }
            browsers.clear()
            pendingServices.clear()
            browserDelegates.clear()
            serviceDelegates.clear()
        }
    }

    actual fun close() {
        for (b in browsers) {
            b.stop()
            b.delegate = null
        }
        browsers.clear()
        pendingServices.clear()
        browserDelegates.clear()
        serviceDelegates.clear()
    }

    private fun serviceKey(service: NSNetService): String = "${service.type}|${service.name}"
}
