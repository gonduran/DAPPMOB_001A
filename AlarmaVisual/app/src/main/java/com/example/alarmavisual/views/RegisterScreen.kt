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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.alarmavisual.registeredUsers
import kotlinx.coroutines.delay

@Composable
fun RegisterScreen(navController: NavHostController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var colorIndex by remember { mutableStateOf(0) }

    // Lista de colores para el parpadeo del mensaje de error
    val errorColors = listOf(Color.Red, Color.Yellow, Color.Magenta)
    val animatedColor by animateColorAsState(targetValue = errorColors[colorIndex])

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

    // Función para efecto de vibración, cambio de colores y control del mensaje de error
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
            text = "Registrarse",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Campo Nombre
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre", color = MaterialTheme.colorScheme.onSurface) },
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

        // Campo Correo Electrónico
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

        // Campo Contraseña
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
        Spacer(modifier = Modifier.height(8.dp))

        // Campo Confirmar Contraseña
        TextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirmar contraseña", color = MaterialTheme.colorScheme.onSurface) },
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

        // Mostrar mensaje de error si los datos son incorrectos
        if (showError) {
            Text(
                text = errorMessage,
                color = animatedColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Botón para registrar el usuario
        Button(
            onClick = {
                when {
                    name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                        errorMessage = "Todos los campos son obligatorios"
                        showError = true
                    }
                    password != confirmPassword -> {
                        errorMessage = "Las contraseñas no coinciden"
                        showError = true
                    }
                    else -> {
                        // Guardar el usuario si todo es correcto
                        registeredUsers.add(Pair(email, password))
                        navController.navigate("login")
                    }
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
                text = "Registrarse",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterPreview() {
    val navController = rememberNavController()
    MaterialTheme {
        RegisterScreen(navController = navController)
    }
}