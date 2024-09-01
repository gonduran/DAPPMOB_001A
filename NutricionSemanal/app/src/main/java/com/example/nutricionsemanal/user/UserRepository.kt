package com.example.nutricionsemanal.user

interface UserRepository {
    fun registerUser(name: String, email: String, password: String): Boolean
    fun isUserRegistered(email: String): Boolean
    fun validateCredentials(email: String, password: String): Boolean
    fun sendPasswordRecovery(email: String): Boolean
    fun getUserName(email: String): String?
}