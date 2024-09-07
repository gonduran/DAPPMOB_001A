package com.example.alarmavisual.views

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.alarmavisual.R
import com.example.alarmavisual.broadcast.AlarmReceiver
import com.example.alarmavisual.ui.theme.AlarmaVisualTheme
import java.util.*
import com.example.alarmavisual.alarm.CustomAlarmManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay

@Composable
fun AlarmListScreen(navController: NavHostController, alarmManager: CustomAlarmManager) {
    val gradientColors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFF77A8AF)
    )

    val context = LocalContext.current
    var alarmList by remember { mutableStateOf(alarmManager.getAlarms()) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var colorIndex by remember { mutableStateOf(0) }

    val errorColors = listOf(Color.Red, Color.Yellow, Color.Magenta)
    val animatedColor by animateColorAsState(targetValue = errorColors[colorIndex])

    // Función para activar la vibración
    fun vibrateDevice() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    // Efecto para manejar el parpadeo y la vibración cuando se muestra el mensaje de error
    LaunchedEffect(showError) {
        if (showError) {
            repeat(10) {
                colorIndex = (colorIndex + 1) % errorColors.size
                vibrateDevice()
                delay(500)
            }
            showError = false
        }
    }

    // Verificar y solicitar permiso de alarma exacta si es necesario
    CheckAndRequestExactAlarmPermission(context)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text("Alarmas Configuradas", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar alarmas configuradas
        if (alarmList.isNotEmpty()) {
            alarmList.forEach { alarm ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Columna para los detalles de la alarma
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 16.dp)
                    ) {
                        Text("Hora: ${alarm.time}", style = MaterialTheme.typography.bodyLarge)
                        Text("Días: ${alarm.days.joinToString(", ")}", style = MaterialTheme.typography.bodyMedium)
                        Text("Activa: ${if (alarm.isActive) "Sí" else "No"}", style = MaterialTheme.typography.bodyMedium)

                        // Activar o desactivar la alarma según el estado y los días seleccionados
                        scheduleAlarm(context, alarm.time, alarm.isActive, alarm.days)
                    }

                    // Botón para editar con icono
                    IconButton(
                        onClick = {
                            navController.navigate("editAlarmScreen/${alarm.id}")
                        },
                        modifier = Modifier.size(48.dp) // Ajustar tamaño
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.editar), // Reemplaza con el ID del recurso de imagen de edición
                            contentDescription = "Editar Alarma"
                        )
                    }

                    // Botón para eliminar con icono
                    IconButton(
                        onClick = {
                            // Eliminar la alarma y actualizar la lista
                            alarmManager.removeAlarm(alarm)
                            alarmList = alarmManager.getAlarms() // Recargar la lista de alarmas
                            errorMessage = "Alarma eliminada correctamente"
                            showError = true
                        },
                        modifier = Modifier.size(48.dp) // Ajustar tamaño
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.despertador_eliminar), // Reemplaza con el ID del recurso de imagen de eliminar
                            contentDescription = "Eliminar Alarma"
                        )
                    }
                }
            }
        } else {
            Text("No hay alarmas configuradas.")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.navigate("addAlarmScreen") }) {
            Text("Agregar Alarma")
        }

        // Mostrar mensaje de error si las credenciales no son correctas
        if (showError) {
            Text(
                text = errorMessage,
                color = animatedColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

fun CheckAndRequestExactAlarmPermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!alarmManager.canScheduleExactAlarms()) {
            Toast.makeText(context, "Permiso necesario para alarmas exactas", Toast.LENGTH_LONG).show()
            try {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error al abrir la configuración", Toast.LENGTH_LONG).show()
            }
        }
    }
}

fun scheduleAlarm(context: Context, time: String, isActive: Boolean, days: List<String>) {
    val (hour, minute) = time.split(":").map { it.toInt() }
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
    if (alarmManager == null) {
        Toast.makeText(context, "Error al obtener el AlarmManager", Toast.LENGTH_SHORT).show()
        return
    }

    val daysOfWeekMap = mapOf(
        "Lun" to Calendar.MONDAY,
        "Mar" to Calendar.TUESDAY,
        "Mié" to Calendar.WEDNESDAY,
        "Jue" to Calendar.THURSDAY,
        "Vie" to Calendar.FRIDAY,
        "Sáb" to Calendar.SATURDAY,
        "Dom" to Calendar.SUNDAY
    )

    if (isActive) {
        days.forEach { day ->
            val dayOfWeek = daysOfWeekMap[day] ?: return@forEach // Si el día no es válido, continuar con el siguiente
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.DAY_OF_WEEK, dayOfWeek)
            }

            // Si la hora y el día seleccionados ya pasaron, programarlo para la próxima semana
            val currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            if (currentDayOfWeek > dayOfWeek || (currentDayOfWeek == dayOfWeek && System.currentTimeMillis() > calendar.timeInMillis)) {
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
            }

            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntentId = dayOfWeek * 100 + hour * 60 + minute // ID único por cada día y hora
            val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getBroadcast(
                    context, pendingIntentId, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
            } else {
                PendingIntent.getBroadcast(
                    context, pendingIntentId, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                Toast.makeText(context, "Alarma programada para $day a las $time", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error al programar la alarma para $day: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    } else {
        // Cancelar las alarmas si no está activa
        days.forEach { day ->
            val dayOfWeek = daysOfWeekMap[day] ?: return@forEach
            val pendingIntentId = dayOfWeek * 100 + hour * 60 + minute
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, pendingIntentId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
        Toast.makeText(context, "Alarma desactivada", Toast.LENGTH_SHORT).show()
    }
}

@Preview(showBackground = true)
@Composable
fun AlarmListScreenPreview() {
    val navController = rememberNavController()
    val alarmManager = CustomAlarmManager(context = LocalContext.current)

    // Agregamos alarmas de ejemplo para el preview
    alarmManager.addAlarm("07:30", listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sab", "Dom"), true)
    alarmManager.addAlarm("08:45", listOf("Mar", "Jue"), false)

    AlarmListScreen(navController = navController, alarmManager = alarmManager)
}