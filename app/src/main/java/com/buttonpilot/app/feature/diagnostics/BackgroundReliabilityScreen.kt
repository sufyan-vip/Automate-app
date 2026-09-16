package com.buttonpilot.app.feature.diagnostics

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
private fun InfoRowLocal(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackgroundReliabilityScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Background Reliability") }, navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) }
            })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("How ButtonPilot stays reliable", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "ButtonPilot is designed for near-zero CPU while no hardware events occur. It uses event-driven architecture:\n\n" +
                        "• Accessibility callbacks\n" +
                        "• StateFlow\n" +
                        "• Foreground service only while recording\n" +
                        "• No while(true) polling loops\n" +
                        "• Lightweight event processing",
                style = MaterialTheme.typography.bodyMedium
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("OEM Battery Optimization Steps", fontWeight = FontWeight.Bold)
                    Text("Some brands aggressively stop background apps:", style = MaterialTheme.typography.bodySmall)
                    Text("• Samsung: Settings → Apps → ButtonPilot → Battery → Unrestricted", style = MaterialTheme.typography.bodySmall)
                    Text("• Xiaomi/Redmi: Settings → Battery → App battery saver → ButtonPilot → No restrictions + Autostart", style = MaterialTheme.typography.bodySmall)
                    Text("• Realme/Oppo: Settings → Battery → App battery management → ButtonPilot → Allow background + Allow autostart", style = MaterialTheme.typography.bodySmall)
                    Text("• Vivo: Settings → Battery → Background power consumption → ButtonPilot → Allow", style = MaterialTheme.typography.bodySmall)
                    Text("• Motorola/Stock: Settings → Battery → Battery optimization → Don't optimize", style = MaterialTheme.typography.bodySmall)
                    Text("• OnePlus: Settings → Battery → Battery optimization → Don't optimize", style = MaterialTheme.typography.bodySmall)
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Compatibility Result", fontWeight = FontWeight.Bold)
                    InfoRowLocal("Feature", "Status")
                    Divider()
                    InfoRowLocal("Triple Volume Down", "Supported")
                    InfoRowLocal("Screen-Off Detection", if (Build.MANUFACTURER.lowercase().contains("xiaomi")) "Limited" else "Supported")
                    InfoRowLocal("Root Input Adapter", "Check Diagnostics")
                    InfoRowLocal("Background Recording Start", if (Build.VERSION.SDK_INT >= 31) "Platform Restricted" else "Supported")
                }
            }

            Text(
                "Note: Do not attempt to bypass OS protections invisibly. If OEM kills services, we explain battery-optimization settings to user.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
