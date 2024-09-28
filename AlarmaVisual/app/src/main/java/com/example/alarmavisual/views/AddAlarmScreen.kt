package com.example.alarmavisual.views

import android.annotation.SuppressLint
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.alarmavisual.alarm.Alarm
import com.example.alarmavisual.alarm.CustomAlarmManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import java.util.UUID

@SuppressLint("DefaultLocale")
@Composable
fun AddAlarmScreen(navController: NavHostController, alarmManager: CustomAlarmManager) {
    var selectedTime by remember { mutableStateOf("") }
    val selectedDays = remember { mutableStateListOf<String>() }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var colorIndex by remember { mutableStateOf(0) }
    val errorColors = listOf(Color.Red, Color.Yellow, Color.Magenta)
    val animatedColor by animateColorAsState(targetValue = errorColors[colorIndex])
    var isAlarmActivated by remember { mutableStateOf(true) }
    var isAlarmRepeat by remember { mutableStateOf(true) }
    var alarmLabel by remember { mutableStateOf("") }
    val context = LocalContext.current
    // Obtener el usuario autenticado
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid ?: "" // Obtener el UID del usuario autenticado

    // Mostrar error si el usuario no está autenticado
    if (userId.isEmpty()) {
        Toast.makeText(context, "Usuario no autenticado.", Toast.LENGTH_LONG).show()
        return
    }

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

    // Función para mostrar el mensaje con animación de color y vibración
    LaunchedEffect(showError) {
        if (showError) {
            repeat(10) {
                colorIndex = (colorIndex + 1) % errorColors.size
                vibrateDevice()
                delay(500)
            }
            delay(2000) // Esperar 2 segundos antes de volver al listado
            navController.popBackStack() // Volver al listado de alarmas
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

        Text("Agregar Alarma", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar mensaje animado cuando hay cambios
        if (showError) {
            Text(
                text = errorMessage,
                color = animatedColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Campo de titulo alarma
        TextField(
            value = alarmLabel,
            onValueChange = { alarmLabel = it },
            label = { Text("Titulo Alarma", color = MaterialTheme.colorScheme.onSurface) },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
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

        // Switch para repetir la alarma
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = isAlarmRepeat,
                onCheckedChange = { isAlarmRepeat = it }
            )
            Text(if (isAlarmRepeat) "Repetir Alarma" else "No Repetir Alarma", style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botones para guardar y cancelar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
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
                        // Agregar la alarma en Firestore
                        val newAlarm = Alarm(
                            id = UUID.randomUUID().toString(),
                            time = selectedTime,
                            days = selectedDays,
                            active = isAlarmActivated,
                            label = alarmLabel,
                            repeat = isAlarmRepeat
                        )
                        // Guardar la alarma en Firestore asociada al usuario
                        alarmManager.saveAlarmToFirestore(userId, newAlarm, context)
                        errorMessage = "Alarma configurada con éxito."
                        showError = true
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Guardar")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = { navController.popBackStack() }, // Cancelar y volver
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text("Cancelar")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddAlarmScreenPreview() {
    MaterialTheme {
        AddAlarmFake(navController = null, alarmManager = null)
    }
}

@Composable
fun AddAlarmFake(navController: NavHostController?, alarmManager: CustomAlarmManager?) {
    var selectedTime by remember { mutableStateOf("08:30") }
    val selectedDays = remember { mutableStateListOf("Lun", "Mar") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var colorIndex by remember { mutableStateOf(0) }
    val errorColors = listOf(Color.Red, Color.Yellow, Color.Magenta)
    val animatedColor by animateColorAsState(targetValue = errorColors[colorIndex])
    var isAlarmActivated by remember { mutableStateOf(true) }
    var isAlarmRepeat by remember { mutableStateOf(true) }
    var alarmLabel by remember { mutableStateOf("Alarma de ejemplo") }

    // Simulación del TimePicker (sin contexto en Preview)
    val timePickerDialog = {
        selectedTime = "08:30" // Simulación
    }

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

        Text("Agregar Alarma", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar mensaje de error simulado
        if (showError) {
            Text(
                text = errorMessage,
                color = animatedColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Campo de titulo alarma
        TextField(
            value = alarmLabel,
            onValueChange = { alarmLabel = it },
            label = { Text("Titulo Alarma") },
            singleLine = true,
            colors = TextFieldDefaults.colors()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Seleccionar Hora
        Text("Hora seleccionada: $selectedTime", style = MaterialTheme.typography.bodyLarge)
        Button(onClick = { timePickerDialog() }) {
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

        // Switch para repetir la alarma
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = isAlarmRepeat,
                onCheckedChange = { isAlarmRepeat = it }
            )
            Text(if (isAlarmRepeat) "Repetir Alarma" else "No Repetir Alarma", style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botones para guardar y cancelar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Botón para guardar
            Button(onClick = { /* Acción simulada de guardar */ }, modifier = Modifier.weight(1f)) {
                Text("Guardar")
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Botón para cancelar
            Button(onClick = { /* Acción simulada de cancelar */ }, modifier = Modifier.weight(1f)) {
                Text("Cancelar")
            }
        }
    }
}