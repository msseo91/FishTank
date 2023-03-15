package com.marine.fishtank.compose

import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.marine.fishtank.R
import com.marine.fishtank.model.Temperature
import com.marine.fishtank.view.TemperatureMarker
import com.orhanobut.logger.Logger
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

@Composable
fun MonitorPage(
    temperatureList: List<Temperature> = emptyList(),
    onRequestTemperatures: (Int) -> Unit = {},
) {
    Logger.d("MonitorPage!")

    val position = remember { mutableStateOf(1f) }
    val positionRange = remember { mutableStateOf(10f) }
    val scrollState = rememberScrollState()

    Column {
        Chart(
            temperatureList,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(400.dp, 600.dp)
                .verticalScroll(scrollState),
            maximumCount = positionRange.value,
        )

        Logger.d("Slider! days=${position.value}")
        Slider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp),
            value = position.value,
            valueRange = 1f..30f,
            steps = 0,
            onValueChange = { value: Float ->
                position.value = value
            },
            onValueChangeFinished = {
                onRequestTemperatures(position.value.toInt())
            }
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            modifier = Modifier.padding(horizontal = 20.dp),
            text = "${position.value.toInt()} Days"
        )

        Spacer(modifier = Modifier.height(20.dp))
        Slider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp),
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

@Composable
fun Chart(
    temperatureList: List<Temperature>,
    modifier: Modifier,
    maximumCount: Float
) {
    Logger.d("Composing Chart!")

    val minTemperature = temperatureList.minBy { it.temperature }
    val maxTemperature = temperatureList.maxBy { it.temperature }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            Logger.d("Factory LineChart")
            LineChart(context).apply {
                val temperatureMarker = TemperatureMarker(context)
                temperatureMarker.chartView = this
                marker = temperatureMarker

                axisRight.apply {
                    isEnabled = true
                    setDrawGridLines(false)
                    setDrawLabels(false)
                }

                // no description text
                description.isEnabled = false

                // enable touch gestures
                setTouchEnabled(true)

                dragDecelerationFrictionCoef = 0.9f

                // enable scaling and dragging
                isDragEnabled = true
                setScaleEnabled(true)
                setDrawGridBackground(false)
                isHighlightPerDragEnabled = true

                // if disabled, scaling can be done on x- and y-axis separately
                setPinchZoom(true)

                // set an alternative background color
                setBackgroundColor(Color.WHITE)

                xAxis.apply {
                    textSize = 10f
                    textColor = Color.BLACK
                    setDrawGridLines(false)
                    setDrawAxisLine(true)
                    position = XAxis.XAxisPosition.BOTTOM

                    labelRotationAngle = 45f
                    axisMaxLabels = 5
                    axisMaximum = temperatureList.size.toFloat() - 0.5f
                    axisMinimum = -0.5f

                    valueFormatter = object : IAxisValueFormatter {
                        override fun getFormattedValue(value: Float, axisBase: AxisBase): String {
                            if (data.dataSets.isEmpty()) {
                                return ""
                            }
                            val entry = data.dataSets[0].getEntryForXValue(value, 0f)
                            entry?.data?.let {
                                val tmp = it as Temperature
                                return SimpleDateFormat("MM-dd HH:mm").format(tmp.time)
                            }

                            return ""
                        }
                    }
                }

                axisLeft.apply {
                    textColor = Color.BLACK
                    setDrawGridLines(true)
                    setDrawAxisLine(true)

                    //String setter in x-Axis
                    valueFormatter = IAxisValueFormatter { value, _ -> String.format("%.2f", value) }
                    axisMaximum = maxTemperature.temperature + 3
                    axisMinimum = minTemperature.temperature - 3

                    //spaceBottom = 20f
                    spaceTop = 20f
                }

                legend.apply {
                    textSize = 12f
                }

                val entryList = mutableListOf<Entry>()
                val dataSet = LineDataSet(entryList, "Water temperature").apply {
                    axisDependency = YAxis.AxisDependency.LEFT
                    color = ColorTemplate.getHoloBlue()
                    setCircleColor(Color.BLACK)
                    lineWidth = 3f
                    circleRadius = 3f
                    fillAlpha = 65
                    fillColor = ColorTemplate.getHoloBlue()
                    highLightColor = R.color.purple_200
                    setDrawCircleHole(false)
                }

                // create a data object with the data sets
                data = LineData(dataSet).apply {
                    setValueTextColor(Color.BLACK)
                    setValueTextSize(10f)
                    setValueFormatter { value, _, _, _ -> String.format("%.2f", value) }
                }
            }
        },
        update = {
            Logger.d("Update LineChart mx=$maximumCount, size=${temperatureList.size}")

            val dataSet = it.data.getDataSetByIndex(0) as LineDataSet
            dataSet.entries = temperatureList.mapIndexed { index, temperature ->
                Entry(index.toFloat(), temperature.temperature, temperature)
            }

            it.data.notifyDataChanged()
            it.notifyDataSetChanged()
            it.invalidate()

            it.setVisibleXRange(1f, maximumCount)
            it.moveViewToX((temperatureList.size - 1).toFloat())
        })
}

@Preview
@Composable
fun MonitorPagePreview() {
    MonitorPage(
        temperatureList =
        buildList {
            repeat(10) {
                add(
                    Temperature(temperature = Random.nextDouble(24.0, 26.0).toFloat(),
                        time = Date().apply { time -= 1000 * 60 * 5 * it })
                )
            }
        }.reversed()

    )
}