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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.alarmavisual.R
import kotlinx.coroutines.delay
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RegisterScreen(navController: NavHostController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var colorIndex by remember { mutableStateOf(0) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var isRegistrationSuccessful by remember { mutableStateOf(false) } // Variable para controlar si el registro fue exitoso
    var isPasswordFocused by remember { mutableStateOf(false) } // Para saber si el campo tiene el foco

    val errorColors = listOf(Color.Red, Color.Yellow, Color.Magenta)
    val animatedColor by animateColorAsState(targetValue = errorColors[colorIndex])

    val context = LocalContext.current
    // Firebase Auth instance
    val auth = FirebaseAuth.getInstance()
    // Firebase Firestore instance
    val db = FirebaseFirestore.getInstance()

    // Función para manejar errores
    fun handleError(action: () -> Unit) {
        try {
            action()
        } catch (e: Exception) {
            errorMessage = "Ocurrió un error inesperado: ${e.message}"
            showError = true
        }
    }

    // Función lambda para validar el correo
    val isEmailValid: (String) -> Boolean = {
        android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches()
    }

    // Función para validar cadenas de texto
    val validarCaracteresMaliciosos: (String) -> Boolean = { input ->
        "[<>&\"';*]".toRegex().containsMatchIn(input)
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

    // Función para validar la contraseña
    fun validarPassword(password: String): String? {
        if (password.length < 6) {
            return "La contraseña debe tener al menos 6 caracteres."
        }
        if (!password.any { it.isUpperCase() }) {
            return "La contraseña debe tener al menos una letra mayúscula."
        }
        if (!password.any { it.isDigit() }) {
            return "La contraseña debe tener al menos un dígito."
        }
        if (!password.any { "!@#\$%^&*()-_=+[]{}|;:'\",.<>?/`~".contains(it) }) {
            return "La contraseña debe tener al menos un carácter especial."
        }
        // Validar si hay dígitos consecutivos en secuencia (ascendente o descendente) o repetidos
        for (i in 0 until password.length - 2) {
            val char1 = password[i]
            val char2 = password[i + 1]
            val char3 = password[i + 2]

            // Verificar si son dígitos consecutivos iguales (como "111", "222")
            if (char1.isDigit() && char1 == char2 && char2 == char3) {
                return "No puede haber más de 2 dígitos consecutivos iguales."
            }

            // Verificar si son dígitos consecutivos ascendentes (como "123", "234")
            if (char1.isDigit() && char2.isDigit() && char3.isDigit()) {
                val num1 = char1.toString().toInt()
                val num2 = char2.toString().toInt()
                val num3 = char3.toString().toInt()

                if (num2 == num1 + 1 && num3 == num2 + 1) {
                    return "No puede haber secuencias de 3 dígitos consecutivos ascendentes."
                }

                if (num2 == num1 - 1 && num3 == num2 - 1) {
                    return "No puede haber secuencias de 3 dígitos consecutivos descendentes."
                }
            }
        }
        // Si todo está bien, retorna null (sin error)
        return null
    }

    // Función para manejar la animación de error
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

    // Delay y navegación después de un registro exitoso
    LaunchedEffect(isRegistrationSuccessful) {
        if (isRegistrationSuccessful) {
            delay(2000)
            navController.navigate("login")
        }
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
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logoreloj),
            contentDescription = "Logo Alarma Visual",
            modifier = Modifier.size(180.dp)
        )

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
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
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
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Campo Contraseña
        TextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = validarPassword(it)
            },
            label = { Text("Contraseña", color = MaterialTheme.colorScheme.onSurface) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.onFocusChanged { focusState ->
                isPasswordFocused = focusState.isFocused
            }
        )
        // Mostrar el texto de ayuda solo cuando el campo tiene el foco
        if (isPasswordFocused) {
            Text(
                text = "Debe tener al menos 6 caracteres, al menos una letra mayúscula, un dígito, un carácter especial y sin secuencias consecutivas de dígitos.",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Campo Confirmar Contraseña
        TextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                confirmPasswordError = validarPassword(it)
            },
            label = { Text("Confirmar contraseña", color = MaterialTheme.colorScheme.onSurface) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

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

        // Botón para registrar el usuario
        Button(
            onClick = {
                handleError {
                    when {
                        name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                            errorMessage = "Todos los campos son obligatorios."
                            showError = true
                        }
                        !isEmailValid(email) -> {
                            errorMessage = "Por favor, ingrese un correo válido."
                            showError = true
                        }
                        validarCaracteresMaliciosos(name) || validarCaracteresMaliciosos(email) ||
                                validarCaracteresMaliciosos(password) || validarCaracteresMaliciosos(confirmPassword) -> {
                            errorMessage = "Alerta: Se detectaron caracteres potencialmente maliciosos."
                            showError = true
                        }
                        passwordError != null -> {
                            errorMessage = passwordError.toString()
                            showError = true
                        }
                        confirmPasswordError != null -> {
                            errorMessage = confirmPasswordError.toString()
                            showError = true
                        }
                        password != confirmPassword -> {
                            errorMessage = "Las contraseñas no coinciden."
                            showError = true
                        }
                        else -> {
                            // Registro en Firebase Authentication
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        // Registro exitoso
                                        val user = auth.currentUser
                                        // Guardar el nombre y correo en Firestore
                                        val userMap = hashMapOf(
                                            "name" to name,
                                            "email" to email
                                        )
                                        db.collection("users").document(user?.uid ?: "")
                                            .set(userMap)
                                            .addOnSuccessListener {
                                                // Enviar correo de verificación
                                                user?.sendEmailVerification()
                                                    ?.addOnCompleteListener { emailTask ->
                                                        if (emailTask.isSuccessful) {
                                                            errorMessage = "Registro exitoso. Por favor, verifica tu correo."
                                                            showError = true
                                                            isRegistrationSuccessful = true // Activar la navegación después del registro exitoso
                                                        } else {
                                                            errorMessage = "Error al enviar el correo de verificación: ${emailTask.exception?.message}"
                                                            showError = true
                                                        }
                                                    }
                                            }
                                            .addOnFailureListener { e ->
                                                errorMessage = "Error al guardar el usuario en Firestore: $e"
                                                showError = true
                                            }
                                    } else {
                                        if (task.exception is FirebaseAuthUserCollisionException) {
                                            errorMessage = "El correo ya está registrado."
                                            showError = true
                                        } else {
                                            errorMessage = "Error en el registro: ${task.exception?.message}. Inténtalo nuevamente."
                                            showError = true
                                        }
                                    }
                                }
                        }
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

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate("login") }) {
            Text("Volver")
        }
    }
}

@Composable
fun RegisterFake(navController: NavHostController?) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var colorIndex by remember { mutableStateOf(0) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var isRegistrationSuccessful by remember { mutableStateOf(false) }
    var isPasswordFocused by remember { mutableStateOf(false) }

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

    // Función para manejar la animación de error
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

    // Delay y navegación después de un registro exitoso
    LaunchedEffect(isRegistrationSuccessful) {
        if (isRegistrationSuccessful) {
            delay(2000)
            navController?.navigate("login")
        }
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
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logoreloj),
            contentDescription = "Logo Alarma Visual",
            modifier = Modifier.size(180.dp)
        )

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
            label = { Text("Nombre") },
            singleLine = true,
            colors = TextFieldDefaults.colors()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Campo Correo Electrónico
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            singleLine = true,
            colors = TextFieldDefaults.colors()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Campo Contraseña
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            colors = TextFieldDefaults.colors(),
            modifier = Modifier.onFocusChanged { focusState ->
                isPasswordFocused = focusState.isFocused
            }
        )
        if (isPasswordFocused) {
            Text(
                text = "Debe tener al menos 6 caracteres, una letra mayúscula, un dígito, y un carácter especial.",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Campo Confirmar Contraseña
        TextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirmar contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            colors = TextFieldDefaults.colors()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar mensajes de error
        if (showError) {
            Text(
                text = errorMessage,
                color = animatedColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Botón para registrar el usuario
        Button(
            onClick = { /* Acción simulada para registro */ },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = "Registrarse",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController?.navigate("login") }) {
            Text("Volver")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterPreview() {
    // Usamos null para navController y userRepository en el preview
    MaterialTheme {
        RegisterFake(navController = null)
    }
}