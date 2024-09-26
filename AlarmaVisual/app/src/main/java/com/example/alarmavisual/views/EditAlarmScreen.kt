package com.example.alarmavisual.views

import android.annotation.SuppressLint
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.alarmavisual.alarm.Alarm
import com.example.alarmavisual.alarm.CustomAlarmManager
import kotlinx.coroutines.delay
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@SuppressLint("DefaultLocale")
@Composable
fun EditAlarmScreen(navController: NavHostController, alarmManager: CustomAlarmManager, alarmId: String) {
    val context = LocalContext.current

    // Obtener el usuario autenticado
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid ?: "" // Obtener el UID del usuario autenticado

    // Verificar que el usuario esté autenticado
    if (userId.isEmpty()) {
        Toast.makeText(context, "Usuario no autenticado.", Toast.LENGTH_LONG).show()
        return
    }

    var selectedTime by remember { mutableStateOf("") }
    val selectedDays = remember { mutableStateListOf<String>() }
    var isActive by remember { mutableStateOf(true) }
    var isAlarmRepeat by remember { mutableStateOf(true) }
    var alarmLabel by remember { mutableStateOf("") }
    var showMessage by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var colorIndex by remember { mutableStateOf(0) }


    // Cargar la alarma real desde Firestore si no es Preview
    // Cargar la alarma actual desde Firestore
    LaunchedEffect(alarmId) {
        alarmManager.loadAlarmFromFirestore(userId, alarmId) { alarm ->
            selectedTime = alarm.time
            selectedDays.clear()
            selectedDays.addAll(alarm.days)
            isActive = alarm.active
            isAlarmRepeat = alarm.repeat
            alarmLabel = alarm.label
        }
    }

    // Lista de colores para el mensaje
    val messageColors = listOf(Color.Red, Color.Yellow, Color.Magenta)
    val animatedColor by animateColorAsState(targetValue = messageColors[colorIndex])

    // Función para activar la vibración
    fun vibrateDevice() {
        val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    // Función para mostrar el mensaje con animación de color y vibración
    LaunchedEffect(showMessage) {
        if (showMessage) {
            repeat(10) {
                colorIndex = (colorIndex + 1) % messageColors.size
                vibrateDevice()
                delay(500)
            }
            delay(2000) // Esperar 2 segundos antes de volver al listado
            navController.popBackStack() // Volver al listado de alarmas
            showMessage = false
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

        Text("Editar Alarma", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar mensaje animado cuando hay cambios
        if (showMessage) {
            Text(
                text = message,
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
                checked = isActive,
                onCheckedChange = { isActive = it }
            )
            Text(if (isActive) "Alarma Activa" else "Alarma Desactivada", style = MaterialTheme.typography.bodyLarge)
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
                        message = "Por favor, seleccione una hora."
                        showMessage = true
                    } else if (selectedDays.isEmpty()) {
                        message = "Por favor, seleccione al menos un día."
                        showMessage = true
                    } else {
                        // Actualizar la alarma en Firestore
                        val updatedAlarm = Alarm(
                            id = alarmId,
                            time = selectedTime,
                            days = selectedDays.toList(),
                            active = isActive,
                            label = alarmLabel,
                            repeat = isAlarmRepeat
                        )
                        // Actualizar la alarma en Firestore asociada al usuario
                        alarmManager.updateAlarmInFirestore(userId, updatedAlarm, context)
                        message = "Alarma actualizada con éxito."
                        showMessage = true
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Actualizar Alarma")
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

@Composable
fun EditAlarmScreen(
    navController: NavHostController?,
    alarmManager: CustomAlarmManager?,
    alarmId: String = ""
) {
    var selectedTime by remember { mutableStateOf("08:30") }
    val selectedDays = remember { mutableStateListOf("Lun", "Mar", "Mié") }
    var isActive by remember { mutableStateOf(true) }
    var isAlarmRepeat by remember { mutableStateOf(true) }
    var alarmLabel by remember { mutableStateOf("Ejemplo de Alarma") }
    var showMessage by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var colorIndex by remember { mutableStateOf(0) }

    val context = LocalContext.current

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

        Text("Editar Alarma", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar mensaje animado cuando hay cambios
        if (showMessage) {
            Text(
                text = message,
                color = Color.Red,
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
                checked = isActive,
                onCheckedChange = { isActive = it }
            )
            Text(if (isActive) "Alarma Activa" else "Alarma Desactivada", style = MaterialTheme.typography.bodyLarge)
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
            // Botón para guardar la alarma
            Button(
                onClick = { /* Acción simulada de actualizar */ },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Actualizar Alarma")
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Botón para cancelar
            Button(
                onClick = { navController?.popBackStack() }, // Acción simulada de cancelar
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
fun EditAlarmScreenPreview() {
    // Se usa null para alarmManager y navController en el preview
    MaterialTheme {
        EditAlarmScreen(navController = null, alarmManager = null)
    }
}
