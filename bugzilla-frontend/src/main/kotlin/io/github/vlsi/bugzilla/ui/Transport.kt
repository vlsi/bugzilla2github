package io.github.vlsi.bugzilla.ui

import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.js.jso
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.w3c.fetch.AbortController
import org.w3c.fetch.RequestInit
import org.w3c.fetch.signal
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.js.json

@JsName("encodeURIComponent")
external fun urlEncode(value: String): String

@JsName("decodeURIComponent")
external fun urlDecode(encoded: String): String

class Transport(private val coroutineContext: CoroutineContext) {
    private val jsonConfig = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    internal suspend fun <T> get(
        url: String,
        deserializationStrategy: KSerializer<T>,
        vararg args: Pair<String, Any>
    ): T {
        return jsonConfig.decodeFromString(deserializationStrategy, fetch("GET", url, *args))
    }

    internal suspend fun <T> post(
        url: String,
        deserializationStrategy: KSerializer<T>,
        vararg args: Pair<String, Any>
    ): T {
        return jsonConfig.decodeFromString(deserializationStrategy, fetch("POST", url, *args))
    }

    internal suspend fun <T> getList(
        url: String,
        deserializationStrategy: KSerializer<T>,
        vararg args: Pair<String, Any>
    ): List<T> {
        return jsonConfig.decodeFromString(
            ListSerializer(deserializationStrategy),
            fetch("GET", url, *args)
        )
    }

    private suspend fun fetch(httpMethod: String, path: String, vararg args: Pair<String, Any>): String {
        var url = "/api/$path"
        if (args.isNotEmpty()) {
            url += "?"
            url += args.joinToString("&", transform = { "${it.first}=${urlEncode(it.second.toString())}" })
        }

        return withContext(coroutineContext) {
            val abortController = AbortController()
            val response = window.fetch(
                url,
                jso {
                    this.method = httpMethod
                    this.headers = json(
                        "Accept" to "application/json",
                        "Content-Type" to "application/json"
                    )
                    this.signal = abortController.signal
                }
            ).let { promise ->
                suspendCancellableCoroutine { cont ->
                    cont.invokeOnCancellation { abortController.abort() }
                    promise.then(
                        onFulfilled = { cont.resume(it) },
                        onRejected = { cont.resumeWithException(it) }
                    )
                }
            }

            response.text().await()
        }
    }
}
