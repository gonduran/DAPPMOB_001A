package com.example.alarmavisual.viewmodels

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarmavisual.helpers.VibratorHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val vibratorHelper: VibratorHelper,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main,
    val delayDuration: Long = 500L,
    val repeatTimes: Int = 10
) : ViewModel() {

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    private val _showError = MutableStateFlow(false)
    val showError: StateFlow<Boolean> = _showError

    private val _isRegistrationSuccessful = MutableStateFlow(false)
    val isRegistrationSuccessful: StateFlow<Boolean> = _isRegistrationSuccessful

    // Índice para animación de colores de error
    private val _colorIndex = MutableStateFlow(0)
    val colorIndex: StateFlow<Int> = _colorIndex

    // Lista de colores de error
    val errorColors = listOf(Color.Red, Color.Yellow, Color.Magenta)

    fun onNameChange(newName: String) {
        _name.value = newName
    }

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun onConfirmPasswordChange(newConfirmPassword: String) {
        _confirmPassword.value = newConfirmPassword
    }

    private fun handleError(action: () -> Unit) {
        try {
            action()
        } catch (e: Exception) {
            _errorMessage.value = "Ocurrió un error inesperado: ${e.message}"
            triggerErrorEffect()
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun containsMaliciousCharacters(input: String): Boolean {
        return "[<>&\"';*]".toRegex().containsMatchIn(input)
    }

    private fun validatePassword(password: String): String? {
        if (password.length < 6) {
            return "La contraseña debe tener al menos 6 caracteres."
        }
        if (!password.any { it.isUpperCase() }) {
            return "La contraseña debe tener al menos una letra mayúscula."
        }
        if (!password.any { it.isDigit() }) {
            return "La contraseña debe tener al menos un dígito."
        }
        if (!password.any { "!@#\$%^&*()-_=+[]{}|;:'\",.<>?/`~".contains(it) }) {
            return "La contraseña debe tener al menos un carácter especial."
        }
        for (i in 0 until password.length - 2) {
            val char1 = password[i]
            val char2 = password[i + 1]
            val char3 = password[i + 2]

            if (char1.isDigit() && char1 == char2 && char2 == char3) {
                return "No puede haber más de 2 dígitos consecutivos iguales."
            }

            if (char1.isDigit() && char2.isDigit() && char3.isDigit()) {
                val num1 = char1.toString().toInt()
                val num2 = char2.toString().toInt()
                val num3 = char3.toString().toInt()

                if (num2 == num1 + 1 && num3 == num2 + 1) {
                    return "No puede haber secuencias de 3 dígitos consecutivos ascendentes."
                }

                if (num2 == num1 - 1 && num3 == num2 - 1) {
                    return "No puede haber secuencias de 3 dígitos consecutivos descendentes."
                }
            }
        }
        return null
    }

    fun register() {
        handleError {
            val currentName = _name.value
            val currentEmail = _email.value
            val currentPassword = _password.value
            val currentConfirmPassword = _confirmPassword.value

            when {
                currentName.isEmpty() || currentEmail.isEmpty() || currentPassword.isEmpty() || currentConfirmPassword.isEmpty() -> {
                    _errorMessage.value = "Todos los campos son obligatorios."
                    triggerErrorEffect()
                }
                !isEmailValid(currentEmail) -> {
                    _errorMessage.value = "Por favor, ingrese un correo válido."
                    triggerErrorEffect()
                }
                containsMaliciousCharacters(currentName) || containsMaliciousCharacters(currentEmail) ||
                        containsMaliciousCharacters(currentPassword) || containsMaliciousCharacters(currentConfirmPassword) -> {
                    _errorMessage.value = "Alerta: Se detectaron caracteres potencialmente maliciosos."
                    triggerErrorEffect()
                }
                validatePassword(currentPassword) != null -> {
                    _errorMessage.value = validatePassword(currentPassword).toString()
                    triggerErrorEffect()
                }
                currentPassword != currentConfirmPassword -> {
                    _errorMessage.value = "Las contraseñas no coinciden."
                    triggerErrorEffect()
                }
                else -> {
                    // Registro en Firebase Authentication
                    auth.createUserWithEmailAndPassword(currentEmail, currentPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Registro exitoso
                                val user = auth.currentUser
                                // Guardar el nombre y correo en Firestore
                                val userMap = hashMapOf(
                                    "name" to currentName,
                                    "email" to currentEmail
                                )
                                db.collection("users").document(user?.uid ?: "")
                                    .set(userMap)
                                    .addOnSuccessListener {
                                        // Enviar correo de verificación
                                        user?.sendEmailVerification()
                                            ?.addOnCompleteListener { emailTask ->
                                                if (emailTask.isSuccessful) {
                                                    _errorMessage.value = "Registro exitoso. Por favor, verifica tu correo."
                                                    _showError.value = true
                                                    _isRegistrationSuccessful.value = true
                                                    triggerSuccessEffect()
                                                } else {
                                                    _errorMessage.value = "Error al enviar el correo de verificación: ${emailTask.exception?.message}"
                                                    triggerErrorEffect()
                                                }
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        _errorMessage.value = "Error al guardar el usuario en Firestore: $e"
                                        triggerErrorEffect()
                                    }
                            } else {
                                if (task.exception is FirebaseAuthUserCollisionException) {
                                    _errorMessage.value = "El correo ya está registrado."
                                    triggerErrorEffect()
                                } else {
                                    _errorMessage.value = "Error en el registro: ${task.exception?.message}. Inténtalo nuevamente."
                                    triggerErrorEffect()
                                }
                            }
                        }
                }
            }
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
            // Se puede agregar más efectos aquí
        }
    }

    fun resetShowError() {
        _showError.value = false
    }

    // Función para restablecer el estado de registro exitoso
    fun resetRegisterSuccess() {
        _isRegistrationSuccessful.value = false
    }
}