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
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.alarmavisual.R
import kotlinx.coroutines.delay
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RecoverPasswordScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var colorIndex by remember { mutableStateOf(0) }
    var isRegistrationSuccessful by remember { mutableStateOf(false) } // Variable para controlar si el registro fue exitoso

    // Colores de error para el parpadeo
    val errorColors = listOf(Color.Red, Color.Yellow, Color.Magenta)
    val animatedColor by animateColorAsState(targetValue = errorColors[colorIndex])

    val context = LocalContext.current
    // Firebase Auth instance
    val auth = FirebaseAuth.getInstance()
    // Firebase Firestore instance
    val db = FirebaseFirestore.getInstance()

    // Función lambda para la vibración
    val vibrateDevice: () -> Unit = {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (e: Exception) {
            Toast.makeText(context, "Error al activar la vibración", Toast.LENGTH_SHORT).show()
        }
    }

    // Función para mostrar el error y aplicar vibración
    @Composable
    fun handleError(effect: () -> Unit) {
        LaunchedEffect(showError) {
            if (showError) {
                repeat(10) {
                    colorIndex = (colorIndex + 1) % errorColors.size
                    effect() // Llamar al efecto (vibración) pasado como parámetro
                    delay(500)
                }
                showError = false
            }
        }
    }

    // Delay y navegación después de un registro exitoso
    LaunchedEffect(isRegistrationSuccessful) {
        if (isRegistrationSuccessful) {
            delay(2000)
            navController.navigate("login")
        }
    }

    // Llamada a la función de orden superior pasando la función lambda de vibración
    handleError(vibrateDevice)

    // Lambda para validar el correo electrónico
    val isEmailValid: (String) -> Boolean = { email ->
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Lambda para manejar el click del botón
    val handleRecoveryClick: () -> Unit = {
        try {
            when {
                email.isEmpty() -> {
                    errorMessage = "El correo electrónico es obligatorio"
                    showError = true
                }
                !isEmailValid(email) -> {
                    errorMessage = "Por favor, ingrese un correo válido"
                    showError = true
                }
                else -> {
                    // Verificar en Firestore si el correo está registrado
                    db.collection("users")
                        .whereEqualTo("email", email)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (!documents.isEmpty) {
                                // El correo existe en Firestore, enviar el correo de recuperación
                                auth.sendPasswordResetEmail(email)
                                    .addOnCompleteListener { resetTask ->
                                        if (resetTask.isSuccessful) {
                                            errorMessage = "Correo de recuperación enviado"
                                            showError = true
                                            isRegistrationSuccessful = true // Activar la navegación después del registro exitoso
                                        } else {
                                            errorMessage = "Error: ${resetTask.exception?.message}"
                                            showError = true
                                        }
                                    }
                            } else {
                                // El correo no está registrado en Firestore
                                errorMessage = "El correo no está registrado."
                                showError = true                            }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(context, "Error al verificar el correo: $exception", Toast.LENGTH_LONG).show()
                        }
                }
            }
        } catch (e: Exception) {
            errorMessage = "Error en el envío de correo de recuperación"
            showError = true
        }
    }

    // Define los colores de degradado
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
            contentDescription = "Logo",
            modifier = Modifier.size(180.dp)
        )

        Text(
            text = "Recuperar Contraseña",
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
        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar mensaje de error si hay problemas con el correo electrónico
        if (showError) {
            Text(
                text = errorMessage,
                color = animatedColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Botón para enviar instrucciones
        Button(
            onClick = handleRecoveryClick, // Usar la lambda definida
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = "Enviar instrucciones",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate("login") }) {
            Text("Volver")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecoverPasswordScreenPreview() {
    val navController = rememberNavController()
    MaterialTheme {
        RecoverPasswordFake(navController = navController)
    }
}

@Composable
fun RecoverPasswordFake(navController: NavHostController?) {
    var email by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var colorIndex by remember { mutableStateOf(0) }
    var isRegistrationSuccessful by remember { mutableStateOf(false) }

    val errorColors = listOf(Color.Red, Color.Yellow, Color.Magenta)
    val animatedColor by animateColorAsState(targetValue = errorColors[colorIndex])

    // Función simulada para la vibración en lugar de depender del contexto
    val vibrateDevice: () -> Unit = {
        // Simulación de vibración
    }

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

    LaunchedEffect(isRegistrationSuccessful) {
        if (isRegistrationSuccessful) {
            delay(2000)
            // Simular navegación
        }
    }

    val isEmailValid: (String) -> Boolean = {
        android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches()
    }

    val handleRecoveryClick: () -> Unit = {
        when {
            email.isEmpty() -> {
                errorMessage = "El correo electrónico es obligatorio"
                showError = true
            }
            !isEmailValid(email) -> {
                errorMessage = "Por favor, ingrese un correo válido"
                showError = true
            }
            else -> {
                errorMessage = "Correo de recuperación enviado"
                showError = true
                isRegistrationSuccessful = true
            }
        }
    }

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
            contentDescription = "Logo",
            modifier = Modifier.size(180.dp)
        )

        Text(
            text = "Recuperar Contraseña",
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
        Spacer(modifier = Modifier.height(16.dp))

        if (showError) {
            Text(
                text = errorMessage,
                color = animatedColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = handleRecoveryClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = "Enviar instrucciones",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { /* Simular navegación */ }) {
            Text("Volver")
        }
    }
}