package com.marineseo.fishtank.mapper

import com.marineseo.fishtank.model.*
import org.apache.ibatis.annotations.Delete
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select
import org.apache.ibatis.annotations.Update
import java.sql.Timestamp
import java.util.*

const val TB_USER = "user"
const val COL_USER_ID = "id"
const val COL_USER_NAME = "name"
const val COL_USER_PASSWORD = "password"

const val COL_TEMP_ID = "id"
const val COL_TEMP_TEMPERATURE = "temperature"
const val COL_TEMP_TIME = "time"

@Mapper
interface DatabaseMapper {

    @Select("SELECT * FROM $TB_USER WHERE id=#{userId}")
    fun getUser(@Param("userId") id: String): User

    @Insert("INSERT INTO $TB_TEMPERATURE($COL_TEMP_TEMPERATURE, $COL_TEMP_TIME) VALUES(#{temperature},#{time})")
    fun insertTemperature(temperature: Temperature)

    @Select("SELECT * FROM $TB_TEMPERATURE WHERE $COL_TEMP_TIME BETWEEN #{from, jdbcType=TIMESTAMP} AND #{until, jdbcType=TIMESTAMP}")
    fun fetchTemperature(@Param("from") from: Timestamp, @Param("until") until: Timestamp): List<Temperature>

    @Insert( "INSERT INTO ${Task.TB_TASK}" +
            "(${Task.COL_USER_ID}, ${Task.COL_TYPE}, " +
            "${Task.COL_DATA},${Task.COL_EXECUTE_TIME}, ${Task.COL_STATE}" +
            ")" +
            "VALUES(#{userId},#{type},#{data},#{executeTime, jdbcType=TIMESTAMP},#{state})")
    fun insertTask(task: Task)

    @Delete("DELETE FROM ${Task.TB_TASK}")
    fun deleteAllTasks()

    @Select(
        "SELECT * FROM ${Task.TB_TASK} " +
                "WHERE ${Task.COL_STATE}=${Task.STATE_STANDBY} AND ${Task.COL_EXECUTE_TIME} < #{time, jdbcType=TIMESTAMP}" +
                "ORDER BY ${Task.COL_EXECUTE_TIME} ASC " +
                "LIMIT 1"
    )
    fun fetchTask(@Param("time") timeStamp: Timestamp): Task?

    @Select(
        "SELECT * FROM ${Task.TB_TASK} " +
                "WHERE ${Task.COL_TYPE}=${Task.TYPE_REPLACE_WATER} " +
                "ORDER BY ${Task.COL_EXECUTE_TIME} DESC " +
                "LIMIT 1"
    )
    fun getLastReplaceTask(): Task?

    @Update(
        "UPDATE ${Task.TB_TASK} " +
                "SET ${Task.COL_TYPE}=#{type}, ${Task.COL_STATE}=#{state}, ${Task.COL_DATA}=#{data}, " +
                "${Task.COL_USER_ID}=#{userId}, ${Task.COL_EXECUTE_TIME}=#{executeTime, jdbcType=TIMESTAMP} " +
                "WHERE ${Task.COL_ID}=#{id}"
    )
    fun updateTask(task: Task)

    @Insert("INSERT INTO ${PeriodicTask.TABLE_NAME}" +
            "(${PeriodicTask.COL_USER_ID}, ${PeriodicTask.COL_TYPE}, ${PeriodicTask.COL_DATA}, ${PeriodicTask.COL_TIME}) " +
            "VALUES(#{userId}, #{type}, #{data}, #{time})")
    fun insertPeriodicTask(periodicTask: PeriodicTask)

    @Select("SELECT * FROM ${PeriodicTask.TABLE_NAME} " +
            "WHERE userId=#{userId} ORDER BY ${PeriodicTask.COL_TIME} ASC")
    fun fetchPeriodicTasks(@Param("userId") userId: String): List<PeriodicTask>

    @Update("Update ${PeriodicTask.TABLE_NAME} " +
            "SET ${PeriodicTask.COL_USER_ID}=#{userId}, ${PeriodicTask.COL_TYPE}=#{type}, " +
            "${PeriodicTask.COL_DATA}=#{data}, ${PeriodicTask.COL_TIME}=#{time} " +
            "WHERE ${PeriodicTask.COL_ID}=#{id}")
    fun updatePeriodicTask(periodicTask: PeriodicTask)

    @Delete("DELETE FROM ${PeriodicTask.TABLE_NAME} WHERE id=#{id}")
    fun deletePeriodicTask(@Param("id") id: Int)

    @Select("SELECT * FROM ${PeriodicTask.TABLE_NAME} WHERE id=#{id}")
    fun selectPeriodicTask(@Param("id") id: Int): PeriodicTask?

    @Select("SELECT * FROM ${PeriodicTask.TABLE_NAME}")
    fun getAllPeriodicTask(): List<PeriodicTask>
}