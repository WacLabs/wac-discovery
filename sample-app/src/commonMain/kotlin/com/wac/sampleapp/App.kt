package com.wac.sampleapp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun App() {
    val scope = rememberCoroutineScope()
    val viewModel = remember { DiscoveryViewModel(scope) }
    val state by viewModel.state.collectAsState()

    AppTheme {
        val selected = state.selectedDevice
        if (selected != null) {
            DetailScreen(
                device = selected,
                onBack = { viewModel.selectDevice(null) },
            )
        } else {
            SearchScreen(
                state = state,
                onSearchChanged = viewModel::onSearchChanged,
                onDeviceClick = viewModel::selectDevice,
                onScanClick = viewModel::startScan,
                onStopScanClick = viewModel::stopScan,
                onToggleMock = viewModel::toggleMockData,
            )
        }
    }
}
