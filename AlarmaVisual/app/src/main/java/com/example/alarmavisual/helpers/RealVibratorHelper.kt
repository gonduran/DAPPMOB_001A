package com.example.alarmavisual.helpers

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator

class RealVibratorHelper(private val context: Context) : VibratorHelper {
    override fun vibrate() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}