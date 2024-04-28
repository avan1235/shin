package `in`.procyk.shin.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import `in`.procyk.shin.component.MainComponent
import `in`.procyk.shin.shared.Option
import `in`.procyk.shin.shared.toNullable

@Composable
internal fun ShortenResponse(
    component: MainComponent,
) {
    val fullUrl by component.fullUrl.subscribeAsState()
    val shortenedUrl by component.shortenedUrl.subscribeAsState()
    var lastShortenedUrl by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(shortenedUrl) {
        (shortenedUrl as? Option.Some)?.let { lastShortenedUrl = it.value }
    }
    AnimatedVisibility(
        visible = shortenedUrl is Option.Some,
        enter = expandVertically(expandFrom = Alignment.Top),
        exit = shrinkVertically(shrinkTowards = Alignment.Top),
    ) {
        (shortenedUrl.toNullable() ?: lastShortenedUrl)?.let { url ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically),
                modifier = Modifier.padding(vertical = 32.dp)
            ) {
                QrCode(
                    url = url,
                    modifier = Modifier.size(256.dp).aspectRatio(1f),
                )
                val color = MaterialTheme.colorScheme.primary
                val annotatedString = remember(url, color) { createUrlAnnotatedString(url, color) }
                val clipboardManager = LocalClipboardManager.current
                val localUriHandler = LocalUriHandler.current
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
                            .getStringAnnotations(UrlTag, position, position)
                            .single()
                            .let {
                                clipboardManager.setText(AnnotatedString(url))
                                localUriHandler.openUri(url)
                            }
                    }
                )
                Row {
                    val checked by component.favourites.isFavourite(fullUrl, url).subscribeAsState()
                    IconToggleButton(
                        checked = checked,
                        onCheckedChange = {
                            when {
                                checked -> component.favourites.deleteFavourite(fullUrl)
                                else -> component.favourites.overwriteFavourite(fullUrl, url)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (checked) Icons.Outlined.Star else Icons.Outlined.StarOutline,
                            contentDescription = "Add to favourites"
                        )
                    }
                    IconButton(
                        onClick = {
                            component.toast("Copied '$url' to clipboard")
                            clipboardManager.setText(AnnotatedString(url)) },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy to clipboard"
                        )
                    }
                }
            }
        }
    }
}

private fun createUrlAnnotatedString(url: String, color: Color): AnnotatedString = buildAnnotatedString {
    append(url)
    addStyle(
        style = SpanStyle(
            color = color,
            fontSize = 18.sp,
        ),
        start = 0,
        end = url.length,
    )
    addStyle(
        style = ParagraphStyle(textAlign = TextAlign.Center),
        start = 0,
        end = url.length,
    )
    addStringAnnotation(
        tag = UrlTag,
        annotation = url,
        start = 0,
        end = url.length
    )
}

private const val UrlTag: String = "SHORT_URL_TAG"
