package com.example.trafficreminder.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.trafficreminder.BuildConfig
import com.example.trafficreminder.data.model.TrafficResult
import com.example.trafficreminder.data.model.TrafficStatus
import com.example.trafficreminder.data.repository.ScheduleRepository
import com.example.trafficreminder.data.repository.TrafficRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TrafficCheckWorker(
    context: Context, params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val scheduleId = inputData.getInt("schedule_id", -1)
        val schedule = ScheduleRepository(applicationContext).getSchedule(scheduleId)
            ?: return Result.failure()

        val origin = getCurrentLocation()
            ?: return Result.retry()   // coba lagi kalau lokasi belum tersedia

        val result = TrafficRepository(BuildConfig.MAPS_API_KEY).getTrafficStatus(
            origin = origin,
            destination = "${schedule.destLat},${schedule.destLng}"
        )

        when (result) {
            is TrafficResult.Success -> {
                if (schedule.onlyIfDelay && result.status == TrafficStatus.CLEAR) {
                    return Result.success()
                }
                showTrafficNotification(schedule.label, result)
            }
            is TrafficResult.Error -> showErrorNotification(result.message)
        }
        return Result.success()
    }

    private suspend fun getCurrentLocation(): String? = withContext(Dispatchers.IO) {
        val granted = ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) return@withContext null

        try {
            val fused = LocationServices.getFusedLocationProviderClient(applicationContext)
            val loc = fused.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY, null
            ).await()
            loc?.let { "${it.latitude},${it.longitude}" }
        } catch (e: SecurityException) {
            null
        }
    }

    private fun showTrafficNotification(label: String, r: TrafficResult.Success) {
        ensureChannel()

        val (title, emoji) = when (r.status) {
            TrafficStatus.HEAVY    -> "Macet Parah!" to "🚨"
            TrafficStatus.MODERATE -> "Ada Kemacetan" to "⚠️"
            TrafficStatus.LIGHT    -> "Sedikit Padat" to "🚗"
            TrafficStatus.CLEAR    -> "Lancar!" to "✅"
        }

        val body = if (r.delayMinutes > 0) {
            "$emoji $label: estimasi ${r.trafficMinutes} menit " +
            "(normalnya ${r.normalMinutes} menit, +${r.delayMinutes} menit)"
        } else {
            "$emoji $label: lancar, estimasi ${r.trafficMinutes} menit."
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                applicationContext, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        NotificationManagerCompat.from(applicationContext).notify(NOTIF_ID, notification)
    }

    private fun showErrorNotification(message: String) {
        ensureChannel()
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("Gagal cek lalu lintas")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(NOTIF_ERROR_ID, notification)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                manager.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID, "Traffic Reminder",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Pengingat kondisi lalu lintas sebelum berangkat"
                    }
                )
            }
        }
    }

    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
        addOnSuccessListener { cont.resume(it) }
        addOnFailureListener { cont.resumeWithException(it) }
    }

    companion object {
        const val CHANNEL_ID = "traffic_reminder_channel"
        const val NOTIF_ID = 1001
        const val NOTIF_ERROR_ID = 1002
    }
}
