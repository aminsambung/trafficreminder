package com.example.trafficreminder.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.trafficreminder.data.model.TrafficSchedule
import java.time.LocalTime

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddScheduleScreen(
    onSave: (TrafficSchedule) -> Unit,
    onPickLocation: () -> Unit
) {
    var label by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(5, 50)) }
    var selectedDays by remember { mutableStateOf(setOf(1, 2, 3, 4, 5)) }
    var onlyIfDelay by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = label,
            onValueChange = { label = it },
            label = { Text("Nama (misal: Ke Kantor)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedButton(onClick = onPickLocation, modifier = Modifier.fillMaxWidth()) {
            Text(
                if (selectedLocation == null) "Pilih Tujuan"
                else "Tujuan: ${selectedLocation!!.first}, ${selectedLocation!!.second}"
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Jam pengingat: ", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.width(8.dp))
            TimePickerButton(
                time = selectedTime,
                onTimeChange = { selectedTime = it }
            )
        }

        Text("Ulangi pada:", style = MaterialTheme.typography.bodyLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val dayNames = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
            dayNames.forEachIndexed { i, name ->
                FilterChip(
                    selected = (i + 1) in selectedDays,
                    onClick = {
                        selectedDays = if ((i + 1) in selectedDays)
                            selectedDays - (i + 1)
                        else selectedDays + (i + 1)
                    },
                    label = { Text(name) }
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = onlyIfDelay, onCheckedChange = { onlyIfDelay = it })
            Spacer(Modifier.width(8.dp))
            Text("Hanya kabari kalau macet")
        }

        Button(
            onClick = {
                val loc = selectedLocation ?: return@Button
                onSave(
                    TrafficSchedule(
                        id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
                        label = label.ifBlank { "Tujuan" },
                        destLat = loc.first,
                        destLng = loc.second,
                        hour = selectedTime.hour,
                        minute = selectedTime.minute,
                        daysOfWeek = selectedDays,
                        onlyIfDelay = onlyIfDelay
                    )
                )
            },
            enabled = selectedLocation != null && selectedDays.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Simpan Jadwal")
        }
    }
}
