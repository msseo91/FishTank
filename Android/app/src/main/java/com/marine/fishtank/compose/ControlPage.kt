package com.marine.fishtank.compose

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marine.fishtank.R
import com.marine.fishtank.api.TankResult
import com.marine.fishtank.model.DeviceState
import com.orhanobut.logger.Logger
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ControlPage(
    tankResult: TankResult<DeviceState> = TankResult.Loading(),
    onRefresh: () -> Unit = {},
    onOutValveClick: (Boolean) -> Unit = {},
    onOutValve2Click: (Boolean) -> Unit = {},
    onInValveClick: (Boolean) -> Unit = {},
    onHeaterClick: (Boolean) -> Unit = {},
    onBrightnessChange: (Float) -> Unit = {}
) {
    Logger.d("Composing ControlTab!")

    val scrollState = rememberScrollState()
    var brightnessValue by remember { mutableStateOf(0f) }
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            onRefresh()
        })

    val deviceState = when(tankResult) {
        is TankResult.Loading -> {
            Logger.d("Loading")
            isRefreshing = true
            DeviceState()
        }
        is TankResult.Success -> {
            Logger.d("Success")
            isRefreshing = false

            // Update brightness value only on first time.
            LaunchedEffect(null) {
                brightnessValue = tankResult.data.lightBrightness
            }
            tankResult.data
        }
        is TankResult.Error -> {
            Logger.e("Error: ${tankResult.message}")
            Toast.makeText(LocalContext.current, tankResult.message, Toast.LENGTH_SHORT).show()
            isRefreshing = false
            DeviceState()
        }
    }


    Box (
        modifier = Modifier.pullRefresh(pullRefreshState)
    ) {
        PullRefreshIndicator(
            modifier = Modifier.align(Alignment.TopCenter),
            refreshing = isRefreshing,
            state = pullRefreshState
        )

        Column(
            modifier = Modifier
                .padding(10.dp)
                .verticalScroll(scrollState)
        ) {
            Text(text = "Functions")
            Divider(modifier = Modifier.padding(vertical = 5.dp))

            SwitchRow(
                state = deviceState.isOutletValve1Enabled,
                text = stringResource(id = R.string.out_valve),
                onClick = onOutValveClick
            )

            SwitchRow(
                state = deviceState.isOutletValve2Enabled,
                text = stringResource(id = R.string.out_valve2),
                onClick = onOutValve2Click
            )

            SwitchRow(
                state = deviceState.isInletValveEnabled,
                text = stringResource(id = R.string.in_valve),
                onClick = onInValveClick
            )

            SwitchRow(
                state = deviceState.isHeaterEnabled,
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
                Text(text = "${stringResource(id = R.string.light_brightness)} (${(brightnessValue * 100).roundToInt()}%)")
                Slider(
                    value = brightnessValue,
                    steps = 100,
                    valueRange = 0f..1f,
                    onValueChange = { value: Float ->
                        Logger.d("Brightness onValueChange $value")
                        brightnessValue = value
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