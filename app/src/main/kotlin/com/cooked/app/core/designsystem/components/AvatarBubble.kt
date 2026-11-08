package com.cooked.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.cooked.app.core.designsystem.CookedTheme

/**
 * AvatarBubble:
 * - Circular avatar bubble showing initials or ember flame icon
 * - Tap callback for opening Settings / Profile
 */
@Composable
fun AvatarBubble(
    modifier: Modifier = Modifier,
    initials: String? = null,
    size: Dp = 40.dp,
    onClick: (() -> Unit)? = null
) {
    val clickableModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Box(
        modifier = clickableModifier
            .size(size)
            .clip(CircleShape)
            .background(CookedTheme.colors.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (!initials.isNullOrBlank()) {
            Text(
                text = initials.take(2).uppercase(),
                style = CookedTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.4f).sp
                ),
                color = CookedTheme.colors.onPrimaryContainer
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.LocalFireDepartment,
                contentDescription = "Profile",
                tint = CookedTheme.colors.primary,
                modifier = Modifier.size(size * 0.6f)
            )
        }
    }
}

@PreviewLightDark
@Preview(fontScale = 1.5f)
@Composable
private fun AvatarBubblePreview() {
    CookedTheme {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarBubble(initials = "MK")
            AvatarBubble(initials = null)
            AvatarBubble(initials = "C", size = 48.dp)
        }
    }
}
