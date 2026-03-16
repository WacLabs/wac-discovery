@file:OptIn(ExperimentalForeignApi::class)

package com.wac.wacdiscovery.ssdp

import com.wac.wacdiscovery.DiscoveredDevice
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.cValue
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readValue
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import platform.darwin.inet_addr
import platform.darwin.inet_ntoa
import platform.posix.AF_INET
import platform.posix.INADDR_ANY
import platform.posix.IPPROTO_IP
import platform.posix.IPPROTO_UDP
import platform.posix.IP_ADD_MEMBERSHIP
import platform.posix.IP_MULTICAST_LOOP
import platform.posix.SOCK_DGRAM
import platform.posix.SOL_SOCKET
import platform.posix.SO_RCVTIMEO
import platform.posix.SO_REUSEADDR
import platform.posix.close
import platform.posix.in_addr
import platform.posix.int32_tVar
import platform.posix.ip_mreq
import platform.posix.recvfrom
import platform.posix.sendto
import platform.posix.setsockopt
import platform.posix.sockaddr_in
import platform.posix.socklen_tVar
import platform.posix.socket
import platform.posix.timeval
import kotlin.time.Duration

/** Convert host-order UShort to network-order (big-endian). */
private fun htons(value: UShort): UShort {
    val v = value.toInt()
    return (((v and 0xFF) shl 8) or ((v shr 8) and 0xFF)).toUShort()
}

internal actual class SsdpDiscoveryEngine actual constructor() {

    actual fun discover(config: SsdpConfig, timeout: Duration): Flow<DiscoveredDevice> = callbackFlow {
        val seenDevices = mutableSetOf<String>()

        memScoped {
            val sock = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP)
            if (sock < 0) {
                channel.close()
                return@callbackFlow
            }

            // Reuse address
            val reuseVal = alloc<int32_tVar>()
            reuseVal.value = 1
            setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, reuseVal.ptr,
                sizeOf<int32_tVar>().convert())

            // Receive timeout
            val tv = alloc<timeval>()
            tv.tv_sec = 2
            tv.tv_usec = 0
            setsockopt(sock, SOL_SOCKET, SO_RCVTIMEO, tv.ptr,
                sizeOf<timeval>().convert())

            // Enable multicast loopback
            val loopVal = alloc<int32_tVar>()
            loopVal.value = 1
            setsockopt(sock, IPPROTO_IP, IP_MULTICAST_LOOP, loopVal.ptr,
                sizeOf<int32_tVar>().convert())

            // Bind to any address
            val bindAddr = alloc<sockaddr_in>()
            bindAddr.sin_family = AF_INET.convert()
            bindAddr.sin_port = htons(0u)
            bindAddr.sin_addr.s_addr = INADDR_ANY
            platform.posix.bind(sock, bindAddr.ptr.reinterpret(),
                sizeOf<sockaddr_in>().convert())

            // Join multicast group
            val mreq = alloc<ip_mreq>()
            mreq.imr_multiaddr.s_addr = inet_addr(config.multicastAddress)
            mreq.imr_interface.s_addr = INADDR_ANY
            setsockopt(sock, IPPROTO_IP, IP_ADD_MEMBERSHIP, mreq.ptr,
                sizeOf<ip_mreq>().convert())

            // Build and send M-SEARCH request
            val request = SsdpMessage.buildMSearchRequest(config)
            val requestBytes = request.encodeToByteArray()
            val destAddr = alloc<sockaddr_in>()
            destAddr.sin_family = AF_INET.convert()
            destAddr.sin_port = htons(config.port.toUShort())
            destAddr.sin_addr.s_addr = inet_addr(config.multicastAddress)

            requestBytes.usePinned { pinned ->
                sendto(sock, pinned.addressOf(0), requestBytes.size.convert(), 0,
                    destAddr.ptr.reinterpret(), sizeOf<sockaddr_in>().convert())
            }

            // Receive loop
            val recvJob = launch {
                val buffer = ByteArray(4096)
                while (isActive) {
                    buffer.usePinned { pinned ->
                        val srcAddr = alloc<sockaddr_in>()
                        val addrLen = alloc<socklen_tVar>()
                        addrLen.value = sizeOf<sockaddr_in>().convert()

                        val bytesRead = recvfrom(sock, pinned.addressOf(0),
                            buffer.size.convert(), 0,
                            srcAddr.ptr.reinterpret(), addrLen.ptr)

                        if (bytesRead > 0) {
                            val responseStr = pinned.get().decodeToString(0, bytesRead.toInt())
                            val addrCValue = srcAddr.sin_addr.readValue()
                            val addrPtr = inet_ntoa(addrCValue)
                            val address = addrPtr?.toKString() ?: "unknown"
                            val device = SsdpMessage.parseResponse(responseStr, address)
                            if (device != null && seenDevices.add(device.uniqueKey)) {
                                trySend(device)
                            }
                        }
                    }
                }
            }

            // Timeout
            launch {
                delay(timeout)
                channel.close()
            }

            awaitClose {
                recvJob.cancel()
                close(sock)
            }
        }
    }.flowOn(Dispatchers.IO)

    actual fun close() { }
}
