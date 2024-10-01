package com.example.alarmavisual.viewmodels

import android.app.Application
import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.runner.RunWith
import com.google.firebase.FirebaseOptions
import org.junit.After
import org.mockito.MockedStatic
import org.mockito.Mockito

// Usar Robolectric como el runner de pruebas
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.P])
@ExperimentalCoroutinesApi
class ConversationViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var context: Context

    @Mock
    lateinit var textToSpeech: TextToSpeech

    @Mock
    lateinit var firestore: FirebaseFirestore

    @Mock
    lateinit var collectionReference: CollectionReference

    @Mock
    lateinit var documentReference: DocumentReference

    private lateinit var mockFirebaseApp: MockedStatic<FirebaseApp>
    private lateinit var mockFirebaseAuth: MockedStatic<FirebaseAuth>

    lateinit var viewModel: ConversationViewModel

    private val testScope = TestScope(UnconfinedTestDispatcher())

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        // Simular FirebaseApp para evitar su inicialización real
        mockFirebaseApp = mockStatic(FirebaseApp::class.java)
        mockFirebaseAuth = mockStatic(FirebaseAuth::class.java)

        val mockFirebaseAppInstance = mock(FirebaseApp::class.java)
        `when`(FirebaseApp.getInstance()).thenReturn(mockFirebaseAppInstance)

        val mockFirebaseAuthInstance = mock(FirebaseAuth::class.java)
        `when`(FirebaseAuth.getInstance()).thenReturn(mockFirebaseAuthInstance)

        // Simular Firebase Firestore
        `when`(firestore.collection("users")).thenReturn(collectionReference)
        `when`(collectionReference.document(anyString())).thenReturn(documentReference)
        `when`(documentReference.collection("interactions")).thenReturn(collectionReference)

        // Inicializar el ViewModel con los mocks
        val application = mock(Application::class.java)
        viewModel = ConversationViewModel(application, textToSpeech, firestore)
    }

    @Test
    fun `speakText should trigger TextToSpeech when text is provided`() = testScope.runTest {
        // Simular el texto ingresado por el usuario
        viewModel.onUserTextChange("Hola, ¿cómo estás?")
        viewModel.speakText()

        // Verificar que se llamó a speak en TextToSpeech
        verify(textToSpeech).speak(eq("Hola, ¿cómo estás?"), eq(TextToSpeech.QUEUE_FLUSH), isNull(), isNull())
    }

    @Test
    fun `speakText should emit error when no text is provided`() = testScope.runTest {
        // Simular que no se ha ingresado texto
        viewModel.onUserTextChange("")

        // Llamar a speakText
        viewModel.speakText()

        // Verificar que se emite el mensaje de error
        val errorMessage = viewModel.errorMessage.first()
        assertEquals("No hay texto para convertir a voz", errorMessage)
    }

    @Test
    fun `onUserTextChange should update user text`() = runTest {
        viewModel.onUserTextChange("Nuevo texto")

        // Verificar que el flujo de texto del usuario tiene el valor correcto
        assertEquals("Nuevo texto", viewModel.userText.first())
    }

    @Test
    fun `handleVoiceRecognitionResult should update speechToTextState with recognized text`() = runTest {
        // Simular un resultado de reconocimiento de voz
        val result = listOf("Hola, esto es una prueba")

        // Ejecutar el método
        viewModel.handleVoiceRecognitionResult(result)

        // Verificar que el estado de speechToTextState se actualizó
        assertEquals("Hola, esto es una prueba", viewModel.speechToTextState.first())
    }

    @Test
    fun `handleVoiceRecognitionResult should update errorMessage when result is null`() = runTest {
        // Ejecutar el método con un resultado nulo
        viewModel.handleVoiceRecognitionResult(null)

        // Verificar que el estado de speechToTextState está vacío
        assertEquals("", viewModel.speechToTextState.first())

        // Verificar que el estado de errorMessage se actualizó correctamente
        assertEquals("No se pudo transcribir el audio.", viewModel.errorMessage.first())
    }

    /*@Test
    fun `saveInteraction should save interaction in Firestore`() = runTest {
        // Datos de prueba
        val userId = "user123"
        val interaction = mapOf("type" to "voice_to_text", "content" to "Hola", "timestamp" to System.currentTimeMillis())

        // Ejecutar el método para guardar la interacción
        viewModel.saveInteraction(userId, interaction)

        // Verificar que se llamó a la referencia de Firestore correctamente
        verify(collectionReference).add(interaction)
    }*/

    @After
    fun tearDown() {
        // Liberar los mocks estáticos
        mockFirebaseApp.close()
        mockFirebaseAuth.close()
    }
}