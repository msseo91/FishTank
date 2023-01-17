package com.marine.fishtank.view

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.marine.fishtank.viewmodel.FishTankViewModel
import com.marine.fishtank.R
import com.marine.fishtank.model.*
import com.orhanobut.logger.Logger
import java.util.*

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
    Logger.d("Composing FishTankScreen")

    val message by viewModel.messageFlow.collectAsStateWithLifecycle()
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
            composable(BottomNavItem.Control.screenRoute) { ControlPage(viewModel) }
            composable(BottomNavItem.Monitor.screenRoute) { MonitorPage(viewModel) }
            composable(BottomNavItem.Camera.screenRoute) { CameraPage() }
            composable(BottomNavItem.Periodic.screenRoute) { SchedulePage(viewModel) }
        }
    }

    Logger.d("End composing FishTankScreen")
}

@Preview
@Composable
fun PreviewFishTankScreen() {
    FishTankScreen()
}