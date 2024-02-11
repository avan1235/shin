package `in`.procyk.shin.ui

import Shorten
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import applyIf
import `in`.procyk.shin.model.ShortenedProtocol
import io.ktor.client.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
internal fun ShortenRequest(
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
            onDone = { client.askForShortenedUrl(scope, url, shortenedProtocol, onResponse) }
        ),
        singleLine = true,
    )
    Button(
        modifier = Modifier.height(50.dp).applyIf(fillMaxWidth) { fillMaxWidth() },
        onClick = { client.askForShortenedUrl(scope, url, shortenedProtocol, onResponse) }
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
            modifier = Modifier
                .height(50.dp)
                .applyIf(fillMaxWidth) { fillMaxWidth() }
                .width(128.dp),
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

private fun normalizeInputUrl(url: String, protocol: ShortenedProtocol): String {
    val protocolDelimiter = "://"
    val idx = url.indexOf(protocolDelimiter)
    val noProtocolUrl = if (idx != -1) url.drop(idx + protocolDelimiter.length) else url
    return protocol.presentableName + protocolDelimiter + noProtocolUrl
}

private fun HttpClient.askForShortenedUrl(
    scope: CoroutineScope,
    url: String,
    shortenedProtocol: ShortenedProtocol,
    onResponse: (String) -> Unit,
): Job = scope.launch {
    try {
        post<Shorten>(Shorten(normalizeInputUrl(url, shortenedProtocol)))
            .takeIf { it.status == HttpStatusCode.OK }
            ?.bodyAsText()
            ?.let(onResponse)
    } catch (_: Exception) {
    }
}
