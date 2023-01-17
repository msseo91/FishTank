package com.marine.fishtank.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.marine.fishtank.R
import com.marine.fishtank.model.Status
import com.marine.fishtank.viewmodel.FishTankViewModel
import com.orhanobut.logger.Logger


@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun ControlPage(viewModel: FishTankViewModel) {
    val dataSource by viewModel.tankControlStateFlow.collectAsStateWithLifecycle()
    val tankState = dataSource.data

    Logger.d("Composing ControlTab! $tankState")

    val scrollState = rememberScrollState()
    var brightnessValue by remember {
        mutableStateOf(tankState.brightness)
    }
    var ratioValue by remember { mutableStateOf(20) }

    SwipeRefresh(
        state = rememberSwipeRefreshState(dataSource.status == Status.LOADING),
        onRefresh = { viewModel.refreshState() }
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .verticalScroll(scrollState)
        ) {
            Text(text = "Functions")
            Divider(modifier = Modifier.padding(vertical = 5.dp))

            SwitchRow(
                state = tankState.outWaterValveState,
                text = stringResource(id = R.string.out_valve),
                onClick = { viewModel.enableOutWater(it) }
            )

            SwitchRow(
                state = tankState.inWaterValveState,
                text = stringResource(id = R.string.in_valve),
                onClick = { viewModel.enableInWater(it) }
            )

            SwitchRow(
                state = tankState.heaterState,
                text = stringResource(id = R.string.heater),
                onClick = { viewModel.enableHeater(it) }
            )

            Divider(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp)
            ) {
                Text(text = "${stringResource(id = R.string.light_brightness)} (${brightnessValue}%)")
                Slider(
                    value = brightnessValue.toFloat(),
                    valueRange = 0f..100f,
                    onValueChange = { value: Float ->
                        Logger.d("Brightness onValueChange $value")
                        brightnessValue = value.toInt()
                    },
                    onValueChangeFinished = {
                        viewModel.changeLightBrightness(brightnessValue)
                    }
                )
            }

            Divider(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = { Text(text = "%") },
                    label = { Text(text = stringResource(id = R.string.replace_ratio)) },
                    value = ratioValue.toString(),
                    onValueChange = { ratioValue = if (it.isNotEmpty() && it.isDigitsOnly()) it.toInt() else 0 }
                )

                /*OutlinedButton(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp),
                    onClick = {
                        if (ratioValue > REPLACE_MAX || ratioValue <= 0) {
                            Toast.makeText(
                                context,
                                "Replace amount should between 0 and $REPLACE_MAX",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        } else {
                            eventHandler(UiEvent.ReplaceWater(ratioValue))
                        }
                    }) {
                    Text(text = stringResource(id = R.string.replace_water))
                }*/
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp)
            ) {
                OutlinedButton(onClick = { viewModel.reconnect() }) {
                    Text(text = "Reconnect")
                }
            }
        }
    }
}

@Composable
fun SwitchRow(
    state: Boolean = false,
    text: String,
    onClick: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 10.dp,
                end = 10.dp,
                top = 10.dp
            ),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = text
        )

        Switch(
            modifier = Modifier.weight(1f),
            checked = state,
            onCheckedChange = onClick
        )
    }
}