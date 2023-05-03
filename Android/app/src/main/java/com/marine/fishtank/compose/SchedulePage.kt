package com.marine.fishtank.compose

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Header
            item {
                PeriodicTaskRow(
                    isHeader = true,
                    taskType = "Type",
                    taskValue = "Value",
                    taskTime = "Time",
                    showDeleteButton = false
                )
            }

            items(periodicTasks) { task ->
                PeriodicTaskItem(task = task, deleteCallback = onDeletePeriodicTask)
            }
        }
    }
}

@Composable
fun PeriodicTaskRow(
    isHeader: Boolean = false,
    id: Int = 0,
    taskType: String,
    taskValue: String,
    taskTime: String,
    showDeleteButton: Boolean = true,
    deleteCallback: (Int) -> Unit = {}
) {
    val rowModifier = if(isHeader) {
        Modifier.background(color = MaterialTheme.colorScheme.secondaryContainer).padding(10.dp)
    } else {
        Modifier.padding(10.dp)
    }
    val fontWeight = if(isHeader) FontWeight.Bold else FontWeight.Normal

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(6f),
            text = taskType,
            fontWeight = fontWeight
        )
        Text(
            modifier = Modifier.weight(3f),
            text = taskValue,
            fontWeight = fontWeight
        )

        Text(
            modifier = Modifier.weight(3f),
            text = taskTime,
            fontWeight = fontWeight
        )

        Spacer(modifier = Modifier.weight(1f))

        if(showDeleteButton) {
            Button(
                onClick = { deleteCallback(id) },
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

@Composable
fun PeriodicTaskItem(task: PeriodicTask, deleteCallback: (Int) -> Unit = {}) {
    PeriodicTaskRow(
        id = task.id,
        taskType = task.typeAsString(LocalContext.current),
        taskValue = "data=${task.data}",
        taskTime = task.time,
        deleteCallback = deleteCallback
    )
}

@Preview
@Composable
fun PreviewSchedulePage() {
    SchedulePage(
        periodicTaskResult = TankResult.Success(
            listOf(
                PeriodicTask(type = PeriodicTask.TYPE_LIGHT, data = 100, time = "18:00"),
                PeriodicTask(type = PeriodicTask.TYPE_LIGHT, data = 0, time = "23:00"),

                PeriodicTask(type = PeriodicTask.TYPE_VALVE_OUT_WATER, data = 0, time = "10:00"),
                PeriodicTask(type = PeriodicTask.TYPE_VALVE_OUT_WATER, data = 1, time = "11:00")
            )
        )
    )
}