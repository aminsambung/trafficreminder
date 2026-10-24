package com.example.trafficreminder.data.model

data class TrafficSchedule(
    val id: Int,
    val label: String,
    val destLat: Double,
    val destLng: Double,
    val hour: Int,
    val minute: Int,
    val daysOfWeek: Set<Int>,     // 1=Senin ... 7=Minggu
    val enabled: Boolean = true,
    val onlyIfDelay: Boolean = false
)
