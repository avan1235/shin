package `in`.procyk.shin.component

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.xxfast.kstore.Codec
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

interface ShinAppComponent : Component {

    val currentScreen: StateFlow<MenuItem>

    val canGoBack: StateFlow<Boolean>

    val mainComponent: MainComponent

    val favouritesComponent: FavouritesComponent

    val scanQRCodeComponent: ScanQRCodeComponent

    fun navigateTo(item: MenuItem)

    fun goBack()

    enum class MenuItem {
        Main,
        ScanQRCode,
        Favourites,
        ;
    }
}

class ShinAppViewModel(
    codec: Codec<ShinStore>,
) : ViewModel(), ShinAppComponent {

    private val _appContext = ShinAppComponentContext(codec)

    override val appContext: ShinAppComponentContext = _appContext

    override val snackbarHostState: SnackbarHostState = _appContext.snackbarHostState

    override fun toast(
        message: String,
        actionLabel: String?,
        withDismissAction: Boolean,
        duration: SnackbarDuration,
    ) {
        viewModelScope.launch {
            _appContext.snackbarHostState.showSnackbar(message, actionLabel, withDismissAction, duration)
        }
    }

    private val _currentScreen = MutableStateFlow(ShinAppComponent.MenuItem.Main)
    override val currentScreen: StateFlow<ShinAppComponent.MenuItem> = _currentScreen

    private val _previousScreen = MutableStateFlow<ShinAppComponent.MenuItem?>(null)
    override val canGoBack: StateFlow<Boolean> = _previousScreen
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    override val mainComponent: MainComponent = MainComponentImpl(
        appContext = _appContext,
        scope = viewModelScope,
        navigateOnScanQRCode = { navigateTo(ShinAppComponent.MenuItem.ScanQRCode) },
    )

    override val favouritesComponent: FavouritesComponent = FavouritesComponentImpl(
        appContext = _appContext,
        scope = viewModelScope,
    )

    override val scanQRCodeComponent: ScanQRCodeComponent = ScanQRCodeComponentImpl(
        appContext = _appContext,
        scope = viewModelScope,
        navigateOnCancel = { scanned ->
            goBack()
            scanned?.let { (mainComponent as MainComponentImpl).onUrlChange(it) }
        },
    )

    override fun navigateTo(item: ShinAppComponent.MenuItem) {
        when (item) {
            ShinAppComponent.MenuItem.Main -> {
                _currentScreen.value = ShinAppComponent.MenuItem.Main
                _previousScreen.value = null
            }
            ShinAppComponent.MenuItem.ScanQRCode -> {
                _previousScreen.value = _currentScreen.value
                _currentScreen.value = ShinAppComponent.MenuItem.ScanQRCode
            }
            ShinAppComponent.MenuItem.Favourites -> {
                _currentScreen.value = ShinAppComponent.MenuItem.Favourites
                _previousScreen.value = null
            }
        }
    }

    override fun goBack() {
        _previousScreen.value?.let { prev ->
            _currentScreen.value = prev
            _previousScreen.value = null
        }
    }
}
