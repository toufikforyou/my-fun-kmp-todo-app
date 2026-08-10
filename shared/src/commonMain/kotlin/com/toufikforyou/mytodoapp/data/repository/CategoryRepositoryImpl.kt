package com.toufikforyou.mytodoapp.data.repository

import com.toufikforyou.mytodoapp.data.local.CategoryDao
import com.toufikforyou.mytodoapp.data.toDomain
import com.toufikforyou.mytodoapp.data.toEntity
import com.toufikforyou.mytodoapp.domain.model.Category
import com.toufikforyou.mytodoapp.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(
    private val dao: CategoryDao,
) : CategoryRepository {

    override fun observeCategories(): Flow<List<Category>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun save(category: Category): Long = dao.upsert(category.toEntity())

    override suspend fun delete(id: Long) = dao.deleteById(id)

    /** Gives a brand new install something to file tasks under instead of an empty picker. */
    suspend fun seedDefaultsIfEmpty() {
        if (dao.count() > 0) return
        listOf(
            Category(name = "Personal", colorArgb = Category.palette[0], sortIndex = 0),
            Category(name = "Work", colorArgb = Category.palette[1], sortIndex = 1),
            Category(name = "Shopping", colorArgb = Category.palette[5], sortIndex = 2),
        ).forEach { dao.upsert(it.toEntity()) }
    }
}
