package com.marine.fishtank.view

import android.app.TimePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.rtsp.RtspMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.marine.fishtank.viewmodel.FishTankViewModel
import com.marine.fishtank.viewmodel.TankState
import com.marine.fishtank.R
import com.marine.fishtank.model.*
import com.orhanobut.logger.Logger
import java.text.SimpleDateFormat
import java.util.*

private const val REPLACE_MAX = 70

sealed class BottomNavItem(
    val titleRes: Int, val iconRes: Int, val screenRoute: String
) {
    object Control : BottomNavItem(R.string.text_control, R.drawable.control, "Control")
    object Monitor : BottomNavItem(R.string.text_monitor, R.drawable.bar_chart, "Monitor")
    object Camera : BottomNavItem(R.string.text_camera, R.drawable.camera, "Camera")
    object Periodic : BottomNavItem(R.string.text_periodic, R.drawable.calendar, "Periodic")
}

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun FishTankScreen(viewModel: FishTankViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val dataSource by viewModel.tankStateFlow.collectAsStateWithLifecycle(DataSource.loading(TankState()))
    val message by viewModel.messageFlow.collectAsStateWithLifecycle()

    Logger.d("Composing FishTankScreen STATUS=${dataSource.status} VALUE=${dataSource.data}")

    val navController = rememberNavController()

    if (message.isNotEmpty()) {
        Toast.makeText(LocalContext.current, message, Toast.LENGTH_LONG).show()
    }

    val items = listOf(
        BottomNavItem.Control,
        BottomNavItem.Monitor,
        BottomNavItem.Camera,
        BottomNavItem.Periodic
    )

    // Scaffold = TAB 전체화면
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FishTank") }
            )
        },
        floatingActionButton = {
            if(dataSource.status == Status.LOADING) {
                CircularProgressIndicator(
                    modifier = Modifier.size(50.dp)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigation {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { navItem ->
                    BottomNavigationItem(
                        selectedContentColor = Color.Magenta,
                        unselectedContentColor = Color.White,
                        alwaysShowLabel = true,
                        label = { Text(stringResource(id = navItem.titleRes)) },
                        selected = currentDestination?.hierarchy?.any { it.route == navItem.screenRoute } == true,
                        onClick = {
                            navController.navigate(navItem.screenRoute) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = navItem.iconRes),
                                contentDescription = stringResource(id = navItem.titleRes),
                                modifier = Modifier
                                    .width(26.dp)
                                    .height(26.dp)
                            )
                        },
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            modifier = Modifier.padding(padding),
            navController = navController,
            startDestination = BottomNavItem.Control.screenRoute
        ) {
            composable(BottomNavItem.Control.screenRoute) { ControlPage(viewModel, dataSource.data) }
            composable(BottomNavItem.Monitor.screenRoute) {
                MonitorPage(
                    viewModel,
                    dataSource.data.temperatureList
                )
            }
            composable(BottomNavItem.Camera.screenRoute) { CameraPage(tankState = dataSource.data) }
            composable(BottomNavItem.Periodic.screenRoute) {
                SchedulePage(
                    viewModel,
                    dataSource.data.periodicTaskList
                )
            }
        }
    }

    Logger.d("End composing FishTankScreen")
}


@Composable
fun SchedulePage(viewModel: FishTankViewModel, periodicTasks: List<PeriodicTask>) {
    Logger.d("Composing SchedulePage!")
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
        viewModel.addPeriodicTask(
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
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            for (task in periodicTasks) {
                item {
                    PeriodicTaskItem(
                        viewModel = viewModel,
                        task = task
                    )
                }
            }
        }
    }
}

@Composable
fun PeriodicTaskItem(viewModel: FishTankViewModel, task: PeriodicTask) {
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
                onClick = { viewModel.deletePeriodicTask(task) },
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
fun CameraPage(tankState: TankState) {
    Logger.d("Composing CameraPage!")

    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                StyledPlayerView(context).apply {
                    player = exoPlayer
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
            update = {
                if (!exoPlayer.isPlaying) {
                    val mediaSource = RtspMediaSource.Factory()
                        .setForceUseRtpTcp(true)
                        .createMediaSource(MediaItem.fromUri(Uri.parse(tankState.connectionSetting.rtspUrl)))
                    exoPlayer.setMediaSource(mediaSource)
                    exoPlayer.prepare()
                    exoPlayer.play()
                }
            }
        )
    }
}

@Composable
fun MonitorPage(viewModel: FishTankViewModel, temperatureList: List<Temperature>) {
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

    AndroidView(
        modifier = modifier,

        factory = { context ->
            Logger.d("Factory LineChart")
            LineChart(context).apply {
                val temperatureMarker = TemperatureMarker(context)
                temperatureMarker.chartView = this
                marker = temperatureMarker

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
                setBackgroundColor(android.graphics.Color.WHITE)

                xAxis.apply {
                    textSize = 11f
                    textColor = android.graphics.Color.BLACK
                    setDrawGridLines(false)
                    setDrawAxisLine(true)
                    position = XAxis.XAxisPosition.BOTTOM

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
                    textColor = android.graphics.Color.BLACK
                    setDrawGridLines(true)
                    setDrawAxisLine(true)

                    //String setter in x-Axis
                    valueFormatter = IAxisValueFormatter { value, axisBase -> String.format("%.2f", value) }
                    //axisMaximum = LineChartConfig.YAXIS_MAX
                    //axisMinimum = LineChartConfig.YAXIS_MIN

                    spaceBottom = 20f
                    spaceTop = 20f
                }

                axisRight.apply {
                    isEnabled = false
                }

                legend.apply {
                    textSize = 12f
                }

                val entryList = mutableListOf<Entry>()
                val dataSet = LineDataSet(entryList, "Water temperature").apply {
                    axisDependency = YAxis.AxisDependency.LEFT
                    color = ColorTemplate.getHoloBlue()
                    setCircleColor(android.graphics.Color.BLACK)
                    lineWidth = 3f
                    circleRadius = 3f
                    fillAlpha = 65
                    fillColor = ColorTemplate.getHoloBlue()
                    highLightColor = R.color.purple_200
                    setDrawCircleHole(false)
                }

                // create a data object with the data sets
                data = LineData(dataSet).apply {
                    setValueTextColor(android.graphics.Color.BLACK)
                    setValueTextSize(10f)
                    setValueFormatter { value, entry, dataSetIndex, viewPortHandler -> String.format("%.2f", value) }
                }
            }
        },
        update = {
            Logger.d("Update LineChart mx=$maximumCount, size=${temperatureList.size}")

            val entryList = mutableListOf<Entry>()
            for (tmp in temperatureList.withIndex()) {
                entryList.add(
                    Entry(tmp.index.toFloat(), tmp.value.temperature, tmp.value)
                )
            }

            val dataSet = it.data.getDataSetByIndex(0) as LineDataSet
            dataSet.values = entryList

            it.data.notifyDataChanged()
            it.notifyDataSetChanged()
            it.invalidate()

            it.setVisibleXRange(1f, maximumCount)
            it.moveViewToX((temperatureList.size - 1).toFloat())
        })
}

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun ControlPage(viewModel: FishTankViewModel, tankState: TankState) {
    Logger.d("Composing ControlTab! $tankState")
    val isRefreshing = viewModel.refreshFlow.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    var brightnessValue by remember {
        mutableStateOf(tankState.brightness)
    }
    var ratioValue by remember { mutableStateOf(20) }

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing.value),
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

@Preview
@Composable
fun PreviewFishTankScreen() {
    FishTankScreen()
}