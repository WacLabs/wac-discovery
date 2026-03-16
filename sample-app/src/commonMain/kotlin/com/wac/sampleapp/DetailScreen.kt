package com.wac.sampleapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wac.wacdiscovery.DiscoveredDevice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    device: DiscoveredDevice,
    onBack: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = {
            TopAppBar(
                title = { Text("Device Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            // Header card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProtocolBadge(device.protocol)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = device.deviceInfo?.friendlyName?.takeIf { it.isNotEmpty() }
                                ?: device.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${device.address}:${device.port}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Device Info section
            device.deviceInfo?.let { info ->
                SectionTitle("Device Information")
                Spacer(Modifier.height(8.dp))
                InfoCard {
                    InfoRow("Friendly Name", info.friendlyName)
                    InfoRow("Manufacturer", info.manufacturer)
                    if (info.manufacturerUrl.isNotEmpty())
                        InfoRow("Manufacturer URL", info.manufacturerUrl)
                    InfoRow("Model", info.modelName)
                    if (info.modelNumber.isNotEmpty())
                        InfoRow("Model Number", info.modelNumber)
                    if (info.serialNumber.isNotEmpty())
                        InfoRow("Serial Number", info.serialNumber)
                    if (info.macAddress.isNotEmpty())
                        InfoRow("MAC Address", info.macAddress)
                    if (info.iconUrl.isNotEmpty())
                        InfoRow("Icon URL", info.iconUrl)
                    if (info.deviceType.isNotEmpty())
                        InfoRow("Device Type", info.deviceType)
                    if (info.presentationUrl.isNotEmpty())
                        InfoRow("Presentation URL", info.presentationUrl)
                }

                // Extra fields
                if (info.extraFields.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    SectionTitle("Extra Fields")
                    Spacer(Modifier.height(8.dp))
                    InfoCard {
                        info.extraFields.forEach { (key, value) ->
                            InfoRow(key, value)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
            }

            // Properties
            if (device.properties.isNotEmpty()) {
                SectionTitle("Properties")
                Spacer(Modifier.height(8.dp))
                InfoCard {
                    device.properties.forEach { (key, value) ->
                        InfoRow(key, value)
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // Raw data
            if (device.rawData.isNotEmpty()) {
                var expanded by remember { mutableStateOf(false) }

                SectionTitle("Raw Data")
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                    onClick = { expanded = !expanded },
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (expanded) "Tap to collapse ▲" else "Tap to expand ▼",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        if (expanded) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = device.rawData,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
private fun InfoCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    if (value.isEmpty()) return
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
