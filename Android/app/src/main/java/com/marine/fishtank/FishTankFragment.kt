package com.marine.fishtank

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.marine.fishtank.compose.ControlScreen
import com.marine.fishtank.compose.Screens
import com.marine.fishtank.compose.SignInScreen
import com.marine.fishtank.viewmodel.ControlViewModel
import com.marine.fishtank.viewmodel.SignInViewModel
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FishTankFragment : Fragment() {
    private val signInViewModel by viewModels<SignInViewModel>()
    private val controlViewModel by viewModels<ControlViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Logger.d("onCreateView")

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val navController = rememberNavController()

                MaterialTheme {
                    NavHost(
                        navController = navController,
                        startDestination = Screens.SignIn.route
                    ) {
                        composable(Screens.SignIn.route) {
                            SignInScreen(signInViewModel) { navController.navigate(Screens.Control.route) }
                        }

                        composable(Screens.Control.route) {
                            ControlScreen(controlViewModel)
                        }
                    }
                }
            }
        }
    }
}



