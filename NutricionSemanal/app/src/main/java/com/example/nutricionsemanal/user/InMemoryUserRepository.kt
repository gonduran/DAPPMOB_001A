package com.example.nutricionsemanal.user

class InMemoryUserRepository : UserRepository {
    // Lista para almacenar usuarios como tuplas (Nombre, Email, Password)
    private val registeredUsers = mutableListOf<Triple<String, String, String>>()

    override fun registerUser(name: String, email: String, password: String): Boolean {
        return if (isUserRegistered(email)) {
            false // El usuario ya está registrado
        } else {
            registeredUsers.add(Triple(name, email, password))
            true // Registro exitoso
        }
    }

    override fun isUserRegistered(email: String): Boolean {
        return registeredUsers.any { it.second == email }
    }

    override fun validateCredentials(email: String, password: String): Boolean {
        return registeredUsers.any { it.second == email && it.third == password }
    }

    override fun sendPasswordRecovery(email: String): Boolean {
        return isUserRegistered(email) // Simula el envío de correo
    }

    override fun getUserName(email: String): String? {
        return registeredUsers.find { it.second == email }?.first
    }
}