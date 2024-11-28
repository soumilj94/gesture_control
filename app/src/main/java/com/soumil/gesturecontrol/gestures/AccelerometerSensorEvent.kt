package com.soumil.gesturecontrol.gestures

import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import com.soumil.gesturecontrol.databinding.ActivityMainBinding

class AccelerometerSensorEvent(
    private val binding: ActivityMainBinding,
    private val audioManager: AudioManager,
    private val sendMediaKeyEvent: (Int) -> Unit,
    private val adjustVolume: (Int) -> Unit
): SensorEventListener {
    private var isMediaActionTrigger = false
    private var isVolumeActionTrigger = false

    @SuppressLint("SetTextI18n")
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER){
            val sides = event.values[0]
            val upDown = event.values[1]
            binding.accelerometer.text = "vertical ${upDown.toInt()}\n horizontal ${sides.toInt()}"

            if (!isMediaActionTrigger){
                when{
                    sides < -5 ->{
                        sendMediaKeyEvent(android.view.KeyEvent.KEYCODE_MEDIA_NEXT)
                        triggerMediaActionWithDelay()
                    }
                    sides > 5 ->{
                        sendMediaKeyEvent(android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS)
                        triggerMediaActionWithDelay()
                    }
                }
            }
            if (!isVolumeActionTrigger){
                when{
                    upDown < -4 ->{
                        adjustVolumeWithDelay(AudioManager.ADJUST_RAISE)
                    }
                    upDown > 4 ->{
                        adjustVolumeWithDelay(AudioManager.ADJUST_LOWER)
                    }
                }
            }
        }
    }

    private fun triggerMediaActionWithDelay() {
        isMediaActionTrigger = true
        Handler(Looper.getMainLooper()).postDelayed({
            isMediaActionTrigger = false
        }, 3000)
    }

    private fun adjustVolumeWithDelay(direction: Int) {
        isVolumeActionTrigger = true
        adjustVolume(direction)
        Handler(Looper.getMainLooper()).postDelayed({
            isVolumeActionTrigger = false
        }, 500)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}