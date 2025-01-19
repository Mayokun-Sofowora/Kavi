package com.mayor.kavi.data.service

import com.mayor.kavi.data.models.detection.DetectionResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface RoboflowService {
    @Multipart
    @POST("{model}/{version}")
    suspend fun detectDice(
        @Path("model") model: String = "kavi-zbra1",
        @Path("version") version: String = "1",
        @Query("api_key") apiKey: String,
        @Part file: MultipartBody.Part,
        @Query("confidence") confidence: Int = 40,
        @Query("overlap") overlap: Int = 30,
        @Query("format") format: String = "json",
        @Query("labels") labels: Boolean = true
    ): Response<DetectionResponse>
}
