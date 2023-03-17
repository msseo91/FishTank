package com.marine.fishtank.api

import com.skydoves.sandwich.ApiResponse
import com.skydoves.sandwich.message
import kotlinx.coroutines.flow.*

sealed interface TankResult<out T> {
    class Success<T>(val data: T) : TankResult<T>
    class Error(val exception: Throwable? = null, val message: String = "") : TankResult<Nothing>
    class Loading : TankResult<Nothing>
}

fun <T> Flow<T>.asTankResult(): Flow<TankResult<T>> {
    return this
        .map<T, TankResult<T>> {
            TankResult.Success(it)
        }
        .onStart { emit(TankResult.Loading()) }
        .catch { emit(TankResult.Error(it)) }
}

fun <T> ApiResponse<T>.asTankResult(): Flow<TankResult<T>> {
    return when(this) {
        is ApiResponse.Success -> flowOf(TankResult.Success(data))
        is ApiResponse.Failure -> flowOf(TankResult.Error(message = message()))
    }
}
