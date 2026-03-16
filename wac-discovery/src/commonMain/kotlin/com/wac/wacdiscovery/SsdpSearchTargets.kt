package com.wac.wacdiscovery

/**
 * Well-known SSDP search target (ST) values.
 *
 * Pass these to [DiscoveryConfig.ssdpSearchTarget]:
 * ```kotlin
 * val config = DiscoveryConfig(
 *     ssdpSearchTarget = SsdpSearchTargets.MEDIA_RENDERER,
 * )
 * ```
 */
object SsdpSearchTargets {
    /** Discover all SSDP devices and services. */
    const val ALL = "ssdp:all"

    /** Discover root devices only. */
    const val ROOT_DEVICE = "upnp:rootdevice"

    /** UPnP Media Renderer (smart TVs, speakers). */
    const val MEDIA_RENDERER = "urn:schemas-upnp-org:device:MediaRenderer:1"

    /** UPnP Media Server (NAS, media libraries). */
    const val MEDIA_SERVER = "urn:schemas-upnp-org:device:MediaServer:1"

    /** DIAL protocol (Netflix, YouTube second screen). */
    const val DIAL = "urn:dial-multiscreen-org:service:dial:1"

    /** Internet Gateway Device (routers). */
    const val IGD = "urn:schemas-upnp-org:device:InternetGatewayDevice:1"

    /** Basic device type 1. */
    const val BASIC_DEVICE = "urn:schemas-upnp-org:device:Basic:1"
}
