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
import androidx.navigation.NavHostController
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.rememberNavController

@Composable
fun RecoverPasswordScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }

    // Define tus colores de degradado
    val gradientColors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFCDDC39)
    )

    val context = LocalContext.current

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

        Text(text = "Recuperar Contraseña", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            /* Lógica de recuperación de contraseña */
            if (email.isEmpty()) {
                // Muestra un Toast de alerta
                Toast.makeText(
                    context,
                    "El correo electrónico es obligatorio.",
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
                // Muestra un Toast de alerta
                Toast.makeText(
                    context,
                    "Enviado correo con instrucciones.",
                    Toast.LENGTH_LONG
                ).show()
                // Falta agregar Lógica para enviar instrucciones de recuperación de contraseña
                navController.navigate("login")
            }
        }) {
            Text(text = "Enviar instrucciones")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate("login") }) {
            Text("Volver")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecoverPasswordPreview() {
    val navController = rememberNavController()
    MaterialTheme {
        RecoverPasswordScreen(navController = navController)
    }
}