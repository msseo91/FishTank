package com.marine.fishtank.model

/**
 * Created by MarineSeo on 2021-06-02.
 */
enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}

data class DataSource<out T>(val status: Status, val data: T ) {
    companion object {
        fun <T> success(data: T): DataSource<T> {
            return DataSource(Status.SUCCESS, data)
        }

        fun <T> error(data: T): DataSource<T> {
            return DataSource(Status.ERROR, data)
        }

        fun <T> loading(data: T): DataSource<T> {
            return DataSource(Status.LOADING, data)
        }
    }
}