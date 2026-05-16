package dev.inspectkit

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import java.util.UUID

object InspectKit {
    private val mainHandler = Handler(Looper.getMainLooper())

    internal val networkEvents = mutableStateListOf<NetworkEvent>()
    internal val databaseSources = mutableStateListOf<DatabaseSource>()
    internal val configState = mutableStateOf(InspectKitConfig())

    fun install(config: InspectKitConfig = InspectKitConfig()) {
        runOnMain {
            configState.value = config
        }
    }

    fun recordNetwork(event: NetworkEvent) {
        runOnMain {
            networkEvents.add(0, event)
            val maxEvents = configState.value.maxNetworkEvents
            while (networkEvents.size > maxEvents) {
                networkEvents.removeAt(networkEvents.lastIndex)
            }
        }
    }

    fun registerDatabase(source: DatabaseSource) {
        runOnMain {
            databaseSources.removeAll { it.name == source.name }
            databaseSources.add(source)
        }
    }

    fun seedDemoData() {
        recordNetwork(
            NetworkEvent(
                id = UUID.randomUUID().toString(),
                method = "POST",
                url = "https://api.example.dev/login",
                statusCode = 200,
                durationMs = 142,
                requestHeaders = mapOf("Content-Type" to "application/json"),
                responseHeaders = mapOf("Cache-Control" to "no-store"),
                requestBody = """{"email":"dev@example.com","password":"***"}""",
                responseBody = """{"token":"sample-token","user":{"id":7,"role":"admin"}}"""
            )
        )
        registerDatabase(
            DatabaseSource(
                name = "DemoCache",
                tables = { listOf("users", "feature_flags") },
                query = { table ->
                    when (table) {
                        "users" -> QueryResult(
                            columns = listOf("id", "email", "role"),
                            rows = listOf(listOf("7", "dev@example.com", "admin"))
                        )
                        else -> QueryResult(
                            columns = listOf("key", "enabled"),
                            rows = listOf(listOf("new_home", "true"), listOf("debug_panel", "true"))
                        )
                    }
                }
            )
        )
    }

    private fun runOnMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            mainHandler.post(block)
        }
    }
}

data class InspectKitConfig(
    val enabled: Boolean = true,
    val maxNetworkEvents: Int = 200,
    val redactHeaders: Set<String> = setOf("authorization", "cookie", "set-cookie"),
    val redactJsonKeys: Set<String> = setOf("password", "token", "access_token", "refresh_token")
)
