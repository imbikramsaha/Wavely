package com.wavely.domain.repository

import com.wavely.domain.model.SortSpec
import com.wavely.domain.model.SortableList
import com.wavely.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/** User settings, persisted via DataStore (design.md §6.4, §10.3). */
interface SettingsRepository {

    val themeMode: Flow<ThemeMode>

    suspend fun setThemeMode(mode: ThemeMode)

    val dynamicColor: Flow<Boolean>

    suspend fun setDynamicColor(enabled: Boolean)

    fun sortSpec(list: SortableList): Flow<SortSpec>

    suspend fun setSortSpec(list: SortableList, spec: SortSpec)
}
