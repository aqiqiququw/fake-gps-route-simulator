package com.example.fakegpsroutesimulator

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos

class LocationSimulatorService : Service() {
    companion object {
        const val EXTRA_LATITUDE = "latitude"
        const val EXTRA_LONGITUDE = "longitude"
        const val EXTRA_SPEED_KMH = "speed_kmh"
        private const val CHANNEL = "route_simulation"
        private const val NOTIFICATION_ID = 7
    }

    private lateinit var fused: FusedLocationProviderClient
    private var job: Job? = null

    override fun onCreate() {
        super.onCreate()
        fused = LocationServices.getFusedLocationProviderClient(this)
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val targetLat = intent?.getDoubleExtra(EXTRA_LATITUDE, Double.NaN) ?: Double.NaN
        val targetLon = intent?.getDoubleExtra(EXTRA_LONGITUDE, Double.NaN) ?: Double.NaN
        val speedKmh = intent?.getDoubleExtra(EXTRA_SPEED_KMH, 30.0) ?: 30.0
        if (targetLat.isNaN() || targetLon.isNaN()) return START_NOT_STICKY

        startForeground(NOTIFICATION_ID, notification("Simulando localização de teste"))
        job?.cancel()
        job = CoroutineScope(Dispatchers.Default).launch {
            // A rota começa na posição conhecida do aparelho/emulador e segue até o destino.
            var current = fused.lastLocation.awaitOrNull() ?: Location("mock").apply {
                latitude = targetLat
                longitude = targetLon
            }
            while (isActive) {
                val next = moveToward(current, targetLat, targetLon, speedKmh)
                next.provider = "mock"
                next.accuracy = 3f
                next.time = System.currentTimeMillis()
                try {
                    fused.setMockLocation(next)
                } catch (_: SecurityException) {
                    stopSelf()
                    break
                }
                current = next
                delay(1_000)
            }
        }
        return START_STICKY
    }

    private fun moveToward(from: Location, lat: Double, lon: Double, kmh: Double): Location {
        val result = Location("mock")
        val fraction = (kmh / 3_600.0) / 111.0 // km/h -> degrees latitude per second
        result.latitude = from.latitude + (lat - from.latitude).coerceIn(-fraction, fraction)
        val longitudeStep = fraction / cos(Math.toRadians(from.latitude)).coerceAtLeast(0.1)
        result.longitude = from.longitude + (lon - from.longitude).coerceIn(-longitudeStep, longitudeStep)
        return result
    }

    private fun notification(text: String): Notification = NotificationCompat.Builder(this, CHANNEL)
        .setSmallIcon(android.R.drawable.ic_menu_mylocation)
        .setContentTitle("Route Simulator")
        .setContentText(text)
        .setOngoing(true)
        .build()

    private fun createChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL, "Simulação de rota", NotificationManager.IMPORTANCE_LOW))
    }

    override fun onDestroy() {
        job?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

private suspend fun com.google.android.gms.tasks.Task<Location>.awaitOrNull(): Location? =
    kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { continuation.resume(it) {} }
        addOnFailureListener { continuation.resume(null) {} }
    }
