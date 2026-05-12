package com.raktaseva.connect.data.remote

import com.raktaseva.connect.data.model.ChatMessage
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("api/chat")
    suspend fun chat(@Body request: ChatRequest): ChatResponse

    @POST("api/notify-donors")
    suspend fun notifyDonors(@Body request: NotifyRequest): NotifyResponse

    data class ChatRequest(
        val messages: List<Map<String, String>>
    )
    data class ChatResponse(val reply: String)

    data class NotifyRequest(
        val requestId: String,
        val bloodGroup: String,
        val lat: Double,
        val lng: Double,
        val radiusKm: Double,
        val hospitalName: String
    )
    data class NotifyResponse(
        val notifiedCount: Int,
        val deliveredIn: Long
    )

    companion object {
        fun create(): ApiService =
            RetrofitClient.instance.create(ApiService::class.java)
    }
}