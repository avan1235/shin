package `in`.procyk.shin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.arkivanov.decompose.defaultComponentContext
import `in`.procyk.shin.component.ShinAppComponentContext
import `in`.procyk.shin.component.ShinComponentImpl

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContext = ShinAppComponentContext()
        val component = ShinComponentImpl(appContext, defaultComponentContext())
        setContent {
            ShinApp(component)
        }
    }
}
