package com.example.alarmavisual.views

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.alarmavisual.alarm.CustomAlarmManager
import com.example.alarmavisual.ui.theme.AlarmaVisualTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AlarmScreen(navController: NavController, alarmManager: CustomAlarmManager) {
    val context = LocalContext.current
    //val scope = rememberCoroutineScope()
    var colorIndex by remember { mutableStateOf(0) }
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        Color.Yellow,
        Color.Magenta
    )

    // Estado para manejar la activación de la alarma
    var isAlarmActive by remember { mutableStateOf(true) }

    // Vibrador del teléfono
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    // Efecto para cambiar colores y vibrar mientras la alarma esté activa
    LaunchedEffect(isAlarmActive) {
        if (isAlarmActive) {
            while (isAlarmActive) {
                // Cambiar color de fondo
                colorIndex = (colorIndex + 1) % colors.size

                // Vibrar el dispositivo
                try {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(
                            500,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace() // Manejo seguro de posibles errores en la vibración
                }

                // Esperar 500 ms antes de cambiar el color y vibrar de nuevo
                delay(500)
            }
        }
    }

    // Función para cancelar la alarma
    fun cancelAlarm() {
        isAlarmActive = false // Detener la alarma

        try {
            vibrator.cancel() // Detener la vibración
        } catch (e: Exception) {
            e.printStackTrace() // Manejo seguro del cancelamiento de la vibración
        }

        // Navegar de vuelta a ClockScreen
        if (!navController.popBackStack()) {
            navController.navigate("alarmListScreen") // Navegar manualmente si no puede hacer pop
        }
    }

    // Layout con el color de fondo cambiando y un botón para cancelar la alarma
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors[colorIndex]),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "¡Alarma activada!",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para cancelar la alarma
            Button(
                onClick = { cancelAlarm() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(text = "Cancelar Alarma", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlarmScreenPreview() {
    val navController = rememberNavController()
    val alarmManager = CustomAlarmManager(context = LocalContext.current)
    MaterialTheme {
        AlarmScreen(navController = navController, alarmManager = alarmManager)
    }
}