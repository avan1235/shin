import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.launch

@Composable
fun App() {
    val client = HttpClient {
        install(Resources)
        defaultRequest {
            host = "api-shorten-kotlin.koyeb.app"
            port = 443
            url { protocol = URLProtocol.HTTPS }
        }
    }
    MaterialTheme {
        var response by remember<MutableState<String?>> { mutableStateOf(null) }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .onKeyEvent { event ->
                    if (event.run { key == Key.Escape && type == KeyEventType.KeyDown }) {
                        response = null
                        true
                    } else false
                },
            verticalArrangement = Arrangement.Center,
        ) {
            ShortenRequest(client, onResponse = { response = it })

            AnimatedVisibility(
                visible = response != null,
                enter = expandVertically(expandFrom = Alignment.Top),
                exit = shrinkVertically(shrinkTowards = Alignment.Top),
            ) {
                Column {
                    Spacer(Modifier.height(16.dp))
                    response?.let { uri ->
                        val color = MaterialTheme.colors.primary
                        val annotatedString = remember(uri, color) {
                            buildAnnotatedString {
                                append(uri)
                                addStyle(
                                    style = SpanStyle(
                                        color = color,
                                        fontSize = 18.sp,
                                    ),
                                    start = 0,
                                    end = uri.length,
                                )
                                addStyle(
                                    style = ParagraphStyle(textAlign = TextAlign.Center),
                                    start = 0,
                                    end = uri.length,
                                )
                                addStringAnnotation(
                                    tag = URL_TAG,
                                    annotation = uri,
                                    start = 0,
                                    end = uri.length
                                )
                            }
                        }
                        val clipboardManager = LocalClipboardManager.current
                        val interactionSource = remember { MutableInteractionSource() }
                        val isHovered by interactionSource.collectIsHoveredAsState()
                        ClickableText(
                            text = annotatedString,
                            modifier = Modifier
                                .fillMaxWidth()
                                .hoverable(interactionSource),
                            style = if (isHovered) TextStyle(textDecoration = TextDecoration.Underline) else TextStyle.Default,
                            onClick = { position ->
                                annotatedString
                                    .getStringAnnotations(URL_TAG, position, position)
                                    .single()
                                    .let {
                                        val url = it.item.normalizeAsHttpUrl()
                                        clipboardManager.setText(AnnotatedString(url))
                                    }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShortenRequest(
    client: HttpClient,
    onResponse: (String) -> Unit,
    space: Dp = 8.dp,
) {
    BoxWithConstraints {
        val maxWidth = maxWidth
        if (maxHeight > maxWidth) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(
                    space = space,
                    alignment = Alignment.CenterVertically,
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ShortenRequestElements(client, onResponse, fillMaxWidth = true, maxTextFieldWidth = maxWidth / 2)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    space = space,
                    alignment = Alignment.CenterHorizontally,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ShortenRequestElements(client, onResponse, fillMaxWidth = false, maxTextFieldWidth = maxWidth / 2)
            }
        }
    }
}

@Composable
private fun ShortenRequestElements(
    client: HttpClient,
    onResponse: (String) -> Unit,
    fillMaxWidth: Boolean,
    maxTextFieldWidth: Dp,
) {
    var url by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var shortenedProtocol by remember { mutableStateOf(ShortenedProtocol.entries.first()) }

    ProtocolChooser(
        protocol = shortenedProtocol,
        onChange = { shortenedProtocol = it },
        fillMaxWidth = fillMaxWidth
    )
    val focusRequester = remember { FocusRequester() }
    OutlinedTextField(
        value = url,
        onValueChange = { url = it },
        modifier = Modifier
            .focusRequester(focusRequester)
            .height(50.dp)
            .applyIf(fillMaxWidth) { fillMaxWidth() }
            .applyIf(!fillMaxWidth) { widthIn(max = maxTextFieldWidth) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = {
                scope.launch {
                    client.post<Shorten>(Shorten(normalizeUrl(url, shortenedProtocol)))
                        .takeIf { it.status == HttpStatusCode.OK }
                        ?.bodyAsText()
                        ?.let(onResponse)
                }
            }
        ),
        singleLine = true,
    )
    Button(
        modifier = Modifier.height(50.dp).applyIf(fillMaxWidth) { fillMaxWidth() },
        onClick = {
            scope.launch {
                client.post<Shorten>(Shorten(normalizeUrl(url, shortenedProtocol)))
                    .takeIf { it.status == HttpStatusCode.OK }
                    ?.bodyAsText()
                    ?.let(onResponse)
            }
        }
    ) {
        Text(text = "Shorten")
    }
    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ProtocolChooser(
    protocol: ShortenedProtocol,
    onChange: (ShortenedProtocol) -> Unit,
    fillMaxWidth: Boolean,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = protocol.presentableName,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.height(50.dp).applyIf(fillMaxWidth) { fillMaxWidth() }.width(128.dp),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ShortenedProtocol.entries.forEach { protocol ->
                DropdownMenuItem(
                    content = { Text(protocol.presentableName) },
                    onClick = {
                        onChange(protocol)
                        expanded = false
                    })
            }
        }
    }
}

private fun normalizeUrl(url: String, protocol: ShortenedProtocol): String {
    val protocolDelimiter = "://"
    val idx = url.indexOf(protocolDelimiter)
    val noProtocolUrl = if (idx != -1) url.drop(idx + protocolDelimiter.length) else url
    return protocol.presentableName + protocolDelimiter + noProtocolUrl
}

private enum class ShortenedProtocol(val presentableName: String) {
    HTTPS("https"),
    HTTP("http"),
    ;
}

private fun String.normalizeAsHttpUrl() = applyIf(!contains("://")) { "http://$this" }

private const val URL_TAG: String = "SHORT_URL_TAG"

private inline fun <T : Any> T.applyIf(condition: Boolean, action: T.() -> T): T = if (condition) action(this) else this
