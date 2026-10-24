package com.example.trafficreminder.data.repository

import android.content.Context
import com.example.trafficreminder.data.model.TrafficSchedule
import com.google.gson.Gson

class ScheduleRepository(context: Context) {
    private val prefs = context.getSharedPreferences("traffic_schedules", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveSchedule(schedule: TrafficSchedule) {
        prefs.edit().putString("schedule_${schedule.id}", gson.toJson(schedule)).apply()
    }

    fun deleteSchedule(id: Int) {
        prefs.edit().remove("schedule_$id").apply()
    }

    fun getAllSchedules(): List<TrafficSchedule> {
        return prefs.all.values.mapNotNull { value ->
            runCatching { gson.fromJson(value as String, TrafficSchedule::class.java) }.getOrNull()
        }
    }

    fun getSchedule(id: Int): TrafficSchedule? {
        val json = prefs.getString("schedule_$id", null) ?: return null
        return runCatching { gson.fromJson(json, TrafficSchedule::class.java) }.getOrNull()
    }
}
