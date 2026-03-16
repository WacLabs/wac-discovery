@file:OptIn(ExperimentalForeignApi::class)

package com.wac.wacdiscovery

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionConfiguration
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataTaskWithURL
import kotlin.coroutines.resume

internal actual object HttpFetcher {
    actual suspend fun fetch(url: String, timeoutMs: Int): String = suspendCancellableCoroutine { cont ->
        val nsUrl = NSURL.URLWithString(url) ?: run {
            cont.resume("")
            return@suspendCancellableCoroutine
        }

        val config = NSURLSessionConfiguration.defaultSessionConfiguration.apply {
            timeoutIntervalForRequest = timeoutMs / 1000.0
            timeoutIntervalForResource = timeoutMs / 1000.0
        }
        val session = NSURLSession.sessionWithConfiguration(config)

        val task = session.dataTaskWithURL(nsUrl) { data, _, error ->
            if (error != null || data == null) {
                cont.resume("")
                return@dataTaskWithURL
            }
            val str = NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString() ?: ""
            cont.resume(str)
        }
        task.resume()

        cont.invokeOnCancellation { task.cancel() }
    }
}
