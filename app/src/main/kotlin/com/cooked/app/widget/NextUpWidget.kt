package com.cooked.app.widget

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
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
import com.cooked.app.domain.deadline.HeatCalculator
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.repository.ItemRepository
import com.cooked.app.scheduling.alarm.AlarmScheduler
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class NextUpWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface NextUpEntryPoint {
        fun itemRepository(): ItemRepository
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(140.dp, 140.dp), // 2x2
            DpSize(280.dp, 140.dp)  // 4x2
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            NextUpEntryPoint::class.java
        )
        val items = try {
            entryPoint.itemRepository().getActiveItems().sortedBy { it.dueAt }.take(3)
        } catch (_: Exception) {
            emptyList()
        }

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .appWidgetBackground()
                        .background(CookedGlanceColors.Surface)
                        .cornerRadius(16.dp)
                        .padding(12.dp)
                ) {
                    // Header
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Next Up",
                            style = TextStyle(
                                color = CookedGlanceColors.OnSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = GlanceModifier.defaultWeight()
                        )

                        Text(
                            text = "${items.size} due",
                            style = TextStyle(
                                color = CookedGlanceColors.OnSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(8.dp))

                    if (items.isEmpty()) {
                        Box(
                            modifier = GlanceModifier
                                .fillMaxSize()
                                .clickable(actionStartActivity<MainActivity>()),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nothing due. Suspiciously calm.",
                                style = TextStyle(
                                    color = CookedGlanceColors.OnSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    } else {
                        Column(
                            modifier = GlanceModifier.fillMaxSize()
                        ) {
                            items.forEachIndexed { index, item ->
                                NextUpItemRow(context = context, item = item)
                                if (index < items.size - 1) {
                                    Spacer(modifier = GlanceModifier.height(6.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun NextUpItemRow(
    context: Context,
    item: Item
) {
    val zone = ZoneId.systemDefault()
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    val timeStr = timeFormatter.format(item.dueAt.atZone(zone))

    val heatColor = when {
        item.kind == ItemKind.DEADLINE -> {
            when (HeatCalculator.calculate(item, Instant.now()).heatLevel) {
                com.cooked.app.core.designsystem.HeatLevel.CHILL -> CookedGlanceColors.Chill
                com.cooked.app.core.designsystem.HeatLevel.WARM -> CookedGlanceColors.Warm
                com.cooked.app.core.designsystem.HeatLevel.TOASTY -> CookedGlanceColors.Toasty
                com.cooked.app.core.designsystem.HeatLevel.COOKED -> CookedGlanceColors.Cooked
                com.cooked.app.core.designsystem.HeatLevel.DONE -> CookedGlanceColors.Chill
            }
        }
        item.isOverdue -> CookedGlanceColors.Cooked
        else -> CookedGlanceColors.Primary
    }

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(CookedGlanceColors.SurfaceContainer)
            .cornerRadius(10.dp)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Heat dot
        Box(
            modifier = GlanceModifier
                .size(8.dp)
                .background(heatColor)
                .cornerRadius(4.dp)
        ) {}

        Spacer(modifier = GlanceModifier.width(8.dp))

        // Title and time
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = item.title,
                style = TextStyle(
                    color = CookedGlanceColors.OnSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )
            Text(
                text = timeStr,
                style = TextStyle(
                    color = CookedGlanceColors.OnSurfaceVariant,
                    fontSize = 10.sp
                )
            )
        }

        Spacer(modifier = GlanceModifier.width(6.dp))

        // Done checkbox / tap target
        Box(
            modifier = GlanceModifier
                .size(24.dp)
                .background(CookedGlanceColors.Surface)
                .cornerRadius(12.dp)
                .clickable(
                    actionRunCallback<MarkDoneActionCallback>(
                        actionParametersOf(MarkDoneActionCallback.itemIdKey to item.id)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✓",
                style = TextStyle(
                    color = CookedGlanceColors.Primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

class MarkDoneActionCallback : ActionCallback {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface CallbackEntryPoint {
        fun itemRepository(): ItemRepository
        fun alarmScheduler(): AlarmScheduler
    }

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val itemId = parameters[itemIdKey] ?: return
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            CallbackEntryPoint::class.java
        )

        entryPoint.itemRepository().markDone(itemId, Instant.now())
        entryPoint.alarmScheduler().cancel(itemId)

        // Immediately update this widget instance within 1 second
        NextUpWidget().update(context, glanceId)
    }

    companion object {
        val itemIdKey = ActionParameters.Key<String>("item_id")
    }
}

class NextUpWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NextUpWidget()
}
