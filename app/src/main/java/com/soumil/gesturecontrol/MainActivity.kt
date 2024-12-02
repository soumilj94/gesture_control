package com.soumil.gesturecontrol

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Spinner
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

        val filter = IntentFilter("com.soumil.gesturecontrol.TILE_STATE_CHANGED")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(tileStateReceiver, filter, RECEIVER_EXPORTED)
        }

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

        binding.sensorSensitivity.apply {
            max = 2
            progress = 1

            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val sideSensitivity: Int
                    val upDownSensitivity: Int

                    when(progress){
                        0 -> {
                            sideSensitivity = 4
                            upDownSensitivity = 2
                        }
                        1 -> {
                            sideSensitivity = 5
                            upDownSensitivity = 4
                        }
                        2 -> {
                            sideSensitivity = 5
                            upDownSensitivity = 6
                        }
                        else -> {
                            sideSensitivity = 5
                            upDownSensitivity = 4
                        }
                    }
                    accelerometerSensorEvent.updateSensitivity(sideSensitivity, upDownSensitivity)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })
        }

        val htrSpinner = findViewById<Spinner>(R.id.htrSpin)
        val adapter = ArrayAdapter.createFromResource(this, R.array.spinner_items, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        htrSpinner.adapter = adapter

        htrSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long){
                val selectedItem = parent.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private val tileStateReceiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.soumil.gesturecontrol.TILE_STATE_CHANGED"){
                val isActive = intent.getBooleanExtra("isActive", false)
                if (isActive){
                    registerAccelerometerSensor()
                    registerProximitySensor()
                }
                else{
                    unregisterAccelerometerSensor()
                    unregisterProximitySensor()
                }
            }
        }
    }

    private fun registerAccelerometerSensor() {
        sensorManager.registerListener(
            accelerometerSensorEvent,
            accelerometerSensor,
            SensorManager.SENSOR_DELAY_GAME,
        )
    }

    private fun registerProximitySensor(){
        sensorManager.registerListener(
            proximitySensorEvent,
            proximitySensor,
            SensorManager.SENSOR_DELAY_GAME,
        )
    }

    private fun unregisterAccelerometerSensor() {
        sensorManager.unregisterListener(accelerometerSensorEvent)
    }

    private fun unregisterProximitySensor(){
        sensorManager.unregisterListener(proximitySensorEvent)
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

        unregisterReceiver(tileStateReceiver)
        unregisterAccelerometerSensor()
        unregisterProximitySensor()
    }

    fun getSensorListener(sensor: Sensor): SensorEventListener? {
        return when (sensor.type) {
            Sensor.TYPE_PROXIMITY -> {
                proximitySensorEvent // Return the registered listener for proximity sensor
            }
            Sensor.TYPE_ACCELEROMETER -> {
                accelerometerSensorEvent // Return the registered listener for accelerometer
            }
            else -> {
                null
            }
        }
    }
}
