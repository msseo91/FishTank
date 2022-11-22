package com.marine.fishtank

import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController

enum class Screen { LogIn, FishTank }

fun Fragment.navigate(from: Screen, to: Screen) {
    if(from == to) {
        throw IllegalArgumentException("Navigating to same screen is not allowed!")
    }

    when(to) {
        Screen.LogIn -> {
            findNavController().navigate(R.id.logInFragment)
        }
        Screen.FishTank -> {
            findNavController().navigate(R.id.fishTankFragment, null,
                NavOptions.Builder().setPopUpTo(R.id.logInFragment, true).build())
        }
    }
}