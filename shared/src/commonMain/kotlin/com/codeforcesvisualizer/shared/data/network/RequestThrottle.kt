package com.codeforcesvisualizer.shared.data.network

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * Runs API calls one at a time, at least [minInterval] apart. The Codeforces API allows one call
 * every two seconds and answers faster callers with "Call limit exceeded".
 *
 * A caller that is cancelled while waiting its turn leaves the queue without making its call.
 */
class RequestThrottle(
    private val minInterval: Duration = 2.seconds,
    private val timeSource: TimeSource = TimeSource.Monotonic,
) {
    private val mutex = Mutex()
    private var lastCallEnd: TimeMark? = null

    suspend fun <T> run(block: suspend () -> T): T = mutex.withLock {
        lastCallEnd?.let { delay(minInterval - it.elapsedNow()) }
        try {
            block()
        } finally {
            lastCallEnd = timeSource.markNow()
        }
    }
}
