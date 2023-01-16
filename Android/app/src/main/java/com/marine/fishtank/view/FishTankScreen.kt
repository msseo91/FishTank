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
import androidx.lifecycle.viewmodel.compose.viewModel
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
fun FishTankScreen(viewModel: FishTankViewModel = viewModel()) {
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

@Preview
@Composable
fun PreviewFishTankScreen() {
    FishTankScreen()
}