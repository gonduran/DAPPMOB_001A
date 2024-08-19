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
import com.example.alarmavisual.ui.theme.AlarmaVisualTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AlarmScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var colorIndex by remember { mutableStateOf(0) }
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        Color.Yellow,
        Color.Magenta
    )

    // Vibrador del teléfono
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    // Lanzar una vibración y cambiar de color en un bucle
    LaunchedEffect(Unit) {
        scope.launch {
            while (true) {
                // Cambiar color de fondo
                colorIndex = (colorIndex + 1) % colors.size

                // Vibrar el dispositivo
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        500,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )

                // Esperar 500 ms antes de cambiar el color y vibrar de nuevo
                delay(500)
            }
        }
    }

    // Layout con el color de fondo cambiando
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors[colorIndex]),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "¡Alarma activada!",
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AlarmScreenPreview() {
    AlarmaVisualTheme {
        AlarmScreen(navController = NavHostController(LocalContext.current))
    }
}