package com.example.alarmavisual.views

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.alarmavisual.alarm.CustomAlarmManager
import kotlinx.coroutines.delay

@Composable
fun AddAlarmScreen(navController: NavHostController, alarmManager: CustomAlarmManager) {
    var selectedTime by remember { mutableStateOf("") }
    val selectedDays = remember { mutableStateListOf<String>() }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var colorIndex by remember { mutableStateOf(0) }
    val errorColors = listOf(Color.Red, Color.Yellow, Color.Magenta)
    val animatedColor by animateColorAsState(targetValue = errorColors[colorIndex])
    var isAlarmActivated by remember { mutableStateOf(true) } // Variable para almacenar si la alarma está activada o no

    val context = LocalContext.current

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

    // Mostrar mensaje de error con animación
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

    // TimePicker para seleccionar la hora de la alarma
    val timePickerDialog = android.app.TimePickerDialog(
        context,
        { _, hour: Int, minute: Int ->
            selectedTime = String.format("%02d:%02d", hour, minute)
        }, 12, 0, true
    )

    val gradientColors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFF77A8AF)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text("Configurar Alarma", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Seleccionar Hora
        Text("Hora seleccionada: $selectedTime", style = MaterialTheme.typography.bodyLarge)
        Button(onClick = { timePickerDialog.show() }) {
            Text("Seleccionar Hora")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Seleccionar Días de la semana en dos filas
        Text("Seleccionar días", style = MaterialTheme.typography.bodyLarge)
        val daysOfWeekRow1 = listOf("Lun", "Mar", "Mié", "Jue")
        val daysOfWeekRow2 = listOf("Vie", "Sáb", "Dom")

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                daysOfWeekRow1.forEach { day ->
                    FilterChip(
                        selected = selectedDays.contains(day),
                        onClick = {
                            if (selectedDays.contains(day)) {
                                selectedDays.remove(day)
                            } else {
                                selectedDays.add(day)
                            }
                        },
                        label = { Text(day) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                daysOfWeekRow2.forEach { day ->
                    FilterChip(
                        selected = selectedDays.contains(day),
                        onClick = {
                            if (selectedDays.contains(day)) {
                                selectedDays.remove(day)
                            } else {
                                selectedDays.add(day)
                            }
                        },
                        label = { Text(day) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Switch para activar o desactivar la alarma
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = isAlarmActivated,
                onCheckedChange = { isAlarmActivated = it }
            )
            Text(if (isAlarmActivated) "Alarma Activada" else "Alarma Desactivada", style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para guardar la alarma con validaciones
        Button(
            onClick = {
                if (selectedTime.isEmpty()) {
                    errorMessage = "Por favor, seleccione una hora."
                    showError = true
                } else if (selectedDays.isEmpty()) {
                    errorMessage = "Por favor, seleccione al menos un día."
                    showError = true
                } else {
                    // Agregar la alarma al CustomAlarmManager
                    alarmManager.addAlarm(selectedTime, selectedDays, isAlarmActivated)
                    Toast.makeText(context, "Alarma configurada con éxito.", Toast.LENGTH_SHORT).show()
                    navController.popBackStack() // Volver atrás después de guardar
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Guardar Alarma")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showError) {
            Text(
                text = errorMessage,
                color = animatedColor,
                fontSize = 24.sp,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddAlarmScreenPreview() {
    val navController = rememberNavController()
    val alarmManager = CustomAlarmManager(context = LocalContext.current)
    AddAlarmScreen(navController = navController, alarmManager = alarmManager)
}