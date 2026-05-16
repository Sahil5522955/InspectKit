package dev.inspectkit

data class NetworkEvent(
    val id: String,
    val method: String,
    val url: String,
    val statusCode: Int?,
    val durationMs: Long,
    val requestHeaders: Map<String, String>,
    val responseHeaders: Map<String, String>,
    val requestBody: String?,
    val responseBody: String?,
    val error: String? = null,
    val startedAtMs: Long = System.currentTimeMillis()
)

data class DatabaseSource(
    val name: String,
    val tables: () -> List<String>,
    val query: (tableName: String) -> QueryResult
)

data class QueryResult(
    val columns: List<String>,
    val rows: List<List<String>>
)
