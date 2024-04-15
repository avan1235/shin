package `in`.procyk.shin.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.procyk.shin.shared.applyIf

@Composable
internal fun ShinTextButton(
    text: String,
    fillMaxWidth: Boolean = false,
    onClick: () -> Unit,
) {
    Button(
        modifier = Modifier
            .height(56.dp)
            .applyIf(fillMaxWidth) { fillMaxWidth() },
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
        )
    }
}
