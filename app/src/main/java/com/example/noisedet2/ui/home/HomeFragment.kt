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
import com.example.noisedet2.R

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var userLocationPlacemark: PlacemarkMapObject? = null

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
                "Location permission is required to show your position",
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
    }

    private fun checkLocationPermissionAndLocate() {
        val fine = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
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
            override fun onLocationStatusUpdated(status: LocationStatus) {
                // Optional: handle LocationStatus.NOT_AVAILABLE etc.
            }

            override fun onLocationUpdated(location: Location) {
                showUserLocation(location.position)
            }
        })
    }

    private fun showUserLocation(point: Point) {
        val map = binding.mapview.map

        // Remove previous temporary location marker
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

    override fun onStart() {
        super.onStart()
        binding.mapview.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        binding.mapview.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        userLocationPlacemark = null
        _binding = null
    }
}
