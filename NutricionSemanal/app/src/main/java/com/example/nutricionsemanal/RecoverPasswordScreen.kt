package com.example.nutricionsemanal

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun RecoverPasswordScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Recuperar Contraseña", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { /* Lógica de recuperación de contraseña */ }) {
            Text(text = "Enviar instrucciones")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecoverPasswordPreview() {
    MaterialTheme {
        //RecoverPasswordScreen()
    }
}