package com.example.trafficreminder.data.model

enum class TrafficStatus { CLEAR, LIGHT, MODERATE, HEAVY }

sealed class TrafficResult {
    data class Success(
        val status: TrafficStatus,
        val normalMinutes: Int,
        val trafficMinutes: Int,
        val delayMinutes: Int,
        val destination: String
    ) : TrafficResult()

    data class Error(val message: String) : TrafficResult()
}
