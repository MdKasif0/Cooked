package com.cooked.app.data.template

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cooked.app.core.config.FeatureFlags
import com.cooked.app.domain.template.BuiltInTemplates
import com.cooked.app.domain.template.ItemTemplate
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.templateDataStore by preferencesDataStore(name = "cooked_templates")

interface TemplateRepository {
    fun getTemplates(): Flow<List<ItemTemplate>>
    suspend fun saveCustomTemplate(template: ItemTemplate, isPro: Boolean): Result<Unit>
    suspend fun deleteCustomTemplate(templateId: String)
}

@Singleton
class TemplateRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : TemplateRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val customTemplatesKey = stringPreferencesKey("custom_templates_json")

    override fun getTemplates(): Flow<List<ItemTemplate>> {
        return context.templateDataStore.data.map { prefs ->
            if (!FeatureFlags.isTemplatesEnabled) return@map emptyList()

            val customJson = prefs[customTemplatesKey]
            val customTemplates = if (!customJson.isNullOrBlank()) {
                try {
                    json.decodeFromString<List<ItemTemplate>>(customJson)
                } catch (_: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }
            BuiltInTemplates.all + customTemplates
        }
    }

    override suspend fun saveCustomTemplate(template: ItemTemplate, isPro: Boolean): Result<Unit> {
        var result: Result<Unit> = Result.success(Unit)
        context.templateDataStore.edit { prefs ->
            val customJson = prefs[customTemplatesKey]
            val existing = if (!customJson.isNullOrBlank()) {
                try {
                    json.decodeFromString<List<ItemTemplate>>(customJson)
                } catch (_: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }

            if (!isPro && existing.size >= 3) {
                result = Result.failure(IllegalStateException("Free tier is limited to 3 custom templates. Upgrade to Pro for unlimited."))
                return@edit
            }

            val updated = existing.filterNot { it.id == template.id } + template.copy(isCustom = true)
            prefs[customTemplatesKey] = json.encodeToString(updated)
        }
        return result
    }

    override suspend fun deleteCustomTemplate(templateId: String) {
        context.templateDataStore.edit { prefs ->
            val customJson = prefs[customTemplatesKey] ?: return@edit
            val existing = try {
                json.decodeFromString<List<ItemTemplate>>(customJson)
            } catch (_: Exception) {
                emptyList()
            }
            val updated = existing.filterNot { it.id == templateId }
            prefs[customTemplatesKey] = json.encodeToString(updated)
        }
    }
}

