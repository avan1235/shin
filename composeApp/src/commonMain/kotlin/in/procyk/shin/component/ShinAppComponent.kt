package `in`.procyk.shin.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import `in`.procyk.shin.component.ShinAppComponent.Child
import `in`.procyk.shin.component.ShinAppComponent.MenuItem
import kotlinx.serialization.Serializable

interface ShinAppComponent : Component {

    val stack: Value<ChildStack<*, Child>>

    val activeMenuItem: Value<MenuItem>

    fun navigateTo(item: MenuItem)

    enum class MenuItem {
        Main,
        ScanQRCode,
        Favourites,
        ;
    }

    sealed class Child(val showTopMenu: Boolean) {
        class Main(val component: MainComponent) : Child(showTopMenu = true)
        class ScanQRCode(val component: ScanQRCodeComponent) : Child(showTopMenu = false)
        class Favourites(val component: FavouritesComponent) : Child(showTopMenu = true)
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

    override val activeMenuItem: Value<MenuItem> = stack.map {
        when (it.active.instance) {
            is Child.Favourites -> MenuItem.Favourites
            is Child.Main -> MenuItem.Main
            is Child.ScanQRCode -> MenuItem.ScanQRCode
        }
    }

    private fun child(config: Config, childComponentContext: ComponentContext): Child = when (config) {
        is Config.Main -> Child.Main(
            MainComponentImpl(
                appContext = appContext,
                componentContext = childComponentContext,
                navigateOnScanQRCode = { navigateTo(MenuItem.ScanQRCode) },
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

        is Config.Favourites -> Child.Favourites(
            FavouritesComponentImpl(
                appContext = appContext,
                componentContext = childComponentContext
            )
        )
    }

    override fun navigateTo(item: MenuItem) {
        when (item) {
            MenuItem.Main -> navigation.popTo(index = 0)
            MenuItem.ScanQRCode -> Config.ScanQRCode.let(navigation::pushToFront)
            MenuItem.Favourites -> Config.Favourites.let(navigation::pushToFront)
        }
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Main : Config

        @Serializable
        data object ScanQRCode : Config

        @Serializable
        data object Favourites : Config
    }
}
