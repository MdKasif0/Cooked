package com.cooked.app.feature.cook

import android.os.SystemClock
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cooked.app.core.designsystem.CookedRadius
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.FontFeatureSettingsTabular
import com.cooked.app.core.designsystem.HeatLevel
import com.cooked.app.core.designsystem.components.CookedCard
import com.cooked.app.core.designsystem.components.EggMascot
import com.cooked.app.core.designsystem.components.HeatBar
import com.cooked.app.core.designsystem.components.PrimaryButton
import com.cooked.app.domain.model.Item
import com.cooked.app.scheduling.notification.LiveCountdownNotificationManager
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookSessionScreen(
    item: Item,
    onFinishSession: (sessionMinutes: Int, newProgress: Int) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedMinutes by remember { mutableIntStateOf(25) }
    var isRunning by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var secondsRemaining by remember { mutableIntStateOf(25 * 60) }
    var elapsedMinutesCooked by remember { mutableIntStateOf(0) }

    var showFinishSheet by remember { mutableStateOf(false) }
    var progressSliderValue by remember { mutableFloatStateOf(item.progress.toFloat()) }

    // Tick countdown loop when running and not paused
    LaunchedEffect(isRunning, isPaused) {
        if (isRunning && !isPaused) {
            val finishEpochMillis = System.currentTimeMillis() + (secondsRemaining * 1000L)
            LiveCountdownNotificationManager.showCookSessionCountdown(
                context = context,
                itemTitle = item.title,
                finishEpochMillis = finishEpochMillis
            )

            while (secondsRemaining > 0 && isRunning && !isPaused) {
                delay(1000L)
                secondsRemaining--
                if ((selectedMinutes * 60 - secondsRemaining) % 60 == 0) {
                    elapsedMinutesCooked = (selectedMinutes * 60 - secondsRemaining) / 60
                }
            }

            if (secondsRemaining <= 0) {
                isRunning = false
                elapsedMinutesCooked = selectedMinutes
                showFinishSheet = true
                LiveCountdownNotificationManager.cancelCookSessionCountdown(context)
            }
        } else if (isPaused) {
            LiveCountdownNotificationManager.cancelCookSessionCountdown(context)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            LiveCountdownNotificationManager.cancelCookSessionCountdown(context)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CookedTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cook Session",
                        style = CookedTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = CookedTheme.colors.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isRunning) {
                            showFinishSheet = true
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = CookedTheme.colors.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CookedTheme.colors.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = item.title,
                    style = CookedTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = CookedTheme.colors.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isRunning) "Stay in the zone · No multitasking" else "Pick duration to start cooking",
                    style = CookedTheme.typography.bodySmall,
                    color = CookedTheme.colors.onSurfaceVariant
                )
            }

            // Center: Mascot & Large Countdown
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                EggMascot(
                    level = if (isRunning) HeatLevel.WARM else HeatLevel.CHILL,
                    size = 120.dp,
                    animated = true
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Big Tabular Timer
                val mins = secondsRemaining / 60
                val secs = secondsRemaining % 60
                val timerString = String.format("%02d:%02d", mins, secs)

                Text(
                    text = timerString,
                    style = CookedTheme.typography.displayLarge.copy(
                        fontSize = 64.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFeatureSettings = FontFeatureSettingsTabular
                    ),
                    color = if (isRunning && !isPaused) CookedTheme.colors.primary else CookedTheme.colors.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (!isRunning) {
                    // Duration Preset Chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        listOf(25, 45, 60).forEach { minsPreset ->
                            FilterChip(
                                selected = selectedMinutes == minsPreset,
                                onClick = {
                                    selectedMinutes = minsPreset
                                    secondsRemaining = minsPreset * 60
                                },
                                label = { Text("${minsPreset}m") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CookedTheme.colors.primary,
                                    selectedLabelColor = CookedTheme.colors.onPrimary
                                )
                            )
                        }
                    }
                }
            }

            // Controls at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp)
            ) {
                if (!isRunning) {
                    PrimaryButton(
                        text = "Start Cooking ($selectedMinutes min) 🔥",
                        onClick = {
                            secondsRemaining = selectedMinutes * 60
                            isRunning = true
                            isPaused = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pause / Resume
                        IconButton(
                            onClick = { isPaused = !isPaused },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(CookedRadius.Button))
                                .background(CookedTheme.colors.surfaceContainer)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                                    contentDescription = if (isPaused) "Resume" else "Pause",
                                    tint = CookedTheme.colors.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isPaused) "Resume" else "Pause",
                                    style = CookedTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = CookedTheme.colors.onSurface
                                )
                            }
                        }

                        // Stop / Finish
                        PrimaryButton(
                            text = "Finish Early 🏁",
                            onClick = {
                                isRunning = false
                                showFinishSheet = true
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    // Finish Progress Slider Dialog
    if (showFinishSheet) {
        val completedMinutes = if (elapsedMinutesCooked > 0) elapsedMinutesCooked else 1
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable { /* prevent click-through */ },
            contentAlignment = Alignment.Center
        ) {
            CookedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    EggMascot(
                        level = HeatLevel.CHILL,
                        isDone = progressSliderValue >= 100f,
                        size = 64.dp,
                        animated = true
                    )

                    Text(
                        text = "Session Complete!",
                        style = CookedTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = CookedTheme.colors.onSurface
                    )

                    Text(
                        text = "You cooked for $completedMinutes minutes. How much progress did you make?",
                        style = CookedTheme.typography.bodyMedium,
                        color = CookedTheme.colors.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "New Progress",
                                style = CookedTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = CookedTheme.colors.onSurface
                            )
                            Text(
                                text = "${progressSliderValue.toInt()}%",
                                style = CookedTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = CookedTheme.colors.primary
                            )
                        }

                        Slider(
                            value = progressSliderValue,
                            onValueChange = { progressSliderValue = it },
                            valueRange = 0f..100f,
                            steps = 19, // 5% steps
                            colors = SliderDefaults.colors(
                                thumbColor = CookedTheme.colors.primary,
                                activeTrackColor = CookedTheme.colors.primary,
                                inactiveTrackColor = CookedTheme.colors.surfaceContainerHigh
                            )
                        )
                    }

                    PrimaryButton(
                        text = "Save Progress & Drop Heat 📉",
                        onClick = {
                            showFinishSheet = false
                            onFinishSession(completedMinutes, progressSliderValue.toInt())
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

