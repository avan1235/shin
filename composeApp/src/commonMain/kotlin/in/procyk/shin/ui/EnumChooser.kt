package `in`.procyk.shin.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import applyIf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T> EnumChooser(
    entries: Iterable<T>,
    value: T,
    onValueChange: (T) -> Unit,
    presentableName: T.() -> String,
    label: String? = null,
    fillMaxWidth: Boolean = true,
    defaultWidth: Dp = 128.dp,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            readOnly = true,
            value = value.presentableName(),
            onValueChange = {},
            label = label?.let { { Text(label) } },
            modifier = Modifier
                .menuAnchor()
                .height(64.dp)
                .applyIf(fillMaxWidth) { fillMaxWidth() }
                .width(defaultWidth),
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
            entries.forEach { protocol ->
                DropdownMenuItem(
                    text = {
                        Text(protocol.presentableName())
                    },
                    onClick = {
                        onValueChange(protocol)
                        expanded = false
                    },
                )
            }
        }
    }
}
