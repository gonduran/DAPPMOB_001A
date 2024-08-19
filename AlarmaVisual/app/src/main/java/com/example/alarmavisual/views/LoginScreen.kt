package com.example.alarmavisual.views

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.alarmavisual.registeredUsers
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var colorIndex by remember { mutableStateOf(0) }

    val errorColors = listOf(Color.Red, Color.Yellow, Color.Magenta)
    val animatedColor by animateColorAsState(targetValue = errorColors[colorIndex])
    val context = LocalContext.current // Obtener el contexto actual para la vibración

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

    // Función para lanzar un efecto para controlar el parpadeo y el tiempo de visibilidad
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Iniciar sesión",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Campo para el correo electrónico
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico", color = MaterialTheme.colorScheme.onSurface) },
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
        Spacer(modifier = Modifier.height(8.dp))

        // Campo para la contraseña
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña", color = MaterialTheme.colorScheme.onSurface) },
            visualTransformation = PasswordVisualTransformation(),
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

        // Botón personalizado para iniciar sesión
        Button(
            onClick = {
                // Buscar al usuario registrado
                val user = registeredUsers.find { it.first == email && it.second == password }
                if (user != null) {
                    // Si las credenciales son correctas, navega a la siguiente pantalla
                    navController.navigate("clockScreen")
                } else {
                    // Si las credenciales son incorrectas, mostrar un mensaje de error
                    errorMessage = "Correo o contraseña incorrectos"
                    showError = true
                }
            },

            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
        ) {

            Text(
                text = "Iniciar sesión",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para recuperar contraseña
        TextButton(onClick = { navController.navigate("recoverPassword") }) {
            Text(
                text = "¿Olvidaste tu contraseña?",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        // Botón para ir a la pantalla de registro
        TextButton(onClick = { navController.navigate("register") }) {
            Text(
                text = "¿No tienes cuenta? Regístrate",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    val navController = rememberNavController()
    MaterialTheme {
        LoginScreen(navController = navController)
    }
}