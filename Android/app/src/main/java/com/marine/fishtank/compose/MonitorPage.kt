package com.marine.fishtank.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.tehras.charts.line.LineChart
import com.github.tehras.charts.line.LineChartData
import com.github.tehras.charts.line.renderer.line.SolidLineDrawer
import com.github.tehras.charts.line.renderer.point.FilledCircularPointDrawer
import com.github.tehras.charts.piechart.animation.simpleChartAnimation
import com.marine.fishtank.viewmodel.ControlViewModel
import com.orhanobut.logger.Logger

@Composable
fun MonitorPage(viewModel: ControlViewModel = viewModel()) {
    Logger.d("MonitorPage!")

    val dataSource by viewModel.temperatureFlow.collectAsStateWithLifecycle()
    val temperatureList = dataSource.data

    val position = remember { mutableStateOf(1f) }
    val positionRange = remember { mutableStateOf(10f) }
    val scrollState = rememberScrollState()

    Column {
        LineChart(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(10.dp),
            linesChartData = listOf(
                LineChartData(
                    points = temperatureList.map {
                        LineChartData.Point(it.temperature, "")
                    },
                    lineDrawer = SolidLineDrawer()
                )
            ),
            animation = simpleChartAnimation(),
            pointDrawer = FilledCircularPointDrawer(),
            horizontalOffset = 5f,
        )

        Logger.d("Slider! days=${position.value}")

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(15.dp)
        ) {

            Slider(
                modifier = Modifier.fillMaxWidth(),
                value = position.value,
                valueRange = 1f..30f,
                steps = 0,
                onValueChange = { value: Float ->
                    position.value = value
                },
                onValueChangeFinished = {
                    viewModel.startFetchTemperature(position.value.toInt())
                }
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                modifier = Modifier.padding(horizontal = 20.dp),
                text = "${position.value.toInt()} Days"
            )

            Spacer(modifier = Modifier.height(20.dp))
            Slider(
                modifier = Modifier.fillMaxWidth(),
                value = positionRange.value,
                valueRange = 1f..288f,
                steps = 0,
                onValueChange = { value: Float ->
                    positionRange.value = value
                },
                onValueChangeFinished = {
                }
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                modifier = Modifier.padding(horizontal = 20.dp),
                text = "${positionRange.value.toInt()} MAX"
            )
        }
    }
}

@Composable
@Preview
fun MonitorPagePreview() {
    MonitorPage()
}
