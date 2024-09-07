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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    // Animación de escala para la imagen y el texto
    val scale = remember { Animatable(0f) }

    // Función lambda para la navegación después del delay
    val navigateToLogin: () -> Unit = {
        try {
            navController.navigate("login")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Función que maneja el retraso y la navegación
    @Composable
    fun handleNavigationEffect(navigationAction: () -> Unit) {
        LaunchedEffect(key1 = true) {
            try {
                delay(3000) // 3 segundos de espera
                navigationAction() // Ejecuta la lambda de navegación
            } catch (e: Exception) {
                // Manejar errores de navegación si ocurren
                e.printStackTrace()
            }
        }
    }

    // Usar la función para manejar la navegación después del delay
    handleNavigationEffect(navigateToLogin)

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1.2f,
            animationSpec = tween(
                durationMillis = 1000, // 1 segundo para la animación de escala
            )
        )
    }

    // Define tus colores de degradado
    val gradientColors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFF77A8AF)
    )

    // Pantalla de Splash con Imagen y Texto
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors)) // Color de fondo
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