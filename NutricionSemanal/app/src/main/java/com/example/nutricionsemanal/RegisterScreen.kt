package com.example.nutricionsemanal

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.navigation.NavHostController
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.rememberNavController
import com.example.nutricionsemanal.user.InMemoryUserRepository
import com.example.nutricionsemanal.user.UserRepository

@Composable
fun RegisterScreen(navController: NavHostController, userRepository: UserRepository) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) } // Variable para mostrar errores de contraseña
    var confirmPasswordError by remember { mutableStateOf<String?>(null) } // Variable para mostrar errores de contraseña

    val context = LocalContext.current

    // Define tus colores de degradado
    val gradientColors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFCDDC39)
    )

    // Lógica para validar el correo electrónico
    fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun validarCaracteresMaliciosos(input: String): Boolean {
        // Expresión regular que detecta posibles caracteres maliciosos o inyecciones
        val regex = "[<>&\"';*]".toRegex()

        // Si encuentra coincidencias, retorna true (caracteres maliciosos detectados)
        return regex.containsMatchIn(input)
    }

    // Función para validar la contraseña
    fun validarPassword(password: String): String? {
        if (password.length < 6) {
            return "La contraseña debe tener al menos 6 caracteres"
        }
        if (!password.any { it.isUpperCase() }) {
            return "La contraseña debe tener al menos una letra mayúscula"
        }
        if (!password.any { it.isDigit() }) {
            return "La contraseña debe tener al menos un dígito"
        }
        if (!password.any { "!@#\$%^&*()-_=+[]{}|;:'\",.<>?/`~".contains(it) }) {
            return "La contraseña debe tener al menos un carácter especial"
        }
        // Validar si hay dígitos consecutivos en secuencia (ascendente o descendente) o repetidos
        for (i in 0 until password.length - 2) {
            val char1 = password[i]
            val char2 = password[i + 1]
            val char3 = password[i + 2]

            // Verificar si son dígitos consecutivos iguales (como "111", "222")
            if (char1.isDigit() && char1 == char2 && char2 == char3) {
                return "No puede haber más de 2 dígitos consecutivos iguales"
            }

            // Verificar si son dígitos consecutivos ascendentes (como "123", "234")
            if (char1.isDigit() && char2.isDigit() && char3.isDigit()) {
                val num1 = char1.toString().toInt()
                val num2 = char2.toString().toInt()
                val num3 = char3.toString().toInt()

                if (num2 == num1 + 1 && num3 == num2 + 1) {
                    return "No puede haber secuencias de 3 dígitos consecutivos ascendentes"
                }

                if (num2 == num1 - 1 && num3 == num2 - 1) {
                    return "No puede haber secuencias de 3 dígitos consecutivos descendentes"
                }
            }
        }
        // Si todo está bien, retorna null (sin error)
        return null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp)
            .background(Brush.verticalGradient(gradientColors)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(id = R.drawable.nutricion_sf),
            contentDescription = "Nutrición Semanal",
            modifier = Modifier
                .size(280.dp)
        )

        Text(text = "Registrarse", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = {
                password = it
                // Validar la contraseña cada vez que cambia
                passwordError = validarPassword(it)
            },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        // Mostrar mensaje de error si la contraseña no es válida
        if (passwordError != null) {
            Text(
                text = passwordError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                // Validar la contraseña cada vez que cambia
                confirmPasswordError = validarPassword(it)
            },
            label = { Text("Confirmar contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        // Mostrar mensaje de error si la contraseña no es válida
        if (confirmPasswordError != null) {
            Text(
                text = confirmPasswordError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            /* Lógica de registro */
            if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                //Al menos un campo está vacío, muestra un mensaje al usuario
                Toast.makeText(
                    context,
                    "Por favor, complete todos los campos.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                // Verifica si algunod e los campos contiene caracteres maliciosos
                if (validarCaracteresMaliciosos(name) ||
                    validarCaracteresMaliciosos(email) ||
                    validarCaracteresMaliciosos(password) ||
                    validarCaracteresMaliciosos(confirmPassword)
                ) {
                    // Muestra un Toast de alerta
                    Toast.makeText(
                        context,
                        "Alerta: Se detectaron caracteres potencialmente maliciosos.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (!isEmailValid(email)) {
                    // Muestra un Toast de alerta
                    Toast.makeText(
                        context,
                        "Por favor, ingrese un correo válido.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (password != confirmPassword) {
                    // Contraseñas no son iguales
                    Toast.makeText(
                        context,
                        "Contraseñas deben ser iguales",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (passwordError != null) {
                    Toast.makeText(
                        context,
                        passwordError,
                        Toast.LENGTH_LONG
                    ).show()
                } else if (confirmPasswordError != null) {
                    Toast.makeText(
                        context,
                        confirmPasswordError,
                        Toast.LENGTH_LONG
                    ).show()
                } else {

                    when {
                        userRepository.isUserRegistered(email) -> {
                            Toast.makeText(
                                context,
                                "El correo ya está registrado.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else -> {
                            if (userRepository.registerUser(name, email, password)) {
                                Toast.makeText(
                                    context,
                                    "Registro exitoso.",
                                    Toast.LENGTH_LONG
                                ).show()
                                navController.navigate("login")
                            } else {
                                Toast.makeText(
                                    context,
                                    "Error en el registro. Inténtalo nuevamente.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                    /*Toast.makeText(
                        context,
                        "Registro exitoso.",
                        Toast.LENGTH_LONG
                    ).show()
                    // Guardar el usuario si todo es correcto
                    registeredUsers.add(Pair(email, password))
                    navController.navigate("login")*/
                }
            }
        }) {
            Text(text = "Registrarse")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate("login") }) {
            Text("Volver")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterPreview() {
    val navController = rememberNavController()
    MaterialTheme {
        RegisterScreen(navController = navController, userRepository = InMemoryUserRepository())
    }
}