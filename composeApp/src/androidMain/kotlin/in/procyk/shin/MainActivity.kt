package `in`.procyk.shin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.arkivanov.decompose.defaultComponentContext
import `in`.procyk.shin.component.ShinAppComponentContext.Companion.rememberShinAppComponentContext
import `in`.procyk.shin.component.ShinAppComponentImpl
import `in`.procyk.shin.component.ShinStore
import `in`.procyk.shin.component.rememberShinCodec

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val codec = rememberShinCodec<ShinStore>()
            val appContext = rememberShinAppComponentContext(codec)
            val component = ShinAppComponentImpl(appContext, defaultComponentContext())
            ShinApp(component)
        }
    }
}
