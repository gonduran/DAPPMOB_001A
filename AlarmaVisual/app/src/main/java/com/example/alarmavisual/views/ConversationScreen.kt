package com.example.alarmavisual.views

import android.app.Application
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.alarmavisual.viewmodels.ConversationViewModel
import java.util.*
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.navigation.NavHostController
import com.example.alarmavisual.viewmodels.ConversationViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class) // Opt-in para la API experimental de Material 3
@Composable
fun ConversationScreen(navController: NavHostController, viewModel: ConversationViewModel = viewModel()) {
    val context = LocalContext.current
    val textToSpeech = remember {
        TextToSpeech(context) {
            if (it != TextToSpeech.SUCCESS) {
                Toast.makeText(context, "Error inicializando TextToSpeech", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val viewModel: ConversationViewModel = viewModel(
        factory = ConversationViewModelFactory(
            context.applicationContext as Application,
            textToSpeech,
            firestore = FirebaseFirestore.getInstance()
        )
    )

    val speechToTextState by viewModel.speechToTextState.collectAsState()
    val textToSpeechState by viewModel.textToSpeechState.collectAsState()
    val userText by viewModel.userText.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val gradientColors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFF77A8AF)
    )

    // Solicitamos permiso para grabar audio
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) viewModel.startListening(context)
        }
    )

    // Inicializar el ActivityResultLauncher para el reconocimiento de voz
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            val recognizedText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            viewModel.handleVoiceRecognitionResult(recognizedText)
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Conversación Inclusiva") },
                    actions = {
                        IconButton(onClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(
                                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                )
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla ahora...")
                            }
                            speechRecognizerLauncher.launch(intent)
                        }) {
                            Icon(Icons.Filled.Mic, contentDescription = "Iniciar Conversión de Voz")
                        }
                    }
                )
            },
            snackbarHost = {
                // Si hay un mensaje de error, lo mostramos en un Snackbar
                errorMessage?.let { message ->
                    Snackbar(
                        action = {
                            TextButton(onClick = { /* Handle click action if needed */ }) {
                                Text("OK")
                            }
                        },
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(message)
                    }
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(gradientColors))
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "Texto Capturado:", style = MaterialTheme.typography.bodyLarge)
                    Text(text = speechToTextState, modifier = Modifier.fillMaxWidth())

                    OutlinedTextField(
                        value = userText,
                        onValueChange = { viewModel.onUserTextChange(it) },
                        label = { Text("Escribe tu mensaje") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.width(16.dp)) // Espacio entre los dos botones

                    Button(
                        onClick = { viewModel.speakText() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Convertir Texto a Voz")
                    }

                    Spacer(modifier = Modifier.width(16.dp)) // Espacio entre los dos botones

                    // Botón "Volver al Home Menu"
                    Button(
                        onClick = { navController.navigate("homeMenu")  },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Text("Volver")
                    }
                }
            }
        }
    }
}

// Versión para Preview, sin utilizar ViewModel o Context
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreenPreview() {
    val mockTextToSpeech = remember { mutableStateOf("Texto de ejemplo") }
    val mockSpeechToText = remember { mutableStateOf("Transcripción de voz simulada") }
    val mockUserText = remember { mutableStateOf("") }
    val gradientColors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFF77A8AF)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Conversación Inclusiva") },
                    actions = {
                        IconButton(onClick = { /* No action for preview */ }) {
                            Icon(Icons.Filled.Mic, contentDescription = "Iniciar Conversión de Voz")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(gradientColors))
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "Texto Capturado:", style = MaterialTheme.typography.bodyLarge)
                    Text(text = mockSpeechToText.value, modifier = Modifier.fillMaxWidth())

                    OutlinedTextField(
                        value = mockUserText.value,
                        onValueChange = { mockUserText.value = it },
                        label = { Text("Escribe tu mensaje") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.width(16.dp)) // Espacio entre los dos botones

                    Button(
                        onClick = { /* No action for preview */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Convertir Texto a Voz")
                    }

                    Spacer(modifier = Modifier.width(16.dp)) // Espacio entre los dos botones

                    // Botón "Volver al Home Menu"
                    Button(
                        onClick = { /* No action for preview */  },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Text("Volver")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewConversationScreen() {
    ConversationScreenPreview()
}