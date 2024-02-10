package `in`.procyk.shin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import applyIf

@Composable
internal fun ShortenResponse(shortenedUrl: String?) {
    AnimatedVisibility(
        visible = shortenedUrl != null,
        enter = expandVertically(expandFrom = Alignment.Top),
        exit = shrinkVertically(shrinkTowards = Alignment.Top),
    ) {
        shortenedUrl?.let {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically)
            ) {
                val uri by remember(it) { mutableStateOf(it.normalizeAsHttpUrl()) }
                Spacer(Modifier.height(16.dp))
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
                                clipboardManager.setText(AnnotatedString(uri))
                                localUriHandler.openUri(uri)
                            }
                    }
                )
                Spacer(Modifier.height(16.dp))
                QrCode(uri)
                Button(
                    onClick = { clipboardManager.setText(AnnotatedString(uri)) },
                ) {
                    Text("Copy to Clipboard")
                }
            }
        }
    }
}

private fun String.normalizeAsHttpUrl() = applyIf(!contains("://")) { "http://$this" }

private const val URL_TAG: String = "SHORT_URL_TAG"
