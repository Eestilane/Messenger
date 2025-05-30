package com.example.messenger.libs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object ThrottleOperators{
    fun <T> throttleLatest(
        intervalMs: Long = 300L,
        coroutineScope: CoroutineScope,
        destinationFunction: (T)-> Unit
    ): (T)-> Unit {
        var throttleJob: Job? = null
        var latestParam: T
        return { param: T ->
            latestParam = param
            if (throttleJob?.isCompleted != false) {
                throttleJob = coroutineScope.launch {
                    delay(intervalMs)
                    destinationFunction(latestParam)
                }
            }
        }
    }
}
