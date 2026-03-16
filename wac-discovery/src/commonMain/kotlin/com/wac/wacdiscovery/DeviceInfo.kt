package com.wac.wacdiscovery

/**
 * Rich device information fetched from UPnP device description XML.
 *
 * Obtained by resolving the SSDP `LOCATION` URL or mDNS endpoint.
 * Not all fields may be available — depends on the device.
 *
 * @property friendlyName Human-readable device name (e.g. "Living Room TV").
 * @property manufacturer Device manufacturer (e.g. "Samsung Electronics").
 * @property manufacturerUrl Manufacturer website URL.
 * @property modelName Device model name (e.g. "UN55TU7000").
 * @property modelNumber Device model number.
 * @property modelDescription Device model description.
 * @property serialNumber Device serial number.
 * @property udn Unique Device Name (UUID).
 * @property deviceType UPnP device type URN.
 * @property iconUrl URL to device icon/logo (relative or absolute).
 * @property presentationUrl URL for device's web UI.
 * @property macAddress MAC address if available in device description.
 * @property extraFields Any additional XML fields not mapped above.
 */
data class DeviceInfo(
    val friendlyName: String = "",
    val manufacturer: String = "",
    val manufacturerUrl: String = "",
    val modelName: String = "",
    val modelNumber: String = "",
    val modelDescription: String = "",
    val serialNumber: String = "",
    val udn: String = "",
    val deviceType: String = "",
    val iconUrl: String = "",
    val presentationUrl: String = "",
    val macAddress: String = "",
    val extraFields: Map<String, String> = emptyMap(),
)
