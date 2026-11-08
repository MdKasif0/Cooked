package com.cooked.app.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import com.cooked.app.core.designsystem.components.CookedCategoryIcon
import com.cooked.app.core.designsystem.components.ShimmerPlaceholder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.repository.ItemRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class CountdownConfigActivity : ComponentActivity() {

    @Inject
    lateinit var itemRepository: ItemRepository

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            CookedTheme {
                var deadlines by remember { mutableStateOf<List<Item>?>(null) }
                val scope = rememberCoroutineScope()

                LaunchedEffect(Unit) {
                    val allItems = itemRepository.getActiveItems()
                    deadlines = allItems.filter { it.kind == ItemKind.DEADLINE }.sortedBy { it.dueAt }
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "Choose Deadline to Pin",
                                    style = CookedTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = CookedTheme.colors.background,
                                titleContentColor = CookedTheme.colors.onBackground
                            )
                        )
                    },
                    containerColor = CookedTheme.colors.background
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        val list = deadlines
                        if (list == null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp, vertical = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                repeat(4) {
                                    ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(64.dp))
                                }
                            }
                        } else if (list.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No active deadlines found.\nCreate a deadline in Cooked first!",
                                    style = CookedTheme.typography.bodyLarge,
                                    color = CookedTheme.colors.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(list, key = { it.id }) { item ->
                                    DeadlineToPinRow(
                                        item = item,
                                        onClick = {
                                            scope.launch {
                                                saveWidgetDeadline(this@CountdownConfigActivity, appWidgetId, item.id)

                                                // Update widget instance
                                                try {
                                                    val glanceManager = GlanceAppWidgetManager(this@CountdownConfigActivity)
                                                    val glanceId = glanceManager.getGlanceIdBy(appWidgetId)
                                                    CountdownWidget().update(this@CountdownConfigActivity, glanceId)
                                                } catch (_: Exception) {}

                                                val resultValue = Intent().apply {
                                                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                                                }
                                                setResult(Activity.RESULT_OK, resultValue)
                                                finish()
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val PREFS_NAME = "countdown_widget_prefs"

        fun saveWidgetDeadline(context: Context, appWidgetId: Int, deadlineId: String) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString("widget_$appWidgetId", deadlineId)
                .apply()
        }

        fun getWidgetDeadline(context: Context, appWidgetId: Int): String? {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString("widget_$appWidgetId", null)
        }
    }
}

@Composable
private fun DeadlineToPinRow(
    item: Item,
    onClick: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("EEE, d MMM · h:mm a")
    val dueStr = formatter.format(item.dueAt.atZone(ZoneId.systemDefault()))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = CookedTheme.colors.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, CookedTheme.colors.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CookedCategoryIcon(
                keyOrEmoji = item.emoji,
                size = 32.dp,
                tint = CookedTheme.colors.primary
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = CookedTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = CookedTheme.colors.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Due: $dueStr",
                    style = CookedTheme.typography.bodySmall,
                    color = CookedTheme.colors.onSurfaceVariant
                )
            }
        }
    }
}

