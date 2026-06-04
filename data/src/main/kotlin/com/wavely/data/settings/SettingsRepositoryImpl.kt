package com.wavely.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.wavely.domain.model.SortDirection
import com.wavely.domain.model.SortField
import com.wavely.domain.model.SortSpec
import com.wavely.domain.model.SortableList
import com.wavely.domain.model.ThemeMode
import com.wavely.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    private object Keys {
        val THEME = intPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        fun sortField(list: SortableList) = stringPreferencesKey("sort_field_${list.name}")
        fun sortDir(list: SortableList) = stringPreferencesKey("sort_dir_${list.name}")
    }

    override val themeMode: Flow<ThemeMode> =
        dataStore.data.map { prefs ->
            ThemeMode.entries.getOrElse(prefs[Keys.THEME] ?: ThemeMode.SYSTEM.ordinal) { ThemeMode.SYSTEM }
        }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME] = mode.ordinal }
    }

    override val dynamicColor: Flow<Boolean> =
        dataStore.data.map { it[Keys.DYNAMIC_COLOR] ?: false }

    override suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    override fun sortSpec(list: SortableList): Flow<SortSpec> =
        dataStore.data.map { prefs ->
            val field = prefs[Keys.sortField(list)]?.let { runCatching { SortField.valueOf(it) }.getOrNull() }
                ?: SortField.TITLE
            val dir = prefs[Keys.sortDir(list)]?.let { runCatching { SortDirection.valueOf(it) }.getOrNull() }
                ?: SortDirection.ASC
            SortSpec(field, dir)
        }

    override suspend fun setSortSpec(list: SortableList, spec: SortSpec) {
        dataStore.edit {
            it[Keys.sortField(list)] = spec.field.name
            it[Keys.sortDir(list)] = spec.direction.name
        }
    }
}
