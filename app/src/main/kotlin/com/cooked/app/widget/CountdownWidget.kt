package com.cooked.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.cooked.app.MainActivity
import com.cooked.app.R
import com.cooked.app.core.designsystem.HeatLevel
import com.cooked.app.domain.deadline.HeatCalculator
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.repository.ItemRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CountdownWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface CountdownEntryPoint {
        fun itemRepository(): ItemRepository
    }

    override val sizeMode: SizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            CountdownEntryPoint::class.java
        )
        val repo = entryPoint.itemRepository()

        var appWidgetId = -1
        try {
            appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        } catch (_: Exception) {}

        val pinnedId = if (appWidgetId != -1) {
            CountdownConfigActivity.getWidgetDeadline(context, appWidgetId)
        } else null

        val deadline = try {
            val item = pinnedId?.let { repo.getItem(it) }
            if (item != null && item.kind == ItemKind.DEADLINE && !item.isCompleted && !item.isDeleted) {
                item
            } else {
                repo.getActiveItems().filter { it.kind == ItemKind.DEADLINE }.minByOrNull { it.dueAt }
            }
        } catch (_: Exception) {
            null
        }

        provideContent {
            GlanceTheme {
                if (deadline == null) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .appWidgetBackground()
                            .background(CookedGlanceColors.Surface)
                            .cornerRadius(28.dp)
                            .padding(16.dp)
                            .clickable(actionStartActivity<MainActivity>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No pinned deadline.\nTap to choose one.",
                            style = TextStyle(
                                color = CookedGlanceColors.OnSurfaceVariant,
                                fontSize = 12.sp
                            )
                        )
                    }
                } else {
                    val now = Instant.now()
                    val heatResult = HeatCalculator.calculate(deadline, now)
                    val (heatColor, eggRes) = when (heatResult.heatLevel) {
                        HeatLevel.CHILL -> CookedGlanceColors.Chill to R.drawable.egg_chill
                        HeatLevel.WARM -> CookedGlanceColors.Warm to R.drawable.egg_warm
                        HeatLevel.TOASTY -> CookedGlanceColors.Toasty to R.drawable.egg_toasty
                        HeatLevel.COOKED -> CookedGlanceColors.Cooked to R.drawable.egg_cooked
                        HeatLevel.DONE -> CookedGlanceColors.Chill to R.drawable.egg_done
                    }

                    // Format big tabular countdown
                    val countdownStr = if (now.isAfter(deadline.dueAt)) {
                        "Cooked!"
                    } else {
                        val duration = Duration.between(now, deadline.dueAt)
                        val days = duration.toDays()
                        val hours = duration.toHours() % 24
                        val mins = duration.toMinutes() % 60
                        when {
                            days > 0 -> "${days}d ${hours}h"
                            hours > 0 -> "${hours}h ${mins}m"
                            else -> "${mins}m"
                        }
                    }

                    val startByLine = if (deadline.startBy != null) {
                        val formatter = DateTimeFormatter.ofPattern("EEE h:mm a")
                        "Start by: " + formatter.format(deadline.startBy.atZone(ZoneId.systemDefault()))
                    } else {
                        heatResult.reason
                    }

                    Column(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .appWidgetBackground()
                            .background(CookedGlanceColors.Surface)
                            .cornerRadius(28.dp)
                            .padding(14.dp)
                            .clickable(actionStartActivity<MainActivity>()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Title + Mascot + Emoji
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                provider = ImageProvider(eggRes),
                                contentDescription = "Egg Mascot",
                                modifier = GlanceModifier.size(22.dp)
                            )
                            Spacer(modifier = GlanceModifier.width(6.dp))
                            Text(
                                text = deadline.title,
                                style = TextStyle(
                                    color = CookedGlanceColors.OnSurface,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1,
                                modifier = GlanceModifier.defaultWeight()
                            )
                            if (!deadline.emoji.isNullOrBlank()) {
                                Spacer(modifier = GlanceModifier.width(4.dp))
                                Text(
                                    text = deadline.emoji,
                                    style = TextStyle(fontSize = 14.sp)
                                )
                            }
                        }

                        Spacer(modifier = GlanceModifier.height(8.dp))

                        // Big Tabular Countdown
                        Text(
                            text = countdownStr,
                            style = TextStyle(
                                color = heatColor,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Spacer(modifier = GlanceModifier.height(6.dp))

                        // HeatBar line
                        val progressFraction = (deadline.progress.toFloat() / 100f).coerceIn(0.05f, 1f)
                        Row(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .background(CookedGlanceColors.SurfaceContainer)
                                .cornerRadius(3.dp)
                        ) {
                            Box(
                                modifier = GlanceModifier
                                    .defaultWeight()
                                    .height(5.dp)
                                    .background(heatColor)
                                    .cornerRadius(3.dp)
                            ) {}
                        }

                        Spacer(modifier = GlanceModifier.height(6.dp))

                        // Start by line
                        Text(
                            text = startByLine,
                            style = TextStyle(
                                color = CookedGlanceColors.OnSurfaceVariant,
                                fontSize = 11.sp
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

class CountdownWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CountdownWidget()
}
