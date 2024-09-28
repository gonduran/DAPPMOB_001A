package com.example.alarmavisual.broadcast

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class AlarmForegroundService : Service() {

    override fun onCreate() {
        super.onCreate()

        // Crear el canal de notificación para versiones de Android Oreo o superiores
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "alarm_channel"
            val channelName = "Alarm Channel"
            val notificationChannel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        // Crear y mostrar la notificación
        val notification: Notification = NotificationCompat.Builder(this, "alarm_channel")
            .setContentTitle("Alarma activada")
            .setContentText("Tu alarma está sonando.")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // Iniciar el servicio en primer plano con la notificación
        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Manejar la lógica de la alarma (vibración, reproducción de sonido, etc.)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}