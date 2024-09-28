import com.google.firebase.FirebaseApp
import org.junit.Before
import org.junit.Test
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.runner.RunWith
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.alarmavisual.alarm.CustomAlarmManager
import org.mockito.Mockito.mock

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class HomeMenuScreenTest {

    private lateinit var context: Context
    private lateinit var alarmManager: CustomAlarmManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        FirebaseApp.initializeApp(context) // Inicializar Firebase
        alarmManager = mock(CustomAlarmManager::class.java)
    }

    @Test
    fun testWidgetUpdateTaskIsScheduled() {
        // Aquí van las pruebas para verificar la llamada a scheduleWidgetUpdateTask
        alarmManager.scheduleWidgetUpdateTask(context)
        // Validar el comportamiento esperado
    }
}