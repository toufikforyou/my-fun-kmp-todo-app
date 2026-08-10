package com.toufikforyou.mytodoapp.data.repository

import com.toufikforyou.mytodoapp.data.local.SETTINGS_ROW_ID
import com.toufikforyou.mytodoapp.data.local.SettingsDao
import com.toufikforyou.mytodoapp.data.toDomain
import com.toufikforyou.mytodoapp.data.toEntity
import com.toufikforyou.mytodoapp.domain.model.AppSettings
import com.toufikforyou.mytodoapp.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(
    private val dao: SettingsDao,
) : SettingsRepository {

    /** The row is absent until the user changes something, so a missing row means "defaults". */
    override fun observeSettings(): Flow<AppSettings> =
        dao.observe(SETTINGS_ROW_ID).map { it?.toDomain() ?: AppSettings() }

    override suspend fun update(transform: (AppSettings) -> AppSettings) {
        val current = dao.get(SETTINGS_ROW_ID)?.toDomain() ?: AppSettings()
        dao.upsert(transform(current).toEntity())
    }
}
