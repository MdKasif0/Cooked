package com.cooked.app.scheduling.alarm

import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cooked.app.MainActivity
import com.cooked.app.core.designsystem.DarkBackground
import com.cooked.app.core.designsystem.DarkOnBackground
import com.cooked.app.core.designsystem.DarkOnPrimary
import com.cooked.app.core.designsystem.DarkOnPrimaryContainer
import com.cooked.app.core.designsystem.DarkOnSurfaceVariant
import com.cooked.app.core.designsystem.DarkOutline
import com.cooked.app.core.designsystem.DarkPrimary
import com.cooked.app.core.designsystem.DarkPrimaryContainer
import com.cooked.app.core.designsystem.DarkSurfaceContainer
import com.cooked.app.core.designsystem.HeatCookedDark
import com.cooked.app.core.designsystem.components.CookedCategoryIcon
import com.cooked.app.data.datastore.UserPreferencesRepository
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.Personality
import com.cooked.app.domain.repository.ItemRepository
import com.cooked.app.scheduling.notification.NagStage
import com.cooked.app.scheduling.notification.NotificationCopy
import com.cooked.app.scheduling.notification.ReminderNotificationBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class AlarmActivity : ComponentActivity() {

    @Inject
    lateinit var itemRepository: ItemRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        turnScreenOnAndUnlock()

        val itemId = intent.getStringExtra(ReminderNotificationBuilder.EXTRA_ITEM_ID)
        val notificationId = intent.getIntExtra(ReminderNotificationBuilder.EXTRA_NOTIFICATION_ID, -1)
        val nagCount = intent.getIntExtra(ReminderNotificationBuilder.EXTRA_NAG_COUNT, 5)

        if (itemId == null) {
            finish()
            return
        }

        setContent {
            AlarmScreen(
                itemId = itemId,
                nagCount = nagCount,
                onDone = {
                    val notificationManager = getSystemService(NotificationManager::class.java)
                    if (notificationId != -1) notificationManager?.cancel(notificationId)
                    alarmScheduler.cancel(itemId)
                    finish()
                },
                onSnooze = { minutes ->
                    val notificationManager = getSystemService(NotificationManager::class.java)
                    if (notificationId != -1) notificationManager?.cancel(notificationId)
                    finish()
                },
                onOpenApp = {
                    val mainIntent = Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra(ReminderNotificationBuilder.EXTRA_ITEM_ID, itemId)
                    }
                    startActivity(mainIntent)
                    finish()
                }
            )
        }
    }

    private fun turnScreenOnAndUnlock() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(KeyguardManager::class.java)
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }

    @Composable
    private fun AlarmScreen(
        itemId: String,
        nagCount: Int,
        onDone: () -> Unit,
        onSnooze: (Int) -> Unit,
        onOpenApp: () -> Unit
    ) {
        var item by remember { mutableStateOf<Item?>(null) }
        var personality by remember { mutableStateOf(Personality.SPICY) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(itemId) {
            item = itemRepository.getItem(itemId)
            personality = userPreferencesRepository.personality.first()
        }

        val currentItem = item
        val effectivePersonality = currentItem?.nag?.personalityOverride ?: personality
        val copyText = if (currentItem != null) {
            NotificationCopy.getCopy(
                personality = effectivePersonality,
                stage = NagStage.FINAL_CALL,
                title = currentItem.title,
                nagCount = nagCount
            )
        } else "Loading..."

        // Slide to dismiss state
        var offsetX by remember { mutableFloatStateOf(0f) }
        val maxSwipeThreshold = 350f

        val performSnooze10m = {
            scope.launch {
                val until = Instant.now().plus(Duration.ofMinutes(10))
                itemRepository.snooze(itemId, until)
                currentItem?.let { alarmScheduler.schedule(it.copy(snoozedUntil = until)) }
                onSnooze(10)
            }
        }

        // Back button counts as snooze 10m
        BackHandler {
            performSnooze10m()
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DarkBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 40.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top header / Flame badge
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    CookedCategoryIcon(
                        keyOrEmoji = currentItem?.emoji ?: "flame",
                        size = 80.dp,
                        tint = DarkPrimary
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = currentItem?.title ?: "Reminder",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkOnBackground,
                            lineHeight = 38.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    if (!currentItem?.note.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = currentItem?.note ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            color = DarkOnSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Calm notification message pill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = DarkSurfaceContainer,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = copyText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Bottom Action buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Big "Done" button
                    Button(
                        onClick = {
                            scope.launch {
                                itemRepository.markDone(itemId)
                                onDone()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkPrimary,
                            contentColor = DarkOnPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "DONE",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )
                    }

                    // Secondary "+10m" and "Tomorrow" buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { performSnooze10m() },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = DarkOnBackground
                            )
                        ) {
                            Text(
                                text = "+10m",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    val zone = ZoneId.systemDefault()
                                    val tomorrow9am = LocalDate.now(zone).plusDays(1)
                                        .atTime(9, 0)
                                        .atZone(zone)
                                        .toInstant()
                                    itemRepository.snooze(itemId, tomorrow9am)
                                    currentItem?.let { alarmScheduler.schedule(it.copy(snoozedUntil = tomorrow9am)) }
                                    onSnooze(24 * 60)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = DarkOnBackground
                            )
                        ) {
                            Text(
                                text = "Tomorrow",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }

                    // Slide to dismiss bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .background(DarkSurfaceContainer, RoundedCornerShape(26.dp))
                            .draggable(
                                orientation = Orientation.Horizontal,
                                state = rememberDraggableState { delta ->
                                    offsetX = (offsetX + delta).coerceIn(0f, maxSwipeThreshold)
                                },
                                onDragStopped = {
                                    if (offsetX >= maxSwipeThreshold * 0.75f) {
                                        performSnooze10m()
                                    } else {
                                        offsetX = 0f
                                    }
                                }
                            )
                            .padding(4.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "Slide to snooze 10m →",
                            style = MaterialTheme.typography.labelMedium,
                            color = DarkOnSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        Box(
                            modifier = Modifier
                                .offset { IntOffset(offsetX.roundToInt(), 0) }
                                .size(44.dp)
                                .background(DarkPrimaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            CookedCategoryIcon(
                                keyOrEmoji = "routine",
                                size = 22.dp,
                                tint = DarkOnPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
