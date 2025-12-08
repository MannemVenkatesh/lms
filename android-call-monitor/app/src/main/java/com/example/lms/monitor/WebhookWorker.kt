package com.example.lms.monitor

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.Date

class WebhookWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val caller = inputData.getString("caller") ?: return Result.failure()
        
        val prefs = applicationContext.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val serverUrl = prefs.getString(MainActivity.KEY_SERVER_URL, "")
        val targetNumber = prefs.getString(MainActivity.KEY_TARGET_NUMBER, "")
        
        if (serverUrl.isNullOrEmpty() || targetNumber.isNullOrEmpty()) {
            log("Config missing. Cannot send webhook.")
            return Result.failure()
        }
        
        log("Sending webhook for $caller to $serverUrl")

        try {
            val retrofit = Retrofit.Builder()
                .baseUrl(serverUrl) // User must ensure trailing slash if needed, or we handle it
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val apiService = retrofit.create(LmsApiService::class.java)
            
            // Generate a fake CallSid
            val callSid = "android_" + System.currentTimeMillis()
            
            val response = apiService.sendMissedCall(caller, targetNumber, callSid).execute()
            
            if (response.isSuccessful) {
                log("Webhook SUCCESS: ${response.code()}")
                return Result.success()
            } else {
                log("Webhook FAILED: ${response.code()} ${response.message()}")
                return Result.retry()
            }
        } catch (e: Exception) {
            log("Webhook ERROR: ${e.message}")
            return Result.retry()
        }
    }

    private fun log(message: String) {
        val prefs = applicationContext.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val currentLogs = prefs.getString(MainActivity.KEY_LOGS, "")
        
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(Date())
        val logLine = "[$timestamp] [Worker] $message\n"
        val newLogs = logLine + currentLogs
        
        val trimmedLogs = if (newLogs.length > 5000) newLogs.substring(0, 5000) else newLogs
        
        prefs.edit().putString(MainActivity.KEY_LOGS, trimmedLogs).apply()
    }
}
