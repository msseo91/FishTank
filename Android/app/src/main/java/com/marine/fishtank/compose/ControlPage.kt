package com.marine.fishtank.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Slider
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.marine.fishtank.R
import com.marine.fishtank.model.DataSource
import com.marine.fishtank.model.Status
import com.marine.fishtank.viewmodel.TankState
import com.orhanobut.logger.Logger


@Composable
fun ControlPage(
    dataSource: DataSource<TankState> = DataSource.loading(TankState()),
    onRefresh: () -> Unit = {},
    onOutValveClick: (Boolean) -> Unit = {},
    onOutValve2Click: (Boolean) -> Unit = {},
    onInValveClick: (Boolean) -> Unit = {},
    onHeaterClick: (Boolean) -> Unit = {},
    onBrightnessChange: (Int) -> Unit = {}
) {
    Logger.d("Composing ControlTab!")

    val tankState = dataSource.data
    val scrollState = rememberScrollState()
    var brightnessValue by remember { mutableStateOf(tankState.brightness) }

    SwipeRefresh(
        state = rememberSwipeRefreshState(dataSource.status == Status.LOADING),
        onRefresh = onRefresh
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
                onClick = onOutValveClick
            )

            SwitchRow(
                state = tankState.outWaterValve2State,
                text = stringResource(id = R.string.out_valve2),
                onClick = onOutValve2Click
            )

            SwitchRow(
                state = tankState.inWaterValveState,
                text = stringResource(id = R.string.in_valve),
                onClick = onInValveClick
            )

            SwitchRow(
                state = tankState.heaterState,
                text = stringResource(id = R.string.heater),
                onClick = onHeaterClick
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
                    onValueChangeFinished = { onBrightnessChange(brightnessValue) }
                )
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

@Preview
@Composable
fun ControlPagePreview() {
    ControlPage()
}