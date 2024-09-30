package com.example.alarmavisual.viewmodels

import app.cash.turbine.test
import com.example.alarmavisual.helpers.VibratorHelper
import com.google.android.gms.tasks.*
import com.google.firebase.auth.*
import com.google.firebase.firestore.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@ExperimentalCoroutinesApi
class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel
    private val auth: FirebaseAuth = mock()
    private val db: FirebaseFirestore = mock()
    private val vibratorHelper: VibratorHelper = mock()
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(
            auth,
            db,
            vibratorHelper,
            dispatcher = testDispatcher,
            delayDuration = 0L, // Sin retraso en las pruebas
            repeatTimes = 1     // Solo una repetición
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `signIn with invalid email triggers error effect`() = testScope.runTest {
        // Simular email inválido
        viewModel.onEmailChange("invalidemail")
        viewModel.onPasswordChange("password123")

        viewModel.showError.test {
            viewModel.signIn()
            advanceUntilIdle()
            skipItems(1) // Omitimos el valor inicial de false

            val showErrorValue = awaitItem()
            assertTrue(showErrorValue)

            // Si showError se restablece a false, consumimos el siguiente evento
            val resetValue = awaitItem()
            assertFalse(resetValue)

            // Verificamos que no hay más eventos
            expectNoEvents()
        }

        verify(vibratorHelper, atLeastOnce()).vibrateError()
    }

    @Test
    fun `signIn with empty email and password triggers error effect`() = testScope.runTest {
        // Simular entradas vacías
        viewModel.onEmailChange("")
        viewModel.onPasswordChange("")

        viewModel.showError.test {
            viewModel.signIn()
            advanceUntilIdle()
            skipItems(1)

            val showErrorValue = awaitItem()
            assertTrue(showErrorValue)

            // Consumimos el restablecimiento a false si ocurre
            val resetValue = awaitItem()
            assertFalse(resetValue)

            expectNoEvents()
        }

        verify(vibratorHelper, atLeastOnce()).vibrateError()
    }

    @Test
    fun `signIn with incorrect credentials triggers authentication error`() = testScope.runTest {
        val email = "test@example.com"
        val password = "wrongpassword"

        val exception = Exception("Invalid credentials")

        // Configurar el mock de FirebaseAuth para autenticación fallida
        val authResultTask = mockFailedAuthTask(exception)
        whenever(auth.signInWithEmailAndPassword(eq(email), eq(password))).thenReturn(authResultTask)

        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)

        viewModel.showError.test {
            viewModel.signIn()
            advanceUntilIdle()
            skipItems(1)

            val showErrorValue = awaitItem()
            assertTrue(showErrorValue)

            // Consumimos el restablecimiento a false si ocurre
            val resetValue = awaitItem()
            assertFalse(resetValue)

            expectNoEvents()
        }

        verify(vibratorHelper, atLeastOnce()).vibrateError()
    }

    @Test
    fun `signIn with valid credentials but Firestore user not found triggers error`() = testScope.runTest {
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

        viewModel.showError.test {
            viewModel.signIn()
            advanceUntilIdle()
            skipItems(1)

            val showErrorValue = awaitItem()
            assertTrue(showErrorValue)

            // Consumimos el restablecimiento a false si ocurre
            val resetValue = awaitItem()
            assertFalse(resetValue)

            expectNoEvents()
        }

        verify(vibratorHelper, atLeastOnce()).vibrateError()
    }

    // Funciones auxiliares para mockear tareas exitosas y fallidas

    private fun mockSuccessfulAuthTask(): Task<AuthResult> {
        val task = mock<Task<AuthResult>>()
        whenever(task.isSuccessful).thenReturn(true)
        whenever(task.result).thenReturn(mock())
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