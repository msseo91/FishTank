package com.marineseo.fishtank.model

import java.util.*
import javax.persistence.*

@Entity
@Table(name = Task.TB_TASK)
data class Task(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Int = 0,

    @Column(nullable = false)
    val userId: String = "",

    @Column(nullable = false)
    var type: Int = TYPE_NONE,

    @Column
    var data: Int = 0,

    @Temporal(TemporalType.TIMESTAMP)
    var executeTime: Date = Date(),

    @Column
    var state: Int = STATE_STANDBY,
) {
    companion object {
        const val TYPE_NONE = -1
        const val TYPE_REPLACE_WATER = 0
        const val TYPE_VALVE_OUT_WATER = 1
        const val TYPE_VALVE_IN_WATER = 2
        const val TYPE_PURIFIER = 3
        const val TYPE_LIGHT = 4
        const val TYPE_PUMP = 5

        /**
         * Task is standby, waiting to execute
         */
        const val STATE_STANDBY = 0

        /**
         * Task is executing
         */
        const val STATE_EXECUTING = 1

        /**
         * Task is finished
         */
        const val STATE_FINISH = 2

        const val DATA_CLOSE = 0
        const val DATA_OPEN = 1

        const val TB_TASK = "task"
        const val COL_ID = "id"
        const val COL_USER_ID = "userId"
        const val COL_TYPE = "type"
        const val COL_DATA = "data"
        const val COL_EXECUTE_TIME = "executeTime"
        const val COL_STATE = "state"
    }

}
