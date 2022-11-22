package com.marine.fishtank.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Temperature (
    var id: Int = 0,
    var temperature: Float,
    var time: Date = Date()
): Parcelable