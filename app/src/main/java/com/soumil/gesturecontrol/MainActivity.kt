package com.soumil.gesturecontrol

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.media.AudioManager
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import com.soumil.gesturecontrol.databinding.ActivityMainBinding
import com.soumil.gesturecontrol.gestures.AccelerometerSensorEvent
import com.soumil.gesturecontrol.gestures.ProximitySensorEvent

class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager
    private lateinit var audioManager: AudioManager

    private lateinit var proximitySensor: Sensor
    private lateinit var accelerometerSensor: Sensor

    private lateinit var proximitySensorEvent: ProximitySensorEvent
    private lateinit var accelerometerSensorEvent: AccelerometerSensorEvent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)!!
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!

        proximitySensorEvent = ProximitySensorEvent(this, binding) { toggleSwitch() }
        accelerometerSensorEvent = AccelerometerSensorEvent(
            binding,
            audioManager,
            ::sendMediaKeyEvent,
            ::adjustVolume
        )

        sensorManager.registerListener(
            proximitySensorEvent,
            proximitySensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        sensorManager.registerListener(
            accelerometerSensorEvent,
            accelerometerSensor,
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    private fun sendMediaKeyEvent(keyCode: Int) {
        val keyEventDown = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
        val keyEventUp = KeyEvent(KeyEvent.ACTION_UP, keyCode)
        audioManager.dispatchMediaKeyEvent(keyEventDown)
        audioManager.dispatchMediaKeyEvent(keyEventUp)
    }

    private fun adjustVolume(direction: Int) {
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            direction,
            AudioManager.FLAG_SHOW_UI
        )
    }

    private fun toggleSwitch() {
        val isChecked = binding.switch1.isChecked
        binding.switch1.isChecked = !isChecked

        val keyCode = if (!isChecked) KeyEvent.KEYCODE_MEDIA_PLAY else KeyEvent.KEYCODE_MEDIA_PAUSE
        sendMediaKeyEvent(keyCode)
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(proximitySensorEvent)
        sensorManager.unregisterListener(accelerometerSensorEvent)
    }
}
