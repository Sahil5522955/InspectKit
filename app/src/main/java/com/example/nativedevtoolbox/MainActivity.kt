package com.example.nativedevtoolbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dev.inspectkit.InspectKit
import dev.inspectkit.InspectKitPanel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        InspectKit.install()
        InspectKit.seedDemoData()
        setContent {
            NativeDevToolboxApp()
        }
    }
}

@Composable
fun NativeDevToolboxApp() {
    MaterialTheme(
        colorScheme = toolboxColorScheme(),
        typography = MaterialTheme.typography
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold { padding ->
                ToolboxHome(Modifier.padding(padding))
            }
        }
    }
}

@Composable
private fun ToolboxHome(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9), Color(0xFFEFF6FF))
                )
            ),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Header() }
        item { InspectKitPanel() }
        item { DensityConverterCard() }
        item { ColorTokenCard() }
        item { AdbHelperCard() }
        item { PermissionCard() }
        item { SnippetCard() }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Header() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFF244C5A), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Apps, contentDescription = null, tint = Color.White)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "InspectKit Demo",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B2632)
                )
                Text(
                    "Drop-in inspection for native Android debug builds",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B)
                )
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Network", "Database", "Runtime", "Clipboard").forEach {
                AssistChip(onClick = {}, label = { Text(it) })
            }
        }
    }
}

@Composable
private fun DensityConverterCard() {
    var dpText by remember { mutableStateOf("16") }
    var densityText by remember { mutableStateOf("3") }
    val dp = dpText.toFloatOrNull() ?: 0f
    val density = densityText.toFloatOrNull() ?: 0f
    val px = (dp * density).roundToInt()

    ToolCard(
        title = "Density Converter",
        subtitle = "Convert dp to px for spec checks and screenshots.",
        icon = Icons.Outlined.Straighten
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            NumberField("dp", dpText, { dpText = it }, Modifier.weight(1f))
            NumberField("density", densityText, { densityText = it }, Modifier.weight(1f))
        }
        OutputBlock("Result", "$dpText dp at ${densityText}x = $px px")
    }
}

@Composable
private fun ColorTokenCard() {
    var hex by remember { mutableStateOf("#0F766E") }
    val normalized = normalizeHex(hex)
    val composeColor = normalized?.replace("#", "0xFF") ?: "invalid"
    val xmlColor = normalized ?: "invalid"

    ToolCard(
        title = "Color Tokens",
        subtitle = "Generate Compose and XML color declarations.",
        icon = Icons.Outlined.ColorLens
    ) {
        OutlinedTextField(
            value = hex,
            onValueChange = { hex = it },
            label = { Text("Hex color") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
        OutputBlock(
            "Generated",
            "val BrandPrimary = Color($composeColor)\n<color name=\"brand_primary\">$xmlColor</color>"
        )
    }
}

@Composable
private fun AdbHelperCard() {
    var packageName by remember { mutableStateOf("com.example.app") }
    val commands = listOf(
        "adb shell am force-stop $packageName",
        "adb shell pm clear $packageName",
        "adb shell am start -n $packageName/.MainActivity",
        "adb logcat --pid=${'$'}(adb shell pidof -s $packageName)"
    ).joinToString("\n")

    ToolCard(
        title = "ADB Commands",
        subtitle = "Build common command lines from a package name.",
        icon = Icons.Outlined.Bolt
    ) {
        OutlinedTextField(
            value = packageName,
            onValueChange = { packageName = it.trim() },
            label = { Text("Package name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
        OutputBlock("Commands", commands)
    }
}

@Composable
private fun PermissionCard() {
    ToolCard(
        title = "Permission Checklist",
        subtitle = "Quick reminders for modern Android permission flows.",
        icon = Icons.Outlined.Security
    ) {
        ChecklistRow("Android 13+", "Request POST_NOTIFICATIONS before showing notifications.")
        ChecklistRow("Android 12+", "Use BLUETOOTH_SCAN/CONNECT instead of location for many Bluetooth flows.")
        ChecklistRow("Android 11+", "Prefer scoped storage and system pickers over broad file access.")
        ChecklistRow("Runtime", "Explain the user-facing benefit before launching the permission dialog.")
    }
}

@Composable
private fun SnippetCard() {
    val clipboard = LocalClipboardManager.current
    val snippet = """
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                // Continue the feature.
            }
        }
    """.trimIndent()

    ToolCard(
        title = "Starter Snippet",
        subtitle = "Copyable Compose-friendly native Android patterns.",
        icon = Icons.Outlined.Code
    ) {
        OutputBlock("Runtime permission launcher", snippet)
        Button(
            onClick = { clipboard.setText(AnnotatedString(snippet)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Outlined.ContentCopy, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Copy snippet")
        }
    }
}

@Composable
private fun ToolCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    content: @Composable Column.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFE4F2EC), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Color(0xFF16624F))
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.SemiBold, color = Color(0xFF17212B))
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF697684))
                }
            }
            content()
        }
    }
}

@Composable
private fun NumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}

@Composable
private fun OutputBlock(label: String, value: String) {
    val clipboard = LocalClipboardManager.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF3F6F9), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                label.uppercase(Locale.US),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF627181),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { clipboard.setText(AnnotatedString(value)) }) {
                Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy $label")
            }
        }
        Text(
            value,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF18232E)
        )
    }
}

@Composable
private fun ChecklistRow(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold, color = Color(0xFF263541))
        Text(body, style = MaterialTheme.typography.bodySmall, color = Color(0xFF5B6875))
    }
}

private fun normalizeHex(input: String): String? {
    val trimmed = input.trim().removePrefix("#")
    if (trimmed.length != 6 || trimmed.any { it !in '0'..'9' && it !in 'a'..'f' && it !in 'A'..'F' }) {
        return null
    }
    return "#${trimmed.uppercase(Locale.US)}"
}

@Composable
private fun toolboxColorScheme() = androidx.compose.material3.lightColorScheme(
    primary = Color(0xFF16624F),
    onPrimary = Color.White,
    secondary = Color(0xFF1D4E89),
    tertiary = Color(0xFF475569),
    background = Color(0xFFF8FAFC),
    surface = Color.White
)

@Preview(showBackground = true)
@Composable
private fun NativeDevToolboxPreview() {
    NativeDevToolboxApp()
}
