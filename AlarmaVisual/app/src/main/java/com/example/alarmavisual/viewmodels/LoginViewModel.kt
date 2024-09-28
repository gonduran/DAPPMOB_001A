package com.example.alarmavisual.viewmodels

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LoginViewModel(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val context: Context
) : ViewModel() {

    // Estados para email y contraseña
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    // Estados para mensajes y errores
    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    private val _showError = MutableStateFlow(false)
    val showError: StateFlow<Boolean> = _showError.asStateFlow()

    private val _isLoginSuccessful = MutableStateFlow(false)
    val isLoginSuccessful: StateFlow<Boolean> = _isLoginSuccessful.asStateFlow()

    // Indice para animación de colores de error
    private val _colorIndex = MutableStateFlow(0)
    val colorIndex: StateFlow<Int> = _colorIndex.asStateFlow()

    // Lista de colores de error
    val errorColors = listOf(Color.Red, Color.Yellow, Color.Magenta)

    // Funciones para actualizar email y contraseña
    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    // Función para validar email
    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Función para iniciar sesión
    fun signIn() {
        val email = _email.value
        val password = _password.value

        try {
            when {
                email.isEmpty() || password.isEmpty() -> {
                    _errorMessage.value = "Todos los campos son obligatorios"
                    triggerErrorEffect()
                }
                !isEmailValid(email) -> {
                    _errorMessage.value = "Por favor, ingrese un correo válido"
                    triggerErrorEffect()
                }
                else -> {
                    _showError.value = false // Ocultar mensajes previos
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                val userId = user?.uid

                                if (userId != null) {
                                    db.collection("users").document(userId)
                                        .get()
                                        .addOnSuccessListener { document ->
                                            if (document.exists()) {
                                                val userName = document.getString("name")
                                                _errorMessage.value = "Inicio de sesión exitoso. ¡Bienvenido $userName!"
                                                _showError.value = true
                                                _isLoginSuccessful.value = true
                                                triggerSuccessEffect()
                                            } else {
                                                _errorMessage.value = "Error: El usuario no tiene un nombre registrado."
                                                triggerErrorEffect()
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            _errorMessage.value = "Error al recuperar el nombre: ${e.message}"
                                            triggerErrorEffect()
                                        }
                                } else {
                                    _errorMessage.value = "Error: No se pudo obtener el UID del usuario."
                                    triggerErrorEffect()
                                }
                            } else {
                                _errorMessage.value = "Error en el inicio de sesión: ${task.exception?.message}"
                                triggerErrorEffect()
                            }
                        }
                }
            }
        } catch (e: Exception) {
            _errorMessage.value = "Error al iniciar sesión."
            triggerErrorEffect()
        }
    }

    // Función para manejar el efecto de error (vibración y cambio de color)
    private fun triggerErrorEffect() {
        _showError.value = true
        viewModelScope.launch {
            repeat(10) {
                _colorIndex.value = (_colorIndex.value + 1) % errorColors.size
                vibrateDevice()
                delay(500)
            }
            _showError.value = false
        }
    }

    // Función para manejar el efecto de éxito
    private fun triggerSuccessEffect() {
        viewModelScope.launch {
            vibrateDeviceSuccess()
            // Puedes agregar más efectos aquí si lo deseas
        }
    }

    // Función para vibrar el dispositivo (error)
    private fun vibrateDevice() {
        val vibrator = getVibrator()
        try {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (e: Exception) {
            // Manejar excepción si es necesario
            e.printStackTrace()
        }
    }

    // Función para vibrar el dispositivo (éxito)
    private fun vibrateDeviceSuccess() {
        val vibrator = getVibrator()
        try {
            // Vibración diferente para éxito (por ejemplo, doble vibración corta)
            val pattern = longArrayOf(0, 500, 100, 500)
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } catch (e: Exception) {
            // Manejar excepción si es necesario
            e.printStackTrace()
        }
    }

    // Función para obtener el Vibrator
    private fun getVibrator(): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    // Función para restablecer el estado de login exitoso
    fun resetLoginSuccess() {
        _isLoginSuccessful.value = false
    }
}