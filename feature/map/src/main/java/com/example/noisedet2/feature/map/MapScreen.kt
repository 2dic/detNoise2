package com.example.noisedet2.feature.map

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.noisedet2.core.common.noise.NoiseMeter
import com.example.noisedet2.core.ui.noise.formatDb
import com.example.noisedet2.core.ui.noise.formatTimestamp
import com.example.noisedet2.core.ui.noise.noiseCategoryLabel
import com.example.noisedet2.core.ui.noise.noiseTextColor
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationManager
import com.yandex.mapkit.location.LocationStatus
import com.yandex.mapkit.mapview.MapView

@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember { MapView(context) }
    val placemarks = remember { MeasurementMapState(context) }
    var mapReady by remember { mutableStateOf(false) }
    var locationManager by remember { mutableStateOf<LocationManager?>(null) }

    fun requestLocation(moveCamera: Boolean, onlyIfNothingSelected: Boolean = false) {
        val manager = locationManager
            ?: MapKitFactory.getInstance().createLocationManager().also { locationManager = it }
        manager.requestSingleUpdate(object : LocationListener {
            override fun onLocationStatusUpdated(status: LocationStatus) {
                if (moveCamera && status == LocationStatus.NOT_AVAILABLE) {
                    viewModel.onLocationUnavailable()
                }
            }

            override fun onLocationUpdated(location: Location) {
                val point = location.position
                val shouldMove = moveCamera &&
                    (!onlyIfNothingSelected || !viewModel.hasSelection())
                viewModel.onUserLocation(
                    latitude = point.latitude,
                    longitude = point.longitude,
                    moveCamera = shouldMove
                )
            }
        })
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            requestLocation(moveCamera = true)
        } else {
            Toast.makeText(context, "Location permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startMeasurement()
        } else {
            Toast.makeText(
                context,
                "Microphone permission is required to measure noise",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        var started = false
        fun startMap() {
            if (started) return
            MapKitFactory.getInstance().onStart()
            mapView.onStart()
            started = true
            mapReady = true
        }
        fun stopMap() {
            if (!started) return
            mapReady = false
            mapView.onStop()
            MapKitFactory.getInstance().onStop()
            started = false
        }
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> startMap()
                Lifecycle.Event.ON_STOP -> stopMap()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            startMap()
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            stopMap()
        }
    }

    LaunchedEffect(mapReady) {
        if (!mapReady || !hasLocationPermission(context)) return@LaunchedEffect
        if (viewModel.shouldRequestInitialLocation()) {
            requestLocation(moveCamera = true, onlyIfNothingSelected = true)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { text ->
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }
    }

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { mapView },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    if (!mapReady) return@AndroidView
                    placemarks.onMarkerClick = viewModel::selectMeasurement
                    placemarks.sync(
                        mapView = view,
                        measurements = state.measurements,
                        selectedId = state.selected?.id,
                        userLatitude = state.userLatitude,
                        userLongitude = state.userLongitude,
                        cameraToken = state.cameraToken,
                        cameraLatitude = state.cameraLatitude,
                        cameraLongitude = state.cameraLongitude,
                    )
                }
            )

            state.selected?.let { measurement ->
                val level = NoiseMeter.levelCategory(measurement.dbLevel)
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 8.dp)) {
                        Text(
                            text = formatDb(measurement.dbLevel),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = noiseTextColor(level)
                        )
                        Text(
                            text = noiseCategoryLabel(level),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = formatTimestamp(measurement.timestamp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = viewModel::clearSelection,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Close")
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 72.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val level = state.liveDb?.let(NoiseMeter::levelCategory)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Noise level",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = state.liveDb?.let(::formatDb) ?: "— dB",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (level != null) noiseTextColor(level) else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = state.status,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (state.isMeasuring) {
                                viewModel.stopMeasurement()
                            } else if (hasAudioPermission(context)) {
                                viewModel.startMeasurement()
                            } else {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(if (state.isMeasuring) "Stop" else "Measure Noise")
                    }
                    FilledIconButton(
                        onClick = {
                            if (hasLocationPermission(context)) {
                                requestLocation(moveCamera = true)
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            painter = painterResource(android.R.drawable.ic_menu_mylocation),
                            contentDescription = "My location"
                        )
                    }
                }
            }
        }
    }
}

private fun hasLocationPermission(context: android.content.Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return fine || coarse
}

private fun hasAudioPermission(context: android.content.Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context, Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED
}
