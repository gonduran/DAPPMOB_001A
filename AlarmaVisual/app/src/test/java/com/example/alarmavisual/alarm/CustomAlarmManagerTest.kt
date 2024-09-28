import android.content.Context
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.example.alarmavisual.alarm.CustomAlarmManager
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class CustomAlarmManagerTest {

    private lateinit var customAlarmManager: CustomAlarmManager

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var workManager: WorkManager

    @Mock
    private lateinit var firebaseAuth: FirebaseAuth

    @Mock
    private lateinit var firestore: FirebaseFirestore

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        customAlarmManager = CustomAlarmManager(context)
        FirebaseApp.initializeApp(context)
    }

    @Test
    fun testScheduleWidgetUpdateTask() {
        // Ejecutamos la tarea de programación del widget
        customAlarmManager.scheduleWidgetUpdateTask(context)

        // Verificamos si WorkManager ha sido llamado
        verify(workManager, times(1)).enqueue(any(WorkRequest::class.java))
    }
}