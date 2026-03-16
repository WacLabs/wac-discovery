package com.wac.wacdiscovery.ssdp

import com.wac.wacdiscovery.DiscoveredDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.MulticastSocket
import java.net.NetworkInterface
import java.net.SocketException
import java.net.SocketTimeoutException
import kotlin.time.Duration

internal actual class SsdpDiscoveryEngine actual constructor() {

    actual fun discover(config: SsdpConfig, timeout: Duration): Flow<DiscoveredDevice> = callbackFlow {
        val seenDevices = mutableSetOf<String>()
        val multicastGroup = InetAddress.getByName(config.multicastAddress)
        val request = SsdpMessage.buildMSearchRequest(config)
        val requestBytes = request.toByteArray(Charsets.UTF_8)

        val socket = DatagramSocket(null).apply {
            reuseAddress = true
            bind(InetSocketAddress(0))
            soTimeout = 2000
        }

        var multicastSocket: MulticastSocket? = null
        try {
            multicastSocket = MulticastSocket(config.port).apply {
                reuseAddress = true
                soTimeout = 2000
            }
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val ni = interfaces.nextElement()
                if (ni.isUp && !ni.isLoopback && ni.supportsMulticast()) {
                    try {
                        multicastSocket.joinGroup(
                            InetSocketAddress(multicastGroup, config.port), ni,
                        )
                    } catch (_: Exception) { }
                }
            }
        } catch (_: Exception) { }

        val packet = DatagramPacket(
            requestBytes, requestBytes.size, multicastGroup, config.port,
        )
        socket.send(packet)

        val unicastJob = launch {
            val buffer = ByteArray(4096)
            try {
                while (isActive) {
                    try {
                        val recv = DatagramPacket(buffer, buffer.size)
                        socket.receive(recv)
                        val response = String(recv.data, 0, recv.length, Charsets.UTF_8)
                        val address = recv.address.hostAddress ?: continue
                        val device = SsdpMessage.parseResponse(response, address) ?: continue
                        if (seenDevices.add(device.uniqueKey)) trySend(device)
                    } catch (_: SocketTimeoutException) { }
                }
            } catch (_: SocketException) { }
        }

        val multicastJob = multicastSocket?.let { ms ->
            launch {
                val buffer = ByteArray(4096)
                try {
                    while (isActive) {
                        try {
                            val recv = DatagramPacket(buffer, buffer.size)
                            ms.receive(recv)
                            val response = String(recv.data, 0, recv.length, Charsets.UTF_8)
                            val address = recv.address.hostAddress ?: continue
                            val device = SsdpMessage.parseResponse(response, address) ?: continue
                            if (seenDevices.add(device.uniqueKey)) trySend(device)
                        } catch (_: SocketTimeoutException) { }
                    }
                } catch (_: SocketException) { }
            }
        }

        launch {
            delay(timeout)
            channel.close()
        }

        awaitClose {
            unicastJob.cancel()
            multicastJob?.cancel()
            socket.close()
            multicastSocket?.close()
        }
    }.flowOn(Dispatchers.IO)

    actual fun close() { }
}
