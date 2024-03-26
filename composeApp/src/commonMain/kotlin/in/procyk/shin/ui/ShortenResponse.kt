package `in`.procyk.shin.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ShortenResponse(
    shortenedUrl: String?,
) {
    var lastShortenedUrl by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(shortenedUrl) {
        if (shortenedUrl != null) {
            lastShortenedUrl = shortenedUrl
        }
    }
    AnimatedVisibility(
        visible = shortenedUrl != null,
        enter = expandVertically(expandFrom = Alignment.Top),
        exit = shrinkVertically(shrinkTowards = Alignment.Top),
    ) {
        (shortenedUrl ?: lastShortenedUrl)?.let { url ->
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
                val annotatedString = remember(url, color) {
                    buildAnnotatedString {
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
                            tag = URL_TAG,
                            annotation = url,
                            start = 0,
                            end = url.length
                        )
                    }
                }
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
                            .getStringAnnotations(URL_TAG, position, position)
                            .single()
                            .let {
                                clipboardManager.setText(AnnotatedString(url))
                                localUriHandler.openUri(url)
                            }
                    }
                )
                OutlinedButton(
                    onClick = { clipboardManager.setText(AnnotatedString(url)) },
                ) {
                    Text("Copy to Clipboard")
                }
            }
        }
    }
}

private const val URL_TAG: String = "SHORT_URL_TAG"
