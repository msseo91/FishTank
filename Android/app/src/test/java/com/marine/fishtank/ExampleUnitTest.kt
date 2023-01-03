package com.marine.fishtank

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun flowTest() = runBlocking {
        val state = MutableSharedFlow<Int>(
            replay = 10
        )
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            delay(100)
            state.collect {
                println(it)
            }
        }

        for (i in 1..20) {
            delay(10)
            state.emit(i)
        }
    }
}