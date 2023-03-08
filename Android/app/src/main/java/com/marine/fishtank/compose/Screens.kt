package com.marine.fishtank.compose

sealed class Screens(val route: String) {
    object SignIn : Screens("signIn")
    object Control : Screens("control")
}