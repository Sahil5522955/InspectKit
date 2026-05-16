package dev.inspectkit

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.util.UUID

class OkHttpInspectInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val started = System.currentTimeMillis()
        val requestBody = runCatching {
            val body = request.body ?: return@runCatching null
            val buffer = Buffer()
            body.writeTo(buffer)
            buffer.readUtf8()
        }.getOrNull()

        return try {
            val response = chain.proceed(request)
            val responseBody = response.body
            val responseBodyText = responseBody?.string()
            val duration = System.currentTimeMillis() - started

            InspectKit.recordNetwork(
                NetworkEvent(
                    id = UUID.randomUUID().toString(),
                    method = request.method,
                    url = request.url.toString(),
                    statusCode = response.code,
                    durationMs = duration,
                    requestHeaders = request.headers.asMap().redacted(),
                    responseHeaders = response.headers.asMap().redacted(),
                    requestBody = requestBody?.redactJsonLikeText(),
                    responseBody = responseBodyText?.redactJsonLikeText()
                )
            )

            response.newBuilder()
                .body(responseBodyText.orEmpty().toResponseBody(responseBody?.contentType()))
                .build()
        } catch (throwable: Throwable) {
            InspectKit.recordNetwork(
                NetworkEvent(
                    id = UUID.randomUUID().toString(),
                    method = request.method,
                    url = request.url.toString(),
                    statusCode = null,
                    durationMs = System.currentTimeMillis() - started,
                    requestHeaders = request.headers.asMap().redacted(),
                    responseHeaders = emptyMap(),
                    requestBody = requestBody?.redactJsonLikeText(),
                    responseBody = null,
                    error = throwable.message ?: throwable::class.java.simpleName
                )
            )
            throw throwable
        }
    }
}

private fun Map<String, String>.redacted(): Map<String, String> {
    val blocked = InspectKit.configState.value.redactHeaders
    return mapValues { (key, value) ->
        if (key.lowercase() in blocked) "[redacted]" else value
    }
}

private fun okhttp3.Headers.asMap(): Map<String, String> {
    return names().associateWith { name -> values(name).joinToString("; ") }
}

private fun String.redactJsonLikeText(): String {
    return InspectKit.configState.value.redactJsonKeys.fold(this) { text, key ->
        val escapedKey = Regex.escape(key)
        text.replace(
            Regex(""""$escapedKey"\s*:\s*"[^"]*"""", RegexOption.IGNORE_CASE),
            """"$key":"[redacted]""""
        )
    }
}
