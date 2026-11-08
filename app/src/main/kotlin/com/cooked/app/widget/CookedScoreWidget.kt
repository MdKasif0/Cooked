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
import com.cooked.app.domain.deadline.CookedScoreCalculator
import com.cooked.app.domain.repository.ItemRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.Instant

class CookedScoreWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface CookedScoreEntryPoint {
        fun itemRepository(): ItemRepository
    }

    override val sizeMode: SizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            CookedScoreEntryPoint::class.java
        )
        val activeItems = try {
            entryPoint.itemRepository().getActiveItems()
        } catch (_: Exception) {
            emptyList()
        }

        val scoreResult = CookedScoreCalculator.calculate(
            items = activeItems,
            now = Instant.now()
        )

        val score = scoreResult.score
        val (heatColor, eggRes) = when (scoreResult.heatLevel) {
            HeatLevel.CHILL -> CookedGlanceColors.Chill to R.drawable.egg_chill
            HeatLevel.WARM -> CookedGlanceColors.Warm to R.drawable.egg_warm
            HeatLevel.TOASTY -> CookedGlanceColors.Toasty to R.drawable.egg_toasty
            HeatLevel.COOKED -> CookedGlanceColors.Cooked to R.drawable.egg_cooked
            HeatLevel.DONE -> CookedGlanceColors.Chill to R.drawable.egg_done
        }

        provideContent {
            GlanceTheme {
                Row(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .appWidgetBackground()
                        .background(CookedGlanceColors.Surface)
                        .cornerRadius(16.dp)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .clickable(actionStartActivity<MainActivity>()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mascot Egg
                    Image(
                        provider = ImageProvider(eggRes),
                        contentDescription = "Cooked Mascot",
                        modifier = GlanceModifier.size(38.dp)
                    )

                    Spacer(modifier = GlanceModifier.width(10.dp))

                    // Mini Gauge: Rounded box with big score number
                    Box(
                        modifier = GlanceModifier
                            .size(44.dp)
                            .background(CookedGlanceColors.SurfaceContainer)
                            .cornerRadius(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$score",
                            style = TextStyle(
                                color = heatColor,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.width(10.dp))

                    // Labels
                    Column(
                        modifier = GlanceModifier.defaultWeight()
                    ) {
                        Text(
                            text = "Cooked Score",
                            style = TextStyle(
                                color = CookedGlanceColors.OnSurface,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Spacer(modifier = GlanceModifier.height(2.dp))

                        Text(
                            text = "${scoreResult.heatLevel.displayName} · ${scoreResult.label}",
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

class CookedScoreWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CookedScoreWidget()
}
