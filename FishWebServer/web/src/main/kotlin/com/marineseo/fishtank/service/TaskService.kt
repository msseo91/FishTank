package com.marineseo.fishtank.service

import com.marineseo.fishtank.mapper.PeriodicTaskRepository
import com.marineseo.fishtank.mapper.TaskRepository
import com.marineseo.fishtank.model.PeriodicTask
import com.marineseo.fishtank.model.Task
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.util.*

private const val WATER_VOLUME = 100000 // ml
private const val WATER_OUT_IN_MINUTE = 578 // ml
private const val WATER_REPLACE_RATIO_MAX = 0.5

private const val TAG = "TaskService"
private const val TASK_INTERVAL = 1000L * 3

@Service
class TaskService(
    private val raspberryService: RaspberryService,
    private val periodicTaskRepository: PeriodicTaskRepository,
    private val taskRepository: TaskRepository
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @Scheduled(fixedDelay = TASK_INTERVAL)
    fun executeTask() {

        fetchTask()?.let { task ->
            if(task.state != Task.STATE_STANDBY) {
                logger.warn("Pass this task. State is not STANDBY.")
                return@let
            }
            logger.info("Executing $task")

            when (task.type) {
                Task.TYPE_REPLACE_WATER -> {

                }
                Task.TYPE_VALVE_IN_WATER -> {
                    raspberryService.enableInWaterValve(
                        open = task.data == Task.DATA_OPEN
                    )
                }
                Task.TYPE_VALVE_OUT_WATER -> {
                    raspberryService.enableOutWaterValve(
                        open = task.data == Task.DATA_OPEN
                    )
                }
                Task.TYPE_LIGHT -> {
                    raspberryService.adjustBrightness(task.data * 0.01f)
                }
                Task.TYPE_PUMP -> {
                    raspberryService.enablePump(task.data == Task.DATA_OPEN)
                }
                Task.TYPE_PURIFIER -> {
                    // TODO
                }
            }

            task.state = Task.STATE_FINISH
            taskRepository.save(task)
        }
    }

    fun createReplaceWaterTask(ratio: Float) {
        if (ratio < 0 || ratio > WATER_REPLACE_RATIO_MAX) {
            // Wrong param.
            logger.error("Wrong ratio parameter! ratio=$ratio")
            return
        }

        // Calculate the amount of water that needs to be replaced.
        val amountOfWater = (WATER_VOLUME * ratio)
        val outTime = amountOfWater / WATER_OUT_IN_MINUTE
        val outTimeInSec = (outTime * 60).toInt()
        logger.info("Replace water=$amountOfWater, outTime=$outTimeInSec sec")

        // Create tasks.
        // For tracing(and logging), we put TYPE_REPLACE_WATER task.
        taskRepository.save(Task(type = Task.TYPE_REPLACE_WATER, state = Task.STATE_STANDBY))
        taskRepository.save(Task(
            type = Task.TYPE_VALVE_IN_WATER,
            data = Task.DATA_CLOSE,
            state = Task.STATE_STANDBY
        ))

        taskRepository.save(
            Task(
                type = Task.TYPE_VALVE_OUT_WATER,
                data = Task.DATA_OPEN,
                state = Task.STATE_STANDBY
            )
        )

        val finishTime = System.currentTimeMillis() + (outTimeInSec * 1000L)
        taskRepository.save(
            Task(
                type = Task.TYPE_VALVE_OUT_WATER,
                data = Task.DATA_CLOSE,
                executeTime = Timestamp(finishTime),
                state = Task.STATE_STANDBY
            )
        )

        taskRepository.save(
            Task(
                type = Task.TYPE_VALVE_IN_WATER,
                data = Task.DATA_OPEN,
                executeTime = Timestamp(finishTime + 1000L),
                state = Task.STATE_STANDBY
            )
        )
    }

    @Scheduled(cron = "0 0 0 * * ?")
    fun periodicToTask() {
        logger.info("Start periodicToTask!")
        // Delete previous task
        taskRepository.deleteAll()

        val periodicTasks = periodicTaskRepository.findAll()
        for(periodicTask in periodicTasks) {
            taskRepository.save(Task(
                userId = periodicTask.userId,
                type = periodicTask.type,
                data = periodicTask.data,
                executeTime = Timestamp(Calendar.getInstance().apply {
                    val divided = periodicTask.time.split(":")
                    set(Calendar.HOUR_OF_DAY, divided[0].toInt())
                    set(Calendar.MINUTE, divided[1].toInt())
                }.timeInMillis)
            ))
        }
    }

    private fun fetchTask(): Task? {
        return taskRepository.findByStateAndExecuteTimeGreaterThan(Task.STATE_STANDBY, Date()).firstOrNull()
    }

    fun fetchPeriodicTask(userId: String): List<PeriodicTask> {
        return periodicTaskRepository.findAllByUserId(userId)
    }

    fun addPeriodicTask(periodicTask: PeriodicTask) {
        periodicTaskRepository.save(periodicTask)
        periodicToTask()
    }

    fun deletePeriodicTask(id: Int) {
        periodicTaskRepository.deleteById(id)
        periodicToTask()
    }

    fun selectPeriodicTasK(id: Int): PeriodicTask? {
        return periodicTaskRepository.findById(id).orElse(null)
    }
}