package com.example.alarmavisual.views

import android.app.TimePickerDialog
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.alarmavisual.ui.theme.AlarmaVisualTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ClockScreen(navController: NavHostController) {
    // Estado para almacenar la lista de alarmas
    var alarmList by remember { mutableStateOf(mutableListOf<Pair<String, Boolean>>()) }
    var selectedTime by remember { mutableStateOf("") }

    // Contexto local
    val context = LocalContext.current

    // Reloj para mostrar la hora actual
    val currentTime = remember { mutableStateOf("") }
    val currentDate = remember { mutableStateOf("") }
    val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    // Actualizar la fecha y hora cada segundo
    LaunchedEffect(Unit) {
        while (true) {
            currentDate.value = dateFormat.format(Date())
            currentTime.value = timeFormat.format(Date())
            kotlinx.coroutines.delay(1000L)
        }
    }

    // Time picker dialog para seleccionar una hora de alarma
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour: Int, minute: Int ->
            selectedTime = String.format("%02d:%02d", hour, minute)
            alarmList.add(Pair(selectedTime, false)) // Guardar la alarma en la lista con opción de activación
        }, 12, 0, true
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Espaciado superior
        Spacer(modifier = Modifier.height(32.dp))

        // Cuadro con bordes que contiene la fecha y la hora en dos filas
        Box(
            modifier = Modifier
                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                .padding(16.dp) // Espaciado interno del cuadro
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = currentDate.value,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = currentTime.value,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 24.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Texto de selección de alarma
        Text(
            text = "Configurar Alarma",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar la hora seleccionada
        Text(
            text = if (selectedTime.isEmpty()) "Hora seleccionada: ---" else "Hora seleccionada: $selectedTime",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Botón para abrir el TimePicker
        Button(
            onClick = { timePickerDialog.show() },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Seleccionar Hora",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Mostrar lista de alarmas configuradas
        if (alarmList.isNotEmpty()) {
            Text(
                text = "Alarmas configuradas:",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Mostrar cada alarma configurada con opción de activación/desactivación
            alarmList.forEachIndexed { index, alarm ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Alarma: ${alarm.first}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 18.sp
                    )
                    Switch(
                        checked = alarm.second,
                        onCheckedChange = { isChecked ->
                            alarmList[index] = alarm.copy(second = isChecked) // Activar o desactivar la alarma
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Botón para activar la alarma, al final y separado
        Spacer(modifier = Modifier.weight(1f)) // Añadir espacio para empujar el botón hacia abajo
        Button(
            onClick = {
                navController.navigate("activateAlarm")
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = "Activar Alarma",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ClockPreview() {
    AlarmaVisualTheme {
        ClockScreen(navController = NavHostController(LocalContext.current))
    }
}