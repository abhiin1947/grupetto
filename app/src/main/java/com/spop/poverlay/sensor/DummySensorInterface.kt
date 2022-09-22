package com.spop.poverlay.sensor

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

class DummySensorInterface : SensorInterface {
    override val power: Flow<Float>
        get() = dummyValueFlow(100f)
    override val cadence: Flow<Float>
        get() = dummyValueFlow(50f)
    override val resistance: Flow<Float>
        get() = dummyValueFlow(100f)


    private fun dummyValueFlow(magnitude : Float) = flow {
        val sineValues = generateSequence(0..360 step 10) { it }.flatten().map {
            (sin(Math.toRadians(it.toDouble())) + 1) * (magnitude / 2)
        }
        for(value in sineValues){
            delay(500.milliseconds)
            emit(value.toFloat())
        }
    }
}