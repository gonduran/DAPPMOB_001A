package com.example.alarmavisual.helpers

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class RealVibratorHelper(private val context: Context) : VibratorHelper {

    private fun getVibrator(): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    override fun vibrateError() {
        val vibrator = getVibrator()
        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    override fun vibrateSuccess() {
        val vibrator = getVibrator()
        val pattern = longArrayOf(0, 500, 100, 500)
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
    }
}