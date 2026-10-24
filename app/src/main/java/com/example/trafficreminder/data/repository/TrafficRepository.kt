package com.example.trafficreminder.data.repository

import com.example.trafficreminder.data.model.TrafficResult
import com.example.trafficreminder.data.model.TrafficStatus
import com.example.trafficreminder.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class TrafficRepository(private val apiKey: String) {

    suspend fun getTrafficStatus(
        origin: String,
        destination: String
    ): TrafficResult = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiService.getDirections(
                origin = origin,
                destination = destination,
                departureTime = System.currentTimeMillis() / 1000,
                apiKey = apiKey
            )

            if (response.status != "OK") {
                return@withContext TrafficResult.Error("API status: ${response.status ?: "UNKNOWN"}")
            }

            val leg = response.routes?.firstOrNull()?.legs?.firstOrNull()
                ?: return@withContext TrafficResult.Error("Rute tidak ditemukan")

            val normalSec = leg.duration?.value ?: 0
            val trafficSec = leg.durationInTraffic?.value ?: normalSec
            val delaySec = (trafficSec - normalSec).coerceAtLeast(0)

            val status = when {
                normalSec > 0 && delaySec >= normalSec * 0.5 -> TrafficStatus.HEAVY
                normalSec > 0 && delaySec >= normalSec * 0.2 -> TrafficStatus.MODERATE
                delaySec >= 60 -> TrafficStatus.LIGHT
                else -> TrafficStatus.CLEAR
            }

            TrafficResult.Success(
                status = status,
                normalMinutes = normalSec / 60,
                trafficMinutes = trafficSec / 60,
                delayMinutes = delaySec / 60,
                destination = leg.endAddress ?: "Tujuan"
            )
        } catch (e: IOException) {
            TrafficResult.Error("Tidak ada koneksi internet")
        } catch (e: Exception) {
            TrafficResult.Error("Gagal cek traffic: ${e.message}")
        }
    }
}
