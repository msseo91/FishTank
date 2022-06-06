package com.marine.fishtank

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
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
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.marine.fishtank.model.Temperature
import com.marine.fishtank.viewmodel.FishTankViewModel
import com.marine.fishtank.viewmodel.UiEvent
import com.marine.fishtank.viewmodel.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "FishTankFragment"

class FishTankFragment : Fragment() {
    private val viewModel: FishTankViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Need call to pull the trigger of Composition.
        val sTime = System.currentTimeMillis()

        viewModel.initializeLiveData.observe(viewLifecycleOwner) { connect ->
            if (connect) {
                Log.i(TAG, "Connect to fish server successful.")
                viewModel.startFetchTemperature(1)
                viewModel.readState()
            } else {
                Log.e(TAG, "Fail to connect!")
            }
        }
        viewModel.init()

        Log.d(TAG, "onCreateView viewModel elapse=${System.currentTimeMillis() - sTime}")

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    FishTankScreen(viewModel = viewModel) { uiEvent ->
                        viewModel.uiEvent(uiEvent)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun FishTankScreen(
    viewModel: FishTankViewModel,
    eventHandler: (UiEvent) -> Unit
) {
    Log.d(TAG, "Composing FishTankScreen")

    val uiState: UiState by viewModel.uiState.observeAsState(UiState())
    val temperatureState: List<Temperature> by viewModel.temperatureLiveData.observeAsState(emptyList())
    val connectState: Boolean by viewModel.initializeLiveData.observeAsState(false)

    val tabTitles = listOf("Control", "Monitor", "Camera", "Setting")
    // Default page -> monitor
    val pagerState = rememberPagerState(0)

    // Surface = TAB 전체화면
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column {
            TabRow(
                backgroundColor = Color.Cyan,
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
                    0 -> ControlPage(uiState, connectState, eventHandler)
                    1 -> MonitorPage(temperatureState, uiState, eventHandler)
                    2 -> CameraPage(uiState, eventHandler)
                    3 -> SettingPage(uiState = uiState, eventHandler = eventHandler)
                }
            }
        }
    }

    Log.d(TAG, "End composing FishTankScreen")
}

@Composable
fun SettingPage(uiState: UiState, eventHandler: (UiEvent) -> Unit) {
    Log.d(TAG, "Composing Setting Page!")

    var serverUrlText by remember { mutableStateOf(uiState.connectionSetting.serverUrl) }
    var serverPortText by remember { mutableStateOf(uiState.connectionSetting.serverPort.toString()) }
    var rtspUrlText by remember { mutableStateOf(uiState.connectionSetting.rtspUrl) }

    Column(
        modifier = Modifier
            .padding(15.dp)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Column {
            OutlinedTextField(
                value = serverUrlText,
                label = { Text(text = stringResource(id = R.string.setting_server_url)) },
                onValueChange = { serverUrlText = it }
            )

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
            )

            OutlinedTextField(
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text(text = stringResource(id = R.string.setting_server_port)) },
                value = serverPortText,
                onValueChange = { serverPortText = it.filter { letter -> letter.isDigit() } }
            )

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
            )

            OutlinedTextField(
                value = rtspUrlText,
                label = { Text(text = stringResource(id = R.string.setting_rtsp_url)) },
                onValueChange = { rtspUrlText = it }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, false),
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedButton(
                onClick = { eventHandler(UiEvent.SettingChange(
                    ConnectionSetting(serverUrlText, serverPortText.toInt(), rtspUrlText)
                )) }) {
                Text(text = stringResource(id = R.string.setting_save))
            }
        }
    }
}

@Composable
fun CameraPage(uiState: UiState, eventHandler: (UiEvent) -> Unit) {
    Log.d(TAG, "Composing CameraPage!")

    val composableScope = rememberCoroutineScope()
    var width by remember { mutableStateOf(0.dp) }
    var height by remember { mutableStateOf(0.dp) }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val localDensity = LocalDensity.current
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
            modifier = if(width.value > 0) {
                Modifier
                    .width(width)
                    .height(height)
            } else {
                Modifier.fillMaxSize()
            },
            factory = { context ->
                StyledPlayerView(context).apply {
                    player = exoPlayer
                }
            },
            update = {
                if(!exoPlayer.isPlaying) {
                    val mediaItem = MediaItem.fromUri(Uri.parse(uiState.connectionSetting.rtspUrl))
                    exoPlayer.setMediaItem(mediaItem)
                    exoPlayer.prepare()
                    exoPlayer.play()
                }
            }
        )
    }
}

@Composable
fun MonitorPage(temperatureList: List<Temperature>, uiState: UiState, eventHandler: (UiEvent) -> Unit) {
    Log.d(TAG, "MonitorPage!")

    val position = remember { mutableStateOf(1f) }
    val positionRange = remember { mutableStateOf(10f) }

    Column {
        Chart(
            temperatureList,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(400.dp, 600.dp),
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
                            val tmp = entry.data as Temperature
                            return SimpleDateFormat("MM-dd HH:mm").format(Date(tmp.time))
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
            Log.d(TAG, "Update LineChart mx=$maximumCount")

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

            // it.setVisibleXRangeMaximum(10f)
            //it.moveViewToX((temperatureList.size - 1).toFloat())
        })
}

@Composable
fun ControlPage(uiState: UiState, connectResult: Boolean, eventHandler: (UiEvent) -> Unit) {
    Log.d(TAG, "Composing ControlTab! $uiState")
    Column(modifier = Modifier.padding(10.dp)) {
        Text(text = "Functions")
        Divider(modifier = Modifier.padding(vertical = 5.dp))

        // Create Radio
        RadioGroup(
            listOf(
                RadioBtn(
                    uiState.outWaterValveState,
                    stringResource(R.string.open_water_out),
                    connectResult
                ) { eventHandler(UiEvent.OutWaterEvent(true)) },
                RadioBtn(
                    !uiState.outWaterValveState,
                    stringResource(R.string.close_water_out),
                    connectResult
                ) { eventHandler(UiEvent.OutWaterEvent(false)) }
            )
        )
        RadioGroup(
            listOf(
                RadioBtn(
                    uiState.inWaterValveState,
                    stringResource(R.string.open_water_in),
                    connectResult
                ) { eventHandler(UiEvent.InWaterEvent(true)) },
                RadioBtn(
                    !uiState.inWaterValveState,
                    stringResource(R.string.close_water_in),
                    connectResult
                ) { eventHandler(UiEvent.InWaterEvent(false)) }
            )
        )
        RadioGroup(
            listOf(
                RadioBtn(
                    false,
                    stringResource(R.string.pump_run),
                    connectResult
                ) { eventHandler(UiEvent.PumpEvent(true)) },
                RadioBtn(false, stringResource(R.string.pump_stop), connectResult) {
                    eventHandler(
                        UiEvent.PumpEvent(
                            false
                        )
                    )
                }
            )
        )
        RadioGroup(
            listOf(
                RadioBtn(
                    false,
                    stringResource(R.string.purifier_on),
                    connectResult
                ) { eventHandler(UiEvent.PurifierEvent(true)) },
                RadioBtn(
                    false,
                    stringResource(R.string.purifier_off),
                    connectResult
                ) { eventHandler(UiEvent.PurifierEvent(false)) }
            )
        )
        RadioGroup(
            listOf(
                RadioBtn(false, stringResource(R.string.heater_on), connectResult) {
                    eventHandler(
                        UiEvent.HeaterEvent(
                            true
                        )
                    )
                },
                RadioBtn(false, stringResource(R.string.heater_off), connectResult) {
                    eventHandler(
                        UiEvent.HeaterEvent(
                            false
                        )
                    )
                }
            )
        )
        RadioGroup(
            listOf(
                RadioBtn(false, stringResource(R.string.board_led_on), connectResult) {
                    eventHandler(
                        UiEvent.LedEvent(
                            true
                        )
                    )
                },
                RadioBtn(false, stringResource(R.string.board_led_off), connectResult) {
                    eventHandler(
                        UiEvent.LedEvent(
                            false
                        )
                    )
                }
            )
        )
        OutlinedButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            enabled = connectResult,
            onClick = { eventHandler(UiEvent.ChangeWater()) }) {
            Text(text = stringResource(id = R.string.change_water))
        }
    }
}

data class RadioBtn(
    var selected: Boolean = false,
    var text: String,
    var enabled: Boolean = false,
    var onclick: () -> Unit
)

@Composable
fun RadioGroup(radioList: List<RadioBtn>) {
    var selectedIndex = radioList.indices.firstOrNull { radioList[it].selected } ?: 0

    Row {
        radioList.forEachIndexed { index, radioBtn ->
            val selected = index == selectedIndex
            val onClickHandle = {
                selectedIndex = index
                radioBtn.selected = true

                radioBtn.onclick()
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .selectable(selected = selected, onClick = onClickHandle),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
            ) {
                RadioButton(
                    selected = selected,
                    enabled = radioBtn.enabled,
                    onClick = onClickHandle
                )
                Text(text = radioBtn.text)
            }
        }
    }
}