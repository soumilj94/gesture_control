package com.soumil.gesturecontrol.gestures

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import com.soumil.gesturecontrol.databinding.ActivityMainBinding
import java.util.Locale

class ProximitySensorEvent(
    private val context: Context,
    private val binding: ActivityMainBinding,
    private val toggleSwitch: () -> Unit,
): SensorEventListener {
    private var lastProximityValue: Float = 5f

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_PROXIMITY){
            val proximityValue = event.values[0]
            val formattedValues = String.format(Locale.US, "%.0f", proximityValue)

            binding.proximityValue.text = formattedValues

            if (lastProximityValue == 5f && proximityValue == 0f){
                toggleSwitch.invoke()
            }
            
            lastProximityValue = proximityValue
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}