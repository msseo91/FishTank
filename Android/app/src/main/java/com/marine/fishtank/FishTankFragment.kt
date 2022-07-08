package com.marine.fishtank

import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
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
import com.marine.fishtank.view.TemperatureMarker
import com.marine.fishtank.viewmodel.FishTankViewModel
import com.marine.fishtank.viewmodel.UiEvent
import com.marine.fishtank.viewmodel.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

@OptIn(ExperimentalPagerApi::class)
@Composable
fun FishTankScreen(viewModel: FishTankViewModel) {
    Log.d(TAG, "Composing FishTankScreen")
    val uiState: UiState by viewModel.uiState.observeAsState(UiState())
    val temperatureState: List<Temperature> by viewModel.temperatureLiveData.observeAsState(emptyList())
    val periodicTasks: List<PeriodicTask> by viewModel.periodicTaskLiveData.observeAsState(emptyList())
    val eventHandler = { uiEvent: UiEvent -> viewModel.uiEvent(uiEvent) }
    val isRefreshing by viewModel.isRefreshing.observeAsState(false)

    val tabTitles = listOf("Control", "Monitor", "Camera", "Schedule")

    // Default page -> monitor
    val pagerState = rememberPagerState(0)

    // Surface = TAB 전체화면
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = { viewModel.refreshState() }
        ) {
            Column {
                TabRow(
                    backgroundColor = colorResource(id = R.color.purple_500),
                    selectedTabIndex = pagerState.currentPage,
                    indicator = { tabPositions -> // 3.
                        TabRowDefaults.Indicator(
                            Modifier.pagerTabIndicatorOffset(
                                pagerState,
                                tabPositions
                            )
                        )
                    }) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(selected = pagerState.currentPage == index,
                            onClick = { CoroutineScope(Dispatchers.Main).launch { pagerState.scrollToPage(index) } },
                            text = { Text(text = title) })
                    }
                }
                HorizontalPager(
                    modifier = Modifier.fillMaxSize(),
                    count = tabTitles.size,
                    state = pagerState,
                    verticalAlignment = Alignment.Top
                ) { tabIndex ->
                    when (tabIndex) {
                        0 -> ControlPage(uiState, eventHandler)
                        1 -> MonitorPage(temperatureState, eventHandler)
                        2 -> CameraPage(uiState, eventHandler)
                        3 -> SchedulePage(periodicTasks, eventHandler)
                    }
                }
            }
        }
    }

    Log.d(TAG, "End composing FishTankScreen")
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SchedulePage(periodicTasks: List<PeriodicTask>, eventHandler: (UiEvent) -> Unit) {
    Log.d(TAG, "Composing SchedulePage!")
    val context = LocalContext.current
    var openDialog: Boolean by remember { mutableStateOf(false) }

    var typeExpand: Boolean by remember { mutableStateOf(false) }
    val typeOptions = arrayOf( R.string.out_valve, R.string.in_valve, R.string.purifier, R.string.light)
    var selectedTypeOption by remember { mutableStateOf(typeOptions[0]) }

    var valueBooleanExpand: Boolean by remember { mutableStateOf(false) }
    val valueBooleanOptions = arrayOf( 0, 1)

    var valueLightExpand: Boolean by remember { mutableStateOf(false) }
    val valueLightOptions = 0..255
    var selectedOption by remember { mutableStateOf(valueLightOptions.first)}

    val mCalendar = Calendar.getInstance()
    val currentHour = mCalendar[Calendar.HOUR_OF_DAY]
    val currentMinute = mCalendar[Calendar.MINUTE]
    var actionTime by remember { mutableStateOf("$currentHour:$currentMinute") }

    val timePickerDialog = TimePickerDialog(
        context,
        {_, hour : Int, minute: Int ->
            actionTime = "$hour:${String.format("%02d", minute)}"
        }, currentHour, currentMinute, false
    )
    val source = remember {
        MutableInteractionSource()
    }

    if(source.collectIsPressedAsState().value) {
        timePickerDialog.show()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { openDialog = true }) {
                Icon(Icons.Filled.Add, "PeriodicTask add button")
            }
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding)) {
            for (task in periodicTasks) {
                item {
                    PeriodicTaskItem(
                        action = task.typeAsString(context),
                        exeTime = task.time
                    )
                }
            }
        }

        // Dialog to add Periodic task.
        if (openDialog) {
            Dialog(onDismissRequest = {
                openDialog = false
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
                                .fillMaxWidth())

                        Text(
                            textAlign = TextAlign.Center,
                            text = stringResource(id = R.string.periodic_dialog_title),
                            fontSize = 15.sp
                        )

                        Spacer(modifier = Modifier.height(25.dp))

                        ExposedDropdownMenuBox(
                            expanded = typeExpand,
                            onExpandedChange  = { typeExpand = !typeExpand }
                        ) {
                            TextField(
                                readOnly = true,
                                value = stringResource(id = selectedTypeOption),
                                onValueChange = {},
                                label = { Text(stringResource(id = R.string.periodic_dialog_task_type))},
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = typeExpand
                                    )
                                },
                                colors = ExposedDropdownMenuDefaults.textFieldColors()
                            )

                            ExposedDropdownMenu(
                                expanded = typeExpand,
                                onDismissRequest = { typeExpand = false }
                            ) {
                                typeOptions.forEach { option ->
                                    DropdownMenuItem(onClick = {
                                        typeExpand = false
                                        selectedTypeOption = option
                                    }) {
                                        Text(text = stringResource(id = option))
                                    }
                                }
                            }
                        }

                        Divider(Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(15.dp))

                        if(selectedTypeOption != R.string.light) {
                            ExposedDropdownMenuBox(
                                expanded = valueBooleanExpand,
                                onExpandedChange  = { valueBooleanExpand = !valueBooleanExpand }
                            ) {
                                TextField(
                                    readOnly = true,
                                    value = selectedOption.toString(),
                                    onValueChange = {},
                                    label = { Text(stringResource(id = R.string.periodic_dialog_task_value))},
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = valueBooleanExpand
                                        )
                                    },
                                    colors = ExposedDropdownMenuDefaults.textFieldColors()
                                )

                                ExposedDropdownMenu(
                                    expanded = valueBooleanExpand,
                                    onDismissRequest = { valueBooleanExpand = false }
                                ) {
                                    valueBooleanOptions.forEach { option ->
                                        DropdownMenuItem(onClick = {
                                            valueBooleanExpand = false
                                            selectedOption = option
                                        }) {
                                            Text(text = option.toString())
                                        }
                                    }
                                }
                            }
                        } else {
                            ExposedDropdownMenuBox(
                                expanded = valueLightExpand,
                                onExpandedChange  = { valueLightExpand = !valueLightExpand }
                            ) {
                                TextField(
                                    readOnly = true,
                                    value = selectedOption.toString(),
                                    onValueChange = {},
                                    label = { Text(stringResource(id = R.string.periodic_dialog_task_value))},
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = valueLightExpand
                                        )
                                    },
                                    colors = ExposedDropdownMenuDefaults.textFieldColors()
                                )

                                ExposedDropdownMenu(
                                    expanded = valueLightExpand,
                                    onDismissRequest = { valueLightExpand = false }
                                ) {
                                    valueBooleanOptions.forEach { option ->
                                        DropdownMenuItem(onClick = {
                                            valueLightExpand = false
                                            selectedOption = option
                                        }) {
                                            Text(text = option.toString())
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(15.dp))

                        TextField(
                            readOnly = true,
                            value = actionTime,
                            interactionSource = source,
                            onValueChange = {},
                            label = { Text(stringResource(id = R.string.periodic_dialog_task_time))},
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = typeExpand
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
                                    openDialog = false
                                }
                            ) {
                                Text(stringResource(id = R.string.periodic_dialog_cancel))
                            }

                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    openDialog = false
                                    eventHandler(UiEvent.AddPeriodicTask(
                                        PeriodicTask(
                                            type = PeriodicTask.typeFromResource(selectedTypeOption),
                                            data = selectedOption,
                                            time = actionTime
                                        )
                                    ))
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
}

@Composable
fun PeriodicTaskItem(action: String, exeTime: String) {
    Column() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = androidx.compose.ui.graphics.Color.Black)
                .padding(10.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text(text = action)
            Text(text = exeTime)
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

                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
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
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return String.format("%.2f", value)
                        }
                    }
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
                    setValueFormatter(object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return String.format("%.2f", value)
                        }
                    })
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
fun ControlPage(uiState: UiState, eventHandler: (UiEvent) -> Unit) {
    Log.d(TAG, "Composing ControlTab! $uiState")

    val scrollState = rememberScrollState()
    var ratioValue by remember { mutableStateOf(20) }
    val context = LocalContext.current
    var brightnessPosition by rememberSaveable { mutableStateOf(0) }

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

        SwitchRow(
            state = uiState.ledState,
            text = stringResource(id = R.string.board_led),
            onClick = { eventHandler(UiEvent.LedEvent(it)) }
        )

        Divider(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp))
        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp)
        ) {
            Text(text = "${stringResource(id = R.string.light_brightness)} (${uiState.brightness}%)")
            Slider(
                value = uiState.brightness.toFloat(),
                valueRange = 0f..100f,
                onValueChange = { value: Float ->
                    Log.d(TAG, "Brightness onValueChange $value")
                    brightnessPosition = value.toInt()
                    eventHandler(UiEvent.OnLightBrightnessChange(brightnessPosition, false))
                },
                onValueChangeFinished = {
                    eventHandler(UiEvent.OnLightBrightnessChange(brightnessPosition, true))
                }
            )
        }

        Divider(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp))
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
                        Toast.makeText(context, "Replace amount should between 0 and $REPLACE_MAX", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        eventHandler(UiEvent.ReplaceWater(ratioValue))
                    }
                }) {
                Text(text = stringResource(id = R.string.replace_water))
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
            onCheckedChange =  onClick
        )
    }
}
