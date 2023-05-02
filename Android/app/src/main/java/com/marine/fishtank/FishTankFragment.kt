package com.marine.fishtank

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.marine.fishtank.compose.ControlScreen
import com.marine.fishtank.compose.Screens
import com.marine.fishtank.compose.SignInScreen
import com.marine.fishtank.theme.FishTheme
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FishTankFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Logger.d("onCreateView")

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val navController = rememberNavController()

                FishTheme {
                    NavHost(
                        navController = navController,
                        startDestination = Screens.SignIn.route
                    ) {
                        composable(Screens.SignIn.route) {
                            SignInScreen {
                                if(navController.currentDestination?.route != Screens.Control.route) {
                                    navController.navigate(Screens.Control.route) {
                                        popUpTo(route = Screens.SignIn.route) {
                                            inclusive = true
                                        }
                                    }
                                }
                            }
                        }

                        composable(Screens.Control.route) {
                            ControlScreen()
                        }
                    }
                }
            }
        }
    }
}



