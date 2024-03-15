package `in`.procyk.shin.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import applyIf
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import `in`.procyk.compose.calendar.SelectableCalendar
import `in`.procyk.compose.calendar.rememberSelectableCalendarState
import `in`.procyk.compose.calendar.year.YearMonth
import `in`.procyk.shin.component.ShinComponent
import `in`.procyk.shin.model.ShortenedProtocol
import toNullable

@Composable
internal fun ShortenRequest(
    component: ShinComponent,
    maxWidth: Dp,
    isVertical: Boolean,
    space: Dp = 8.dp,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when {
            isVertical -> Column(
                modifier = Modifier.fillMaxWidth(),
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

            else -> Row(
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
        Spacer(Modifier.height(12.dp))
        ShortenRequestExtraElements(component)
    }
}

@Composable
private fun ShortenRequestExtraElements(
    component: ShinComponent,
) {
    val extraElementsVisible by component.extraElementsVisible.subscribeAsState()
    val rotation by animateFloatAsState(if (extraElementsVisible) 180f else 0f)
    OutlinedButton(onClick = component::onExtraElementsVisibleChange) {
        Text("Extra Options")
        Icon(
            imageVector = Icons.Filled.ArrowDropDown,
            modifier = Modifier.rotate(rotation),
            contentDescription = "Extra Options"
        )
    }
    VerticalAnimatedVisibility(extraElementsVisible) {
        Column(
            modifier = Modifier
                .padding(top = 12.dp)
                .sizeIn(maxWidth = 280.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp, alignment = Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val expirationDate by component.expirationDate.subscribeAsState()
            val expirationDateVisible by component.expirationDateVisible.subscribeAsState()
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Expiration Date", fontSize = 16.sp)
                Switch(
                    checked = expirationDateVisible,
                    onCheckedChange = component::onExpirationDateVisibleChange
                )
            }
            VerticalAnimatedVisibility(expirationDateVisible) {
                val calendarState = rememberSelectableCalendarState(
                    initialMonth = YearMonth.now(),
                    minMonth = YearMonth.now(),
                    initialSelection = listOf(expirationDate),
                    confirmSelectionChange = { component.onExpirationDateChange(it.singleOrNull()) },
                )
                SelectableCalendar(calendarState = calendarState)
            }
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

@Composable
private inline fun VerticalAnimatedVisibility(
    visible: Boolean,
    crossinline content: @Composable() AnimatedVisibilityScope.() -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
        exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
        content = { content() }
    )
}
