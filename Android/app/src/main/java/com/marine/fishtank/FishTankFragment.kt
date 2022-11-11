package com.marine.fishtank

import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
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
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.rtsp.RtspMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.marine.fishtank.model.PeriodicTask
import com.marine.fishtank.model.Temperature
import com.marine.fishtank.model.typeAsString
import com.marine.fishtank.view.PeriodicTaskDialog
import com.marine.fishtank.view.TemperatureMarker
import com.marine.fishtank.viewmodel.FishTankViewModel
import com.marine.fishtank.viewmodel.UiEvent
import com.marine.fishtank.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "FishTankFragment"

private const val REPLACE_MAX = 70

class FishTankFragment : Fragment() {
    private val viewModel: FishTankViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Log.d(TAG, "onCreateView")

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    FishTankScreen(viewModel)
                }
            }
        }
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()

        viewModel.startFetchTemperature(1)
        viewModel.readState()
        viewModel.fetchPeriodicTasks()
    }
}

sealed class BottomNavItem(
    val titleRes: Int, val iconRes: Int, val screenRoute: String
) {
    object Control : BottomNavItem(R.string.text_control, R.drawable.control, "Control")
    object Monitor : BottomNavItem(R.string.text_monitor, R.drawable.bar_chart, "Monitor")
    object Camera : BottomNavItem(R.string.text_camera, R.drawable.camera, "Camera")
    object Periodic : BottomNavItem(R.string.text_periodic, R.drawable.calendar, "Periodic")
}

@Composable
fun FishTankScreen(viewModel: FishTankViewModel) {
    Log.d(TAG, "Composing FishTankScreen")
    val uiState: UiState by viewModel.uiState.observeAsState(UiState())
    val temperatureState: List<Temperature> by viewModel.temperatureLiveData.observeAsState(emptyList())
    val periodicTasks: List<PeriodicTask> by viewModel.periodicTaskLiveData.observeAsState(emptyList())
    val eventHandler = { uiEvent: UiEvent -> viewModel.uiEvent(uiEvent) }

    val navController = rememberNavController()

    val items = listOf(
        BottomNavItem.Control,
        BottomNavItem.Monitor,
        BottomNavItem.Camera,
        BottomNavItem.Periodic
    )

    // Surface = TAB 전체화면
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FishTank")}
            )
        },
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigation() {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    BottomNavigationItem(
                        selectedContentColor = Color.Magenta,
                        unselectedContentColor = Color.White,
                        alwaysShowLabel = true,
                        label = { Text(stringResource(id = screen.titleRes)) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.screenRoute } == true,
                        onClick = {
                            navController.navigate(screen.screenRoute) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = screen.iconRes),
                                contentDescription = stringResource(id = screen.titleRes),
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
            composable(BottomNavItem.Control.screenRoute) { ControlPage(viewModel, uiState, eventHandler) }
            composable(BottomNavItem.Monitor.screenRoute) { MonitorPage(temperatureState, eventHandler) }
            composable(BottomNavItem.Camera.screenRoute) { CameraPage(uiState = uiState, eventHandler = eventHandler) }
            composable(BottomNavItem.Periodic.screenRoute) {
                SchedulePage(
                    periodicTasks = periodicTasks,
                    eventHandler = eventHandler
                )
            }
        }
    }

    Log.d(TAG, "End composing FishTankScreen")
}

@Composable
fun SchedulePage(periodicTasks: List<PeriodicTask>, eventHandler: (UiEvent) -> Unit) {
    Log.d(TAG, "Composing SchedulePage!")
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
        eventHandler(
            UiEvent.AddPeriodicTask(
                PeriodicTask(
                    type = PeriodicTask.typeFromResource(selectedTypeOption.value),
                    data = selectedOption.value,
                    time = actionTime.value
                )
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
                        task = task,
                        eventHandler = eventHandler,
                    )
                }
            }
        }
    }
}

@Composable
fun PeriodicTaskItem(task: PeriodicTask, eventHandler: (UiEvent) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(40.dp, 60.dp)
            .border(width = 1.dp, color = androidx.compose.ui.graphics.Color.Black)
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
                onClick = {
                    eventHandler(UiEvent.DeletePeriodicTask(task))
                },
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
fun CameraPage(uiState: UiState, eventHandler: (UiEvent) -> Unit) {
    Log.d(TAG, "Composing CameraPage!")

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
                    resizeMode = RESIZE_MODE_FIT
                }
            },
            update = {
                if (!exoPlayer.isPlaying) {
                    val mediaSource = RtspMediaSource.Factory()
                        .setForceUseRtpTcp(true)
                        .createMediaSource(MediaItem.fromUri(Uri.parse(uiState.connectionSetting.rtspUrl)))
                    exoPlayer.setMediaSource(mediaSource)
                    exoPlayer.prepare()
                    exoPlayer.play()
                }
            }
        )
    }
}

@Composable
fun MonitorPage(temperatureList: List<Temperature>, eventHandler: (UiEvent) -> Unit) {
    Log.d(TAG, "MonitorPage!")

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
            eventHandler = eventHandler
        )

        Log.d(TAG, "Slider! days=${position.value}")
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
                eventHandler(UiEvent.OnChangeTemperatureRange(position.value.toInt()))
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
    maximumCount: Float,
    eventHandler: (UiEvent) -> Unit
) {
    Log.d(TAG, "Composing Chart!")

    AndroidView(
        modifier = modifier,

        factory = { context ->
            Log.d(TAG, "Factory LineChart")
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
            Log.d(TAG, "Update LineChart mx=$maximumCount, size=${temperatureList.size}")

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

@Composable
fun ControlPage(viewModel: FishTankViewModel, uiState: UiState, eventHandler: (UiEvent) -> Unit) {
    Log.d(TAG, "Composing ControlTab! $uiState")
    val isRefreshing by viewModel.isRefreshing.observeAsState(false)
    val state by remember { mutableStateOf(uiState) }
    val scrollState = rememberScrollState()
    var ratioValue by remember { mutableStateOf(20) }
    val context = LocalContext.current

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
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
                state = uiState.outWaterValveState,
                text = stringResource(id = R.string.out_valve),
                onClick = { eventHandler(UiEvent.OutWaterEvent(it)) }
            )

            SwitchRow(
                state = uiState.inWaterValveState,
                text = stringResource(id = R.string.in_valve),
                onClick = { eventHandler(UiEvent.InWaterEvent(it)) }
            )

            SwitchRow(
                state = uiState.purifierState,
                text = stringResource(id = R.string.purifier),
                onClick = { eventHandler(UiEvent.PurifierEvent(it)) }
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
                Text(text = "${stringResource(id = R.string.light_brightness)} (${state.brightness}%)")
                Slider(
                    value = state.brightness.toFloat(),
                    valueRange = 0f..100f,
                    onValueChange = { value: Float ->
                        Log.d(TAG, "Brightness onValueChange $value")
                        state.brightness = value.toInt()
                        eventHandler(UiEvent.OnLightBrightnessChange(state.brightness, false))
                    },
                    onValueChangeFinished = {
                        eventHandler(UiEvent.OnLightBrightnessChange(state.brightness, true))
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

                OutlinedButton(
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
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp)
            ) {
                OutlinedButton(onClick = {
                    eventHandler(UiEvent.TryReconnect())
                }) {
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
