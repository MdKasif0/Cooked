package com.cooked.app.widget

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
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
import com.cooked.app.feature.add.QuickAddActivity

class QuickAddWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(60.dp, 60.dp),   // 1x1
            DpSize(140.dp, 60.dp)   // 2x1
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                val size = LocalSize.current
                val isWide = size.width >= 120.dp

                if (isWide) {
                    // 2x1 layout: "What's cooking?" hint + Ember "+" button
                    Row(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .appWidgetBackground()
                            .background(CookedGlanceColors.Surface)
                            .cornerRadius(16.dp)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .clickable(actionStartActivity<QuickAddActivity>()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "What's cooking?",
                            style = TextStyle(
                                color = CookedGlanceColors.OnSurfaceVariant,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = GlanceModifier.defaultWeight()
                        )

                        Spacer(modifier = GlanceModifier.width(8.dp))

                        Box(
                            modifier = GlanceModifier
                                .size(40.dp)
                                .background(CookedGlanceColors.Primary)
                                .cornerRadius(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+",
                                style = TextStyle(
                                    color = CookedGlanceColors.OnPrimary,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                } else {
                    // 1x1 layout: Ember "+" button
                    Box(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .appWidgetBackground()
                            .background(CookedGlanceColors.Primary)
                            .cornerRadius(16.dp)
                            .clickable(actionStartActivity<QuickAddActivity>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+",
                            style = TextStyle(
                                color = CookedGlanceColors.OnPrimary,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

class QuickAddWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickAddWidget()
}

