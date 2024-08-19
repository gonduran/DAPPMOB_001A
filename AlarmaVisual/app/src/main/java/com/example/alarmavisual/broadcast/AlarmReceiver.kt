package com.example.alarmavisual.broadcast
/*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.alarmavisual.views.AlarmActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // Mostrar un mensaje cuando se activa la alarma
        Toast.makeText(context, "¡Alarma activada!", Toast.LENGTH_SHORT).show()

        // Iniciar la AlarmActivity para mostrar la pantalla de alarma
        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        ContextCompat.startActivity(context, alarmIntent, null)
    }
}*/
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