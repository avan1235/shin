package `in`.procyk.shin

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import `in`.procyk.compose.camera.permission.rememberCameraPermissionState
import `in`.procyk.shin.component.ShinAppComponent
import `in`.procyk.shin.component.ShinAppComponent.Child
import `in`.procyk.shin.component.ShinAppComponent.MenuItem
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
import `in`.procyk.shin.ui.util.applyIf
import `in`.procyk.shin.ui.util.isEscDown
import kotlinx.coroutines.launch

@Composable
fun ShinApp(component: ShinAppComponent) {
    val permission = rememberCameraPermissionState()
    ShinTheme {
        Children(
            stack = component.stack,
            modifier = Modifier.fillMaxSize(),
            animation = stackAnimation(slide(orientation = Orientation.Vertical))
        ) { child ->
            NavigationDrawer(component, permission.isAvailable, child.instance.showTopMenu) {
                when (val instance = child.instance) {
                    is Child.Main -> MainScreen(instance.component, permission.isAvailable)
                    is Child.ScanQRCode -> ScanQRCodeScreen(instance.component, permission)
                    is Child.Favourites -> FavouritesScreen(instance.component)
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
    val activeMenuItem by component.activeMenuItem.subscribeAsState()
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
                    MenuItem.Main,
                    MenuItem.ScanQRCode.takeIf { isCameraAvailable },
                    MenuItem.Favourites,
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
        Scaffold(
            snackbarHost = { SnackbarHost(component.snackbarHostState) },
            modifier = Modifier
                .fillMaxSize()
                .applyIf(showTopMenu) { windowInsetsPadding(WindowInsets.safeContent) },
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
}

private inline val MenuItem.outlinedIcon: ImageVector
    get() = when (this) {
        MenuItem.Main -> Icons.Outlined.Home
        MenuItem.ScanQRCode -> Icons.Outlined.QrCodeScanner
        MenuItem.Favourites -> Icons.Outlined.Favorite
    }

private inline val MenuItem.filledIcon: ImageVector
    get() = when (this) {
        MenuItem.Main -> Icons.Filled.Home
        MenuItem.ScanQRCode -> Icons.Filled.QrCodeScanner
        MenuItem.Favourites -> Icons.Filled.Favorite
    }

private inline val MenuItem.presentableName: String
    get() = when (this) {
        MenuItem.Main -> "Home"
        MenuItem.ScanQRCode -> "Scan QR Code"
        MenuItem.Favourites -> "Favourites"
    }