package com.example.trafficreminder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.core.content.ContextCompat
import com.example.trafficreminder.alarm.DailyAlarmScheduler
import com.example.trafficreminder.data.repository.ScheduleRepository
import com.example.trafficreminder.ui.AddScheduleScreen

class MainActivity : ComponentActivity() {

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* hasil bisa diabaikan, worker akan fallback */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        askPermissions()

        val repo = ScheduleRepository(this)

        setContent {
            MaterialTheme {
                Surface {
                    AddScheduleScreen(
                        onSave = { schedule ->
                            repo.saveSchedule(schedule)
                            DailyAlarmScheduler.scheduleNext(this, schedule)
                        },
                        onPickLocation = {
                            // TODO: buka Place Autocomplete di bagian lanjutan
                        }
                    )
                }
            }
        }
    }

    private fun askPermissions() {
        val perms = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms += Manifest.permission.POST_NOTIFICATIONS
        }
        val toAsk = perms.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (toAsk.isNotEmpty()) requestPermissions.launch(toAsk.toTypedArray())
    }
}
