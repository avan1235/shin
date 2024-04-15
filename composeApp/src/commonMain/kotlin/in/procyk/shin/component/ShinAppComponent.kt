package `in`.procyk.shin.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import `in`.procyk.shin.component.ShinAppComponent.Child
import kotlinx.serialization.Serializable

interface ShinAppComponent : Component {

    val stack: Value<ChildStack<*, Child>>

    fun onBackClicked(toIndex: Int)

    sealed class Child {
        class Main(val component: MainComponent) : Child()
        class ScanQRCode(val component: ScanQRCodeComponent) : Child()
    }
}

class ShinAppComponentImpl(
    appContext: ShinAppComponentContext,
    componentContext: ComponentContext,
) : AbstractComponent(appContext, componentContext), ShinAppComponent {
    private val navigation: StackNavigation<Config> = StackNavigation()

    override val stack: Value<ChildStack<*, Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Main,
        handleBackButton = true,
        childFactory = ::child,
    )

    private fun child(config: Config, childComponentContext: ComponentContext): Child = when (config) {
        is Config.Main -> Child.Main(
            MainComponentImpl(
                appContext = appContext,
                componentContext = childComponentContext,
                navigateOnScanQRCode = { navigation.push(Config.ScanQRCode) },
            )
        )

        is Config.ScanQRCode -> Child.ScanQRCode(
            ScanQRCodeComponentImpl(
                appContext = appContext,
                componentContext = childComponentContext,
                navigateOnCancel = { scanned ->
                    navigation.pop {
                        scanned?.let { (stack.active.instance as? Child.Main)?.component?.onUrlChange(it) }
                    }
                },
            )
        )
    }


    override fun onBackClicked(toIndex: Int) {
        navigation.popTo(index = toIndex)
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Main : Config

        @Serializable
        data object ScanQRCode : Config
    }
}
