package dev.inspectkit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Dataset
import androidx.compose.material.icons.outlined.Http
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.nio.charset.StandardCharsets

enum class InspectorTab {
    Network,
    Database,
    Runtime
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InspectKitPanel(modifier: Modifier = Modifier) {
    if (!InspectKit.configState.value.enabled) return

    var selectedTab by remember { mutableStateOf(InspectorTab.Network) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = InspectColors.Surface,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 2.dp,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, InspectColors.Border, RoundedCornerShape(8.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            InspectorHeader()
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TabChip(InspectorTab.Network, selectedTab, Icons.Outlined.Http) { selectedTab = it }
                TabChip(InspectorTab.Database, selectedTab, Icons.Outlined.Dataset) { selectedTab = it }
                TabChip(InspectorTab.Runtime, selectedTab, Icons.Outlined.Memory) { selectedTab = it }
            }
            when (selectedTab) {
                InspectorTab.Network -> NetworkTab()
                InspectorTab.Database -> DatabaseTab()
                InspectorTab.Runtime -> RuntimeTab()
            }
        }
    }
}

@Composable
private fun InspectorHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("InspectKit", color = InspectColors.Ink, fontWeight = FontWeight.Bold)
            Text(
                "In-app inspection for debug builds",
                color = InspectColors.Muted,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Pill("${InspectKit.networkEvents.size} calls", InspectColors.Navy, Color.White)
    }
}

@Composable
private fun TabChip(
    tab: InspectorTab,
    selected: InspectorTab,
    icon: ImageVector,
    onSelect: (InspectorTab) -> Unit
) {
    FilterChip(
        selected = selected == tab,
        onClick = { onSelect(tab) },
        label = { Text(tab.name) },
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
        shape = RoundedCornerShape(8.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = InspectColors.Canvas,
            labelColor = InspectColors.Ink,
            iconColor = InspectColors.Muted,
            selectedContainerColor = InspectColors.Navy,
            selectedLabelColor = Color.White,
            selectedLeadingIconColor = Color.White
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected == tab,
            borderColor = InspectColors.Border,
            selectedBorderColor = InspectColors.Navy
        )
    )
}

@Composable
private fun NetworkTab() {
    val events = InspectKit.networkEvents
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (events.isEmpty()) {
            EmptyState("No network calls captured yet. Add OkHttpInspectInterceptor to your debug OkHttpClient.")
        }
        events.forEach { event ->
            NetworkEventCard(event)
        }
    }
}

@Composable
private fun NetworkEventCard(event: NetworkEvent) {
    var expanded by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    val color = when {
        event.error != null -> InspectColors.Danger
        event.statusCode != null && event.statusCode in 200..299 -> InspectColors.Success
        else -> InspectColors.Warning
    }
    val badgeText = event.statusCode?.toString() ?: "ERR"

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            Modifier
                .border(1.dp, InspectColors.Border, RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Pill(event.method, InspectColors.Canvas, InspectColors.Navy)
                Spacer(Modifier.width(8.dp))
                Pill(badgeText, color.copy(alpha = 0.14f), color)
                Spacer(Modifier.width(8.dp))
                Text("${event.durationMs} ms", color = InspectColors.Muted, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { clipboard.setText(AnnotatedString(event.asShareText())) }) {
                    Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy call", tint = InspectColors.Navy)
                }
            }
            Text(
                event.url,
                color = InspectColors.Ink,
                style = MaterialTheme.typography.bodySmall,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )
            Button(
                onClick = { expanded = !expanded },
                colors = ButtonDefaults.buttonColors(containerColor = InspectColors.Navy, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (expanded) "Hide payload" else "View payload")
            }
            if (expanded) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { clipboard.setText(AnnotatedString(event.asCurlCommand())) },
                        colors = ButtonDefaults.buttonColors(containerColor = InspectColors.Canvas, contentColor = InspectColors.Navy),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Copy cURL")
                    }
                }
                PayloadBlock("Request headers", event.requestHeaders.prettyMap())
                PayloadBlock("Request body", event.requestBody.orEmpty().ifBlank { "(empty)" })
                PayloadBlock("Response headers", event.responseHeaders.prettyMap())
                PayloadBlock("Response body", event.responseBody ?: event.error.orEmpty())
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DatabaseTab() {
    var selectedSource by remember { mutableStateOf<String?>(null) }
    var selectedTable by remember { mutableStateOf<String?>(null) }
    val sources = InspectKit.databaseSources
    val source = sources.firstOrNull { it.name == selectedSource } ?: sources.firstOrNull()
    val tables = runCatching { source?.tables?.invoke().orEmpty() }.getOrDefault(emptyList())
    val table = selectedTable ?: tables.firstOrNull()
    val result = table?.let { runCatching { source?.query?.invoke(it) }.getOrNull() }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (sources.isEmpty()) {
            EmptyState("No databases registered yet. Register a Room or SQLite source from your debug Application.")
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sources.forEach {
                    FilterChip(
                        selected = source?.name == it.name,
                        onClick = {
                            selectedSource = it.name
                            selectedTable = null
                        },
                        label = { Text(it.name) }
                    )
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tables.forEach {
                    FilterChip(
                        selected = table == it,
                        onClick = { selectedTable = it },
                        label = { Text(it) }
                    )
                }
            }
            result?.let { QueryResultTable(it) }
        }
    }
}

@Composable
private fun QueryResultTable(result: QueryResult) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var pendingXml by remember { mutableStateOf<String?>(null) }
    val exportXmlLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/xml")
    ) { uri ->
        val xml = pendingXml
        pendingXml = null
        if (uri != null && xml != null) {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(xml.toByteArray(StandardCharsets.UTF_8))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, InspectColors.Border, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Rows: ${result.rows.size}", color = InspectColors.Ink, modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    pendingXml = result.asXml()
                    exportXmlLauncher.launch("inspectkit-db.xml")
                },
                colors = ButtonDefaults.buttonColors(containerColor = InspectColors.Canvas, contentColor = InspectColors.Navy),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Export XML")
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { clipboard.setText(AnnotatedString(result.asCsv())) }) {
                Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy rows", tint = InspectColors.Navy)
            }
        }
        Column(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .verticalScroll(rememberScrollState())
        ) {
            Row {
                result.columns.forEach { Cell(it, header = true) }
            }
            Divider(color = InspectColors.Border)
            result.rows.forEach { row ->
                Row {
                    row.forEach { Cell(it, header = false) }
                }
            }
        }
    }
}

@Composable
private fun RuntimeTab() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        MetricRow("Allocated memory", "${Runtime.getRuntime().totalMemory() / 1024 / 1024} MB")
        MetricRow("Free memory", "${Runtime.getRuntime().freeMemory() / 1024 / 1024} MB")
        MetricRow("Processors", Runtime.getRuntime().availableProcessors().toString())
        EmptyState("Roadmap: composition recomposition counters, slow frame timeline, lifecycle trace, and dependency graph.")
    }
}

@Composable
private fun PayloadBlock(title: String, text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(InspectColors.Canvas, RoundedCornerShape(8.dp))
            .border(1.dp, InspectColors.Border, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Text(title, color = InspectColors.Navy, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            Text(
                text,
                color = InspectColors.Ink,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, InspectColors.Border, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(label, color = InspectColors.Ink, modifier = Modifier.weight(1f))
        Text(value, color = InspectColors.Navy, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(InspectColors.Canvas, RoundedCornerShape(8.dp))
            .border(1.dp, InspectColors.Border, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(message, color = InspectColors.Muted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun Cell(text: String, header: Boolean) {
    Text(
        text = text,
        color = if (header) InspectColors.Navy else InspectColors.Ink,
        fontWeight = if (header) FontWeight.Bold else FontWeight.Normal,
        fontFamily = FontFamily.Monospace,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier
            .width(150.dp)
            .padding(vertical = 8.dp, horizontal = 6.dp)
    )
}

@Composable
private fun Pill(text: String, background: Color, content: Color) {
    Text(
        text = text,
        color = content,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .background(background, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp)
    )
}

private object InspectColors {
    val Surface = Color(0xFFF8FAFC)
    val Canvas = Color(0xFFF1F5F9)
    val Border = Color(0xFFD8DEE6)
    val Ink = Color(0xFF17212B)
    val Muted = Color(0xFF64748B)
    val Navy = Color(0xFF1D4E89)
    val Success = Color(0xFF167A4A)
    val Warning = Color(0xFF9A6700)
    val Danger = Color(0xFFB42318)
}

private fun NetworkEvent.asShareText(): String {
    return buildString {
        appendLine("$method ${statusCode ?: "ERR"} $url")
        appendLine("Duration: $durationMs ms")
        appendLine()
        appendLine("Request headers")
        appendLine(requestHeaders.prettyMap())
        appendLine()
        appendLine("Request body")
        appendLine(requestBody.orEmpty())
        appendLine()
        appendLine("Response headers")
        appendLine(responseHeaders.prettyMap())
        appendLine()
        appendLine("Response body")
        appendLine(responseBody ?: error.orEmpty())
    }
}

private fun Map<String, String>.prettyMap(): String {
    if (isEmpty()) return "(empty)"
    return entries.joinToString("\n") { "${it.key}: ${it.value}" }
}

private fun QueryResult.asCsv(): String {
    return buildString {
        appendLine(columns.joinToString(","))
        rows.forEach { appendLine(it.joinToString(",")) }
    }
}

private fun QueryResult.asXml(): String {
    return buildString {
        appendLine("""<?xml version="1.0" encoding="utf-8"?>""")
        appendLine("<result>")
        appendLine("  <columns>")
        columns.forEach { appendLine("    <column>${it.escapeXml()}</column>") }
        appendLine("  </columns>")
        appendLine("  <rows>")
        rows.forEach { row ->
            appendLine("    <row>")
            row.forEachIndexed { index, value ->
                val col = columns.getOrNull(index) ?: "col$index"
                appendLine("""      <cell column="${col.escapeXml()}">${value.escapeXml()}</cell>""")
            }
            appendLine("    </row>")
        }
        appendLine("  </rows>")
        appendLine("</result>")
    }
}

private fun String.escapeXml(): String {
    return this
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}

private fun NetworkEvent.asCurlCommand(): String {
    val parts = mutableListOf("curl", "-X", method)
    requestHeaders.forEach { (k, v) ->
        parts.add("-H")
        parts.add("$k: $v".shellQuote())
    }
    val body = requestBody
    if (!body.isNullOrBlank()) {
        parts.add("--data-raw")
        parts.add(body.shellQuote())
    }
    parts.add(url.shellQuote())
    return parts.joinToString(" ")
}

private fun String.shellQuote(): String {
    // POSIX-friendly single quote escaping for sharing in terminals.
    return "'" + this.replace("'", "'\"'\"'") + "'"
}
