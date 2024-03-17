package `in`.procyk.shin.ui

import RedirectType
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.arkivanov.decompose.value.Value
import `in`.procyk.compose.calendar.SelectableCalendar
import `in`.procyk.compose.calendar.rememberSelectableCalendarState
import `in`.procyk.compose.calendar.year.YearMonth
import `in`.procyk.shin.component.ShinComponent
import `in`.procyk.shin.model.ShortenedProtocol

@Composable
internal fun ShortenRequest(
    component: ShinComponent,
    maxWidth: Dp,
    isVertical: Boolean,
    space: Dp = 8.dp,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when {
            isVertical -> Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
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
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
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
        LazyRow(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        ) {
            item {
                HideableSettingsColumn(
                    name = "Expiration Date",
                    visible = component.expirationDateVisible,
                    modifier = Modifier.sizeIn(maxWidth = 250.dp),
                    onVisibleChange = component::onExpirationDateVisibleChange,
                ) {
                    val expirationDate by component.expirationDate.subscribeAsState()
                    val calendarState = rememberSelectableCalendarState(
                        initialMonth = YearMonth.now(),
                        minMonth = YearMonth.now(),
                        initialSelection = listOf(expirationDate),
                        confirmSelectionChange = { component.onExpirationDateChange(it.singleOrNull()) },
                    )
                    SelectableCalendar(calendarState = calendarState)
                }
            }
            item {
                HideableSettingsColumn(
                    name = "Redirect Type",
                    visible = component.redirectTypeVisible,
                    modifier = Modifier.width(width = 250.dp),
                    onVisibleChange = component::onRedirectTypeVisibleChange,
                ) {
                    val redirectType by component.redirectType.subscribeAsState()
                    EnumChooser(
                        label = "Redirect Type",
                        entries = RedirectType.entries,
                        value = redirectType,
                        onValueChange = component::onRedirectTypeChange,
                        presentableName = RedirectType::presentableName,
                    )
                }
            }
        }
    }
}

private inline val RedirectType.presentableName: String
    get() = when (this) {
        RedirectType.MovedPermanently -> "301 (Moved Permanently)"
        RedirectType.Found -> "302 (Moved Temporarily)"
    }

@Composable
private inline fun HideableSettingsColumn(
    name: String,
    visible: Value<Boolean>,
    noinline onVisibleChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    crossinline content: @Composable() () -> Unit,
) {
    Column(
        modifier = modifier
            .padding(top = 12.dp, start = 6.dp, end = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp, alignment = Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val contentVisible by visible.subscribeAsState()
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(name, fontSize = 16.sp)
            Switch(
                checked = contentVisible,
                onCheckedChange = onVisibleChange,
            )
        }
        val alpha by animateFloatAsState(if (contentVisible) 1f else 0f)
        Box(
            modifier = Modifier
                .alpha(alpha)
                .applyIf(!contentVisible) { height(1.dp) }
        ) {
            content()
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

    EnumChooser(
        label = "Protocol",
        entries = ShortenedProtocol.entries,
        value = protocol,
        onValueChange = component::onProtocolChange,
        presentableName = ShortenedProtocol::presentableName,
        fillMaxWidth = fillMaxWidth,
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
