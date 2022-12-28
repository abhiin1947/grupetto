package com.spop.poverlay.sensor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class DeadSensorInterface : SensorInterface {
    override val power: Flow<Float>
        get() = flow { }
    override val cadence: Flow<Float>
        get() = flow { }
    override val resistance: Flow<Float>
        get() = flow { }
    init {
        Timber.e("using dead sensor interface, sensor values will not update")
    }
}