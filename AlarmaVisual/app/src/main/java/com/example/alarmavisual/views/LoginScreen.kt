package com.example.alarmavisual.views

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.alarmavisual.R
import com.example.alarmavisual.viewmodels.LoginViewModel
import com.example.alarmavisual.viewmodels.LoginViewModelFactory
import kotlinx.coroutines.delay
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController

@Composable
fun LoginScreen(navController: NavHostController) {
    val context = LocalContext.current

    // Proporcionar el contexto al ViewModel
    val loginViewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(context = context)
    )

    val email by loginViewModel.email.collectAsState()
    val password by loginViewModel.password.collectAsState()
    val errorMessage by loginViewModel.errorMessage.collectAsState()
    val showError by loginViewModel.showError.collectAsState()
    val colorIndex by loginViewModel.colorIndex.collectAsState()
    val isLoginSuccessful by loginViewModel.isLoginSuccessful.collectAsState()
    val errorColors = loginViewModel.errorColors
    val animatedColor by animateColorAsState(targetValue = errorColors[colorIndex])

    // Función para manejar la navegación con retraso después de un login exitoso
    LaunchedEffect(isLoginSuccessful) {
        if (isLoginSuccessful) {
            delay(2000) // Espera 2 segundos antes de navegar
            navController.navigate("homeMenu")
            // Restablecer el estado de login exitoso
            loginViewModel.resetLoginSuccess()
        }
    }

    // Definir el color del mensaje basado en el estado de éxito o error
    val messageColor by remember {
        derivedStateOf {
            if (isLoginSuccessful) Color.Green else animatedColor
        }
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
            contentDescription = "Alarma Visual",
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
            onValueChange = { loginViewModel.onEmailChange(it) },
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
            onValueChange = { loginViewModel.onPasswordChange(it) },
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

        // Mostrar mensaje de error o éxito
        if (showError) {
            Text(
                text = errorMessage,
                color = messageColor,
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
                loginViewModel.signIn()
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
        LoginScreenPreview(navController = navController)
    }
}

@Composable
fun LoginScreenPreview(navController: NavHostController) {
    // Variables de estado locales para el preview
    var email by remember { mutableStateOf("test@example.com") }
    var password by remember { mutableStateOf("password") }
    var errorMessage by remember { mutableStateOf("Error de ejemplo") }
    var showError by remember { mutableStateOf(true) }
    var colorIndex by remember { mutableStateOf(0) }
    val errorColors = listOf(Color.Red, Color.Yellow, Color.Magenta)
    val animatedColor by animateColorAsState(targetValue = errorColors[colorIndex])

    // Simula el cambio de color para el mensaje de error
    LaunchedEffect(Unit) {
        repeat(10) {
            colorIndex = (colorIndex + 1) % errorColors.size
            delay(500)
        }
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
        // Imagen del logo (asegúrate de tener el recurso en tu proyecto)
        Image(
            painter = painterResource(id = R.drawable.logoreloj),
            contentDescription = "Logo",
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
            ),
            modifier = Modifier.fillMaxWidth()
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
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar mensaje de error si es necesario
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
                // Acción simulada de inicio de sesión
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
        TextButton(onClick = { /* Acción simulada */ }) {
            Text(
                text = "¿Olvidaste tu contraseña?",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        // Botón para ir a la pantalla de registro
        TextButton(onClick = { /* Acción simulada */ }) {
            Text(
                text = "¿No tienes cuenta? Regístrate",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}