package `in`.procyk.shin

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.procyk.compose.camera.permission.rememberCameraPermissionState
import `in`.procyk.compose.util.NoSystemBarsScreen
import `in`.procyk.shin.component.ShinAppComponent
import `in`.procyk.shin.component.ShinAppComponentContext
import `in`.procyk.shin.component.ShinAppViewModel
import `in`.procyk.shin.component.rememberShinCodec
import `in`.procyk.shin.ui.component.BottomBanner
import `in`.procyk.shin.ui.component.BottomBannerItem
import `in`.procyk.shin.ui.component.ShinBanner
import `in`.procyk.shin.ui.icons.Github
import `in`.procyk.shin.ui.icons.Html5
import `in`.procyk.shin.ui.icons.LinkedIn
import `in`.procyk.shin.ui.icons.ShinIcons
import `in`.procyk.shin.ui.screen.FavouritesScreen
import `in`.procyk.shin.ui.screen.MainScreen
import `in`.procyk.shin.ui.screen.ScanQRCodeScreen
import `in`.procyk.shin.ui.theme.ShinTheme
import `in`.procyk.shin.ui.theme.SystemBarsScreen
import `in`.procyk.shin.ui.util.applyIf
import `in`.procyk.shin.ui.util.isEscDown
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ShinApp() {
    val codec = rememberShinCodec()
    val vm = viewModel { ShinAppViewModel(codec) }
    val permission = rememberCameraPermissionState()
    val currentScreen by vm.currentScreen.collectAsState()
    ShinTheme {
        AnimatedContent(
            targetState = currentScreen,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                slideInVertically { it } togetherWith slideOutVertically { -it }
            },
        ) { screen ->
            NavigationDrawer(vm, permission.isAvailable, screen != ShinAppComponent.MenuItem.ScanQRCode) {
                when (screen) {
                    ShinAppComponent.MenuItem.Main ->
                        MainScreen(vm.mainComponent, vm.favouritesComponent, permission.isAvailable)
                    ShinAppComponent.MenuItem.ScanQRCode ->
                        ScanQRCodeScreen(vm.scanQRCodeComponent, permission)
                    ShinAppComponent.MenuItem.Favourites ->
                        FavouritesScreen(vm.favouritesComponent)
                }
            }
        }
    }
}

@Composable
private inline fun NavigationDrawer(
    component: ShinAppComponent,
    isCameraAvailable: Boolean,
    showTopMenu: Boolean,
    crossinline content: @Composable BoxScope.() -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val activeMenuItem by component.currentScreen.collectAsState()
    ModalNavigationDrawer(
        modifier = Modifier
            .onKeyEvent handle@{ event ->
                when {
                    drawerState.isOpen && event.isEscDown -> scope.launch { drawerState.close() }
                    else -> return@handle false
                }
                return@handle true
            },
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = DrawerDefaults.shape
                ) {
                    ShinBanner(
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                listOfNotNull(
                    ShinAppComponent.MenuItem.Main,
                    ShinAppComponent.MenuItem.ScanQRCode.takeIf { isCameraAvailable },
                    ShinAppComponent.MenuItem.Favourites,
                ).forEach { item ->
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                if (item == activeMenuItem) item.filledIcon else item.outlinedIcon,
                                contentDescription = "Menu item icon"
                            )
                        },
                        label = {
                            Text(item.presentableName)
                        },
                        selected = item == activeMenuItem,
                        onClick = {
                            scope
                                .launch { drawerState.close() }
                                .invokeOnCompletion { component.navigateTo(item) }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    )
                }
                Spacer(
                    modifier = Modifier.weight(1f)
                )
                BottomBanner(
                    title = "Find Me On",
                    BottomBannerItem("https://github.com/avan1235/", ShinIcons.Github),
                    BottomBannerItem("https://www.linkedin.com/in/maciej-procyk/", ShinIcons.LinkedIn),
                    BottomBannerItem("https://procyk.in", ShinIcons.Html5),
                )
            }
        },
    ) {
        if (showTopMenu) SystemBarsScreen {
            NavigationDrawerScaffold(component, showTopMenu, drawerState, keyboardController, scope, content)
        } else NoSystemBarsScreen {
            NavigationDrawerScaffold(component, showTopMenu, drawerState, keyboardController, scope, content)
        }
    }
}

@Composable
private inline fun NavigationDrawerScaffold(
    component: ShinAppComponent,
    showTopMenu: Boolean,
    drawerState: DrawerState,
    keyboardController: SoftwareKeyboardController?,
    scope: CoroutineScope,
    crossinline content: @Composable (BoxScope.() -> Unit)
) {
    Scaffold(
        snackbarHost = { SnackbarHost(component.snackbarHostState) },
        modifier = Modifier.fillMaxSize(),
    ) {
        if (showTopMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 48.dp),
                content = content,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                IconButton(
                    modifier = Modifier.align(Alignment.CenterStart),
                    onClick = {
                        scope.launch {
                            drawerState.run {
                                if (isClosed) {
                                    keyboardController?.hide()
                                    open()
                                } else {
                                    close()
                                }
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                content = content,
            )
        }
    }
}

private inline val ShinAppComponent.MenuItem.outlinedIcon: ImageVector
    get() = when (this) {
        ShinAppComponent.MenuItem.Main -> Icons.Outlined.Home
        ShinAppComponent.MenuItem.ScanQRCode -> Icons.Outlined.QrCodeScanner
        ShinAppComponent.MenuItem.Favourites -> Icons.Outlined.Favorite
    }

private inline val ShinAppComponent.MenuItem.filledIcon: ImageVector
    get() = when (this) {
        ShinAppComponent.MenuItem.Main -> Icons.Filled.Home
        ShinAppComponent.MenuItem.ScanQRCode -> Icons.Filled.QrCodeScanner
        ShinAppComponent.MenuItem.Favourites -> Icons.Filled.Favorite
    }

private inline val ShinAppComponent.MenuItem.presentableName: String
    get() = when (this) {
        ShinAppComponent.MenuItem.Main -> "Home"
        ShinAppComponent.MenuItem.ScanQRCode -> "Scan QR Code"
        ShinAppComponent.MenuItem.Favourites -> "Favourites"
    }
