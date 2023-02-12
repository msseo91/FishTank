package com.marineseo.fishtank

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
open class FishWebServerApplication

fun main(args: Array<String>) {
    runApplication<FishWebServerApplication>(*args)
}
