package com.example.trafficreminder.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.trafficreminder.data.repository.ScheduleRepository

/**
 * Reschedule semua alarm setelah HP restart,
 * karena AlarmManager hilang saat reboot.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val schedules = ScheduleRepository(context).getAllSchedules()
        schedules.filter { it.enabled }.forEach {
            DailyAlarmScheduler.scheduleNext(context, it)
        }
    }
}
