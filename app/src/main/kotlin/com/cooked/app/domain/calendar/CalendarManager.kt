package com.cooked.app.domain.calendar

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.cooked.app.domain.rescue.TimeBlock
import java.time.Instant
import java.time.temporal.ChronoUnit

object CalendarManager {

    fun hasCalendarPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Reads busy blocks from the user's on-device calendars for the next [days] days.
     * Guaranteed 100% read-only; never creates, alters, or uploads calendar data.
     */
    fun getBusyBlocks(
        context: Context,
        startInstant: Instant = Instant.now(),
        days: Int = 14,
        selectedCalendarIds: Set<String> = emptySet()
    ): List<TimeBlock> {
        if (!hasCalendarPermission(context)) return emptyList()

        val endInstant = startInstant.plus(days.toLong(), ChronoUnit.DAYS)
        val startMillis = startInstant.toEpochMilli()
        val endMillis = endInstant.toEpochMilli()

        val builder: Uri.Builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, startMillis)
        ContentUris.appendId(builder, endMillis)

        val projection = arrayOf(
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.ALL_DAY,
            CalendarContract.Instances.CALENDAR_ID
        )

        val blocks = mutableListOf<TimeBlock>()

        try {
            val cursor: Cursor? = context.contentResolver.query(
                builder.build(),
                projection,
                null,
                null,
                CalendarContract.Instances.BEGIN + " ASC"
            )

            cursor?.use {
                val beginCol = it.getColumnIndex(CalendarContract.Instances.BEGIN)
                val endCol = it.getColumnIndex(CalendarContract.Instances.END)
                val titleCol = it.getColumnIndex(CalendarContract.Instances.TITLE)
                val allDayCol = it.getColumnIndex(CalendarContract.Instances.ALL_DAY)
                val calIdCol = it.getColumnIndex(CalendarContract.Instances.CALENDAR_ID)

                while (it.moveToNext()) {
                    val calId = if (calIdCol != -1) it.getString(calIdCol) else ""
                    if (selectedCalendarIds.isNotEmpty() && !selectedCalendarIds.contains(calId)) {
                        continue
                    }

                    val isAllDay = if (allDayCol != -1) it.getInt(allDayCol) == 1 else false
                    val begin = if (beginCol != -1) it.getLong(beginCol) else 0L
                    val end = if (endCol != -1) it.getLong(endCol) else 0L
                    val title = if (titleCol != -1) it.getString(titleCol) ?: "Busy" else "Busy"

                    if (end > begin && !isAllDay) {
                        blocks.add(
                            TimeBlock(
                                start = Instant.ofEpochMilli(begin),
                                end = Instant.ofEpochMilli(end),
                                title = title
                            )
                        )
                    }
                }
            }
        } catch (_: SecurityException) {
            return emptyList()
        } catch (_: Exception) {
            return emptyList()
        }

        return blocks
    }
}
