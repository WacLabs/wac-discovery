package com.wac.wacdiscovery

/**
 * A filter predicate for [DiscoveredDevice]s.
 *
 * Supports composition via [and], [or], and [not] combinators.
 *
 * Usage:
 * ```kotlin
 * val filter = DeviceFilters.SAMSUNG and DeviceFilters.SMART_TV
 * val customFilter = DeviceFilter.byKeyword("my-custom-device")
 * val combined = filter or customFilter
 * ```
 */
fun interface DeviceFilter {

    /** Returns `true` if [device] passes this filter. */
    fun matches(device: DiscoveredDevice): Boolean

    /** Combines two filters — device must match BOTH. */
    infix fun and(other: DeviceFilter): DeviceFilter =
        DeviceFilter { matches(it) && other.matches(it) }

    /** Combines two filters — device must match EITHER. */
    infix fun or(other: DeviceFilter): DeviceFilter =
        DeviceFilter { matches(it) || other.matches(it) }

    /** Inverts this filter. */
    operator fun not(): DeviceFilter =
        DeviceFilter { !matches(it) }

    companion object {
        /** A filter that accepts all devices. */
        val None: DeviceFilter = DeviceFilter { true }

        /**
         * Creates a filter that matches devices containing ANY of the given
         * keywords (case-insensitive) in name, address, or properties.
         */
        fun byKeyword(vararg keywords: String): DeviceFilter = DeviceFilter { device ->
            val searchable = buildString {
                append(device.name.lowercase())
                append(' ')
                append(device.address.lowercase())
                append(' ')
                device.properties.entries.forEach { (k, v) ->
                    append(k.lowercase())
                    append(' ')
                    append(v.lowercase())
                    append(' ')
                }
            }
            keywords.any { searchable.contains(it.lowercase()) }
        }

        /**
         * Creates a filter from a custom string query.
         * Matches if any device field contains the query (case-insensitive).
         *
         * Use this for custom/unknown device brands:
         * ```kotlin
         * val myFilter = DeviceFilter.byKeyword("my-obscure-brand")
         * ```
         */
        fun byString(query: String): DeviceFilter = byKeyword(query)

        /**
         * Creates a filter matching a specific SSDP search target (ST header).
         */
        fun bySearchTarget(st: String): DeviceFilter = DeviceFilter { device ->
            device.properties["ST"]?.equals(st, ignoreCase = true) == true
        }

        /**
         * Creates a filter matching devices by IP address pattern.
         */
        fun byAddress(pattern: Regex): DeviceFilter = DeviceFilter { device ->
            pattern.containsMatchIn(device.address)
        }

        /**
         * Creates a fully custom filter from a lambda.
         *
         * ```kotlin
         * val filter = DeviceFilter.custom { it.port == 8080 }
         * ```
         */
        fun custom(predicate: (DiscoveredDevice) -> Boolean): DeviceFilter =
            DeviceFilter(predicate)
    }
}
