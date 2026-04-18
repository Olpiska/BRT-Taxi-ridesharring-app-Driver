package com.example.brtaxidriver.data.model

import com.google.gson.annotations.SerializedName

data class RideResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("driver_id") val driverId: Int?,
    @SerializedName("pickup_lat") val pickupLat: Double,
    @SerializedName("pickup_lng") val pickupLng: Double,
    @SerializedName("dropoff_lat") val dropoffLat: Double,
    @SerializedName("dropoff_lng") val dropoffLng: Double,
    @SerializedName("status") val status: String,
    @SerializedName("fare") val fare: Double?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("passenger_phone") val passengerPhone: String? // Yolcunun telefon numarası
)
