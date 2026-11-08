package com.cooked.app.feature.snitch

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cooked.app.core.designsystem.CookedRadius
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.HeatLevel
import com.cooked.app.core.designsystem.components.EggMascot
import com.cooked.app.core.designsystem.components.GhostButton
import com.cooked.app.core.designsystem.components.PrimaryButton
import com.cooked.app.core.designsystem.components.SecondaryButton
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.Personality
import com.cooked.app.domain.snitch.SnitchManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnitchBottomSheet(
    item: Item,
    snoozeCount: Int,
    personality: Personality,
    contactName: String,
    preferredApp: String,
    onDismiss: () -> Unit,
    onSnitchSent: (String) -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current
    val message = SnitchManager.buildSnitchMessage(
        personality = personality,
        contactName = contactName,
        itemTitle = item.title,
        snoozeCount = snoozeCount
    )

    val headline = when (personality) {
        Personality.MILD -> "Want a friend to keep you honest?"
        Personality.SPICY -> "Time to snitch? You've snoozed $snoozeCount times."
        Personality.UNHINGED -> "Accountability intervention: roast incoming."
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CookedTheme.colors.surface,
        shape = RoundedCornerShape(topStart = CookedRadius.Sheet, topEnd = CookedRadius.Sheet),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CookedTheme.spacing.screenHorizontal)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EggMascot(
                level = HeatLevel.COOKED,
                size = 72.dp,
                animated = true
            )

            Text(
                text = headline,
                style = CookedTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = CookedTheme.colors.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = "You've snoozed '${item.title}' $snoozeCount times. Cooked won't send anything automatically—you tap send.",
                style = CookedTheme.typography.bodyMedium,
                color = CookedTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // Preview Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(CookedRadius.Card))
                    .background(CookedTheme.colors.surfaceContainer)
                    .padding(14.dp)
            ) {
                Text(
                    text = "MESSAGE PREVIEW",
                    style = CookedTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = CookedTheme.colors.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message,
                    style = CookedTheme.typography.bodySmall,
                    color = CookedTheme.colors.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action 1: Send to Buddy
            PrimaryButton(
                text = if (preferredApp == "whatsapp") "Send via WhatsApp 💬" else "Share with Friend 📤",
                onClick = {
                    val shareIntent = SnitchManager.createShareIntent(context, message, preferredApp)
                    context.startActivity(shareIntent)
                    onSnitchSent(preferredApp)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Action 2: Share Card to Story
            SecondaryButton(
                text = "Snitch Yourself Publicly (Story Card) 📸",
                onClick = {
                    val cardUri = SnitchManager.createStoryCardUri(context, item.title, snoozeCount)
                    if (cardUri != null) {
                        val storyIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "image/png"
                            putExtra(Intent.EXTRA_STREAM, cardUri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(storyIntent, "Share Snitch Card"))
                        onSnitchSent("story")
                    }
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            )

            GhostButton(
                text = "I'll do it right now, don't snitch",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

