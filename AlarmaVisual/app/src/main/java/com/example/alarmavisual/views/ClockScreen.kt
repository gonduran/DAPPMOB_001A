package com.example.alarmavisual.views

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.alarmavisual.broadcast.AlarmReceiver
import com.example.alarmavisual.ui.theme.AlarmaVisualTheme
import java.util.*
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri

@Composable
fun ClockScreen(navController: NavHostController) {
    var alarmList by remember { mutableStateOf(mutableListOf<Pair<String, Boolean>>()) }
    var selectedTime by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Verificar y solicitar el permiso si es necesario
    CheckAndRequestExactAlarmPermission(context)

    // Función para programar la alarma
    fun scheduleAlarm(hour: Int, minute: Int) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            if (alarmManager == null) {
                Toast.makeText(context, "Error al obtener el AlarmManager", Toast.LENGTH_SHORT).show()
                return
            }

            // Crear Intent para AlarmReceiver
            val intent = Intent(context, AlarmReceiver::class.java)

            // Configuración del PendingIntent según la versión de Android
            val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getBroadcast(
                    context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
            } else {
                PendingIntent.getBroadcast(
                    context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

            // Configurar la alarma con la hora y minuto seleccionados
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }

            // Si la hora seleccionada es antes de la hora actual, programarla para el día siguiente
            if (calendar.timeInMillis < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            // Configurar el AlarmManager para activar la alarma en la hora seleccionada
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )

            Toast.makeText(context, "Alarma programada para las $hour:$minute", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace() // Esto mostrará el error en Logcat
            Toast.makeText(context, "Error al programar la alarma: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // TimePicker para seleccionar la hora de la alarma
    val timePickerDialog = android.app.TimePickerDialog(
        context,
        { _, hour: Int, minute: Int ->
            selectedTime = String.format("%02d:%02d", hour, minute)
            alarmList.add(Pair(selectedTime, true)) // Guardar la alarma en la lista
            scheduleAlarm(hour, minute) // Programar la alarma con AlarmManager
        }, 12, 0, true
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Cuadro con bordes para la fecha y la hora
        Box(
            modifier = Modifier
                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Text(
                text = if (selectedTime.isEmpty()) "Seleccione una hora" else "Alarma configurada para las $selectedTime",
                style = MaterialTheme.typography.headlineSmall,
                fontSize = 20.sp
            )
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Botón para abrir el TimePicker
        Button(
            onClick = { timePickerDialog.show() },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Seleccionar Hora", style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Mostrar alarmas configuradas
        if (alarmList.isNotEmpty()) {
            Text(
                text = "Alarmas configuradas:",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))

            alarmList.forEach { alarm ->
                Text(
                    text = "Alarma: ${alarm.first}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ClockPreview() {
    AlarmaVisualTheme {
        ClockScreen(navController = rememberNavController())
    }
}

@Composable
fun CheckAndRequestExactAlarmPermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!alarmManager.canScheduleExactAlarms()) {
            Toast.makeText(context, "Permiso necesario para programar alarmas exactas", Toast.LENGTH_LONG).show()

            try {
                // Intent para abrir la configuración de permisos de exact alarms
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    // Esto redirige a los permisos de la app si la acción está disponible
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                // Si falló al intentar redirigir al usuario
                e.printStackTrace()
                Toast.makeText(context, "Error al abrir la configuración", Toast.LENGTH_LONG).show()
            }
        }
    }
}