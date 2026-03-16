package com.wac.sampleapp

import com.wac.wacdiscovery.DeviceInfo
import com.wac.wacdiscovery.DiscoveredDevice
import com.wac.wacdiscovery.DiscoveryConfig
import com.wac.wacdiscovery.DiscoveryProtocol
import com.wac.wacdiscovery.NetworkDiscovery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

data class AppState(
    val devices: List<DiscoveredDevice> = emptyList(),
    val searchQuery: String = "",
    val isScanning: Boolean = false,
    val selectedDevice: DiscoveredDevice? = null,
    val useMockData: Boolean = true,
)

val AppState.filteredDevices: List<DiscoveredDevice>
    get() {
        if (searchQuery.isBlank()) return devices
        val q = searchQuery.lowercase()
        return devices.filter { device ->
            device.name.lowercase().contains(q) ||
                device.address.lowercase().contains(q) ||
                (device.deviceInfo?.friendlyName?.lowercase()?.contains(q) == true) ||
                (device.deviceInfo?.manufacturer?.lowercase()?.contains(q) == true)
        }
    }

class DiscoveryViewModel(private val scope: CoroutineScope) {

    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    private var discovery: NetworkDiscovery? = null
    private var scanJob: Job? = null

    init {
        loadMockData()
    }

    fun onSearchChanged(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    fun selectDevice(device: DiscoveredDevice?) {
        _state.value = _state.value.copy(selectedDevice = device)
    }

    fun toggleMockData() {
        val newUseMock = !_state.value.useMockData
        _state.value = _state.value.copy(useMockData = newUseMock, devices = emptyList())
        if (newUseMock) loadMockData()
    }

    fun startScan() {
        if (_state.value.isScanning) return

        if (_state.value.useMockData) {
            loadMockData()
            return
        }

        scanJob?.cancel()
        discovery?.close()
        discovery = NetworkDiscovery()

        _state.value = _state.value.copy(isScanning = true, devices = emptyList())

        scanJob = scope.launch {
            try {
                discovery!!.discover(
                    DiscoveryConfig(
                        timeout = 12.seconds,
                        resolveDeviceInfo = true,
                        resolveTimeout = 3.seconds,
                    )
                ).collect { device ->
                    val current = _state.value.devices
                    _state.value = _state.value.copy(devices = current + device)
                }
            } finally {
                _state.value = _state.value.copy(isScanning = false)
                discovery?.close()
            }
        }
    }

    fun stopScan() {
        scanJob?.cancel()
        _state.value = _state.value.copy(isScanning = false)
        discovery?.close()
    }

    private fun loadMockData() {
        _state.value = _state.value.copy(
            devices = createMockDevices(),
            isScanning = false,
        )
    }
}

private fun createMockDevices(): List<DiscoveredDevice> = listOf(
    DiscoveredDevice(
        name = "Samsung-Linux/4.1, UPnP/1.0",
        address = "192.168.50.190",
        port = 9197,
        protocol = DiscoveryProtocol.SSDP,
        properties = mapOf(
            "LOCATION" to "http://192.168.50.190:9197/dmr",
            "USN" to "uuid:SAMSUNG-TV-001::upnp:rootdevice",
            "ST" to "upnp:rootdevice",
        ),
        rawData = "HTTP/1.1 200 OK\r\nLOCATION: http://192.168.50.190:9197/dmr\r\nST: upnp:rootdevice",
        deviceInfo = DeviceInfo(
            friendlyName = "Living Room TV",
            manufacturer = "Samsung Electronics",
            manufacturerUrl = "http://www.samsung.com",
            modelName = "UN55TU7000",
            modelNumber = "AllShare1.0",
            serialNumber = "TEEK9QADGC2BV9W",
            macAddress = "AA:BB:CC:DD:EE:FF",
            iconUrl = "http://192.168.50.190:9197/icon_LRG.png",
            deviceType = "urn:schemas-upnp-org:device:MediaRenderer:1",
            presentationUrl = "http://192.168.50.190:8001",
            extraFields = mapOf("dlna:X_DLNADOC" to "DMR-1.50"),
        ),
    ),
    DiscoveredDevice(
        name = "Google-Cast/1.36",
        address = "192.168.50.42",
        port = 8008,
        protocol = DiscoveryProtocol.MDNS,
        properties = mapOf(
            "SERVICE_TYPE" to "_googlecast._tcp",
            "SERVICE_NAME" to "Chromecast-Ultra",
            "fn" to "Office Chromecast",
        ),
        rawData = "_googlecast._tcp Chromecast-Ultra 192.168.50.42:8008",
        deviceInfo = DeviceInfo(
            friendlyName = "Office Chromecast",
            manufacturer = "Google Inc.",
            modelName = "Chromecast Ultra",
        ),
    ),
    DiscoveredDevice(
        name = "Sonos/1.0",
        address = "192.168.50.88",
        port = 1400,
        protocol = DiscoveryProtocol.SSDP,
        properties = mapOf(
            "LOCATION" to "http://192.168.50.88:1400/xml/device_description.xml",
            "USN" to "uuid:SONOS-BEAM-001",
            "ST" to "urn:schemas-upnp-org:device:ZonePlayer:1",
        ),
        rawData = "HTTP/1.1 200 OK\r\nLOCATION: http://192.168.50.88:1400/xml/device_description.xml",
        deviceInfo = DeviceInfo(
            friendlyName = "Bedroom Speaker",
            manufacturer = "Sonos, Inc.",
            modelName = "Sonos Beam",
            modelNumber = "S14",
            serialNumber = "B8-E9-37-8A-5C-01",
        ),
    ),
    DiscoveredDevice(
        name = "LG-WebOS/5.0",
        address = "192.168.50.55",
        port = 3000,
        protocol = DiscoveryProtocol.SSDP,
        properties = mapOf(
            "LOCATION" to "http://192.168.50.55:3000/rootDesc.xml",
            "USN" to "uuid:LG-TV-001",
            "ST" to "upnp:rootdevice",
        ),
        rawData = "HTTP/1.1 200 OK\r\nLOCATION: http://192.168.50.55:3000/rootDesc.xml",
        deviceInfo = DeviceInfo(
            friendlyName = "Kitchen TV",
            manufacturer = "LG Electronics",
            modelName = "OLED55C1",
            modelNumber = "webOS 6.0",
            serialNumber = "305KXYZ1234",
            deviceType = "urn:schemas-upnp-org:device:MediaRenderer:1",
        ),
    ),
    DiscoveredDevice(
        name = "HP-Printer",
        address = "192.168.50.100",
        port = 631,
        protocol = DiscoveryProtocol.MDNS,
        properties = mapOf(
            "SERVICE_TYPE" to "_ipp._tcp",
            "SERVICE_NAME" to "HP-LaserJet-Pro",
            "ty" to "HP LaserJet Pro MFP M428fdn",
        ),
        rawData = "_ipp._tcp HP-LaserJet-Pro 192.168.50.100:631",
        deviceInfo = DeviceInfo(
            friendlyName = "Office Printer",
            manufacturer = "HP Inc.",
            modelName = "LaserJet Pro MFP M428fdn",
        ),
    ),
    DiscoveredDevice(
        name = "Apple-AirPlay/590.1",
        address = "192.168.50.12",
        port = 7000,
        protocol = DiscoveryProtocol.MDNS,
        properties = mapOf(
            "SERVICE_TYPE" to "_airplay._tcp",
            "SERVICE_NAME" to "Apple-TV-4K",
            "model" to "AppleTV11,1",
        ),
        rawData = "_airplay._tcp Apple-TV-4K 192.168.50.12:7000",
        deviceInfo = DeviceInfo(
            friendlyName = "Living Room Apple TV",
            manufacturer = "Apple Inc.",
            modelName = "Apple TV 4K",
            modelNumber = "3rd Gen",
        ),
    ),
)
