package com.example.noisedet2.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.noisedet2.core.common.noise.NoiseMeter
import com.example.noisedet2.core.data.local.entity.NoiseMeasurementEntity
import com.example.noisedet2.core.ui.noise.formatCoordinates
import com.example.noisedet2.core.ui.noise.formatDb
import com.example.noisedet2.core.ui.noise.formatTimestamp
import com.example.noisedet2.core.ui.noise.noiseCategoryLabel
import com.example.noisedet2.core.ui.noise.noiseMarkerColor
import com.example.noisedet2.core.ui.noise.noiseOnColor
import kotlinx.coroutines.launch

@Composable
fun HistoryScreen(
    onOpenMap: () -> Unit = {},
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val measurements by viewModel.measurements.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var pendingDelete by remember { mutableStateOf<NoiseMeasurementEntity?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }
    var lastDeleted by remember { mutableStateOf<NoiseMeasurementEntity?>(null) }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 88.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HistoryHeader(
                count = measurements.size,
                onClearAll = { showClearDialog = true }
            )

            if (measurements.isEmpty()) {
                EmptyHistory(modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 4.dp,
                        end = 16.dp,
                        bottom = 96.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(measurements, key = { it.id }) { measurement ->
                        MeasurementCard(
                            measurement = measurement,
                            onShowOnMap = {
                                viewModel.showOnMap(measurement)
                                onOpenMap()
                            },
                            onDelete = { pendingDelete = measurement }
                        )
                    }
                }
            }
        }
    }

    pendingDelete?.let { measurement ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete measurement?") },
            text = {
                Text(
                    "${formatDb(measurement.dbLevel)} at " +
                        "${formatTimestamp(measurement.timestamp)} will be removed."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDelete = null
                        lastDeleted = measurement
                        viewModel.delete(measurement)
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "Measurement deleted",
                                actionLabel = "Undo",
                                withDismissAction = true
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                lastDeleted?.let(viewModel::restore)
                                lastDeleted = null
                            }
                        }
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Delete all measurements?") },
            text = {
                Text("This removes every saved measurement from the history and the map.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        viewModel.deleteAll()
                    }
                ) {
                    Text("Delete all")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun HistoryHeader(
    count: Int,
    onClearAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 16.dp, end = 8.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Measurement history",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = when (count) {
                    0 -> "No saved measurements"
                    1 -> "1 saved measurement"
                    else -> "$count saved measurements"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (count > 0) {
            TextButton(onClick = onClearAll) {
                Text("Clear all")
            }
        }
    }
}

@Composable
private fun MeasurementCard(
    measurement: NoiseMeasurementEntity,
    onShowOnMap: () -> Unit,
    onDelete: () -> Unit,
) {
    val level = NoiseMeter.levelCategory(measurement.dbLevel)
    val accent = Color(noiseMarkerColor(level))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 14.dp, end = 4.dp, bottom = 14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 5.dp)
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatDb(measurement.dbLevel),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = accent,
                        contentColor = noiseOnColor(level),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = noiseCategoryLabel(level),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimestamp(measurement.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatCoordinates(measurement.latitude, measurement.longitude),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                measurement.note?.takeIf { it.isNotBlank() }?.let { note ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                FilledTonalButton(
                    onClick = onShowOnMap,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(android.R.drawable.ic_dialog_map),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Посмотреть на карте")
                }
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete measurement",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyHistory(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No measurements yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Open the Map tab, tap Measure Noise, and your results will appear here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
