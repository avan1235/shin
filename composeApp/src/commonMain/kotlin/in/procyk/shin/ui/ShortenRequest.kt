package `in`.procyk.shin.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.value.Value
import `in`.procyk.compose.calendar.SelectableCalendar
import `in`.procyk.compose.calendar.rememberSelectableCalendarState
import `in`.procyk.compose.calendar.year.YearMonth
import `in`.procyk.shin.component.MainComponent
import `in`.procyk.shin.model.ShortenedProtocol
import `in`.procyk.shin.shared.RedirectType
import `in`.procyk.shin.shared.applyIf

internal fun LazyListScope.ShortenRequestItems(
    component: MainComponent,
    maxWidth: Dp,
    isVertical: Boolean,
    isCameraAvailable: Boolean,
    space: Dp = 8.dp,
) {
    item {
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
                    isCameraAvailable = isCameraAvailable,
                    maxTextFieldWidth = maxWidth / 2,
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
                    isCameraAvailable = isCameraAvailable,
                    maxTextFieldWidth = maxWidth / 2,
                )
            }
        }
    }
    item {
        ShortenRequestExtraElements(component, isVertical)
    }
}

@Composable
private fun ShortenRequestExtraElements(
    component: MainComponent,
    isVertical: Boolean,
) {
    val extraElementsVisible by component.extraElementsVisible.subscribeAsState()
    val rotation by animateFloatAsState(if (extraElementsVisible) 180f else 0f)
    OutlinedButton(onClick = component::onExtraElementsVisibleChange) {
        Text("Extra Options")
        Icon(
            imageVector = Icons.Filled.ArrowDropDown,
            modifier = Modifier.rotate(rotation),
            contentDescription = "Extra Options",
        )
    }
    VerticalAnimatedVisibility(extraElementsVisible) {
        when {
            isVertical -> Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ExpandableSettings(component, isVertical)
            }

            else -> Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            ) {
                ExpandableSettings(component, isVertical)
            }
        }
    }
}

@Composable
private fun ExpandableSettings(
    component: MainComponent,
    isVertical: Boolean,
) {
    ExpandableSetting(
        name = "Custom Prefix",
        visible = component.customPrefixVisible,
        isVertical = isVertical,
        fillMaxWidth = true,
        onVisibleChange = component::onCustomPrefixVisibleChange,
    ) {
        val customPrefix by component.customPrefix.subscribeAsState()
        ShinTextField(
            value = customPrefix,
            label = "Prefix",
            onValueChange = component::onCustomPrefixChange,
            modifier = Modifier.fillMaxWidth(),
        )
    }
    ExpandableSetting(
        name = "Expiration Date",
        visible = component.expirationDateVisible,
        isVertical = isVertical,
        fillMaxWidth = false,
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
    ExpandableSetting(
        name = "Redirect Type",
        visible = component.redirectTypeVisible,
        isVertical = isVertical,
        fillMaxWidth = true,
        onVisibleChange = component::onRedirectTypeVisibleChange,
    ) {
        val redirectType by component.redirectType.subscribeAsState()
        EnumChooser(
            entries = RedirectType.entries,
            value = redirectType,
            onValueChange = component::onRedirectTypeChange,
            presentableName = RedirectType::presentableName,
        )
    }
}

private inline val RedirectType.presentableName: String
    get() = when (this) {
        RedirectType.MovedPermanently -> "301 (Moved Permanently)"
        RedirectType.Found -> "302 (Moved Temporarily)"
    }

@Composable
private inline fun ExpandableSetting(
    name: String,
    visible: Value<Boolean>,
    isVertical: Boolean,
    fillMaxWidth: Boolean,
    noinline onVisibleChange: (Boolean) -> Unit,
    crossinline content:
    @Composable
        () -> Unit,
) {
    Column(
        modifier = when {
            isVertical -> Modifier.fillMaxWidth()
            else -> Modifier.sizeIn(maxWidth = 270.dp)
        },
        verticalArrangement = Arrangement.spacedBy(4.dp, alignment = Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val contentVisible by visible.subscribeAsState()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = when {
                isVertical -> Arrangement.SpaceBetween
                else -> Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            },
            verticalAlignment = Alignment.CenterVertically,
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
                .applyIf(fillMaxWidth && isVertical) { fillMaxWidth() }
                .applyIf(!fillMaxWidth || !isVertical) { sizeIn(maxWidth = 270.dp) }
                .applyIf(!contentVisible) { height(1.dp) },
        ) {
            content()
        }
    }
}

@Composable
private fun ShortenRequestElements(
    component: MainComponent,
    fillMaxWidth: Boolean,
    isCameraAvailable: Boolean,
    maxTextFieldWidth: Dp,
) {
    val url by component.url.subscribeAsState()
    val protocol by component.protocol.subscribeAsState()

    EnumChooser(
        entries = ShortenedProtocol.entries,
        value = protocol,
        onValueChange = component::onProtocolChange,
        presentableName = ShortenedProtocol::presentableName,
        label = "Protocol",
        fillMaxWidth = fillMaxWidth,
    )
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    Row(
        modifier = (Modifier as Modifier)
            .applyIf(fillMaxWidth) { fillMaxWidth() }
            .applyIf(!fillMaxWidth) { widthIn(max = maxTextFieldWidth) },
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        ShinTextField(
            value = url,
            onValueChange = component::onUrlChange,
            label = "URL",
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrect = false,
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    component.onShorten()
                    keyboardController?.hide()
                },
            ),
        )
        if (isCameraAvailable) {
            FilledTonalIconButton(
                modifier = Modifier.size(56.dp),
                onClick = component::onScanQRCode,
                shape = RoundedCornerShape(12.dp),
                enabled = true,
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Scan QR Code",
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
    ShinTextButton(
        text = "Shorten",
        fillMaxWidth = fillMaxWidth,
        onClick = component::onShorten,
    )
    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
    }
}

@Composable
private inline fun VerticalAnimatedVisibility(
    visible: Boolean,
    crossinline content:
    @Composable()
    AnimatedVisibilityScope.() -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
        exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
        content = { content() },
    )
}
