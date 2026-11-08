package com.example.noisedet2.feature.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RadialGradient
import android.graphics.Shader
import com.example.noisedet2.core.common.noise.NoiseLevel
import com.example.noisedet2.core.common.noise.NoiseMeter
import com.example.noisedet2.core.data.local.entity.NoiseMeasurementEntity
import com.example.noisedet2.core.ui.noise.noiseMarkerColor
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import kotlin.math.cos
import kotlin.math.pow

/**
 * Keeps Yandex placemarks in sync with saved measurements.
 *
 * Each measurement is rendered as a soft radial "zone" (blob) instead of a point.
 * Zones are semi-transparent: when measurements are close they visually merge,
 * and different colors blend smoothly in the overlap.
 */
internal class MeasurementMapState(context: Context) {

    private val icons = BlobIconCache(context)
    private val markers = mutableMapOf<Long, MarkerRecord>()
    private var userPlacemark: PlacemarkMapObject? = null
    private var lastCameraToken = 0L
    var onMarkerClick: (Long) -> Unit = {}

    fun sync(
        mapView: MapView,
        measurements: List<NoiseMeasurementEntity>,
        selectedId: Long?,
        userLatitude: Double?,
        userLongitude: Double?,
        cameraToken: Long,
        cameraLatitude: Double?,
        cameraLongitude: Double?,
    ) {
        val map = mapView.map
        val mapObjects = map.mapObjects
        val aliveIds = measurements.mapTo(mutableSetOf()) { it.id }

        val stale = markers.keys.filter { it !in aliveIds }
        stale.forEach { id ->
            markers.remove(id)?.let { record ->
                mapObjects.remove(record.placemark)
            }
        }

        measurements.forEach { measurement ->
            val level = NoiseMeter.levelCategory(measurement.dbLevel)
            val selected = measurement.id == selectedId
            val point = Point(measurement.latitude, measurement.longitude)
            val existing = markers[measurement.id]
            if (existing == null) {
                val placemark = mapObjects.addPlacemark().apply {
                    geometry = point
                    setIcon(icons.forLevel(level, selected), blobStyle())
                    setScaleFunction(zoneScaleFunction(measurement.latitude, selected))
                    zIndex = if (selected) 20f else 10f
                    addTapListener { _, _ ->
                        onMarkerClick(measurement.id)
                        true
                    }
                }
                markers[measurement.id] = MarkerRecord(placemark, level, selected)
            } else {
                existing.placemark.geometry = point
                if (existing.level != level || existing.selected != selected) {
                    existing.placemark.setIcon(icons.forLevel(level, selected), blobStyle())
                    existing.placemark.setScaleFunction(
                        zoneScaleFunction(measurement.latitude, selected)
                    )
                    existing.placemark.zIndex = if (selected) 20f else 10f
                    existing.level = level
                    existing.selected = selected
                }
            }
        }

        if (userLatitude != null && userLongitude != null) {
            val point = Point(userLatitude, userLongitude)
            val current = userPlacemark
            if (current == null) {
                userPlacemark = mapObjects.addPlacemark().apply {
                    geometry = point
                    setIcon(icons.user, blobStyle())
                    zIndex = 50f
                }
            } else {
                current.geometry = point
            }
        }

        if (cameraToken != 0L &&
            cameraToken != lastCameraToken &&
            cameraLatitude != null &&
            cameraLongitude != null
        ) {
            lastCameraToken = cameraToken
            map.move(
                com.yandex.mapkit.map.CameraPosition(
                    Point(cameraLatitude, cameraLongitude),
                    15f,
                    0f,
                    0f
                ),
                Animation(Animation.Type.SMOOTH, 0.8f),
                null
            )
        }
    }

    private fun blobStyle(): IconStyle {
        return IconStyle().apply {
            anchor = PointF(0.5f, 0.5f)
            scale = 1f
        }
    }

    /**
     * Zones keep a roughly constant geographic size.
     * Scale doubles with every zoom level, with clamps so they stay visible
     * when zoomed out and do not become enormous when zoomed in.
     */
    private fun zoneScaleFunction(latitude: Double, selected: Boolean): List<PointF> {
        val diameterMeters = ZONE_DIAMETER_METERS * if (selected) 1.2 else 1.0
        val metersPerPixelAtZoom0 = METERS_PER_PIXEL_AT_EQUATOR * cos(Math.toRadians(latitude))
        val scaleAtZoom0 =
            diameterMeters / (metersPerPixelAtZoom0 * ICON_SIZE_PX.toDouble())

        return (MIN_ZOOM..MAX_ZOOM).map { zoom ->
            val scale = (scaleAtZoom0 * 2.0.pow(zoom))
                .toFloat()
                .coerceIn(MIN_SCALE, MAX_SCALE)
            PointF(zoom.toFloat(), scale)
        }
    }

    private class MarkerRecord(
        val placemark: PlacemarkMapObject,
        var level: NoiseLevel,
        var selected: Boolean,
    )

    private companion object {
        const val ICON_SIZE_PX = 256

        /** Approximate diameter of one measurement zone. */
        const val ZONE_DIAMETER_METERS = 300.0

        const val METERS_PER_PIXEL_AT_EQUATOR = 156_543.03392

        const val MIN_ZOOM = 0
        const val MAX_ZOOM = 22
        const val MIN_SCALE = 0.12f
        const val MAX_SCALE = 3.0f
    }
}

private class BlobIconCache(context: Context) {

    private val density = context.resources.displayMetrics.density
    private val userIconSizePx = (18f * density).toInt().coerceAtLeast(12)
    private val cache = mutableMapOf<String, ImageProvider>()

    fun forLevel(level: NoiseLevel, selected: Boolean): ImageProvider {
        return cache.getOrPut("${level.name}_$selected") {
            ImageProvider.fromBitmap(blobBitmap(noiseMarkerColor(level), selected))
        }
    }

    val user: ImageProvider by lazy {
        ImageProvider.fromBitmap(userBitmap(userIconSizePx))
    }

    private fun blobBitmap(color: Int, selected: Boolean): Bitmap {
        val size = ICON_SIZE.toInt()
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val radius = size / 2f
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        val centerAlpha = if (selected) 200 else 165
        val middleAlpha = if (selected) 130 else 95

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                radius,
                radius,
                radius,
                intArrayOf(
                    Color.argb(centerAlpha, red, green, blue),
                    Color.argb(middleAlpha, red, green, blue),
                    Color.argb(0, red, green, blue)
                ),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(radius, radius, radius, paint)
        return bitmap
    }

    private fun userBitmap(size: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val center = size / 2f
        val white = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        val blue = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = USER_COLOR }
        canvas.drawCircle(center, center, size * 0.42f, white)
        canvas.drawCircle(center, center, size * 0.30f, blue)
        return bitmap
    }

    private companion object {
        const val ICON_SIZE = 256f
        const val USER_COLOR = 0xFF1E88E5.toInt()
    }
}
