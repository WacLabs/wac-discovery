package com.wac.wacdiscovery

/**
 * Parses UPnP device description XML into [DeviceInfo].
 *
 * Uses simple string extraction (no XML library dependency).
 */
internal object DeviceDescriptionParser {

    fun parse(xml: String, baseUrl: String = ""): DeviceInfo {
        // Find the <device> block (first one = root device)
        val deviceBlock = extractBlock(xml, "device") ?: return DeviceInfo()

        val friendlyName = extractTag(deviceBlock, "friendlyName")
        val manufacturer = extractTag(deviceBlock, "manufacturer")
        val manufacturerUrl = extractTag(deviceBlock, "manufacturerURL")
        val modelName = extractTag(deviceBlock, "modelName")
        val modelNumber = extractTag(deviceBlock, "modelNumber")
        val modelDescription = extractTag(deviceBlock, "modelDescription")
        val serialNumber = extractTag(deviceBlock, "serialNumber")
        val udn = extractTag(deviceBlock, "UDN")
        val deviceType = extractTag(deviceBlock, "deviceType")
        val presentationUrl = extractTag(deviceBlock, "presentationURL")

        // Extract icon URL (prefer largest PNG, fallback to first icon)
        val iconUrl = extractBestIconUrl(deviceBlock, baseUrl)

        // Try to find MAC address in various locations
        val macAddress = extractMacAddress(xml)

        // Collect extra fields not covered above
        val knownFields = setOf(
            "friendlyName", "manufacturer", "manufacturerURL",
            "modelName", "modelNumber", "modelDescription",
            "serialNumber", "UDN", "deviceType", "presentationURL",
            "iconList", "serviceList", "deviceList",
        )
        val extraFields = extractAllTags(deviceBlock)
            .filterKeys { it !in knownFields }

        return DeviceInfo(
            friendlyName = friendlyName,
            manufacturer = manufacturer,
            manufacturerUrl = manufacturerUrl,
            modelName = modelName,
            modelNumber = modelNumber,
            modelDescription = modelDescription,
            serialNumber = serialNumber,
            udn = udn,
            deviceType = deviceType,
            iconUrl = iconUrl,
            presentationUrl = presentationUrl,
            macAddress = macAddress,
            extraFields = extraFields,
        )
    }

    private fun extractTag(xml: String, tag: String): String {
        val regex = Regex("<$tag>([^<]*)</$tag>", RegexOption.IGNORE_CASE)
        return regex.find(xml)?.groupValues?.get(1)?.trim() ?: ""
    }

    private fun extractBlock(xml: String, tag: String): String? {
        val startRegex = Regex("<$tag[^>]*>", RegexOption.IGNORE_CASE)
        val endTag = "</$tag>"
        val startMatch = startRegex.find(xml) ?: return null
        val startIdx = startMatch.range.last + 1
        val endIdx = xml.indexOf(endTag, startIdx, ignoreCase = true)
        if (endIdx < 0) return null
        return xml.substring(startIdx, endIdx)
    }

    private fun extractBestIconUrl(deviceBlock: String, baseUrl: String): String {
        val iconList = extractBlock(deviceBlock, "iconList") ?: return ""

        // Find all icon blocks — replace newlines so . matches everything
        val flatIconList = iconList.replace('\n', ' ').replace('\r', ' ')
        val iconRegex = Regex("<icon>(.*?)</icon>", RegexOption.IGNORE_CASE)
        val icons = iconRegex.findAll(flatIconList).toList()
        if (icons.isEmpty()) return ""

        // Prefer PNG, then largest by width
        val parsed = icons.mapNotNull { match ->
            val block = match.groupValues[1]
            val iconUrl = extractTag(block, "url")
            if (iconUrl.isEmpty()) return@mapNotNull null
            val width = extractTag(block, "width").toIntOrNull() ?: 0
            val mimetype = extractTag(block, "mimetype")
            Triple(iconUrl, width, mimetype)
        }

        val best = parsed
            .sortedWith(compareByDescending<Triple<String, Int, String>> { it.third.contains("png") }
                .thenByDescending { it.second })
            .firstOrNull() ?: return ""

        val bestUrl = best.first
        return if (bestUrl.startsWith("http")) bestUrl
        else "${baseUrl.trimEnd('/')}/${bestUrl.trimStart('/')}"
    }

    private fun extractMacAddress(xml: String): String {
        // Try common patterns: wifiMac, macAddress, MACAddress, etc.
        val macTags = listOf("wifiMac", "macAddress", "MACAddress", "mac", "MAC")
        for (tag in macTags) {
            val value = extractTag(xml, tag)
            if (value.isNotEmpty()) return value
        }

        // Try regex pattern for MAC in the raw XML
        val macRegex = Regex("([0-9A-Fa-f]{2}[:-]){5}[0-9A-Fa-f]{2}")
        return macRegex.find(xml)?.value ?: ""
    }

    private fun extractAllTags(xml: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val regex = Regex("<([a-zA-Z][a-zA-Z0-9]*)>([^<]+)</[a-zA-Z][a-zA-Z0-9]*>")
        regex.findAll(xml).forEach { match ->
            val key = match.groupValues[1]
            val value = match.groupValues[2].trim()
            if (value.isNotEmpty() && key !in result) {
                result[key] = value
            }
        }
        return result
    }
}
