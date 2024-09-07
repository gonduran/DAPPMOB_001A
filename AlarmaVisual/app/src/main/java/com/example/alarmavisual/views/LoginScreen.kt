package com.example.alarmavisual.views

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.alarmavisual.R
import com.example.alarmavisual.user.InMemoryUserRepository
import com.example.alarmavisual.user.UserRepository
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(navController: NavHostController, userRepository: UserRepository) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var colorIndex by remember { mutableStateOf(0) }

    val errorColors = listOf(Color.Red, Color.Yellow, Color.Magenta)
    val animatedColor by animateColorAsState(targetValue = errorColors[colorIndex])
    val context = LocalContext.current

    // Función lambda para vibración
    val vibrateDevice: () -> Unit = {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        try {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (e: Exception) {
            Toast.makeText(context, "Error al activar la vibración", Toast.LENGTH_SHORT).show()
        }
    }

    // Función para manejar los efectos de error
    @Composable
    fun handleErrorEffect(vibrationAction: () -> Unit) {
        LaunchedEffect(showError) {
            if (showError) {
                repeat(10) {
                    colorIndex = (colorIndex + 1) % errorColors.size
                    vibrationAction()
                    delay(500)
                }
                showError = false
            }
        }
    }

    // Ejecutar el efecto de error cuando showError es true
    handleErrorEffect(vibrateDevice)

    // Lambda para validar email
    val isEmailValid: (String) -> Boolean = {
        android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches()
    }

    // Definir colores de degradado
    val gradientColors = listOf(Color(0xFFFFFFFF), Color(0xFF77A8AF))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logoreloj),
            contentDescription = "Nutrición Semanal",
            modifier = Modifier.size(180.dp)
        )

        Text(
            text = "Iniciar sesión",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Campo de correo electrónico
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

        // Campo de contraseña
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

        // Botón de inicio de sesión
        Button(
            onClick = {
                try {
                    when {
                        email.isEmpty() || password.isEmpty() -> {
                            errorMessage = "Todos los campos son obligatorios"
                            showError = true
                        }
                        !isEmailValid(email) -> {
                            errorMessage = "Por favor, ingrese un correo válido"
                            showError = true
                        }
                        !userRepository.validateCredentials(email, password) -> {
                            errorMessage = "Correo o contraseña inválidos"
                            showError = true
                        }
                        else -> {
                            val userName = userRepository.getUserName(email)
                            errorMessage = "Inicio de sesión exitoso. ¡Bienvenido $userName!"
                            showError = true
                            navController.navigate("clockScreen")
                        }
                    }
                } catch (e: Exception) {
                    errorMessage = "Error al iniciar sesión."
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
            Text(text = "Iniciar sesión", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
        LoginScreen(navController = navController, userRepository = InMemoryUserRepository())
    }
}