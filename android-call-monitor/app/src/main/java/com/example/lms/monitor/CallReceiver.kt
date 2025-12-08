package com.example.lms.monitor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Date

class CallReceiver : BroadcastReceiver() {

    companion object {
        private var lastState = TelephonyManager.CALL_STATE_IDLE
        private var callStartTime: Date? = null
        private var isIncoming: Boolean = false
        private var savedNumber: String? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            var state = 0
            
            if (stateStr == TelephonyManager.EXTRA_STATE_IDLE) {
                state = TelephonyManager.CALL_STATE_IDLE
            } else if (stateStr == TelephonyManager.EXTRA_STATE_OFFHOOK) {
                state = TelephonyManager.CALL_STATE_OFFHOOK
            } else if (stateStr == TelephonyManager.EXTRA_STATE_RINGING) {
                state = TelephonyManager.CALL_STATE_RINGING
            }
            
            checkCallState(context, state, number)
        }
    }

    private fun checkCallState(context: Context, state: Int, number: String?) {
        if (lastState == state) {
            return
        }
        
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                isIncoming = true
                callStartTime = Date()
                savedNumber = number
                log(context, "Incoming Call from $number")
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                if (isIncoming) {
                    log(context, "Call Answered")
                    isIncoming = false 
                }
            }
            TelephonyManager.CALL_STATE_IDLE -> {
                if (isIncoming) {
                   log(context, "Call Missed/Rejected from $savedNumber")
                   // Trigger Webhook
                   if (savedNumber != null) {
                       triggerWebhook(context, savedNumber!!)
                   }
                }
                isIncoming = false
            }
        }
        lastState = state
    }
    
    private fun triggerWebhook(context: Context, caller: String) {
        val workManager = WorkManager.getInstance(context)
        val data = Data.Builder()
            .putString("caller", caller)
            .build()
            
        val request = OneTimeWorkRequestBuilder<WebhookWorker>()
            .setInputData(data)
            .build()
            
        workManager.enqueue(request)
        log(context, "Queued Webhook for $caller")
    }
    
    // Simple helper logging (duplicated for simplicity in this context)
    private fun log(context: Context, message: String) {
        val prefs = context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val currentLogs = prefs.getString(MainActivity.KEY_LOGS, "")
        
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(Date())
        val logLine = "[$timestamp] $message\n"
        val newLogs = logLine + currentLogs
        
         // Limit log size 
        val trimmedLogs = if (newLogs.length > 5000) newLogs.substring(0, 5000) else newLogs
        
        prefs.edit().putString(MainActivity.KEY_LOGS, trimmedLogs).apply()
    }
}
