package com.marineseo.fishtank.util

import kotlin.random.Random

fun makeRandomString(len: Int): String {
    val random = Random(System.currentTimeMillis())
    val buffer = StringBuffer()

    repeat(len) {
        val char = random.nextInt('A'.toInt(), 'z'.toInt()).toChar()
        buffer.append(char)
    }

    return buffer.toString()
}

fun String.runCommand(): String  {
    val cmds = this.split(" ").toTypedArray()
    println("cmds=${cmds.joinToString { "[$it]" }}")

    val process = Runtime.getRuntime().exec(cmds)
    return process.inputStream.bufferedReader().readText()
}