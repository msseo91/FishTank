package com.marine.fishtank.compose

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
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
    onLightClick: (Boolean) -> Unit = {},
) {
    Logger.d("Composing ControlTab!")

    val scrollState = rememberScrollState()
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
            tankResult.data
        }
        is TankResult.Error -> {
            Logger.e("Error: ${tankResult.message}")
            Toast.makeText(LocalContext.current, tankResult.message, Toast.LENGTH_SHORT).show()
            isRefreshing = false
            DeviceState()
        }
    }
    Logger.d("ControlPage = $deviceState")

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
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium
                )
        ) {
            SwitchRow(
                state = deviceState.outletValve1Enabled,
                text = stringResource(id = R.string.out_valve),
                onClick = onOutValveClick
            )

            SwitchRow(
                state = deviceState.outletValve2Enabled,
                text = stringResource(id = R.string.out_valve2),
                onClick = onOutValve2Click
            )

            SwitchRow(
                state = deviceState.inletValveEnabled,
                text = stringResource(id = R.string.in_valve),
                onClick = onInValveClick
            )

            SwitchRow(
                state = deviceState.lightOn,
                text = stringResource(id = R.string.light),
                onClick = onLightClick
            )
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text
        )

        Spacer(modifier = Modifier.weight(1f))

        Switch(
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