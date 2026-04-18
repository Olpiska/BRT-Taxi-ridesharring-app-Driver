package com.example.brtaxidriver.data.api

import com.example.brtaxidriver.data.model.RideResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @FormUrlEncoded
    @POST("send_otp.php")
    suspend fun sendOtp(
        @Field("phone") phone: String
    ): Response<GenericResponse>

    @FormUrlEncoded
    @POST("verify_otp.php")
    suspend fun verifyOtp(
        @Field("phone") phone: String,
        @Field("otp") otp: String
    ): Response<LoginResponse>

    @FormUrlEncoded
    @POST("register_driver.php")
    suspend fun registerDriver(
        @Field("phone") phone: String,
        @Field("first_name") firstName: String,
        @Field("last_name") lastName: String,
        @Field("gender") gender: String,
        @Field("nationality") nationality: String
    ): Response<LoginResponse>

    @FormUrlEncoded
    @POST("upload_documents.php")
    suspend fun uploadDocuments(
        @Field("driver_id") driverId: Int,
        @Field("has_id_card") hasIdCard: Boolean,
        @Field("has_medical_check") hasMedical: Boolean,
        @Field("has_psychotechnical") hasPsycho: Boolean,
        @Field("has_criminal_poland") hasCriminalPl: Boolean,
        @Field("has_criminal_other") hasCriminalOther: Boolean
    ): Response<GenericResponse>

    @FormUrlEncoded
    @POST("update_driver_location.php")
    suspend fun updateLocation(
        @Field("driver_id") driverId: Int,
        @Field("lat") lat: Double,
        @Field("lng") lng: Double
    ): Response<GenericResponse>

    @GET("get_avaible_trips.php")
    suspend fun getAvailableRides(): Response<List<RideResponse>>

    @FormUrlEncoded
    @POST("accept_trip.php")
    suspend fun acceptRide(
        @Field("ride_id") rideId: Int,
        @Field("driver_id") driverId: Int
    ): Response<GenericResponse>

    @FormUrlEncoded
    @POST("update_ride_status.php")
    suspend fun updateRideStatus(
        @Field("ride_id") rideId: Int,
        @Field("status") status: String
    ): Response<GenericResponse>

    @GET("get_driver_stats.php")
    suspend fun getDriverStats(
        @Query("driver_id") driverId: Int
    ): Response<DriverStatsResponse>

    @GET("get_ride_history.php")
    suspend fun getRideHistory(
        @Query("driver_id") driverId: Int
    ): Response<List<RideResponse>>
}

data class LoginResponse(
    val success: Boolean,
    val driver_id: Int?,
    val message: String?
)

data class GenericResponse(
    val success: Boolean,
    val message: String?
)

data class DriverStatsResponse(
    val total_trips: Int,
    val total_distance: Double,
    val total_earnings: Double,
    val rating: Double,
    val acceptance_rate: Int
)
