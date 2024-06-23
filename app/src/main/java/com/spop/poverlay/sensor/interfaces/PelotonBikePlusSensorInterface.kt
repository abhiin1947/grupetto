package com.spop.poverlay.sensor.interfaces

import android.content.Context
import android.os.IBinder
import com.spop.poverlay.sensor.v2.BikePlusPowerSensor
import com.spop.poverlay.sensor.v2.BikePlusResistanceSensor
import com.spop.poverlay.sensor.v2.BikePlusRpmSensor
import com.spop.poverlay.sensor.v2.getV2Binder
import com.spop.poverlay.util.windowed
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlin.coroutines.CoroutineContext

class PelotonBikePlusSensorInterface(val context: Context) : SensorInterface, CoroutineScope {
    companion object{
        /**
         * Resistance is filtered with a moving window since it occasionally spikes
         * The last few resistance readings will grouped, and the lowest reading will be shown
         *
         * The spikes are likely a limitation of ADC accuracy
         */
        const val ResistanceMovingAverageWindowSize = 3
    }
    private val binder = MutableSharedFlow<IBinder>(replay = 1)

    init {
        launch(Dispatchers.IO) {
            val service = getV2Binder(context)
            binder.emit(service)
        }
    }

    override val coroutineContext: CoroutineContext
        get() = SupervisorJob()

    fun stop() {
        coroutineContext.cancelChildren()
    }

    override val power: Flow<Float>
        get() = binder.flatMapLatest {
            val powerSensor = BikePlusPowerSensor(it)
            powerSensor.start()
            powerSensor.sensorValue
        }

    override val cadence: Flow<Float>
        get() = binder.flatMapLatest {
            val rpmSensor = BikePlusRpmSensor(it)
            rpmSensor.start()
            rpmSensor.sensorValue
        }

    override val resistance: Flow<Float>
        get() = binder.flatMapLatest {
            val resistanceSensor = BikePlusResistanceSensor(it)
            resistanceSensor.start()
            resistanceSensor.sensorValue
        }
            .windowed(ResistanceMovingAverageWindowSize, 1, true) { readings ->
                // Resistance sensor occasionally spikes for a single reading
                // So take the least of the last few readings
                readings.minOf { it }
            }

}