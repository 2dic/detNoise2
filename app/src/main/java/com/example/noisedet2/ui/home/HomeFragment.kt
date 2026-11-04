package com.example.noisedet2.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.noisedet2.R
import com.example.noisedet2.core.common.noise.NoiseLevel
import com.example.noisedet2.core.common.noise.NoiseMeter
import com.example.noisedet2.databinding.FragmentHomeBinding
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationStatus
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var userLocationPlacemark: PlacemarkMapObject? = null
    private val noiseMeter = NoiseMeter()
    private var measurementJob: Job? = null
    private var isMeasuring = false

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineGranted || coarseGranted) {
            requestCurrentLocation()
        } else {
            Toast.makeText(
                requireContext(),
                "Location permission is required",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val audioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startNoiseMeasurement()
        } else {
            Toast.makeText(
                requireContext(),
                "Microphone permission is required to measure noise",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.locationBtn.setOnClickListener {
            checkLocationPermissionAndLocate()
        }

        binding.measureBtn.setOnClickListener {
            if (isMeasuring) {
                stopNoiseMeasurement()
            } else {
                checkAudioPermissionAndMeasure()
            }
        }
    }

    // ─── Location ──────────────────────────────────────────────

    private fun checkLocationPermissionAndLocate() {
        val fine = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fine || coarse) {
            requestCurrentLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun requestCurrentLocation() {
        val locationManager = MapKitFactory.getInstance().createLocationManager()

        locationManager.requestSingleUpdate(object : LocationListener {
            override fun onLocationStatusUpdated(status: LocationStatus) = Unit

            override fun onLocationUpdated(location: Location) {
                showUserLocation(location.position)
            }
        })
    }

    private fun showUserLocation(point: Point) {
        val map = binding.mapview.map

        userLocationPlacemark?.let { map.mapObjects.remove(it) }

        userLocationPlacemark = map.mapObjects.addPlacemark().apply {
            geometry = point
            setIcon(ImageProvider.fromResource(requireContext(), R.drawable.ic_launcher_foreground))
        }

        map.move(
            CameraPosition(point, 14.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 1.5f),
            null
        )
    }

    // ─── Noise measurement ───────────────────────────────────

    private fun checkAudioPermissionAndMeasure() {
        val granted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            startNoiseMeasurement()
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startNoiseMeasurement() {
        if (isMeasuring) return

        isMeasuring = true
        binding.measureBtn.text = "Stop"
        binding.noiseStatus.text = "Measuring…"

        val samples = mutableListOf<Float>()

        measurementJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                noiseMeter.measure()
                    .onCompletion {
                        // Calculate average when stopped or finished
                        if (samples.isNotEmpty()) {
                            val avg = samples.average().toFloat()
                            showFinalResult(avg)
                        }
                    }
                    .collect { db ->
                        samples.add(db)
                        updateLiveDb(db)
                    }
            } catch (e: Exception) {
                binding.noiseStatus.text = "Error: ${e.message ?: "unknown"}"
                Toast.makeText(requireContext(), "Measurement failed", Toast.LENGTH_SHORT).show()
                resetMeasurementUi()
            }
        }

        // Auto-stop after 4 seconds
        viewLifecycleOwner.lifecycleScope.launch {
            kotlinx.coroutines.delay(4000L)
            if (isMeasuring) {
                stopNoiseMeasurement()
            }
        }
    }

    private fun stopNoiseMeasurement() {
        measurementJob?.cancel()
        measurementJob = null
        isMeasuring = false
        binding.measureBtn.text = "Measure Noise"
    }

    private fun updateLiveDb(db: Float) {
        binding.noiseValue.text = String.format(Locale.US, "%.1f dB", db)
        binding.noiseStatus.text = categoryLabel(NoiseMeter.levelCategory(db))
        applyNoiseColor(db)
    }

    private fun showFinalResult(avgDb: Float) {
        binding.noiseValue.text = String.format(Locale.US, "%.1f dB", avgDb)
        binding.noiseStatus.text = "Result: ${categoryLabel(NoiseMeter.levelCategory(avgDb))}"
        applyNoiseColor(avgDb)
        resetMeasurementUi()
    }

    private fun resetMeasurementUi() {
        isMeasuring = false
        binding.measureBtn.text = "Measure Noise"
    }

    private fun categoryLabel(level: NoiseLevel): String = when (level) {
        NoiseLevel.QUIET -> "Quiet"
        NoiseLevel.MODERATE -> "Moderate"
        NoiseLevel.LOUD -> "Loud"
        NoiseLevel.VERY_LOUD -> "Very loud"
    }

    private fun applyNoiseColor(db: Float) {
        val color = when (NoiseMeter.levelCategory(db)) {
            NoiseLevel.QUIET -> 0xFF4CAF50.toInt()      // Green
            NoiseLevel.MODERATE -> 0xFFFFEB3B.toInt()   // Yellow
            NoiseLevel.LOUD -> 0xFFFF9800.toInt()       // Orange
            NoiseLevel.VERY_LOUD -> 0xFFF44336.toInt()  // Red
        }
        binding.noiseValue.setTextColor(color)
    }

    // ─── Lifecycle ──────────────────────────────────────────

    override fun onStart() {
        super.onStart()
        binding.mapview.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        stopNoiseMeasurement()
        binding.mapview.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        measurementJob?.cancel()
        userLocationPlacemark = null
        _binding = null
        super.onDestroyView()
    }
}
