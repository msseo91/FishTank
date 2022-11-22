package com.marine.fishtank.model

import kotlin.random.Random


object AppId {
    val MY_ID = Random(System.currentTimeMillis()).nextInt(Integer.MAX_VALUE)
}