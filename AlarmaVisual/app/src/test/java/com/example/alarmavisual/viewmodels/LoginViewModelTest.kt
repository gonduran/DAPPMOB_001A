package com.example.alarmavisual.viewmodels

// Importaciones necesarias
import android.content.Context
import android.os.Vibrator
import app.cash.turbine.test
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.junit.Assert.assertTrue
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel
    private val auth: FirebaseAuth = mock()
    private val db: FirebaseFirestore = mock()
    private val vibrator: Vibrator = mock()
    private val context: Context = mock()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        // Configurar el Dispatcher de pruebas
        Dispatchers.setMain(testDispatcher)

        // Crear una instancia del ViewModel con los mocks
        viewModel = LoginViewModel(auth, db, context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `signIn with empty email and password triggers error effect`() = runTest {
        // Simular entradas vacías
        viewModel.onEmailChange("")
        viewModel.onPasswordChange("")

        // Observar el estado
        viewModel.errorMessage.test {
            viewModel.signIn()
            // Ejecutar cualquier tarea pendiente
            advanceUntilIdle()

            // Verificar que se emite el mensaje de error correcto
            assertEquals("Todos los campos son obligatorios", awaitItem())
        }

        // Verificar que se activó el efecto de error
        assertTrue(viewModel.showError.value)
    }

    @Test
    fun `signIn with invalid email triggers error effect`() = runTest {
        // Simular email inválido
        viewModel.onEmailChange("invalidemail")
        viewModel.onPasswordChange("password123")

        viewModel.errorMessage.test {
            viewModel.signIn()
            advanceUntilIdle()
            assertEquals("Por favor, ingrese un correo válido", awaitItem())
        }

        assertTrue(viewModel.showError.value)
    }

    @Test
    fun `signIn with valid credentials and successful authentication`() = runTest {
        val email = "test@example.com"
        val password = "password123"
        val userId = "user123"
        val userName = "Juan Pérez"

        // Configurar el mock de FirebaseAuth para autenticación exitosa
        val authResultTask = mockSuccessfulAuthTask()
        whenever(auth.signInWithEmailAndPassword(eq(email), eq(password))).thenReturn(authResultTask)

        val firebaseUser = mock<FirebaseUser>()
        whenever(auth.currentUser).thenReturn(firebaseUser)
        whenever(firebaseUser.uid).thenReturn(userId)

        // Configurar el mock de FirebaseFirestore para obtener el nombre del usuario
        val documentSnapshot = mock<DocumentSnapshot>()
        whenever(documentSnapshot.exists()).thenReturn(true)
        whenever(documentSnapshot.getString("name")).thenReturn(userName)

        val documentTask = mockSuccessfulDocumentTask(documentSnapshot)

        // Mockear CollectionReference y DocumentReference
        val collectionReference = mock<CollectionReference>()
        val documentReference = mock<DocumentReference>()
        whenever(db.collection("users")).thenReturn(collectionReference)
        whenever(collectionReference.document(eq(userId))).thenReturn(documentReference)
        whenever(documentReference.get()).thenReturn(documentTask)

        // Simular entradas válidas
        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)

        viewModel.errorMessage.test {
            viewModel.signIn()
            advanceUntilIdle()

            assertEquals("Inicio de sesión exitoso. ¡Bienvenido $userName!", awaitItem())
        }

        assertTrue(viewModel.isLoginSuccessful.value)
        assertTrue(viewModel.showError.value)
    }

    @Test
    fun `signIn with incorrect credentials triggers authentication error`() = runTest {
        val email = "test@example.com"
        val password = "wrongpassword"

        // Configurar el mock de FirebaseAuth para autenticación fallida
        val exception = FirebaseAuthInvalidCredentialsException("ERROR_INVALID_CREDENTIAL", "Invalid credentials")
        val authResultTask = mockFailedAuthTask(exception)
        whenever(auth.signInWithEmailAndPassword(eq(email), eq(password))).thenReturn(authResultTask)

        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)

        viewModel.errorMessage.test {
            viewModel.signIn()
            advanceUntilIdle()
            val errorMsg = awaitItem()
            assertTrue(errorMsg.contains("Error en el inicio de sesión"))
        }

        assertTrue(viewModel.showError.value)
    }

    @Test
    fun `signIn with valid credentials but Firestore user not found triggers error`() = runTest {
        val email = "test@example.com"
        val password = "password123"
        val userId = "user123"

        // Configurar el mock de FirebaseAuth para autenticación exitosa
        val authResultTask = mockSuccessfulAuthTask()
        whenever(auth.signInWithEmailAndPassword(eq(email), eq(password))).thenReturn(authResultTask)

        val firebaseUser = mock<FirebaseUser>()
        whenever(auth.currentUser).thenReturn(firebaseUser)
        whenever(firebaseUser.uid).thenReturn(userId)

        // Configurar el mock de FirebaseFirestore para que el documento no exista
        val documentSnapshot = mock<DocumentSnapshot>()
        whenever(documentSnapshot.exists()).thenReturn(false)

        val documentTask = mockSuccessfulDocumentTask(documentSnapshot)

        // Mockear CollectionReference y DocumentReference
        val collectionReference = mock<CollectionReference>()
        val documentReference = mock<DocumentReference>()
        whenever(db.collection("users")).thenReturn(collectionReference)
        whenever(collectionReference.document(eq(userId))).thenReturn(documentReference)
        whenever(documentReference.get()).thenReturn(documentTask)

        // Simular entradas válidas
        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)

        viewModel.errorMessage.test {
            viewModel.signIn()
            advanceUntilIdle()
            assertEquals("Error: El usuario no tiene un nombre registrado.", awaitItem())
        }

        assertTrue(viewModel.showError.value)
    }

    // Funciones auxiliares para mockear tareas exitosas y fallidas

    private fun mockSuccessfulAuthTask(): Task<AuthResult> {
        val task = mock<Task<AuthResult>>()
        whenever(task.isSuccessful).thenReturn(true)
        whenever(task.addOnCompleteListener(any())).thenAnswer { invocation ->
            val listener = invocation.arguments[0] as OnCompleteListener<AuthResult>
            listener.onComplete(task)
            task
        }
        return task
    }

    private fun mockFailedAuthTask(exception: Exception): Task<AuthResult> {
        val task = mock<Task<AuthResult>>()
        whenever(task.isSuccessful).thenReturn(false)
        whenever(task.exception).thenReturn(exception)
        whenever(task.addOnCompleteListener(any())).thenAnswer { invocation ->
            val listener = invocation.arguments[0] as OnCompleteListener<AuthResult>
            listener.onComplete(task)
            task
        }
        return task
    }

    private fun mockSuccessfulDocumentTask(documentSnapshot: DocumentSnapshot): Task<DocumentSnapshot> {
        val task = mock<Task<DocumentSnapshot>>()
        whenever(task.isSuccessful).thenReturn(true)
        whenever(task.result).thenReturn(documentSnapshot)
        whenever(task.addOnSuccessListener(any())).thenAnswer { invocation ->
            val listener = invocation.arguments[0] as OnSuccessListener<DocumentSnapshot>
            listener.onSuccess(documentSnapshot)
            task
        }
        return task
    }
}