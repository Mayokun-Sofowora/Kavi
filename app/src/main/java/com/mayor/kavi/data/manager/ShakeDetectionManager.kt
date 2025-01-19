package com.mayor.kavi.data.manager

import android.content.Context
import android.hardware.*
import com.mayor.kavi.di.AppModule.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.sqrt

/**
 * Manages the detection of shake events using the device's accelerometer sensor.
 * This class listens for shake events and invokes a provided listener when a shake is detected.
 */
class ShakeDetectionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsManager: SettingsManager,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) {
    private val scope = CoroutineScope(dispatcher + SupervisorJob())
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var accelerometerListener: SensorEventListener? = null
    private var lastShakeTime = 0L
    private val shakeCooldown = 500L
    private val shakeThreshold = 15f

    private var onShakeListener: (() -> Unit)? = null
    private var isEnabled = false
    private var isListening = false

    init {
        // Observes shake-enabled setting and starts or stops listening for shakes accordingly
        scope.launch {
            settingsManager.getShakeEnabled().collect { enabled ->
                withContext(Dispatchers.Main) {
                    isEnabled = enabled
                    if (enabled && !isListening) {
                        startListening()
                    } else if (!enabled && isListening) {
                        stopListening()
                    }
                }
            }
        }
    }

    /**
     * Sets the listener to be invoked when a shake is detected.
     *
     * @param listener A function to be invoked when a shake is detected.
     */
    fun setOnShakeListener(listener: () -> Unit) {
        onShakeListener = listener
        if (isEnabled && !isListening) {
            startListening()
        }
    }

    /**
     * Clears the shake listener and stops listening for shakes.
     */
    fun clearOnShakeListener() {
        onShakeListener = null
        stopListening()
    }

    /**
     * Starts listening for shake events using the accelerometer.
     * The listener will be triggered when a shake is detected.
     */
    fun startListening() {
        if (!isEnabled || isListening || accelerometerListener != null) return

        accelerometerListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    if (!isEnabled) return
                    if (it.sensor.type != Sensor.TYPE_ACCELEROMETER) return
                    if (System.currentTimeMillis() - lastShakeTime < shakeCooldown) return

                    val x = it.values[0]
                    val y = it.values[1]
                    val z = it.values[2]
                    val acceleration = sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH

                    if (acceleration > shakeThreshold) {
                        lastShakeTime = System.currentTimeMillis()
                        onShakeListener?.invoke()
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        accelerometer?.let { sensor ->
            sensorManager.registerListener(
                accelerometerListener,
                sensor,
                SensorManager.SENSOR_DELAY_GAME
            )
            isListening = true
        }
    }

    /**
     * Stops listening for shake events.
     */
    fun stopListening() = accelerometerListener?.let {
        if (isListening) {
            sensorManager.unregisterListener(it)
            accelerometerListener = null
            isListening = false
        }
    }
}
