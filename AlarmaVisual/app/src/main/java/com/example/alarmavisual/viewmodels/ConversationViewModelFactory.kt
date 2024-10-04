package com.example.alarmavisual.viewmodels

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.alarmavisual.viewmodels.ConversationViewModel
import com.google.firebase.firestore.FirebaseFirestore

class ConversationViewModelFactory(
    private val application: Application,
    private val textToSpeech: TextToSpeech,
    private val firestore: FirebaseFirestore
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConversationViewModel::class.java)) {
            return ConversationViewModel(application, textToSpeech, firestore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}