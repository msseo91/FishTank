package com.marineseo.fishtank.controller

import com.marineseo.fishtank.model.DeviceState
import com.marineseo.fishtank.model.PeriodicTask
import com.marineseo.fishtank.model.Temperature
import com.marineseo.fishtank.service.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private const val KEY_TOKEN = "token"
private const val KEY_ENABLE = "enable"
private const val KEY_ID = "id"
private const val KEY_PASSWORD = "password"
private const val KEY_DAYS = "days"
private const val KEY_PERCENTAGE = "percentage"
private const val KEY_PERIODIC = "periodicTask"
private const val KEY_TYPE = "type"
private const val KEY_DATA = "data"
private const val KEY_TIME = "time"

const val RESULT_FAIL_GENERAL = 0
const val RESULT_SUCCESS = 100
const val RESULT_FAIL_AUTH = 1
const val RESULT_FAIL_DEVICE_CONNECTION = 2

@RestController
@RequestMapping("/fish")
class FishController(
    private val raspberryService: RaspberryService,
    private val taskService: TaskService,
    private val userService: UserService,
    private val temperatureService: TemperatureService
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @GetMapping("/test")
    fun test(): ResponseEntity<String> {
        return ResponseEntity.ok("Hi I am fish...")
    }

    @PostMapping("/signin")
    fun signIn(
        @RequestParam(KEY_ID) id: String,
        @RequestParam(KEY_PASSWORD) password: String
    ): ResponseEntity<String> {
        val user = userService.signIn(id, password) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)

        // Sign in success!!
        val token = user.token
        logger.info("$id sign-in! token=$token")

        return ResponseEntity.ok(token)
    }

    @PostMapping("/readDBTemperature")
    fun readDBTemperature(
        @RequestParam(KEY_TOKEN) token: String,
        @RequestParam(KEY_DAYS) days: Int
    ): ResponseEntity<List<Temperature>> {
        if (userService.getUserByToken(token) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)

        val temperatureList = temperatureService.readTemperature(days)
        return ResponseEntity.ok(temperatureList)
    }

    @PostMapping("/outWater")
    fun enableOutWater(
        @RequestParam(KEY_TOKEN) token: String,
        @RequestParam(KEY_ENABLE) enable: Boolean
    ): ResponseEntity<Int> {
        if (userService.getUserByToken(token) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)

        raspberryService.enableOutWaterValve(enable)
        return ResponseEntity.ok(RESULT_SUCCESS)
    }

    @PostMapping("/outWater2")
    fun enableOutWater2(
        @RequestParam(KEY_TOKEN) token: String,
        @RequestParam(KEY_ENABLE) enable: Boolean
    ): ResponseEntity<Int> {
        if (userService.getUserByToken(token) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)
        raspberryService.enableOutWaterValve2(enable)
        return ResponseEntity.ok(RESULT_SUCCESS)
    }

    @PostMapping("/inWater")
    fun enableInWater(
        @RequestParam(KEY_TOKEN) token: String,
        @RequestParam(KEY_ENABLE) enable: Boolean
    ): ResponseEntity<Int> {
        if (userService.getUserByToken(token) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)

        raspberryService.enableInWaterValve(enable)
        return ResponseEntity.ok(RESULT_SUCCESS)
    }

    @PostMapping("/read/inWater")
    fun readInWater(@RequestParam(KEY_TOKEN) token: String): ResponseEntity<Boolean> {
        if (userService.getUserByToken(token) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)

        return ResponseEntity.ok(raspberryService.isInWaterValveOpen())
    }

    @PostMapping("/read/outWater")
    fun readOutWater(@RequestParam(KEY_TOKEN) token: String): ResponseEntity<Boolean> {
        if (userService.getUserByToken(token) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)

        return ResponseEntity.ok(raspberryService.isOutWaterValveOpen())
    }

    @PostMapping("/read/outWater2")
    fun readOutWater2(@RequestParam(KEY_TOKEN) token: String): ResponseEntity<Boolean> {
        if (userService.getUserByToken(token) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)
        return ResponseEntity.ok(raspberryService.isOutWaterValve2Open())
    }

    @PostMapping("/read/allState")
    fun readAllState(@RequestParam(KEY_TOKEN) token: String): ResponseEntity<DeviceState> {
        userService.getUserByToken(token) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)

        return ResponseEntity.ok(DeviceState(
            isOutletValve1Enabled = raspberryService.isOutWaterValveOpen(),
            isOutletValve2Enabled = raspberryService.isOutWaterValve2Open(),
            isInletValveEnabled = raspberryService.isInWaterValveOpen(),
            isHeaterEnabled = raspberryService.isHeaterOn(),
            isLightOn = raspberryService.isLightOn(),
            isCo2ValveOpened = raspberryService.isCo2ValveOpen(),
        ))
    }

    @PostMapping("/light/enable")
    fun enableLight(@RequestParam(KEY_TOKEN) token: String, @RequestParam(KEY_ENABLE) enable: Boolean): ResponseEntity<Int> {
        userService.getUserByToken(token) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)
        raspberryService.enableLight(enable)
        return ResponseEntity.ok(RESULT_SUCCESS)
    }

    @PostMapping("/light/read")
    fun readLightState(@RequestParam(KEY_TOKEN) token: String): ResponseEntity<Boolean> {
        userService.getUserByToken(token) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)
        return ResponseEntity.ok(raspberryService.isLightOn())
    }

    @PostMapping("/co2/enable")
    fun enableCo2Valve(@RequestParam(KEY_TOKEN) token: String, @RequestParam(KEY_ENABLE) enable: Boolean): ResponseEntity<Int> {
        userService.getUserByToken(token) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)
        raspberryService.enableCo2Valve(enable)
        return ResponseEntity.ok(RESULT_SUCCESS)
    }

    @PostMapping("/co2/read")
    fun readCo2ValveState(@RequestParam(KEY_TOKEN) token: String): ResponseEntity<Boolean> {
        userService.getUserByToken(token) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)
        return ResponseEntity.ok(raspberryService.isCo2ValveOpen())
    }

    @PostMapping("/func/replaceWater")
    fun replaceWater(
        @RequestParam(KEY_TOKEN) token: String,
        @RequestParam(KEY_PERCENTAGE) percentage: Float
    ): ResponseEntity<Int> {
        if (userService.getUserByToken(token) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)

        taskService.createReplaceWaterTask(percentage)
        return ResponseEntity.ok(RESULT_SUCCESS)
    }

    @PostMapping("/periodic/add")
    fun addPeriodicTask(
        @RequestParam(KEY_TOKEN) token: String,
        @RequestParam(KEY_TYPE) type: Int,
        @RequestParam(KEY_DATA) data: Int,
        @RequestParam(KEY_TIME) time: String
    ): ResponseEntity<Boolean> {
        logger.info("add Periodic Task! type=$type data=$data time=$time")
        val user = userService.getUserByToken(token) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)
        taskService.addPeriodicTask(
            PeriodicTask(
                userId = user.id,
                type = type,
                data = data,
                time = time
            )
        )

        return ResponseEntity.ok(true)
    }

    @PostMapping("/periodic/fetch")
    fun fetchPeriodicTasks(
        @RequestParam(KEY_TOKEN) token: String
    ): ResponseEntity<List<PeriodicTask>> {
        val user = userService.getUserByToken(token) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)
        return ResponseEntity.ok(taskService.fetchPeriodicTask(user.id))
    }

    @PostMapping("/periodic/delete")
    fun deletePeriodicTask(
        @RequestParam(KEY_TOKEN) token: String,
        @RequestParam(KEY_PERIODIC) taskId: Int
    ): ResponseEntity<Boolean> {
        val user = userService.getUserByToken(token) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)
        val targetPeriodicTask =
            taskService.selectPeriodicTasK(taskId) ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)

        if (user.id != targetPeriodicTask.userId) {
            // Only owner can delete task.
            return ResponseEntity.ok(false)
        }

        taskService.deletePeriodicTask(targetPeriodicTask.id)
        return ResponseEntity.ok(true)
    }

    @PostMapping("/heater")
    fun enableHeater(
        @RequestParam(KEY_TOKEN) token: String,
        @RequestParam(KEY_ENABLE) enable: Boolean
    ): ResponseEntity<Int> {
        if (userService.getUserByToken(token) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)

        raspberryService.enableHeater(enable)
        return ResponseEntity.ok(RESULT_SUCCESS)
    }

    @PostMapping("/read/heater")
    fun isHeaterEnabled(@RequestParam(KEY_TOKEN) token: String): ResponseEntity<Boolean> {
        if (userService.getUserByToken(token) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)

        return ResponseEntity.ok(raspberryService.isHeaterOn())
    }
}