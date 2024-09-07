package com.example.alarmavisual.alarm

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import java.util.UUID

// Clase de datos que representa una alarma
data class Alarm(
    val id: String,
    val time: String,
    val days: List<String>,
    var isActive: Boolean
)

class CustomAlarmManager(val context: Context) {
    private val alarms = mutableListOf<Alarm>()

    // Agregar una alarma a la lista
    fun addAlarm(time: String, days: List<String>, isActive: Boolean, id: String = UUID.randomUUID().toString()) {
        val alarm = Alarm(id, time, days, isActive)
        alarms.add(alarm)
    }

    // Obtener todas las alarmas
    fun getAlarms(): List<Alarm> {
        return alarms
    }

    // Obtener una alarma por su ID
    fun getAlarmById(id: String): Alarm? {
        return alarms.find { it.id == id }
    }

    // Actualizar una alarma existente
    fun updateAlarm(id: String, time: String, days: List<String>, isActive: Boolean) {
        val alarmIndex = alarms.indexOfFirst { it.id == id }
        if (alarmIndex != -1) {
            // Reemplazar la alarma vieja con una nueva instancia
            val updatedAlarm = alarms[alarmIndex].copy(time = time, days = days, isActive = isActive)
            alarms[alarmIndex] = updatedAlarm
        }
    }

    // Eliminar una alarma
    fun removeAlarm(alarm: Alarm) {
        alarms.remove(alarm)
    }
}