package com.example.brtaxidriver.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brtaxidriver.data.api.DriverStatsResponse
import com.example.brtaxidriver.data.api.RetrofitClient
import com.example.brtaxidriver.data.model.RideResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class LoginStep {
    PHONE, OTP, REGISTER, DOCUMENTS, LOGGED_IN
}

class MainViewModel : ViewModel() {

    private val _loginStep = MutableStateFlow(LoginStep.PHONE)
    val loginStep: StateFlow<LoginStep> = _loginStep

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber

    private val _driverId = MutableStateFlow<Int?>(null)
    val driverId: StateFlow<Int?> = _driverId

    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline

    private val _availableRides = MutableStateFlow<List<RideResponse>>(emptyList())
    val availableRides: StateFlow<List<RideResponse>> = _availableRides

    private val _currentRide = MutableStateFlow<RideResponse?>(null)
    val currentRide: StateFlow<RideResponse?> = _currentRide

    // Stats
    private val _earnings = MutableStateFlow(0.0)
    val earnings: StateFlow<Double> = _earnings

    private val _driverScore = MutableStateFlow(5.0)
    val driverScore: StateFlow<Double> = _driverScore

    private val _acceptanceRate = MutableStateFlow(100)
    val acceptanceRate: StateFlow<Int> = _acceptanceRate

    private val _nationality = MutableStateFlow("Poland")
    val nationality: StateFlow<String> = _nationality

    // Account Stats
    private val _accountStats = MutableStateFlow<DriverStatsResponse?>(null)
    val accountStats: StateFlow<DriverStatsResponse?> = _accountStats

    private val _rideHistory = MutableStateFlow<List<RideResponse>>(emptyList())
    val rideHistory: StateFlow<List<RideResponse>> = _rideHistory

    private var pollingJob: Job? = null

    fun setPhoneNumber(phone: String) {
        _phoneNumber.value = phone
    }

    fun sendOtp() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.sendOtp(_phoneNumber.value)
                if (response.isSuccessful) {
                    _loginStep.value = LoginStep.OTP
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error sending OTP", e)
            }
        }
    }

    fun verifyOtp(otp: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.verifyOtp(_phoneNumber.value, otp)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        if (body.driver_id != null && body.driver_id != 0) {
                            _driverId.value = body.driver_id
                            _loginStep.value = LoginStep.LOGGED_IN
                            fetchAccountData()
                        } else {
                            _loginStep.value = LoginStep.REGISTER
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error verifying OTP", e)
            }
        }
    }

    fun registerDriver(firstName: String, lastName: String, gender: String, nationality: String) {
        _nationality.value = nationality
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.registerDriver(
                    phone = _phoneNumber.value,
                    firstName = firstName,
                    lastName = lastName,
                    gender = gender,
                    nationality = nationality
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    _driverId.value = response.body()?.driver_id
                    _loginStep.value = LoginStep.DOCUMENTS
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error registering driver", e)
            }
        }
    }

    fun uploadDocuments(
        hasId: Boolean,
        hasMedical: Boolean,
        hasPsycho: Boolean,
        hasCriminalPl: Boolean,
        hasCriminalOther: Boolean
    ) {
        val id = _driverId.value ?: return
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.uploadDocuments(
                    driverId = id,
                    hasIdCard = hasId,
                    hasMedical = hasMedical,
                    hasPsycho = hasPsycho,
                    hasCriminalPl = hasCriminalPl,
                    hasCriminalOther = hasCriminalOther
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    _loginStep.value = LoginStep.LOGGED_IN
                    fetchAccountData()
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error uploading docs", e)
            }
        }
    }

    fun fetchAccountData() {
        val id = _driverId.value ?: return
        viewModelScope.launch {
            try {
                val statsResponse = RetrofitClient.apiService.getDriverStats(id)
                if (statsResponse.isSuccessful) {
                    _accountStats.value = statsResponse.body()
                    _earnings.value = statsResponse.body()?.total_earnings ?: 0.0
                    _driverScore.value = statsResponse.body()?.rating ?: 5.0
                    _acceptanceRate.value = statsResponse.body()?.acceptance_rate ?: 100
                }

                val historyResponse = RetrofitClient.apiService.getRideHistory(id)
                if (historyResponse.isSuccessful) {
                    _rideHistory.value = historyResponse.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error fetching account data", e)
            }
        }
    }

    fun updateRideStatus(status: String) {
        val ride = _currentRide.value ?: return
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.updateRideStatus(ride.id, status)
                if (response.isSuccessful) {
                    // Update local state
                    _currentRide.value = ride.copy(status = status)
                    if (status == "completed" || status == "cancelled") {
                        _currentRide.value = null
                        startPolling() // Start looking for rides again
                        fetchAccountData() // Refresh earnings
                    }
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error updating status", e)
            }
        }
    }

    fun toggleOnline() {
        _isOnline.value = !_isOnline.value
        if (_isOnline.value) {
            startPolling()
        } else {
            stopPolling()
        }
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                fetchRides()
                delay(5000)
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        _availableRides.value = emptyList()
    }

    private suspend fun fetchRides() {
        try {
            val response = RetrofitClient.apiService.getAvailableRides()
            if (response.isSuccessful) {
                _availableRides.value = response.body() ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error fetching rides", e)
        }
    }

    fun updateLocation(lat: Double, lng: Double) {
        val id = _driverId.value ?: return
        if (!_isOnline.value) return
        
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.updateLocation(id, lat, lng)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error updating location", e)
            }
        }
    }

    fun acceptRide(ride: RideResponse) {
        val id = _driverId.value ?: return
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.acceptRide(ride.id, id)
                if (response.isSuccessful) {
                    _currentRide.value = ride.copy(status = "accepted")
                    stopPolling()
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error accepting ride", e)
            }
        }
    }
}
