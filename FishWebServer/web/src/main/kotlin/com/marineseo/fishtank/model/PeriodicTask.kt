package com.marineseo.fishtank.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = PeriodicTask.TABLE_NAME)
data class PeriodicTask(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Int = 0,

    @Column(nullable = false)
    var userId: String = "",

    @Column(nullable = false)
    var type: Int = 0,

    @Column
    var data: Int = 0,

    @Column
    var time: String = ""
) {
    companion object {
        const val TABLE_NAME = "periodicTask"
        const val COL_ID = "id"
        const val COL_USER_ID = "userId"
        const val COL_TYPE = "type"
        const val COL_DATA = "data"
        const val COL_TIME = "time"

        const val TYPE_REPLACE_WATER = Task.TYPE_REPLACE_WATER
        const val TYPE_VALVE_OUT_WATER = Task.TYPE_VALVE_OUT_WATER
        const val TYPE_VALVE_IN_WATER = Task.TYPE_VALVE_OUT_WATER
        const val TYPE_PURIFIER = Task.TYPE_VALVE_OUT_WATER
        const val TYPE_LIGHT = Task.TYPE_VALVE_OUT_WATER
    }
}
