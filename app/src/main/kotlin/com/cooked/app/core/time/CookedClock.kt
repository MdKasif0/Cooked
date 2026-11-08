package com.cooked.app.core.time

import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

interface CookedClock {
    fun now(): Instant
    fun zone(): ZoneId
}

@Singleton
class SystemCookedClock @Inject constructor() : CookedClock {
    override fun now(): Instant = Instant.now()
    override fun zone(): ZoneId = ZoneId.systemDefault()
}

