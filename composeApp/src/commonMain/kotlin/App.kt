import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
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
            host = "0.0.0.0"
            port = 8080
            url { protocol = URLProtocol.HTTP }
        }
    }
    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
        ) {
            var response by remember<MutableState<String?>> { mutableStateOf(null) }
            ShortenRequest(client, onResponse = { response = it })

            AnimatedVisibility(
                visible = response != null,
                enter = expandVertically(expandFrom = Alignment.Top),
                exit = shrinkVertically(shrinkTowards = Alignment.Top),
            ) {
                Column {
                    Spacer(Modifier.height(16.dp))
                    response?.let { uri ->
                        val uriHandler = LocalUriHandler.current
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
                                    tag = "URL",
                                    annotation = uri,
                                    start = 0,
                                    end = uri.length
                                )
                            }
                        }
                        ClickableText(
                            text = annotatedString,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { position ->
                                annotatedString
                                    .getStringAnnotations("URL", position, position)
                                    .single()
                                    .let { uriHandler.openUri(it.item) }
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
        if (maxHeight > maxWidth) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(
                    space = space,
                    alignment = Alignment.CenterVertically,
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ShortenRequestElements(client, onResponse, fillMaxWidth = true)
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
                ShortenRequestElements(client, onResponse, fillMaxWidth = false)
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ShortenRequestElements(
    client: HttpClient,
    onResponse: (String) -> Unit,
    fillMaxWidth: Boolean,
) {
    var url by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    var shortenedProtocol by remember { mutableStateOf(ShortenedProtocol.entries.first()) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = shortenedProtocol.presentableName,
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
                        shortenedProtocol = protocol
                        expanded = false
                    })
            }
        }
    }
    OutlinedTextField(
        value = url,
        onValueChange = { url = it },
        modifier = Modifier.height(50.dp).applyIf(fillMaxWidth) { fillMaxWidth() },
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

private inline fun <T : Any> T.applyIf(condition: Boolean, action: T.() -> T): T = if (condition) action(this) else this
