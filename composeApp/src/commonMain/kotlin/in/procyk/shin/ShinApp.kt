package `in`.procyk.shin

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import `in`.procyk.compose.camera.permission.rememberCameraPermissionState
import `in`.procyk.shin.component.ShinAppComponent
import `in`.procyk.shin.component.ShinAppComponent.Child
import `in`.procyk.shin.component.ShinAppComponent.MenuItem
import `in`.procyk.shin.ui.component.ShinBanner
import `in`.procyk.shin.ui.icons.Github
import `in`.procyk.shin.ui.icons.Html5
import `in`.procyk.shin.ui.icons.LinkedIn
import `in`.procyk.shin.ui.icons.ShinIcons
import `in`.procyk.shin.ui.screen.FavouritesScreen
import `in`.procyk.shin.ui.screen.MainScreen
import `in`.procyk.shin.ui.screen.ScanQRCodeScreen
import `in`.procyk.shin.ui.theme.ShinTheme
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
            NavigationDrawer(component, permission.isAvailable) {
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
    crossinline content: @Composable BoxScope.() -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val activeMenuItem by component.activeMenuItem.subscribeAsState()
    ModalNavigationDrawer(
        modifier = Modifier
            .onKeyEvent { event ->
                (drawerState.isOpen && event.isEscDown).also { isConsumed ->
                    if (isConsumed) scope.launch { drawerState.close() }
                }
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
                ).forEach { item ->
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                when (item) {
                                    MenuItem.Main -> Icons.Outlined.Home
                                    MenuItem.ScanQRCode -> Icons.Outlined.QrCodeScanner
                                    MenuItem.Favourites -> Icons.Outlined.Favorite
                                },
                                contentDescription = "Menu item icon"
                            )
                        },
                        label = {
                            Text(
                                when (item) {
                                    MenuItem.Main -> "Home"
                                    MenuItem.ScanQRCode -> "Scan QR Code"
                                    MenuItem.Favourites -> "Favourites"
                                }
                            )
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Find Me On")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                    ) {
                        FindMeOn(ShinIcons.Github, "https://github.com/avan1235/", "Github")
                        FindMeOn(ShinIcons.LinkedIn, "https://www.linkedin.com/in/maciej-procyk/", "LinkedIn")
                        FindMeOn(ShinIcons.Html5, "https://procyk.in", "web")
                    }
                }
            }
        },
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(component.snackbarHostState) },
            modifier = Modifier.fillMaxSize(),
        ) { contentPadding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(contentPadding),
                content = content,
            )
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                IconButton(
                    modifier = Modifier.align(Alignment.TopStart),
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
        }
    }
}

@Composable
private inline fun FindMeOn(
    icon: ImageVector,
    url: String,
    name: String,
) {
    val uriHandler = LocalUriHandler.current
    IconButton(onClick = { uriHandler.openUri(url) }) {
        Icon(icon, "Find me on $name")
    }
}
