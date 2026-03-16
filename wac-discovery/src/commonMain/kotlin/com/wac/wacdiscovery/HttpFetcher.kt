package com.wac.wacdiscovery

/**
 * Platform-specific HTTP client for fetching UPnP device description XML.
 */
internal expect object HttpFetcher {
    /**
     * Fetches content from the given URL as a String.
     * Returns empty string on failure.
     */
    suspend fun fetch(url: String, timeoutMs: Int = 5000): String
}
