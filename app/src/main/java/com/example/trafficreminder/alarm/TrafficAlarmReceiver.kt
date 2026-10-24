package com.example.trafficreminder.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.trafficreminder.data.repository.ScheduleRepository
import com.example.trafficreminder.worker.TrafficCheckWorker

class TrafficAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getIntExtra("schedule_id", -1)
        if (scheduleId == -1) return

        // 1. Trigger WorkManager untuk cek traffic
        val work = OneTimeWorkRequestBuilder<TrafficCheckWorker>()
            .setInputData(workDataOf("schedule_id" to scheduleId))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "traffic_check_$scheduleId",
            ExistingWorkPolicy.REPLACE,
            work
        )

        // 2. Jadwalkan ulang untuk besok / hari aktif berikutnya
        val schedule = ScheduleRepository(context).getSchedule(scheduleId)
        schedule?.let { DailyAlarmScheduler.scheduleNext(context, it) }
    }
}
