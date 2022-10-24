package com.marineseo.fishtank.fishwebserver.controller

import com.marineseo.fishtank.fishwebserver.model.PeriodicTask
import com.marineseo.fishtank.fishwebserver.model.RESULT_FAIL_DEVICE_CONNECTION
import com.marineseo.fishtank.fishwebserver.model.RESULT_SUCCESS
import com.marineseo.fishtank.fishwebserver.model.Temperature
import com.marineseo.fishtank.fishwebserver.service.ArduinoService
import com.marineseo.fishtank.fishwebserver.service.TaskService
import com.marineseo.fishtank.fishwebserver.service.TemperatureService
import com.marineseo.fishtank.fishwebserver.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

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

@RestController
@RequestMapping("/fish")
class FishController(
    private val arduinoService: ArduinoService,
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

    @PostMapping("/boardLed")
    fun enableBoardLed(
        @RequestParam(KEY_TOKEN) token: String,
        @RequestParam(KEY_ENABLE) enable: Boolean
    ): ResponseEntity<Int> {
        if (userService.getUserByToken(token) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)

        return ResponseEntity.ok(
            if (arduinoService.enableBoardLed(enable)) RESULT_SUCCESS
            else RESULT_FAIL_DEVICE_CONNECTION
        )
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

        return ResponseEntity.ok(
            if (arduinoService.enableOutWaterValve(enable)) RESULT_SUCCESS
            else RESULT_FAIL_DEVICE_CONNECTION
        )
    }

    @PostMapping("/inWater")
    fun enableInWater(
        @RequestParam(KEY_TOKEN) token: String,
        @RequestParam(KEY_ENABLE) enable: Boolean
    ): ResponseEntity<Int> {
        if (userService.getUserByToken(token) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)

        return ResponseEntity.ok(
            if (arduinoService.enableInWaterValve(enable)) RESULT_SUCCESS
            else RESULT_FAIL_DEVICE_CONNECTION
        )
    }

    @PostMapping("/light")
    fun enableLight(
        @RequestParam(KEY_TOKEN) token: String,
        @RequestParam(KEY_ENABLE) enable: Boolean
    ): ResponseEntity<Int> {
        if (userService.getUserByToken(token) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)

        return ResponseEntity.ok(
            if (arduinoService.enableLight(enable)) RESULT_SUCCESS
            else RESULT_FAIL_DEVICE_CONNECTION
        )
    }

    @PostMapping("/purifier")
    fun enablePurifier(
        @RequestParam(KEY_TOKEN) token: String,
        @RequestParam(KEY_ENABLE) enable: Boolean
    ): ResponseEntity<Int> {
        if (userService.getUserByToken(token) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)

        return ResponseEntity.ok(
            if (arduinoService.enablePurifier(enable)) RESULT_SUCCESS
            else RESULT_FAIL_DEVICE_CONNECTION
        )
    }

    @PostMapping("/heater")
    fun enableHeater(
        @RequestParam(KEY_TOKEN) token: String,
        @RequestParam(KEY_ENABLE) enable: Boolean
    ): ResponseEntity<Int> {
        if (userService.getUserByToken(token) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)

        return ResponseEntity.ok(
            if (arduinoService.enableHeater(enable)) RESULT_SUCCESS
            else RESULT_FAIL_DEVICE_CONNECTION
        )
    }

    @PostMapping("/read/inWater")
    fun readInWater(@RequestParam(KEY_TOKEN) token: String): ResponseEntity<Boolean> {
        if (userService.getUserByToken(token) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)

        return ResponseEntity.ok(
            arduinoService.isInWaterValveOpen()
        )
    }

    @PostMapping("/read/outWater")
    fun readOutWater(@RequestParam(KEY_TOKEN) token: String): ResponseEntity<Boolean> {
        if (userService.getUserByToken(token) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)

        return ResponseEntity.ok(
            arduinoService.isOutWaterValveOpen()
        )
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

    @PostMapping("/brightness")
    fun adjustBrightness(
        @RequestParam(KEY_TOKEN) token: String,
        @RequestParam(KEY_PERCENTAGE) percentage: Float
    ): ResponseEntity<Boolean> {
        if (userService.getUserByToken(token) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)

        return ResponseEntity.ok(arduinoService.adjustBrightness(percentage))
    }

    @PostMapping("/brightness/read")
    fun readLightBrightness(
        @RequestParam(KEY_TOKEN) token: String,
    ): ResponseEntity<Float> {
        if (userService.getUserByToken(token) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)
        return ResponseEntity.ok(arduinoService.readBrightness())
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
        @RequestParam(KEY_PERIODIC) periodicTask: PeriodicTask
    ): ResponseEntity<Boolean> {
        val user = userService.getUserByToken(token) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)
        if(user.id != periodicTask.userId) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)
        val targetPeriodicTask = taskService.selectPeriodicTasK(periodicTask.id) ?:return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)

        if(user.id != targetPeriodicTask.userId) {
            // Only owner can delete task.
            return ResponseEntity.ok(false)
        }

        taskService.deletePeriodicTask(targetPeriodicTask.id)
        return ResponseEntity.ok(true)
    }

    @PostMapping("/reconnect")
    fun reconnectArduino(@RequestParam(KEY_TOKEN) token: String) {
        arduinoService.reConnect()
    }

}