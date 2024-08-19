package com.example.alarmavisual.views

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.alarmavisual.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    // Controlar el tiempo de visualización del Splash Screen
    LaunchedEffect(key1 = true) {
        delay(4000) // 4 segundos
        navController.navigate("login") // Después de 4 segundos, navega al login
    }

    // Animación de escala para la imagen y el texto
    val scale = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1.2f,
            animationSpec = tween(
                durationMillis = 1000, // 1 segundo
            )
        )
    }

    // Pantalla de Splash con Imagen y Texto
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary) // Color de fondo
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Imagen con animación de escala
            Image(
                painter = painterResource(id = R.drawable.logoreloj),
                contentDescription = "Alarma Visual",
                modifier = Modifier
                    .size(150.dp)
                    .scale(scale.value)
            )

            Spacer(modifier = Modifier.height(64.dp))
            // Texto con animación de escala
            Text(
                text = "Alarma Visual",
                style = MaterialTheme.typography.titleLarge,
                //fontSize = 30.sp,
                //fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.scale(scale.value)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    val navController = rememberNavController()
    SplashScreen(navController = navController)
}