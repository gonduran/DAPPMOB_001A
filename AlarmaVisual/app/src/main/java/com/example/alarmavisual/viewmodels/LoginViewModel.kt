// LoginViewModel.kt
package com.example.alarmavisual.viewmodels

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarmavisual.helpers.VibratorHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginViewModel(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val vibratorHelper: VibratorHelper,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main, // Añadido para pruebas
    private val delayDuration: Long = 500L, // Añadido para controlar el delay en pruebas
    private val repeatTimes: Int = 10       // Añadido para controlar las repeticiones en pruebas
) : ViewModel() {

    // Estados para email y contraseña
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    // Estados para mensajes y errores
    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    private val _showError = MutableStateFlow(false)
    val showError: StateFlow<Boolean> = _showError

    private val _isLoginSuccessful = MutableStateFlow(false)
    val isLoginSuccessful: StateFlow<Boolean> = _isLoginSuccessful

    // Índice para animación de colores de error
    private val _colorIndex = MutableStateFlow(0)
    val colorIndex: StateFlow<Int> = _colorIndex

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
        // Reemplazamos Patterns.EMAIL_ADDRESS por una expresión regular personalizada
        val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
        return email.matches(emailRegex.toRegex())
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
        viewModelScope.launch(dispatcher) {
            repeat(repeatTimes) {
                _colorIndex.value = (_colorIndex.value + 1) % errorColors.size
                vibratorHelper.vibrateError()
                delay(delayDuration)
            }
            _showError.value = false
        }
    }

    // Función para manejar el efecto de éxito
    private fun triggerSuccessEffect() {
        viewModelScope.launch(dispatcher) {
            vibratorHelper.vibrateSuccess()
            // Puedes agregar más efectos aquí si lo deseas
        }
    }

    // Función para restablecer el estado de login exitoso
    fun resetLoginSuccess() {
        _isLoginSuccessful.value = false
    }
}