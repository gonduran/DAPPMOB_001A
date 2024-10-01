package com.example.alarmavisual.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.example.alarmavisual.MainActivity
import com.example.alarmavisual.alarm.CustomAlarmManager

@Composable
fun HomeMenuScreen(navController: NavHostController?, alarmManager: CustomAlarmManager?) {
    val context = LocalContext.current

    // Aquí llamamos a scheduleWidgetUpdateTask cuando se inicializa la pantalla
    LaunchedEffect(Unit) {
        alarmManager?.scheduleWidgetUpdateTask(context) // Llama a tu función aquí
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
        Text(
            text = "Menu",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Botón para configurar alarmas
        Button(
            onClick = { navController?.navigate("alarmList") }, // Navega a la pantalla de alarmas
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Icon(Icons.Default.Alarm, contentDescription = "Configurar Alarmas")
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Configurar Alarmas", fontSize = 18.sp)
        }

        // Botón para mostrar ubicación
        Button(
            onClick = { navController?.navigate("location") }, // Navega a la pantalla de ubicación
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = "Mostrar Ubicación")
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Mostrar Ubicación", fontSize = 18.sp)
        }

        // Botón para conversación inclusiva
        Button(
            onClick = { navController?.navigate("conversation") }, // Navega a la pantalla de conversación inclusiva
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Icon(Icons.Default.Mic, contentDescription = "Conversar")
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Conversar", fontSize = 18.sp)
        }

        // Botón para cerrar sesión
        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut() // Cerrar sesión de Firebase
                navController?.navigate("login") { popUpTo("login") { inclusive = true } }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = "Cerrar Sesión")
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Cerrar Sesión", fontSize = 18.sp)
        }

        // Botón para salir de la aplicación
        Button(
            onClick = { (context as? MainActivity)?.finish() }, // Finaliza la actividad para salir
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = "Salir")
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Salir", fontSize = 18.sp)
        }

        /*Button(
            onClick = { alarmManager?.forceWidgetUpdate(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
        ) {
            Text(text = "Forzar actualización del widget")
        }*/
    }
}

@Preview(showBackground = true)
@Composable
fun HomeMenuScreenPreview() {
    val fakeNavController = rememberNavController() // Controlador de navegación falso para el preview
    val fakeAlarmManager = null // Simulamos un alarmManager nulo

    MaterialTheme {
        HomeMenuScreen(navController = fakeNavController, alarmManager = fakeAlarmManager)
    }
}