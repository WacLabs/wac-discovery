package com.wac.wacdiscovery

/**
 * Predefined device filters by manufacturer and device type.
 *
 * Combine filters using [DeviceFilter.and], [DeviceFilter.or], [DeviceFilter.not]:
 * ```kotlin
 * val samsungTvs = DeviceFilters.SAMSUNG and DeviceFilters.SMART_TV
 * val notPrinters = !DeviceFilters.PRINTER
 * val tvOrSpeaker = DeviceFilters.SMART_TV or DeviceFilters.SPEAKER
 * ```
 *
 * For custom/unknown brands, use [DeviceFilter.byKeyword] or [DeviceFilter.byString]:
 * ```kotlin
 * val myBrand = DeviceFilter.byKeyword("my-custom-brand", "variant-2")
 * ```
 */
object DeviceFilters {

    // ── Manufacturer Filters ─────────────────────────────────────

    val SAMSUNG = DeviceFilter.byKeyword("samsung")
    val LG = DeviceFilter.byKeyword("lg")
    val SONY = DeviceFilter.byKeyword("sony")
    val PHILIPS = DeviceFilter.byKeyword("philips")
    val APPLE = DeviceFilter.byKeyword("apple")
    val GOOGLE = DeviceFilter.byKeyword("google")
    val ROKU = DeviceFilter.byKeyword("roku")
    val SONOS = DeviceFilter.byKeyword("sonos")
    val XIAOMI = DeviceFilter.byKeyword("xiaomi", "mi ")
    val TCL = DeviceFilter.byKeyword("tcl")
    val HISENSE = DeviceFilter.byKeyword("hisense")
    val VIZIO = DeviceFilter.byKeyword("vizio")

    // ── Device Type Filters ──────────────────────────────────────

    val SMART_TV = DeviceFilter.byKeyword(
        "tv", "television", "mediarenderer", "smarttv", "smart-tv",
    )
    val MEDIA_RENDERER = DeviceFilter.bySearchTarget(
        "urn:schemas-upnp-org:device:MediaRenderer:1",
    )
    val MEDIA_SERVER = DeviceFilter.bySearchTarget(
        "urn:schemas-upnp-org:device:MediaServer:1",
    )
    val SPEAKER = DeviceFilter.byKeyword(
        "speaker", "soundbar", "audio",
    )
    val CHROMECAST = DeviceFilter.byKeyword(
        "chromecast", "google cast", "googlecast",
    )
    val PRINTER = DeviceFilter.byKeyword(
        "printer", "ipp",
    )
    val ROUTER = DeviceFilter.byKeyword(
        "router", "gateway", "igd", "internet gateway",
    )

    // ── Combined Filters ─────────────────────────────────────────

    val SAMSUNG_TV = SAMSUNG and SMART_TV
    val LG_TV = LG and SMART_TV
    val SONY_TV = SONY and SMART_TV
    val XIAOMI_TV = XIAOMI and SMART_TV
    val TCL_TV = TCL and SMART_TV
    val HISENSE_TV = HISENSE and SMART_TV
}
