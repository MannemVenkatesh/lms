package com.example.lms.monitor

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

interface LmsApiService {
    @POST("/api/availability/webhook")
    fun sendMissedCall(
        @Query("Caller") caller: String,
        @Query("To") to: String,
        @Query("CallSid") callSid: String
    ): Call<String>
}
