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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.alarmavisual.R
import com.example.alarmavisual.viewmodels.RegisterViewModel
import com.example.alarmavisual.viewmodels.RegisterViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun RegisterScreen(navController: NavHostController) {
    val context = LocalContext.current

    // Proporcionar el contexto al ViewModel
    val registerViewModel: RegisterViewModel = viewModel(
        factory = RegisterViewModelFactory(context = context)
    )

    val name by registerViewModel.name.collectAsState()
    val email by registerViewModel.email.collectAsState()
    val password by registerViewModel.password.collectAsState()
    val confirmPassword by registerViewModel.confirmPassword.collectAsState()
    val errorMessage by registerViewModel.errorMessage.collectAsState()
    val showError by registerViewModel.showError.collectAsState()
    val isRegistrationSuccessful by registerViewModel.isRegistrationSuccessful.collectAsState()

    var isPasswordFocused by remember { mutableStateOf(false) }
    var colorIndex by remember { mutableStateOf(0) }

    val errorColors = listOf(Color.Red, Color.Yellow, Color.Magenta)
    val animatedColor by animateColorAsState(targetValue = errorColors[colorIndex])

    // Definir el color del mensaje basado en el estado de éxito o error
    val messageColor by remember {
        derivedStateOf {
            if (isRegistrationSuccessful) Color.Green else animatedColor
        }
    }

    val gradientColors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFF77A8AF)
    )

    val coroutineScope = rememberCoroutineScope()

    // Función para manejar la animación de error
    LaunchedEffect(showError) {
        if (showError) {
            repeat(registerViewModel.repeatTimes) {
                colorIndex = (colorIndex + 1) % errorColors.size
                delay(registerViewModel.delayDuration)
            }
            registerViewModel.resetShowError()
        }
    }

    // Delay y navegación después de un registro exitoso
    LaunchedEffect(isRegistrationSuccessful) {
        if (isRegistrationSuccessful) {
            delay(2000)
            navController.navigate("login")
            // Restablecer el estado de registro exitoso
            registerViewModel.resetRegisterSuccess()
        }
    }

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
            onValueChange = { registerViewModel.onNameChange(it) },
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
            onValueChange = { registerViewModel.onEmailChange(it) },
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
            onValueChange = { registerViewModel.onPasswordChange(it) },
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
            onValueChange = { registerViewModel.onConfirmPasswordChange(it) },
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
                color = messageColor,
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
                coroutineScope.launch {
                    registerViewModel.register()
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

@Preview(showBackground = true)
@Composable
fun RegisterPreview() {
    // Usamos RegisterFake para la vista previa sin lógica
    MaterialTheme {
        RegisterFake(navController = null)
    }
}

@Composable
fun RegisterFake(navController: NavHostController?) {
    var name by remember { mutableStateOf("Test User") }
    var email by remember { mutableStateOf("test@example.com") }
    var password by remember { mutableStateOf("Password1!") }
    var confirmPassword by remember { mutableStateOf("Password1!") }
    var errorMessage by remember { mutableStateOf("Registro exitoso. Por favor, verifica tu correo.") }
    var showError by remember { mutableStateOf(true) }
    var colorIndex by remember { mutableStateOf(0) }
    var isPasswordFocused by remember { mutableStateOf(false) }

    val errorColors = listOf(Color.Red, Color.Yellow, Color.Magenta)
    val animatedColor by animateColorAsState(targetValue = errorColors[colorIndex])

    val gradientColors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFF77A8AF)
    )

    // Función para manejar la animación de error
    LaunchedEffect(showError) {
        if (showError) {
            repeat(10) {
                colorIndex = (colorIndex + 1) % errorColors.size
                delay(500)
            }
            showError = false
        }
    }

    // Delay y navegación después de un registro exitoso
    LaunchedEffect(showError) {
        if (showError && errorMessage.startsWith("Registro exitoso")) {
            delay(2000)
            navController?.navigate("login")
        }
    }

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
                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
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