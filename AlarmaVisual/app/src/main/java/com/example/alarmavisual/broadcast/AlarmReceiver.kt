package com.example.alarmavisual.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.alarmavisual.MainActivity

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Crear un intent para iniciar MainActivity y navegar automáticamente a AlarmScreen
        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "alarmScreen") // Indicar que se debe mostrar la pantalla de alarma
        }

        // Iniciar MainActivity automáticamente
        context.startActivity(activityIntent)
    }
}