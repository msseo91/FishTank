package com.marine.fishtank.compose

import android.app.TimePickerDialog
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.marine.fishtank.R

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PeriodicTaskDialog(
    openState: MutableState<Boolean>,
    typeExpand: MutableState<Boolean>,
    typeOptions: List<Int>,
    selectedTypeOption: MutableState<Int>,
    valueBooleanExpand: MutableState<Boolean>,
    valueBooleanOptions: Array<Int>,
    valueLightExpand: MutableState<Boolean>,
    valueLightOptions: Array<Int>,
    selectedOption: MutableState<Int>,
    actionTime: MutableState<String>,
    timePickerDialog: TimePickerDialog,
    onSave: () -> Unit
) {
    val source = remember {
        MutableInteractionSource()
    }

    if (source.collectIsPressedAsState().value) {
        timePickerDialog.show()
    }

    if (openState.value) {
        Dialog(onDismissRequest = {
            openState.value = false
        }) {
            Surface(
                modifier = Modifier
                    .width(250.dp)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(15.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(
                        Modifier
                            .height(5.dp)
                            .fillMaxWidth()
                    )

                    Text(
                        textAlign = TextAlign.Center,
                        text = stringResource(id = R.string.periodic_dialog_title),
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(25.dp))

                    TextDropDownMenu(
                        expanded = typeExpand,
                        text = stringResource(selectedTypeOption.value),
                        label = stringResource(id = R.string.periodic_dialog_task_type),
                        options = typeOptions,
                        onOptionSelected = { option ->
                            typeExpand.value = false
                            selectedTypeOption.value = option
                        }
                    )

                    Divider(Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(15.dp))

                    if (selectedTypeOption.value != R.string.light_brightness) {
                        ExposedDropdownMenuBox(
                            expanded = valueBooleanExpand.value,
                            onExpandedChange = { valueBooleanExpand.value = !valueBooleanExpand.value }
                        ) {
                            TextField(
                                readOnly = true,
                                value = selectedOption.value.toString(),
                                onValueChange = {},
                                label = { Text(stringResource(id = R.string.periodic_dialog_task_value)) },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = valueBooleanExpand.value
                                    )
                                },
                                colors = ExposedDropdownMenuDefaults.textFieldColors()
                            )

                            ExposedDropdownMenu(
                                expanded = valueBooleanExpand.value,
                                onDismissRequest = { valueBooleanExpand.value = false }
                            ) {
                                valueBooleanOptions.forEach { option ->
                                    DropdownMenuItem(onClick = {
                                        valueBooleanExpand.value = false
                                        selectedOption.value = option
                                    }) {
                                        Text(text = option.toString())
                                    }
                                }
                            }
                        }
                    } else {
                        ExposedDropdownMenuBox(
                            expanded = valueLightExpand.value,
                            onExpandedChange = { valueLightExpand.value = !valueLightExpand.value }
                        ) {
                            TextField(
                                readOnly = true,
                                value = selectedOption.value.toString(),
                                onValueChange = {},
                                label = { Text(stringResource(id = R.string.periodic_dialog_task_value)) },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = valueLightExpand.value
                                    )
                                },
                                colors = ExposedDropdownMenuDefaults.textFieldColors()
                            )

                            ExposedDropdownMenu(
                                expanded = valueLightExpand.value,
                                onDismissRequest = { valueLightExpand.value = false }
                            ) {
                                valueLightOptions.forEach { option ->
                                    DropdownMenuItem(onClick = {
                                        valueLightExpand.value = false
                                        selectedOption.value = option
                                    }) {
                                        Text(text = "$option%")
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(15.dp))

                    TextField(
                        readOnly = true,
                        value = actionTime.value,
                        interactionSource = source,
                        onValueChange = {},
                        label = { Text(stringResource(id = R.string.periodic_dialog_task_time)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = typeExpand.value
                            )
                        },
                        colors = ExposedDropdownMenuDefaults.textFieldColors()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                openState.value = false
                            }
                        ) {
                            Text(stringResource(id = R.string.periodic_dialog_cancel))
                        }

                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                openState.value = false
                                onSave()
                            }
                        ) {
                            Text(stringResource(id = R.string.periodic_dialog_save))
                        }
                    }

                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TextDropDownMenu(
    expanded: MutableState<Boolean>,
    text: String,
    label: String,
    options: List<Int>,
    onOptionSelected: (Int) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded.value,
        onExpandedChange = { expanded.value = !expanded.value }
    ) {
        TextField(
            readOnly = true,
            value = text,
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded.value
                )
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(onClick = { onOptionSelected(option) }) {
                    Text(text = stringResource(id = option))
                }
            }
        }
    }
}