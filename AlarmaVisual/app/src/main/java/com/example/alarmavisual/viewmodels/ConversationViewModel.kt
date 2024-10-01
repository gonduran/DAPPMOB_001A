package com.example.alarmavisual.viewmodels

import android.content.Context
import android.os.Bundle
import android.app.Application
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ConversationViewModel(application: Application,
                            private val textToSpeech: TextToSpeech? = null,
                            private val firestore: FirebaseFirestore
) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()

    private val _speechToTextState = MutableStateFlow("")
    val speechToTextState: StateFlow<String> = _speechToTextState

    private val _textToSpeechState = MutableStateFlow("")
    val textToSpeechState: StateFlow<String> = _textToSpeechState

    private val _userText = MutableStateFlow("")
    val userText: StateFlow<String> = _userText

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var speechRecognizer: SpeechRecognizer? = null

    init {
        if (textToSpeech == null) {
            //initializeTextToSpeech(application.applicationContext)
            _errorMessage.value = "TextToSpeech no inicializado"
        }
    }

    fun onUserTextChange(newText: String) {
        _userText.value = newText
    }

    private fun initializeTextToSpeech(context: Context) {
        if (textToSpeech == null) {
            TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech?.language = Locale.getDefault()
                }
            }
        }
    }

    // Función para guardar la interacción en Firestore
    /*fun saveInteraction(type: String, text: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return  // Asegura que el usuario esté autenticado
        val interaction = hashMapOf(
            "type" to type,  // "voz-a-texto" o "texto-a-voz"
            "text" to text,
            "timestamp" to System.currentTimeMillis()
        )

        // Guarda la interacción en la subcolección /users/{userId}/interactions/
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("interactions")
            .add(interaction)
            .addOnSuccessListener {
                // Manejo del éxito del guardado
            }
            .addOnFailureListener { e ->
                // Manejo de errores
                _errorMessage.value = "Error al guardar la interacción: ${e.message}"
            }
    }*/
    fun saveInteraction(userId: String, interaction: Map<String, Any>) {
        firestore.collection("users").document(userId)
            .collection("interactions")
            .add(interaction)
            .addOnSuccessListener {
                // Log success or handle it
                _errorMessage.value = "Interacción guardada exitosamente"
            }
            .addOnFailureListener { e ->
                // Handle the error
                _errorMessage.value = "Error al guardar la interacción: ${e.message}"
            }
    }

    fun startListening(context: Context) {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Hable ahora...")
            }

            val recognitionListener = object : android.speech.RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {
                    _speechToTextState.value = "Error de reconocimiento"
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        _speechToTextState.value = matches[0] // Obtener el primer resultado
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            }

            speechRecognizer?.setRecognitionListener(recognitionListener)
            speechRecognizer?.startListening(intent)
        } else {
            _speechToTextState.value = "Reconocimiento no disponible en este dispositivo"
        }
    }

    fun speakText() {
        val text = _userText.value
        if (text.isNotBlank()) {
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            // Guardar en Firestore la interacción de texto a voz
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return  // Asegura que el usuario esté autenticado
            val interaction = hashMapOf(
                "type" to "texto-a-voz",
                "text" to text,
                "timestamp" to System.currentTimeMillis()
            )
            saveInteraction(userId, interaction)
        } else {
            _errorMessage.value = "No hay texto para convertir a voz"
        }
    }

    override fun onCleared() {
        super.onCleared()
        textToSpeech?.shutdown()
        speechRecognizer?.destroy()
    }

    fun handleVoiceRecognitionResult(result: List<String>?) {
        result?.let {
            val recognizedText = it.firstOrNull() ?: ""
            _speechToTextState.value = recognizedText

            // Guardar en Firestore la interacción de voz a texto
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return  // Asegura que el usuario esté autenticado
            val interaction = hashMapOf(
                "type" to "voz-a-texto",
                "text" to recognizedText,
                "timestamp" to System.currentTimeMillis()
            )
            saveInteraction(userId, interaction)
        } ?: run {
            _errorMessage.value = "No se pudo transcribir el audio."
        }
    }


}