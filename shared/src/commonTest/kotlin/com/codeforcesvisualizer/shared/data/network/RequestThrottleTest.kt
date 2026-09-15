package com.codeforcesvisualizer.shared.data.network

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.testTimeSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class RequestThrottleTest {

    @Test
    fun spacesConsecutiveCalls() = runTest {
        val throttle = RequestThrottle(minInterval = 2.seconds, timeSource = testTimeSource)
        val starts = mutableListOf<Long>()

        repeat(3) { launch { throttle.run { starts += currentTime } } }
        advanceUntilIdle()

        assertEquals(listOf(0L, 2_000L, 4_000L), starts)
    }

    @Test
    fun waitsOnlyForTheRemainingInterval() = runTest {
        val throttle = RequestThrottle(minInterval = 2.seconds, timeSource = testTimeSource)
        throttle.run { }

        delay(1_500)
        var start = -1L
        throttle.run { start = currentTime }

        assertEquals(2_000L, start)
    }

    @Test
    fun cancelledCallerSkipsItsCall() = runTest {
        val throttle = RequestThrottle(minInterval = 2.seconds, timeSource = testTimeSource)
        val calls = mutableListOf<String>()
        throttle.run { calls += "first" }

        val queued = launch { throttle.run { calls += "cancelled" } }
        runCurrent()
        queued.cancel()
        throttle.run { calls += "next" }

        assertEquals(listOf("first", "next"), calls)
    }

    @Test
    fun failedCallStillCountsTowardTheInterval() = runTest {
        val throttle = RequestThrottle(minInterval = 2.seconds, timeSource = testTimeSource)
        runCatching { throttle.run { error("network down") } }

        var start = -1L
        throttle.run { start = currentTime }

        assertEquals(2_000L, start)
    }
}
