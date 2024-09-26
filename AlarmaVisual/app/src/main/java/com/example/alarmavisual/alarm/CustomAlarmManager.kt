package com.example.alarmavisual.alarm

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Clase de datos que representa una alarma
data class Alarm(
    val id: String = "",  // Valor predeterminado
    val time: String = "",  // Valor predeterminado
    val days: List<String> = emptyList(),  // Valor predeterminado
    val active: Boolean = false,  // Valor predeterminado
    val label: String = "",  // Valor predeterminado
    val repeat: Boolean = false  // Valor predeterminado
)

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
                Toast.makeText(context, "Alarma guardada con éxito", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(context, "Alarma actualizada con éxito", Toast.LENGTH_SHORT).show()
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
}