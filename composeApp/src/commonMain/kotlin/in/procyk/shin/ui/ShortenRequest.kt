package `in`.procyk.shin.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import applyIf
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import `in`.procyk.shin.component.ShinComponent
import `in`.procyk.shin.model.ShortenedProtocol
import io.ktor.client.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.datetime.Instant

@Composable
internal fun ShortenRequest(
    component: ShinComponent,
    maxWidth: Dp,
    isVertical: Boolean,
    space: Dp = 8.dp,
) {
    if (isVertical) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(
                space = space,
                alignment = Alignment.CenterVertically,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ShortenRequestElements(
                component = component,
                fillMaxWidth = true,
                maxTextFieldWidth = maxWidth / 2
            )
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                space = space,
                alignment = Alignment.CenterHorizontally,
            ),
            verticalAlignment = Alignment.Bottom,
        ) {
            ShortenRequestElements(
                component = component,
                fillMaxWidth = false,
                maxTextFieldWidth = maxWidth / 2
            )
        }
    }

}

@Composable
private fun ShortenRequestElements(
    component: ShinComponent,
    fillMaxWidth: Boolean,
    maxTextFieldWidth: Dp,
) {
    val url by component.url.subscribeAsState()
    val protocol by component.protocol.subscribeAsState()

    ProtocolChooser(
        protocol = protocol,
        onChange = component::onProtocolChange,
        fillMaxWidth = fillMaxWidth
    )
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        value = url,
        onValueChange = component::onUrlChange,
        label = { Text("URL") },
        modifier = Modifier
            .focusRequester(focusRequester)
            .height(64.dp)
            .applyIf(fillMaxWidth) { fillMaxWidth() }
            .applyIf(!fillMaxWidth) { widthIn(max = maxTextFieldWidth) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = {
                component.onShorten()
                keyboardController?.hide()
            }
        ),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
    )
    Button(
        modifier = Modifier
            .height(56.dp)
            .applyIf(fillMaxWidth) { fillMaxWidth() },
        shape = RoundedCornerShape(12.dp),
        onClick = component::onShorten,
    ) {
        Text(
            text = "Shorten",
            fontSize = 18.sp,
        )
    }
    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
            readOnly = true,
            value = protocol.presentableName,
            onValueChange = {},
            label = { Text("Protocol") },
            modifier = Modifier
                .menuAnchor()
                .height(64.dp)
                .applyIf(fillMaxWidth) { fillMaxWidth() }
                .width(128.dp),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            ShortenedProtocol.entries.forEach { protocol ->
                DropdownMenuItem(
                    text = {
                        Text(protocol.presentableName)
                    },
                    onClick = {
                        onChange(protocol)
                        expanded = false
                    })
            }
        }
    }
}
