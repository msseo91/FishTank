package com.marine.fishtank.server.database

import com.marine.fishtank.server.model.Temperature
import com.marine.fishtank.server.model.User
import java.sql.Date
import java.sql.ResultSet
import java.text.SimpleDateFormat

private const val TAG = "DataBaseHelper"

fun ResultSet.toTemperature(): List<Temperature> {
    val list = mutableListOf<Temperature>()

    first()
    while (next()) {
        list.add(
            Temperature(
                id = getInt(COL_TEMP_ID),
                temperature = getFloat(COL_TEMP_TEMPERATURE),
                time = getTimestamp(COL_TEMP_TIME).time
            )
        )
    }

    return list
}

fun Date.format(): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(this)
}

fun ResultSet.toSingleUser(): User? {
    if (!next()) {
        return null
    }

    return User(
        id = getString(COL_USER_ID),
        name = getString(COL_USER_NAME),
        password = getString(COL_USER_PASSWORD)
    )
}