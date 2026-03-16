package com.wac.wacdiscovery

/**
 * Well-known mDNS service types for common device discovery.
 *
 * Pass these to [DiscoveryConfig.mdnsServiceTypes]:
 * ```kotlin
 * val config = DiscoveryConfig(
 *     mdnsServiceTypes = listOf(
 *         MdnsServiceTypes.SAMSUNG_SMART_VIEW,
 *         MdnsServiceTypes.GOOGLE_CAST,
 *     ),
 * )
 * ```
 */
object MdnsServiceTypes {
    // ── Samsung ──────────────────────────────────────────────────
    const val SAMSUNG_SMART_VIEW = "_samsungmsf._tcp"
    const val SAMSUNG_REMOTE = "_samsungctl._tcp"
    const val SAMSUNG_SMART_DEVICE = "_samsung-smart-device._tcp"

    // ── Google ───────────────────────────────────────────────────
    const val GOOGLE_CAST = "_googlecast._tcp"

    // ── Apple ────────────────────────────────────────────────────
    const val APPLE_AIRPLAY = "_airplay._tcp"
    const val APPLE_HOMEKIT = "_hap._tcp"
    const val APPLE_RAOP = "_raop._tcp"

    // ── LG ───────────────────────────────────────────────────────
    const val LG_SMART_SHARE = "_lgsmartshare._tcp"
    const val LG_WEBOS = "_webos._tcp"

    // ── Other Brands ─────────────────────────────────────────────
    const val ROKU = "_roku._tcp"
    const val SONOS = "_sonos._tcp"
    const val SPOTIFY_CONNECT = "_spotify-connect._tcp"

    // ── Generic ──────────────────────────────────────────────────
    const val HTTP = "_http._tcp"
    const val PRINTER_IPP = "_ipp._tcp"
    const val PRINTER_IPPS = "_ipps._tcp"
    const val SSH = "_ssh._tcp"
    const val SMB = "_smb._tcp"

    /** Discover all service types on the network. */
    const val ALL = "_services._dns-sd._udp"
}
