package com.example.noisedet2.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.noisedet2.R
import com.example.noisedet2.databinding.FragmentHomeBinding
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationManager
import com.yandex.mapkit.location.LocationStatus
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

@Suppress("DEPRECATION")
class HomeFragment : Fragment() {
    private lateinit var mapview: MapView
//    private var locationManager: LocationManager? = null
    private lateinit var locBtn: Button

    private var _binding: FragmentHomeBinding? = null
  // This property is only valid between onCreateView and
  // onDestroyView.
//  private val binding get() = _binding!!

  @SuppressLint("SuspiciousIndentation")
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
      _binding = FragmentHomeBinding.inflate(inflater, container, false)
      return inflater.inflate(R.layout.fragment_home, container, false)
  }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapview = view.findViewById(R.id.mapview)
        locBtn = view.findViewById(R.id.locationBtn)

        locBtn.setOnClickListener {
            val mapKit:MapKit = MapKitFactory.getInstance()
//            val locationData = mapKit.createUserLocationLayer(mapview.mapWindow)
            val locationManager = mapKit.createLocationManager()
//            locationData.isVisible = true

            requestLocationPermission()
            locationManager.requestSingleUpdate(object : LocationListener {
                override fun onLocationStatusUpdated(locStat: LocationStatus) {
                }
                override fun onLocationUpdated(locStat: Location) {
                    mapview.map.mapObjects.addPlacemark().apply {
                        geometry = locStat.position
                        setIcon(ImageProvider.fromResource(requireContext(), R.drawable.ic_launcher_foreground))
                    mapview.map.move(CameraPosition(locStat.position, 11.0f, 0.0f, 0.0f),
                        Animation(Animation.Type.SMOOTH, 3f), null)
                    }
                }
            })
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            // Запрос разрешения
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                0
            )
        }
    }

    override fun onStop() {
        mapview.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        mapview.onStart()
        MapKitFactory.getInstance().onStart()
        super.onStart()
    }

override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}