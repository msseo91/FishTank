package com.marine.fishtank.server.database

import com.marine.fishtank.server.model.Temperature
import com.marine.fishtank.server.util.Log
import java.sql.Connection
import java.sql.Date
import java.sql.DriverManager
import java.sql.Timestamp

private const val CONNECTION_URL = "jdbc:mariadb://127.0.0.1:16650/fishtank"
private const val USER_ID = "root"
private const val PASSWORD = "tjaudtn513!"

const val TB_USER = "user"
const val COL_USER_ID = "id"
const val COL_USER_NAME = "name"
const val COL_USER_PASSWORD = "password"

const val TB_TEMPERATURE = "temperature"
const val COL_TEMP_ID = "id"
const val COL_TEMP_TEMPERATURE = "temperature"
const val COL_TEMP_TIME = "time"

private const val TAG = "DataBase"

object DataBase {
    private lateinit var connection: Connection
    private var isInit = false

    fun initialize() {
        Class.forName("org.mariadb.jdbc.Driver")

        connection = DriverManager.getConnection(CONNECTION_URL, USER_ID, PASSWORD)
        isInit = true
    }

    fun logIn(id: String, password: String): Boolean {
        assertInit()

        val statement = connection.prepareStatement("SELECT * FROM $TB_USER WHERE id=?").apply {
            setString(1, id)
        }
        val user = statement.executeQuery().toSingleUser()
        if (user == null) {
            Log.d(TAG, "User not found for $id")
            return false
        }

        return user.password == password
    }

    fun insertTemperature(temperature: Temperature) {
        assertInit()

        val statement = connection.prepareStatement(
            "INSERT INTO $TB_TEMPERATURE($COL_TEMP_TEMPERATURE, $COL_TEMP_TIME) VALUES(?,?)"
        ).apply {
            setFloat(1, temperature.temperature)
            setTimestamp(2, Timestamp(temperature.time))
        }
        statement.executeUpdate()
    }

    fun fetchTemperature(from: Date, until: Date): List<Temperature> {
        assertInit()

        val sql = "SELECT * FROM $TB_TEMPERATURE WHERE $COL_TEMP_TIME BETWEEN ? AND ?"
        val fromStr = from.format()
        val untilStr = until.format()
        Log.i(TAG, "$sql, param=$fromStr, $untilStr")
        val statement = connection.prepareStatement(sql).apply {
            setString(1, fromStr)
            setString(2, untilStr)
        }

        return statement.executeQuery().toTemperature()
    }

    private fun assertInit() {
        if (!isInit) {
            throw IllegalAccessException("You MUST init first!")
        }
    }
}