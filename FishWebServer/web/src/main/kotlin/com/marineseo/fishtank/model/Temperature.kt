package com.marineseo.fishtank.model

import java.util.Date

data class Temperature (
    var id: Int = 0,
    var temperature: Float = 0f,
    var time: Date = Date()
)