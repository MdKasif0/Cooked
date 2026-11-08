package com.cooked.app.data.db

import androidx.room.TypeConverter
import com.cooked.app.domain.model.ItemEventType
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.model.ItemStatus
import com.cooked.app.domain.model.NagPolicy
import com.cooked.app.domain.model.Recurrence
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant

class Converters {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @TypeConverter
    fun fromInstant(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun toInstant(millis: Long?): Instant? = millis?.let { Instant.ofEpochMilli(it) }

    @TypeConverter
    fun fromItemKind(kind: ItemKind?): String? = kind?.name

    @TypeConverter
    fun toItemKind(value: String?): ItemKind? = value?.let {
        try { ItemKind.valueOf(it) } catch (_: Exception) { ItemKind.REMINDER }
    }

    @TypeConverter
    fun fromItemStatus(status: ItemStatus?): String? = status?.name

    @TypeConverter
    fun toItemStatus(value: String?): ItemStatus? = value?.let {
        try { ItemStatus.valueOf(it) } catch (_: Exception) { ItemStatus.ACTIVE }
    }

    @TypeConverter
    fun fromItemEventType(type: ItemEventType?): String? = type?.name

    @TypeConverter
    fun toItemEventType(value: String?): ItemEventType? = value?.let {
        try { ItemEventType.valueOf(it) } catch (_: Exception) { ItemEventType.CREATED }
    }

    @TypeConverter
    fun fromNagPolicy(policy: NagPolicy?): String? = policy?.let { json.encodeToString(it) }

    @TypeConverter
    fun toNagPolicy(value: String?): NagPolicy? = value?.let {
        try { json.decodeFromString<NagPolicy>(it) } catch (_: Exception) { NagPolicy() }
    }

    @TypeConverter
    fun fromRecurrence(recurrence: Recurrence?): String? = recurrence?.let { json.encodeToString(it) }

    @TypeConverter
    fun toRecurrence(value: String?): Recurrence? = value?.let {
        try { json.decodeFromString<Recurrence>(it) } catch (_: Exception) { null }
    }

    @TypeConverter
    fun fromStringMap(map: Map<String, String>?): String? = map?.let { json.encodeToString(it) }

    @TypeConverter
    fun toStringMap(value: String?): Map<String, String>? = value?.let {
        try { json.decodeFromString<Map<String, String>>(it) } catch (_: Exception) { emptyMap() }
    }
}

