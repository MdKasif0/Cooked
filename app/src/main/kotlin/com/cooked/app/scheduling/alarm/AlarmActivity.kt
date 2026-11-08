package com.cooked.app.scheduling.alarm

import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cooked.app.MainActivity
import com.cooked.app.core.designsystem.CookedTheme
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
import javax.inject.Inject

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
            CookedTheme {
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
        val copyText = if (currentItem != null) {
            NotificationCopy.getCopy(
                personality = personality,
                stage = NagStage.FINAL_CALL,
                title = currentItem.title,
                nagCount = nagCount
            )
        } else "Loading..."

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = currentItem?.emoji ?: "🚨",
                        fontSize = 72.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = currentItem?.title ?: "Reminder",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    if (!currentItem?.note.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentItem?.note ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = copyText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            scope.launch {
                                itemRepository.markDone(itemId)
                                onDone()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = "DONE (I DID IT)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    itemRepository.snooze(itemId, Instant.now().plus(Duration.ofMinutes(10)))
                                    currentItem?.let { alarmScheduler.schedule(it) }
                                    onSnooze(10)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Text("+10 min")
                        }

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    itemRepository.snooze(itemId, Instant.now().plus(Duration.ofHours(1)))
                                    currentItem?.let { alarmScheduler.schedule(it) }
                                    onSnooze(60)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Text("+1 hour")
                        }
                    }

                    OutlinedButton(
                        onClick = onOpenApp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Open Cooked")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
