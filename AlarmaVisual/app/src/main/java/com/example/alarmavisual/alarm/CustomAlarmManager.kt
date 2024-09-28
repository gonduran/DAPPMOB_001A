package com.example.alarmavisual.alarm

import android.app.AlarmManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.alarmavisual.widget.AlarmWidgetProvider
import com.example.alarmavisual.widget.WidgetUpdateWorker
import java.util.concurrent.TimeUnit

open class CustomAlarmManager(private val context: Context) {

    private val firestore = FirebaseFirestore.getInstance()

    // Función para guardar la alarma en Firestore
    fun saveAlarmToFirestore(userId: String, alarm: Alarm, context: Context) {
        val firestore = FirebaseFirestore.getInstance()
        val alarmRef = firestore.collection("users").document(userId).collection("alarms")

        // Guardar la alarma
        alarmRef.document(alarm.id)
            .set(alarm)
            .addOnSuccessListener {
                //Toast.makeText(context, "Alarma guardada con éxito", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al guardar la alarma: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Función para cargar una alarma desde Firestore
    fun loadAlarmFromFirestore(userId: String, alarmId: String, callback: (Alarm) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val alarmRef = firestore.collection("users").document(userId).collection("alarms").document(alarmId)

        alarmRef.get()
            .addOnSuccessListener { document ->
                val alarm = document.toObject(Alarm::class.java)
                if (alarm != null) {
                    callback(alarm)
                }
            }
            .addOnFailureListener {
                // Manejar el error si es necesario
            }
    }

    // Función para actualizar una alarma en Firestore
    fun updateAlarmInFirestore(userId: String, alarm: Alarm, context: Context) {
        val firestore = FirebaseFirestore.getInstance()
        val alarmRef = firestore.collection("users").document(userId).collection("alarms").document(alarm.id)

        alarmRef.set(alarm)
            .addOnSuccessListener {
                //Toast.makeText(context, "Alarma actualizada con éxito", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al actualizar la alarma: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Eliminar alarma en Firestore
    fun deleteAlarmFromFirestore(alarmId: String, onResult: (Boolean) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val firestore = FirebaseFirestore.getInstance()
        if (currentUser != null) {
            val userId = currentUser.uid
            firestore.collection("users").document(userId).collection("alarms").document(alarmId)
                .delete()
                .addOnSuccessListener {
                    onResult(true) // Indicar que la operación fue exitosa
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    onResult(false) // Indicar que la operación falló
                }
        } else {
            onResult(false) // Usuario no autenticado
        }
    }

    fun getAlarmsFromFirestore(context: Context, callback: (List<Alarm>) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val firestore = FirebaseFirestore.getInstance()
        if (currentUser != null) {
            val userId = currentUser.uid
            firestore.collection("users").document(userId).collection("alarms")
                .get()
                .addOnSuccessListener { result ->
                    val alarms = result.documents.mapNotNull { doc ->
                        doc.toObject(Alarm::class.java)
                    }
                    callback(alarms)
                }
                .addOnFailureListener { e ->
                    e.printStackTrace() // Mostrar el error en la consola
                    Toast.makeText(context, "Error al obtener alarmas: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    fun scheduleWidgetUpdateTask(context: Context) {
        // Crear la solicitud periódica para el WorkManager
        val widgetUpdateRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
            15, TimeUnit.MINUTES // Cada 15 minutos
        ).build()

        // Programar la tarea con WorkManager
        WorkManager.getInstance(context).enqueue(widgetUpdateRequest)
    }

    fun forceWidgetUpdate(context: Context) {
        val intent = Intent(context, AlarmWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }
        val ids = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, AlarmWidgetProvider::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        context.sendBroadcast(intent)
    }
}