package com.example.fakegpsroutesimulator

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private val requestCode = 42

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val destination = findViewById<EditText>(R.id.destinationInput)
        val speed = findViewById<EditText>(R.id.speedInput)
        val status = findViewById<TextView>(R.id.statusText)

        findViewById<Button>(R.id.startButton).setOnClickListener {
            if (!hasLocationPermission()) {
                requestPermissions()
                return@setOnClickListener
            }
            val point = destination.text.toString().split(",").mapNotNull { it.trim().toDoubleOrNull() }
            if (point.size != 2 || point[0] !in -90.0..90.0 || point[1] !in -180.0..180.0) {
                destination.error = "Informe latitude,longitude válidas"
                return@setOnClickListener
            }
            val kmh = speed.text.toString().toDoubleOrNull()?.coerceIn(1.0, 200.0) ?: 30.0
            val intent = Intent(this, LocationSimulatorService::class.java)
                .putExtra(LocationSimulatorService.EXTRA_LATITUDE, point[0])
                .putExtra(LocationSimulatorService.EXTRA_LONGITUDE, point[1])
                .putExtra(LocationSimulatorService.EXTRA_SPEED_KMH, kmh)
            ContextCompat.startForegroundService(this, intent)
            status.text = "Simulação ativa em segundo plano"
        }

        findViewById<Button>(R.id.stopButton).setOnClickListener {
            stopService(Intent(this, LocationSimulatorService::class.java))
            status.text = "Parado"
        }
    }

    private fun hasLocationPermission() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestPermissions() {
        val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= 33) permissions += Manifest.permission.POST_NOTIFICATIONS
        ActivityCompat.requestPermissions(this, permissions.toTypedArray(), requestCode)
        Toast.makeText(this, "Conceda a permissão e toque novamente", Toast.LENGTH_LONG).show()
    }
}
