package com.example.trafficreminder.ui

import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.time.LocalTime

/**
 * Versi sederhana: tap → naik 10 menit.
 * Untuk time picker native (Material3 TimePickerDialog) bisa ditambahkan nanti.
 */
@Composable
fun TimePickerButton(
    time: LocalTime,
    onTimeChange: (LocalTime) -> Unit
) {
    OutlinedButton(onClick = { onTimeChange(time.plusMinutes(10)) }) {
        Text("%02d:%02d".format(time.hour, time.minute))
    }
}
