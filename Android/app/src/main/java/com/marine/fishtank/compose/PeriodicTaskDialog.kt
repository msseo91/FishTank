package com.marine.fishtank.compose

import android.app.TimePickerDialog
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.marine.fishtank.R
import com.marine.fishtank.model.PeriodicTask
import com.marine.fishtank.util.TimeUtils
import com.orhanobut.logger.Logger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodicTaskDialog(
    onCancel: () -> Unit = {},
    onSave: (type: Int, value: Int, time: String) -> Unit = {_,_,_->}
) {
    val textOpen = stringResource(R.string.open)
    val textClose = stringResource(R.string.close)
    val typeOptionMap = mapOf(
        stringResource(R.string.out_valve) to PeriodicTask.TYPE_VALVE_OUT_WATER,
        stringResource(R.string.out_valve2) to PeriodicTask.TYPE_VALVE_OUT_WATER_2,
        stringResource(R.string.in_valve) to PeriodicTask.TYPE_VALVE_IN_WATER,
        stringResource(R.string.light) to PeriodicTask.TYPE_LIGHT,
        stringResource(R.string.co2) to PeriodicTask.TYPE_VALVE_CO2
    )
    var selectedType by remember { mutableStateOf(typeOptionMap.keys.first()) }

    val valueOptions = when(selectedType) {
        stringResource(R.string.out_valve),
        stringResource(R.string.out_valve2),
        stringResource(R.string.in_valve),
        stringResource(R.string.co2),
        stringResource(R.string.light)-> {
            listOf(textOpen, textClose)
        }
        else -> { emptyList() }
    }
    Logger.d("Composition. SelectedType=${selectedType}, valueOptions=${valueOptions}")

    var selectedValue by remember { mutableStateOf(textOpen) }
    var selectedTime by remember { mutableStateOf(TimeUtils.currentTimeHHmm()) }

    Dialog(onDismissRequest = onCancel) {
        Surface(
            modifier = Modifier
                .widthIn(250.dp, 350.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(15.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.padding(bottom = 40.dp),
                    text = stringResource(id = R.string.periodic_dialog_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                // Type Selector
                TextDropDownMenu(
                    label = stringResource(id = R.string.periodic_dialog_task_type),
                    textOptions = typeOptionMap.keys.toList(),
                    onOptionSelected = { selectedType = it }
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Value Selector
                TextDropDownMenu(
                    label = stringResource(id = R.string.periodic_dialog_task_value),
                    textOptions = valueOptions,
                    onOptionSelected = { selectedValue = it }
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Time Selector
                TextDropDownMenu(
                    isTimePicker = true,
                    label = stringResource(id = R.string.periodic_dialog_task_time),
                    textOptions = listOf(selectedTime),
                    onTimeSelected = { selectedTime = it}
                )

                // Buttons
                Row(modifier = Modifier.padding(5.dp)) {
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .padding(10.dp),
                        onClick = onCancel
                    ) {
                        Text(
                            text = stringResource(id = R.string.periodic_dialog_cancel)
                        )
                    }

                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .padding(10.dp),
                        onClick = {
                            Logger.d("Save type=${typeOptionMap[selectedType]}, value=${selectedValue}")
                            onSave(
                                typeOptionMap[selectedType]!!,
                                if(selectedValue == textOpen) {
                                    1
                                } else {
                                    0
                                },
                                selectedTime
                            )
                        }
                    ) {
                        Text(
                            text = stringResource(id = R.string.periodic_dialog_save)
                        )
                    }
                }
            }
        }
    }
}

/**
 * @param label Label of DropDown
 * @param textOptions Text of menu items
 * @param onOptionSelected Callback with selected menu item.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextDropDownMenu(
    isTimePicker: Boolean = false,
    label: String,
    textOptions: List<String>,
    onOptionSelected: (String) -> Unit = {},
    onTimeSelected: (String) -> Unit = {}
) {
    var text by remember { mutableStateOf(textOptions[0]) }
    var expanded by remember { mutableStateOf(false) }

    val source = remember { MutableInteractionSource() }
    val timePickerDialog = TimePickerDialog(
        LocalContext.current,
        { _, hour: Int, minute: Int ->
            text = TimeUtils.formatTimeHHmm(hour, minute)
            onTimeSelected(text)
        },
        TimeUtils.currentHour(),
        TimeUtils.currentMinute(),
        false
    )

    if (source.collectIsPressedAsState().value) {
        if(isTimePicker) {
            timePickerDialog.show()
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            if(!isTimePicker) {
                expanded = !expanded
            }
        }
    ) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor(),
            readOnly = true,
            value = text,
            onValueChange = {},
            label = { Text(label) },
            interactionSource = source,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            textOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option) },
                    onClick = {
                        onOptionSelected(option)
                        text = option
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_NO)
@Composable
fun PreviewPeriodicTaskDialog() {
    PeriodicTaskDialog()
}