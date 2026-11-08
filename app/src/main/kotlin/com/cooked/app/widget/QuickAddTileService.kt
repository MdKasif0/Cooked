package com.cooked.app.widget

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.cooked.app.domain.repository.ItemRepository
import com.cooked.app.feature.add.QuickAddActivity
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class QuickAddTileService : TileService() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface QuickAddTileEntryPoint {
        fun itemRepository(): ItemRepository
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onStartListening() {
        super.onStartListening()
        val tile = qsTile ?: return

        serviceScope.launch {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                QuickAddTileEntryPoint::class.java
            )
            val repo = entryPoint.itemRepository()

            val zone = ZoneId.systemDefault()
            val today = LocalDate.now(zone)
            val activeItems = repo.getActiveItems()
            val dueTodayCount = activeItems.count { item ->
                item.dueAt.atZone(zone).toLocalDate() == today
            }

            tile.label = "Cooked: add"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = if (dueTodayCount > 0) "$dueTodayCount due today" else "All clear"
            }
            tile.state = Tile.STATE_ACTIVE
            tile.updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        val intent = Intent(this, QuickAddActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }
}
