package com.marine.fishtank.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.marine.fishtank.R
import com.marine.fishtank.viewmodel.ControlViewModel
import com.orhanobut.logger.Logger

@Composable
fun ControlScreen(viewModel: ControlViewModel = hiltViewModel()) {
    Logger.d("Composing ControlScreen")

    val tankState by viewModel.tankControlStateFlow.collectAsStateWithLifecycle()
    val navController = rememberNavController()

    val items = listOf(
        BottomNavItem.Control,
        BottomNavItem.Monitor,
        BottomNavItem.Camera,
        BottomNavItem.Periodic
    )

    // Scaffold = TAB 전체화면
    Scaffold(
        topBar = { TopAppBar(title = { Text("FishTank") }) },
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
                                modifier = Modifier.width(26.dp).height(26.dp)
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
            composable(BottomNavItem.Control.screenRoute) {
                ControlPage(
                    dataSource = tankState,
                    onRefresh = { viewModel.refreshState() },
                    onOutValveClick = { viewModel.enableOutWater(it) },
                    onOutValve2Click = { viewModel.enableOutWater2(it) },
                    onInValveClick = { viewModel.enableInWater(it) },
                    onHeaterClick = { viewModel.enableHeater(it) },
                    onBrightnessChange = { viewModel.changeLightBrightness(it) },
                )
            }
            composable(BottomNavItem.Monitor.screenRoute) { MonitorPage(viewModel) }
            composable(BottomNavItem.Camera.screenRoute) { CameraPage() }
            composable(BottomNavItem.Periodic.screenRoute) { SchedulePage(viewModel) }
        }
    }

    Logger.d("End composing ControlScreen")
}

@Preview
@Composable
fun PreviewControlScreen() {
    ControlScreen()
}

sealed class BottomNavItem(
    val titleRes: Int, val iconRes: Int, val screenRoute: String
) {
    object Control : BottomNavItem(R.string.text_control, R.drawable.control, "Control")
    object Monitor : BottomNavItem(R.string.text_monitor, R.drawable.bar_chart, "Monitor")
    object Camera : BottomNavItem(R.string.text_camera, R.drawable.camera, "Camera")
    object Periodic : BottomNavItem(R.string.text_periodic, R.drawable.calendar, "Periodic")
}