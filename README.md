<p align="center">
  <h1 align="center">📡 WAC Discovery</h1>
  <p align="center">
    <strong>Kotlin Multiplatform library for network device discovery</strong>
  </p>
  <p align="center">
    Discover smart TVs, speakers, routers, and IoT devices on your local network<br/>
    using <strong>SSDP</strong> and <strong>mDNS</strong> — from a single, unified API.
  </p>
  <p align="center">
    <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-2.1.10-7F52FF.svg?logo=kotlin&logoColor=white" alt="Kotlin"></a>
    <a href="https://central.sonatype.com/artifact/io.github.waclabs/wac-discovery"><img src="https://img.shields.io/maven-central/v/io.github.waclabs/wac-discovery.svg?label=Maven%20Central" alt="Maven Central"></a>
    <a href="LICENSE"><img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" alt="License"></a>
    <a href="#-supported-platforms"><img src="https://img.shields.io/badge/Platforms-Android%20%7C%20iOS%20%7C%20JVM-blue.svg" alt="Platforms"></a>
  </p>
</p>

---

## Table of Contents

- [Quick Start](#-quick-start)
- [Installation](#-installation)
- [Supported Platforms](#-supported-platforms)
- [Core Concepts](#-core-concepts)
- [Device Info Resolution](#-device-info-resolution)
- [Filtering Devices](#-filtering-devices)
- [Advanced Configuration](#-advanced-configuration)
- [Preset Constants Reference](#-preset-constants-reference)
- [API Reference](#-api-reference)
- [Platform-Specific Setup](#-platform-specific-setup)
- [License](#-license)

---

## 🚀 Quick Start

Discover all devices on your local network in **3 lines**:

```kotlin
val discovery = NetworkDiscovery()

discovery.discover().collect { device ->
    println("${device.name} — ${device.address}:${device.port}")
}

discovery.close()
```

That's it. The library scans via both **SSDP** (UPnP) and **mDNS** simultaneously, deduplicates results, and emits them as a Kotlin `Flow`.

---

## 📥 Installation

### Maven Central

```kotlin
// build.gradle.kts (your KMP module)
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.waclabs:wac-discovery:0.2.2")
        }
    }
}
```

> **Note:** The library is published to **Maven Central** — no extra repository configuration needed. All platforms (Android, iOS, JVM) are available as auto-resolved KMP artifacts.

---

## 📱 Supported Platforms

| Platform | SSDP Implementation | mDNS Implementation |
|----------|--------------------|-------------------|
| **Android** (minSdk 21) | `java.net.MulticastSocket` | `android.net.nsd.NsdManager` |
| **iOS** (arm64, simulatorArm64, x64) | POSIX BSD sockets | `NSNetServiceBrowser` |
| **JVM** (Desktop) | `java.net.MulticastSocket` | `JmDNS` |

---

## 🧩 Core Concepts

### `NetworkDiscovery`

The main entry point. Call `discover()` to get a `Flow<DiscoveredDevice>` and `close()` when done.

### `DiscoveredDevice`

Every discovered device is represented as:

```kotlin
data class DiscoveredDevice(
    val name: String,                      // e.g. "Samsung-Linux/4.1, UPnP/1.0"
    val address: String,                   // e.g. "192.168.50.190"
    val port: Int,                         // e.g. 9197
    val protocol: DiscoveryProtocol,       // SSDP or MDNS
    val properties: Map<String, String>,   // parsed headers / TXT records
    val rawData: String,                   // full raw response for custom parsing
    val deviceInfo: DeviceInfo?,           // rich info (null until resolved)
)
```

- **`properties`** — Contains all parsed SSDP headers (`LOCATION`, `USN`, `ST`, ...) or mDNS TXT records (`SERVICE_TYPE`, `SERVICE_NAME`, ...).
- **`rawData`** — The complete, unmodified response string. Use this if your device has a custom protocol and you need to parse it yourself.
- **`deviceInfo`** — Rich device metadata fetched from the UPnP description XML. `null` by default; see [Device Info Resolution](#-device-info-resolution).

### `DiscoveryConfig`

Controls how discovery works:

```kotlin
discovery.discover(
    DiscoveryConfig(
        protocols = setOf(DiscoveryProtocol.SSDP, DiscoveryProtocol.MDNS),
        timeout = 10.seconds,
    )
)
```

---

## 📺 Device Info Resolution

By default, the library only returns what SSDP/mDNS responses contain (name, IP, port, headers). To get **rich device metadata** — friendly name, manufacturer, model, serial number, icon — enable device info resolution:

### Auto-resolve (all devices)

```kotlin
discovery.discover(
    DiscoveryConfig(
        resolveDeviceInfo = true,           // fetch UPnP device description
        resolveTimeout = 3.seconds,         // HTTP timeout per device (default: 5s)
    )
).collect { device ->
    device.deviceInfo?.let { info ->
        println("📺 ${info.friendlyName}")       // "Living Room TV"
        println("🏭 ${info.manufacturer}")        // "Samsung Electronics"
        println("📱 ${info.modelName}")            // "UN55TU7000"
        println("🔑 ${info.serialNumber}")         // "ABC123XYZ"
        println("🖼️  ${info.iconUrl}")              // "http://192.168.50.190:9197/icon_LRG.png"
    }
}
```

### Manual resolve (per device)

```kotlin
discovery.discover().collect { device ->
    // Only resolve devices you care about
    if ("samsung" in device.name.lowercase()) {
        val resolved = device.resolveDeviceInfo(timeoutMs = 3000)
        println(resolved.deviceInfo?.friendlyName)
    }
}
```

### What `DeviceInfo` contains

| Field | Example |
|-------|---------||
| `friendlyName` | `"Living Room TV"` |
| `manufacturer` | `"Samsung Electronics"` |
| `manufacturerUrl` | `"http://www.samsung.com"` |
| `modelName` | `"UN55TU7000"` |
| `modelNumber` | `"AllShare1.0"` |
| `serialNumber` | `"TEEK9QADGC2BV9W"` |
| `macAddress` | `"AA:BB:CC:DD:EE:FF"` (if available) |
| `iconUrl` | `"http://192.168.50.190:9197/icon_LRG.png"` |
| `deviceType` | `"urn:schemas-upnp-org:device:MediaRenderer:1"` |
| `presentationUrl` | `"http://192.168.50.190:8001"` |
| `extraFields` | Any additional XML fields as `Map<String, String>` |

> **Note:** Device info resolution works via HTTP GET to the SSDP `LOCATION` URL. Not all devices expose a description XML — mDNS-only devices typically will not have `deviceInfo`.

---

## 🎯 Filtering Devices

### Preset Filters

Filter by manufacturer or device type using built-in presets:

```kotlin
// Samsung TVs only
discovery.discover(
    DiscoveryConfig(filter = DeviceFilters.SAMSUNG_TV)
).collect { println(it.name) }
```

### Combining Filters

Use `and`, `or`, `not` to build complex filter expressions:

```kotlin
// Smart TVs that are NOT LG
val filter = DeviceFilters.SMART_TV and !DeviceFilters.LG
discovery.discover(DiscoveryConfig(filter = filter))

// Samsung OR Sony
val filter = DeviceFilters.SAMSUNG or DeviceFilters.SONY
discovery.discover(DiscoveryConfig(filter = filter))
```

### Custom Keyword Filter

For brands not in the presets:

```kotlin
// Match by keyword (case-insensitive, searches name + address + properties)
val filter = DeviceFilter.byKeyword("my-custom-brand", "variant-2")

// Simple string match
val filter = DeviceFilter.byString("obscure-device-v3")
```

### Custom Lambda Filter

Full control with a lambda:

```kotlin
val filter = DeviceFilter.custom { device ->
    device.port == 8080 && device.address.startsWith("192.168.1.")
}
```

### Other Filter Factories

```kotlin
// Match by SSDP search target
DeviceFilter.bySearchTarget("urn:schemas-upnp-org:device:MediaRenderer:1")

// Match by IP pattern
DeviceFilter.byAddress(Regex("192\\.168\\.50\\..*"))
```

---

## ⚙️ Advanced Configuration

### Custom Configuration

```kotlin
discovery.discover(
    DiscoveryConfig(
        protocols = setOf(DiscoveryProtocol.SSDP, DiscoveryProtocol.MDNS),
        timeout = 15.seconds,              // discovery duration
        resolveDeviceInfo = true,           // fetch UPnP descriptions
        resolveTimeout = 3.seconds,         // HTTP timeout per device
        ssdpSearchTarget = SsdpSearchTargets.ROOT_DEVICE,
        ssdpMx = 5,
    )
)
```

### SSDP-Only Scan

```kotlin
discovery.discover(
    DiscoveryConfig(
        protocols = setOf(DiscoveryProtocol.SSDP),
        timeout = 15.seconds,
        ssdpSearchTarget = SsdpSearchTargets.ROOT_DEVICE,
        ssdpMx = 5,
    )
)
```

### mDNS-Only Scan

```kotlin
discovery.discover(
    DiscoveryConfig(
        protocols = setOf(DiscoveryProtocol.MDNS),
        timeout = 12.seconds,
        mdnsServiceTypes = listOf(
            MdnsServiceTypes.SAMSUNG_SMART_VIEW,
            MdnsServiceTypes.GOOGLE_CAST,
            MdnsServiceTypes.APPLE_AIRPLAY,
        ),
    )
)
```

### Android ViewModel Integration

```kotlin
class DevicesViewModel : ViewModel() {
    private val discovery = NetworkDiscovery()

    val devices = discovery.discover(
        DiscoveryConfig(resolveDeviceInfo = true)
    )
        .scan(emptyList<DiscoveredDevice>()) { acc, device -> acc + device }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    override fun onCleared() {
        discovery.close()
    }
}
```

### Using Raw Data for Custom Parsing

Some devices use custom protocols beyond standard UPnP. Access the full raw response:

```kotlin
discovery.discover().collect { device ->
    // Parse the raw SSDP response yourself
    val raw = device.rawData
    val customField = raw.lines()
        .find { it.startsWith("X-CUSTOM-HEADER:") }
        ?.substringAfter(":")?.trim()
}
```

---

## 📋 Preset Constants Reference

### Device Filters (`DeviceFilters`)

#### By Manufacturer

| Filter | Matches |
|--------|---------|
| `SAMSUNG` | Samsung devices |
| `LG` | LG devices |
| `SONY` | Sony devices |
| `APPLE` | Apple devices |
| `GOOGLE` | Google devices |
| `ROKU` | Roku devices |
| `SONOS` | Sonos devices |
| `XIAOMI` | Xiaomi / Mi devices |
| `PHILIPS` | Philips devices |
| `TCL` | TCL devices |
| `HISENSE` | Hisense devices |
| `VIZIO` | Vizio devices |

#### By Device Type

| Filter | Matches |
|--------|---------|
| `SMART_TV` | TVs and media renderers |
| `SPEAKER` | Speakers and soundbars |
| `CHROMECAST` | Google Cast devices |
| `PRINTER` | Network printers |
| `ROUTER` | Routers and gateways |
| `MEDIA_RENDERER` | UPnP MediaRenderer |
| `MEDIA_SERVER` | UPnP MediaServer |

#### Combined

| Filter | Equivalent |
|--------|-----------|
| `SAMSUNG_TV` | `SAMSUNG and SMART_TV` |
| `LG_TV` | `LG and SMART_TV` |
| `SONY_TV` | `SONY and SMART_TV` |
| `XIAOMI_TV` | `XIAOMI and SMART_TV` |
| `TCL_TV` | `TCL and SMART_TV` |
| `HISENSE_TV` | `HISENSE and SMART_TV` |

---

### mDNS Service Types (`MdnsServiceTypes`)

| Constant | Service Type | Description |
|----------|-------------|-------------|
| `SAMSUNG_SMART_VIEW` | `_samsungmsf._tcp` | Samsung Smart View |
| `SAMSUNG_REMOTE` | `_samsungctl._tcp` | Samsung Remote Control |
| `GOOGLE_CAST` | `_googlecast._tcp` | Chromecast / Google Cast |
| `APPLE_AIRPLAY` | `_airplay._tcp` | AirPlay |
| `APPLE_HOMEKIT` | `_hap._tcp` | HomeKit |
| `APPLE_RAOP` | `_raop._tcp` | Remote Audio Output |
| `LG_SMART_SHARE` | `_lgsmartshare._tcp` | LG SmartShare |
| `LG_WEBOS` | `_webos._tcp` | LG webOS |
| `ROKU` | `_roku._tcp` | Roku |
| `SONOS` | `_sonos._tcp` | Sonos |
| `SPOTIFY_CONNECT` | `_spotify-connect._tcp` | Spotify Connect |
| `HTTP` | `_http._tcp` | Any HTTP service |
| `PRINTER_IPP` | `_ipp._tcp` | IPP Printer |
| `SSH` | `_ssh._tcp` | SSH |
| `ALL` | `_services._dns-sd._udp` | All services |

---

### SSDP Search Targets (`SsdpSearchTargets`)

| Constant | Search Target | Description |
|----------|--------------|-------------|
| `ALL` | `ssdp:all` | All devices and services |
| `ROOT_DEVICE` | `upnp:rootdevice` | Root devices only |
| `MEDIA_RENDERER` | `urn:...MediaRenderer:1` | Media renderers (TVs, speakers) |
| `MEDIA_SERVER` | `urn:...MediaServer:1` | Media servers (NAS, DLNA) |
| `DIAL` | `urn:...dial:1` | DIAL (Netflix, YouTube 2nd screen) |
| `IGD` | `urn:...InternetGatewayDevice:1` | Routers / gateways |
| `BASIC_DEVICE` | `urn:...Basic:1` | UPnP basic devices |

---

## 📄 API Reference

### `DiscoveredDevice`

| Property | Type | Description |
|----------|------|-------------|
| `name` | `String` | Device/service name from response |
| `address` | `String` | IP address |
| `port` | `Int` | Service port |
| `protocol` | `DiscoveryProtocol` | `SSDP` or `MDNS` |
| `properties` | `Map<String, String>` | Parsed SSDP headers or mDNS TXT records |
| `rawData` | `String` | Full raw response for custom parsing |
| `deviceInfo` | `DeviceInfo?` | Rich info (`null` until resolved) |

| Method | Description |
|--------|-------------|
| `resolveDeviceInfo(timeoutMs)` | Fetches UPnP description XML and returns a new device with `deviceInfo` populated |

### `DiscoveryConfig`

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `protocols` | `Set<DiscoveryProtocol>` | `{SSDP, MDNS}` | Which protocols to use |
| `timeout` | `Duration` | `10.seconds` | Total scan duration |
| `filter` | `DeviceFilter` | `None` | Post-discovery device filter |
| `resolveDeviceInfo` | `Boolean` | `false` | Auto-fetch UPnP device description |
| `resolveTimeout` | `Duration` | `5.seconds` | HTTP timeout per device info fetch |
| `ssdpSearchTarget` | `String` | `"ssdp:all"` | SSDP search target (ST header) |
| `ssdpMx` | `Int` | `3` | SSDP max response delay (seconds) |
| `mdnsServiceTypes` | `List<String>` | `[ALL]` | mDNS service types to browse |

### `DeviceFilter`

| Factory | Description |
|---------|-------------|
| `DeviceFilter.None` | Accept all devices |
| `DeviceFilter.byKeyword(vararg keywords)` | Match if any keyword found (case-insensitive) |
| `DeviceFilter.byString(query)` | Match by a single string query |
| `DeviceFilter.bySearchTarget(st)` | Match SSDP search target |
| `DeviceFilter.byAddress(pattern)` | Match IP by regex |
| `DeviceFilter.custom { ... }` | Custom lambda predicate |

| Combinator | Usage |
|------------|-------|
| `filter1 and filter2` | Match both |
| `filter1 or filter2` | Match either |
| `!filter` | Invert |

---

## 🔒 Platform-Specific Setup

### Android

**Permissions** — add to `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_MULTICAST_STATE" />
```

**mDNS Context** — initialize in your `Application` class:

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MdnsContextHolder.init(applicationContext)
    }
}
```

**MulticastLock** — required for SSDP on some devices:

```kotlin
val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
val lock = wifiManager.createMulticastLock("discovery").apply {
    setReferenceCounted(true)
    acquire()
}
// ... run discovery ...
lock.release()
```

### iOS

No additional setup required. The library uses POSIX sockets (SSDP) and `NSNetServiceBrowser` (mDNS) natively.

### JVM

No additional setup required. mDNS uses the bundled `JmDNS` library.

---

## 📜 License

```
Copyright 2025 WacLabs

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
