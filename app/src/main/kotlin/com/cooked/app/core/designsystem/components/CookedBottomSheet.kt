package com.cooked.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.cooked.app.core.designsystem.CookedRadius
import com.cooked.app.core.designsystem.CookedShapes
import com.cooked.app.core.designsystem.CookedTheme

/**
 * CookedBottomSheet:
 * - 28dp top corner radius
 * - Tonal surfaceContainer background
 * - Custom drag handle
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookedBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = RoundedCornerShape(
            topStart = CookedRadius.Sheet,
            topEnd = CookedRadius.Sheet
        ),
        containerColor = CookedTheme.colors.surfaceContainer,
        contentColor = CookedTheme.colors.onSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(CookedTheme.colors.outline)
            )
        },
        content = content
    )
}

/**
 * Sheet layout content wrapper for standalone previews.
 */
@Composable
fun SheetPreviewContent(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(topStart = CookedRadius.Sheet, topEnd = CookedRadius.Sheet),
        color = CookedTheme.colors.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CookedTheme.spacing.xl24),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(CookedTheme.colors.outline)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = CookedTheme.typography.titleLarge,
                color = CookedTheme.colors.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@PreviewLightDark
@Preview(fontScale = 1.5f)
@Composable
private fun CookedBottomSheetPreview() {
    CookedTheme {
        SheetPreviewContent(title = "Quick Add") {
            PrimaryButton(
                text = "Save Reminder",
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

