package com.cooked.app.feature.insights.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.HeatLevel
import com.cooked.app.core.designsystem.components.CookedCategoryIcon
import com.cooked.app.core.designsystem.components.EggMascot
import com.cooked.app.core.designsystem.components.HeatChip
import com.cooked.app.domain.insights.model.AnalyticsSummary
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

@Composable
fun CookedReportDialog(
    summary: AnalyticsSummary,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 4 })
    var hideTitles by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = CookedTheme.colors.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Story Progress Indicators & Close
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        repeat(4) { pageIndex ->
                            val isActive = pagerState.currentPage == pageIndex
                            val isCompleted = pagerState.currentPage > pageIndex
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(3.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isActive -> CookedTheme.colors.primary
                                            isCompleted -> CookedTheme.colors.primary.copy(alpha = 0.5f)
                                            else -> CookedTheme.colors.surfaceContainerHigh
                                        }
                                    )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "WEEKLY COOKED REPORT",
                            style = CookedTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = CookedTheme.colors.primary
                        )
                        IconButton(onClick = onDismissRequest) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close",
                                tint = CookedTheme.colors.onSurfaceVariant
                            )
                        }
                    }
                }

                // Story Pages
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { page ->
                    when (page) {
                        0 -> ReportPageHeadline(summary = summary)
                        1 -> ReportPageOnTime(summary = summary)
                        2 -> ReportPageSnooze(summary = summary, hideTitles = hideTitles)
                        3 -> ReportPageForecastAndShare(
                            summary = summary,
                            hideTitles = hideTitles,
                            onToggleHideTitles = { hideTitles = !hideTitles },
                            onShare = {
                                shareReportPng(
                                    context = context,
                                    summary = summary,
                                    hideTitles = hideTitles
                                )
                            }
                        )
                    }
                }

                // Bottom Navigation Arrows
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (pagerState.currentPage > 0) {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Previous page",
                                tint = CookedTheme.colors.onSurfaceVariant
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }

                    Text(
                        text = "${pagerState.currentPage + 1} / 4",
                        style = CookedTheme.typography.labelSmall,
                        color = CookedTheme.colors.onSurfaceVariant
                    )

                    if (pagerState.currentPage < 3) {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = "Next page",
                                tint = CookedTheme.colors.primary
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportPageHeadline(summary: AnalyticsSummary) {
    val onTimePct = (summary.onTimeRate * 100).roundToInt()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EggMascot(
            level = HeatLevel.CHILL,
            isDone = true,
            size = 80.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "You handled business.",
            style = CookedTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
            color = CookedTheme.colors.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "You conquered ${summary.doneCount} tasks this week, landing $onTimePct% on time.",
            style = CookedTheme.typography.bodyLarge,
            color = CookedTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun ReportPageOnTime(summary: AnalyticsSummary) {
    val lateMedian = summary.latenessMedianMinutes ?: 25
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CookedCategoryIcon(
            keyOrEmoji = "routine",
            size = 64.dp,
            tint = CookedTheme.colors.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Delay Reality Check",
            style = CookedTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
            color = CookedTheme.colors.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "When you were late, you were ~${lateMedian}m late. Perfectly respectable—zero catastrophe.",
            style = CookedTheme.typography.bodyLarge,
            color = CookedTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun ReportPageSnooze(summary: AnalyticsSummary, hideTitles: Boolean) {
    val itemTitle = if (hideTitles) "a secret task" else "\"${summary.snoozeHabit.mostSnoozedItemTitle ?: "Recurring item"}\""
    val snoozedCount = summary.snoozeHabit.mostSnoozedCount.coerceAtLeast(1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EggMascot(
            level = HeatLevel.WARM,
            size = 72.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Top Snoozed Champion",
            style = CookedTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
            color = CookedTheme.colors.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "You snoozed $itemTitle $snoozedCount times. Look, we completely get it.",
            style = CookedTheme.typography.bodyLarge,
            color = CookedTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun ReportPageForecastAndShare(
    summary: AnalyticsSummary,
    hideTitles: Boolean,
    onToggleHideTitles: () -> Unit,
    onShare: () -> Unit
) {
    val latestScore = summary.cookedScoreHistory.lastOrNull()?.score ?: 15
    val forecastLevel = when {
        latestScore >= 75 -> HeatLevel.COOKED
        latestScore >= 50 -> HeatLevel.TOASTY
        latestScore >= 25 -> HeatLevel.WARM
        else -> HeatLevel.CHILL
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EggMascot(
            level = forecastLevel,
            size = 64.dp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Next Week Forecast",
            style = CookedTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
            color = CookedTheme.colors.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        HeatChip(level = forecastLevel)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Projected Cooked Score: $latestScore/100. So you're not cooked.",
            style = CookedTheme.typography.bodyLarge,
            color = CookedTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Privacy Toggle Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onToggleHideTitles() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Checkbox(
                checked = hideTitles,
                onCheckedChange = { onToggleHideTitles() },
                colors = CheckboxDefaults.colors(
                    checkedColor = CookedTheme.colors.primary,
                    checkmarkColor = CookedTheme.colors.onPrimary
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Hide task titles in shared card (Privacy)",
                style = CookedTheme.typography.bodySmall,
                color = CookedTheme.colors.onSurface
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onShare,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(50.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CookedTheme.colors.primary,
                contentColor = CookedTheme.colors.onPrimary
            )
        ) {
            Icon(imageVector = Icons.Rounded.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Share Story Card",
                style = CookedTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

/**
 * Generates an exact 1080x1920 PNG Story Card with crisp typography and watermarks,
 * and launches the Android Sharesheet.
 */
private fun shareReportPng(
    context: Context,
    summary: AnalyticsSummary,
    hideTitles: Boolean
) {
    try {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background: Warm dark charcoal (#0E0D0C)
        val bgPaint = Paint().apply { color = android.graphics.Color.parseColor("#0E0D0C") }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Accent Ember glow bar at top
        val emberPaint = Paint().apply { color = android.graphics.Color.parseColor("#FF7A45") }
        canvas.drawRect(80f, 120f, width - 80f, 132f, emberPaint)

        // Subtitle header
        val headerPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#FF7A45")
            textSize = 34f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("WEEKLY COOKED REPORT", 80f, 210f, headerPaint)

        // Big Main Title
        val titlePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#F4EFE9")
            textSize = 72f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("So you're not cooked.", 80f, 310f, titlePaint)

        // Stat Card 1: Completed Tasks
        drawCard(
            canvas = canvas,
            left = 80f, top = 380f, right = width - 80f, bottom = 620f,
            title = "COMPLETED THIS WEEK",
            stat = "${summary.doneCount} tasks",
            desc = "Knocked out and out of your head."
        )

        // Stat Card 2: On-Time Rate
        val onTimePct = (summary.onTimeRate * 100).roundToInt()
        drawCard(
            canvas = canvas,
            left = 80f, top = 660f, right = width - 80f, bottom = 900f,
            title = "ON-TIME RATE",
            stat = "$onTimePct%",
            desc = if (onTimePct >= 75) "Unbothered, moisturized, in your lane." else "A heavier week than usual. But you lived."
        )

        // Stat Card 3: Snooze Champion
        val topTitle = if (hideTitles) "A classified item" else (summary.snoozeHabit.mostSnoozedItemTitle ?: "The heavy one")
        drawCard(
            canvas = canvas,
            left = 80f, top = 940f, right = width - 80f, bottom = 1180f,
            title = "MOST SNOOZED",
            stat = topTitle,
            desc = "Snoozed ${summary.snoozeHabit.mostSnoozedCount.coerceAtLeast(1)} times. Valid resistance."
        )

        // Stat Card 4: Next Week Cooked Score
        val nextScore = summary.cookedScoreHistory.lastOrNull()?.score ?: 18
        drawCard(
            canvas = canvas,
            left = 80f, top = 1220f, right = width - 80f, bottom = 1460f,
            title = "NEXT WEEK FORECAST",
            stat = "Cooked Score: $nextScore/100",
            desc = "Paced and completely manageable."
        )

        // Watermark at bottom
        val watermarkPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#6B635A")
            textSize = 32f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("made with Cooked • native Android", width / 2f, 1780f, watermarkPaint)

        // Save to cacheDir/images/cooked_report.png
        val imagesFolder = File(context.cacheDir, "images").apply { mkdirs() }
        val file = File(imagesFolder, "cooked_report.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.flush()
        stream.close()

        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share your Cooked Report"))
    } catch (_: Exception) {
        // Fallback gracefully if share fails
    }
}

private fun drawCard(
    canvas: Canvas,
    left: Float, top: Float, right: Float, bottom: Float,
    title: String, stat: String, desc: String
) {
    val cardPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#191715")
        isAntiAlias = true
    }
    val rect = RectF(left, top, right, bottom)
    canvas.drawRoundRect(rect, 32f, 32f, cardPaint)

    val labelPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#A89F94")
        textSize = 28f
        isFakeBoldText = true
        isAntiAlias = true
    }
    canvas.drawText(title, left + 40f, top + 64f, labelPaint)

    val statPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#FF7A45")
        textSize = 58f
        isFakeBoldText = true
        isAntiAlias = true
    }
    canvas.drawText(stat, left + 40f, top + 140f, statPaint)

    val descPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#F4EFE9")
        textSize = 30f
        isAntiAlias = true
    }
    canvas.drawText(desc, left + 40f, top + 196f, descPaint)
}

