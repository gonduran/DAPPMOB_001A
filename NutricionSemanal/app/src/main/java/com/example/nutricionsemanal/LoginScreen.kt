package com.example.nutricionsemanal

import android.widget.Toast
import androidx.compose.animation.core.Animatable
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
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource

@Composable
fun LoginScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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

        Text(text = "Iniciar sesión", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if ( email.isBlank() || password.isBlank() ) {
                //Al menos un campo está vacío, muestra un mensaje al usuario
                Toast.makeText(
                    context,
                    "Por favor, complete todos los campos.",
                    Toast.LENGTH_LONG
                ).show()
            } else if (!isEmailValid(email)) {
                // Muestra un Toast de alerta
                Toast.makeText(
                    context,
                    "Por favor, ingrese un correo válido.",
                    Toast.LENGTH_LONG
                ).show()
            } else {

                // Buscar al usuario registrado
                val user = registeredUsers.find { it.first == email && it.second == password }
                if (user != null) {
                    // Si las credenciales son correctas, navega a la siguiente pantalla
                    Toast.makeText(
                        context,
                        "Login exitoso.",
                        Toast.LENGTH_LONG
                    ).show()
                    navController.navigate("recetasScreen")
                } else {
                    Toast.makeText(
                        context,
                        "Usuario o contraseña invalidos.",
                        Toast.LENGTH_LONG
                    ).show()

                }
            }

        }) {
            Text(text = "Iniciar sesión")
        }

        TextButton(onClick = { navController.navigate("recoverPassword") }) {
            Text("¿Olvidaste tu contraseña?")
        }

        TextButton(onClick = { navController.navigate("register") }) {
            Text("Registrarse")
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