package com.example.lms.monitor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.lms.monitor.R
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var etServerUrl: TextInputEditText
    private lateinit var etTargetNumber: TextInputEditText
    private lateinit var tvLogs: TextView

    companion object {
        const val PREFS_NAME = "LmsMonitorPrefs"
        const val KEY_SERVER_URL = "server_url"
        const val KEY_TARGET_NUMBER = "target_number"
        const val KEY_LOGS = "app_logs"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etServerUrl = findViewById(R.id.etServerUrl)
        etTargetNumber = findViewById(R.id.etTargetNumber)
        tvLogs = findViewById(R.id.tvLogs)

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        etServerUrl.setText(prefs.getString(KEY_SERVER_URL, ""))
        etTargetNumber.setText(prefs.getString(KEY_TARGET_NUMBER, ""))
        tvLogs.text = prefs.getString(KEY_LOGS, "Waiting for calls...")

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            val editor = prefs.edit()
            editor.putString(KEY_SERVER_URL, etServerUrl.text.toString())
            editor.putString(KEY_TARGET_NUMBER, etTargetNumber.text.toString())
            editor.apply()
            
            log("Configuration saved.")
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnPermissions).setOnClickListener {
            checkAndRequestPermissions()
        }

        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG),
                101)
        }
    }
    
    // Helper to append logs (also saved to prefs for persistence across restarts)
    private fun log(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val logLine = "[$timestamp] $message\n"
        
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentLogs = prefs.getString(KEY_LOGS, "")
        val newLogs = logLine + currentLogs
        
        // Limit log size ~5000 chars
        val trimmedLogs = if (newLogs.length > 5000) newLogs.substring(0, 5000) else newLogs
        
        prefs.edit().putString(KEY_LOGS, trimmedLogs).apply()
        
        runOnUiThread {
            tvLogs.text = trimmedLogs
        }
    }
}
