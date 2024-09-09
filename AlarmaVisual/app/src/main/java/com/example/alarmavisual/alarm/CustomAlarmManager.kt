package com.example.alarmavisual.alarm

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import java.util.UUID
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

// Clase de datos que representa una alarma
data class Alarm(
    val id: String,
    val time: String,
    val days: List<String>,
    var isActive: Boolean
)

open class CustomAlarmManager(val context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("alarms_prefs", Context.MODE_PRIVATE)

    private val alarms = mutableListOf<Alarm>()

    init {
        // Cargar alarmas desde SharedPreferences
        loadAlarmsFromPrefs()
    }

    // Agregar una alarma a la lista
    fun addAlarm(time: String, days: List<String>, isActive: Boolean, id: String = UUID.randomUUID().toString()): String {
        val alarm = Alarm(id, time, days, isActive)
        alarms.add(alarm)
        saveAlarmsToPrefs()
        return id
    }

    // Obtener todas las alarmas
    open fun getAlarms(): List<Alarm> {
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
            val updatedAlarm = alarms[alarmIndex].copy(time = time, days = days, isActive = isActive)
            alarms[alarmIndex] = updatedAlarm
            saveAlarmsToPrefs()
        }
    }

    // Eliminar una alarma
    open fun removeAlarm(alarm: Alarm) {
        alarms.remove(alarm)
        saveAlarmsToPrefs()
    }

    // Guardar las alarmas en SharedPreferences
    private fun saveAlarmsToPrefs() {
        val editor = sharedPreferences.edit()
        val alarmArray = JSONArray()
        for (alarm in alarms) {
            val alarmObj = JSONObject()
            alarmObj.put("id", alarm.id)
            alarmObj.put("time", alarm.time)
            alarmObj.put("days", JSONArray(alarm.days))
            alarmObj.put("isActive", alarm.isActive)
            alarmArray.put(alarmObj)
        }
        editor.putString("alarms_list", alarmArray.toString())
        editor.apply()
    }

    // Cargar las alarmas desde SharedPreferences
    private fun loadAlarmsFromPrefs() {
        val alarmsString = sharedPreferences.getString("alarms_list", null)
        if (alarmsString != null) {
            val alarmArray = JSONArray(alarmsString)
            for (i in 0 until alarmArray.length()) {
                val alarmObj = alarmArray.getJSONObject(i)
                val id = alarmObj.getString("id")
                val time = alarmObj.getString("time")
                val daysArray = alarmObj.getJSONArray("days")
                val days = mutableListOf<String>()
                for (j in 0 until daysArray.length()) {
                    days.add(daysArray.getString(j))
                }
                val isActive = alarmObj.getBoolean("isActive")
                val alarm = Alarm(id, time, days, isActive)
                alarms.add(alarm)
            }
        }
    }
}