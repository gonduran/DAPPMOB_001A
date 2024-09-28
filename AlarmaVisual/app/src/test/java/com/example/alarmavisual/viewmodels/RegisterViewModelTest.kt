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
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.*
import android.text.TextUtils
import org.mockito.Mockito.mockStatic

@ExperimentalCoroutinesApi
class RegisterViewModelTest {

    private lateinit var viewModel: RegisterViewModel
    private val auth: FirebaseAuth = mock()
    private val db: FirebaseFirestore = mock()
    private val vibratorHelper: VibratorHelper = mock()
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = RegisterViewModel(
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
    fun `register with empty fields triggers error`() = testScope.runTest {
        viewModel.onNameChange("")
        viewModel.onEmailChange("")
        viewModel.onPasswordChange("")
        viewModel.onConfirmPasswordChange("")

        viewModel.showError.test {
            viewModel.register()
            advanceUntilIdle()
            skipItems(1) // Omitimos el valor inicial

            val showErrorValue = awaitItem()
            assertTrue(showErrorValue)

            val resetValue = awaitItem()
            assertFalse(resetValue)

            expectNoEvents()
        }

        verify(vibratorHelper, atLeastOnce()).vibrateError()
    }

    @Test
    fun `register with invalid email triggers error`() = testScope.runTest {
        viewModel.onNameChange("Test User")
        viewModel.onEmailChange("invalidemail")
        viewModel.onPasswordChange("Password1!")
        viewModel.onConfirmPasswordChange("Password1!")

        viewModel.showError.test {
            viewModel.register()
            advanceUntilIdle()
            skipItems(1)

            val showErrorValue = awaitItem()
            assertTrue(showErrorValue)

            val resetValue = awaitItem()
            assertFalse(resetValue)

            expectNoEvents()
        }

        verify(vibratorHelper, atLeastOnce()).vibrateError()
    }

    @Test
    fun `register with weak password triggers error`() = testScope.runTest {
        viewModel.onNameChange("Test User")
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("weak")
        viewModel.onConfirmPasswordChange("weak")

        viewModel.showError.test {
            viewModel.register()
            advanceUntilIdle()
            skipItems(1)

            val showErrorValue = awaitItem()
            assertTrue(showErrorValue)

            val resetValue = awaitItem()
            assertFalse(resetValue)

            expectNoEvents()
        }

        verify(vibratorHelper, atLeastOnce()).vibrateError()
    }

    @Test
    fun `register with mismatched passwords triggers error`() = testScope.runTest {
        viewModel.onNameChange("Test User")
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("Password1!")
        viewModel.onConfirmPasswordChange("Password2!")

        viewModel.showError.test {
            viewModel.register()
            advanceUntilIdle()
            skipItems(1)

            val showErrorValue = awaitItem()
            assertTrue(showErrorValue)

            val resetValue = awaitItem()
            assertFalse(resetValue)

            expectNoEvents()
        }

        verify(vibratorHelper, atLeastOnce()).vibrateError()
    }

    @Test
    fun `register with existing email triggers error`() = testScope.runTest {
        val exception = FirebaseAuthUserCollisionException("ERROR_EMAIL_ALREADY_IN_USE", "Email already in use")

        // Mockear el método TextUtils.isEmpty()
        mockStatic(TextUtils::class.java).use { mockedTextUtils ->
            whenever(TextUtils.isEmpty(any())).thenAnswer { invocation ->
                val argument = invocation.arguments[0] as? String
                argument.isNullOrEmpty()
            }

            // Configurar el mock de FirebaseAuth para registro fallido
            val authResultTask = mockFailedAuthTask(exception)
            whenever(auth.createUserWithEmailAndPassword(any(), any())).thenReturn(authResultTask)

            viewModel.onNameChange("Test User")
            viewModel.onEmailChange("test@example.com")
            viewModel.onPasswordChange("Password1!")
            viewModel.onConfirmPasswordChange("Password1!")

            viewModel.showError.test {
                viewModel.register()
                advanceUntilIdle()
                skipItems(1)

                val showErrorValue = awaitItem()
                assertTrue(showErrorValue)

                val resetValue = awaitItem()
                assertFalse(resetValue)

                expectNoEvents()
            }

            verify(vibratorHelper, atLeastOnce()).vibrateError()
        }
    }

    @Test
    fun `register with valid data succeeds`() = testScope.runTest {
        // Configurar el mock de FirebaseAuth para registro exitoso
        val authResultTask = mockSuccessfulAuthTask()
        whenever(auth.createUserWithEmailAndPassword(any(), any())).thenReturn(authResultTask)

        val firebaseUser = mock<FirebaseUser>()
        whenever(auth.currentUser).thenReturn(firebaseUser)
        whenever(firebaseUser.uid).thenReturn("user123")

        // Mockear el envío de correo de verificación
        val emailVerificationTask = mockSuccessfulVoidTask()
        whenever(firebaseUser.sendEmailVerification()).thenReturn(emailVerificationTask)

        // Configurar el mock de FirebaseFirestore para guardar el usuario
        val documentReference = mock<DocumentReference>()
        val setTask = mockSuccessfulVoidTask()

        // Solucionar NullPointerException en Firestore
        whenever(db.collection(anyString())).thenReturn(mock())
        whenever(db.collection(anyString()).document(anyString())).thenReturn(documentReference)
        whenever(documentReference.set(any())).thenReturn(setTask)

        viewModel.onNameChange("Test User")
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("Password1!")
        viewModel.onConfirmPasswordChange("Password1!")

        viewModel.isRegistrationSuccessful.test {
            viewModel.register()
            advanceUntilIdle()
            skipItems(1) // Omitimos el valor inicial

            val isSuccess = awaitItem()
            assertTrue(isSuccess)

            expectNoEvents()
        }

        verify(vibratorHelper, never()).vibrateError()
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

    private fun mockSuccessfulVoidTask(): Task<Void> {
        val task = mock<Task<Void>>()
        whenever(task.isSuccessful).thenReturn(true)
        whenever(task.addOnCompleteListener(any())).thenAnswer { invocation ->
            val listener = invocation.arguments[0] as OnCompleteListener<Void>
            listener.onComplete(task)
            task
        }
        return task
    }
}