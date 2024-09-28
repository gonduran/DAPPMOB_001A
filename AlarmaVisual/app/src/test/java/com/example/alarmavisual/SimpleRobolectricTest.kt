import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SimpleRobolectricTest {

    @Test
    fun testAppContext() {
        val appContext = ApplicationProvider.getApplicationContext<Application>()
        assertNotNull(appContext)
    }
}