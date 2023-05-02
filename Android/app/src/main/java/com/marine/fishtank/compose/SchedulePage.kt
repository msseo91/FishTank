package com.marine.fishtank.compose

import android.app.TimePickerDialog
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.marine.fishtank.R
import com.marine.fishtank.api.TankResult
import com.marine.fishtank.model.PeriodicTask
import com.marine.fishtank.model.typeAsString
import com.orhanobut.logger.Logger
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulePage(
    periodicTaskResult: TankResult<List<PeriodicTask>>,
    onAddPeriodicTask: (PeriodicTask) -> Unit = {},
    onDeletePeriodicTask: (Int) -> Unit = {}
) {
    Logger.d("Composing SchedulePage!")
    val periodicTasks = when (periodicTaskResult) {
        is TankResult.Loading -> {
            Logger.d("Loading")
            listOf()
        }
        is TankResult.Success -> {
            Logger.d("Success")
            periodicTaskResult.data
        }
        is TankResult.Error -> {
            Logger.d("Error")
            listOf()
        }
    }

    val context = LocalContext.current
    val openDialog = remember { mutableStateOf(false) }

    val typeExpand = remember { mutableStateOf(false) }
    val typeOptions = listOf(R.string.out_valve, R.string.in_valve, R.string.light_brightness)
    val selectedTypeOption = remember { mutableStateOf(typeOptions[0]) }

    val valueBooleanExpand = remember { mutableStateOf(false) }
    val valueBooleanOptions = arrayOf(0, 1)

    val valueLightExpand = remember { mutableStateOf(false) }
    val valueLightOptions = (0..100).toList().toTypedArray()
    val selectedOption = remember { mutableStateOf(valueLightOptions[0]) }

    val mCalendar = Calendar.getInstance()
    val currentHour = mCalendar[Calendar.HOUR_OF_DAY]
    val currentMinute = mCalendar[Calendar.MINUTE]
    val actionTime = remember { mutableStateOf("$currentHour:$currentMinute") }

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour: Int, minute: Int ->
            actionTime.value = "$hour:${String.format("%02d", minute)}"
        }, currentHour, currentMinute, false
    )

    PeriodicTaskDialog(
        openState = openDialog,
        typeExpand = typeExpand,
        typeOptions = typeOptions,
        selectedTypeOption = selectedTypeOption,
        valueBooleanExpand = valueBooleanExpand,
        valueBooleanOptions = valueBooleanOptions,
        valueLightExpand = valueLightExpand,
        valueLightOptions = valueLightOptions,
        selectedOption = selectedOption,
        actionTime = actionTime,
        timePickerDialog = timePickerDialog,
    ) {
        onAddPeriodicTask(
            PeriodicTask(
                type = PeriodicTask.typeFromResource(selectedTypeOption.value),
                data = selectedOption.value,
                time = actionTime.value
            )
        )
    }

    // Floating button
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { openDialog.value = true }) {
                Icon(Icons.Filled.Add, "PeriodicTask add button")
            }
        }
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).fillMaxSize()
        ) {
            for (task in periodicTasks) {
                item {
                    PeriodicTaskItem(task = task, deleteCallback = onDeletePeriodicTask)
                }
            }
        }
    }
}

@Composable
fun PeriodicTaskItem(task: PeriodicTask, deleteCallback: (Int) -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(40.dp, 60.dp)
            .border(width = 1.dp, color = Color.Black)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = task.typeAsString(LocalContext.current))
        Spacer(modifier = Modifier.width(15.dp))
        Text(text = "data=${task.data}")
        Spacer(modifier = Modifier.width(15.dp))
        Text(text = task.time)

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Button(
                onClick = { deleteCallback(task.id) },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    Icons.Rounded.DeleteOutline,
                    contentDescription = "Delete periodic task.",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
            }
        }
    }
}
