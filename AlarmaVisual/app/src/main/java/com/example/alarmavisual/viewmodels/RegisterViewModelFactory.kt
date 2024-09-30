package com.example.alarmavisual.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.alarmavisual.helpers.RealVibratorHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers

class RegisterViewModelFactory(
    private val context: Context,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val dispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.Main
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val vibratorHelper = RealVibratorHelper(context)
        return RegisterViewModel(auth, db, vibratorHelper, dispatcher) as T
    }
}