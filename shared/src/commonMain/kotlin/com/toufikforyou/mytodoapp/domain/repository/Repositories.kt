package com.toufikforyou.mytodoapp.domain.repository

import com.toufikforyou.mytodoapp.domain.model.AppSettings
import com.toufikforyou.mytodoapp.domain.model.Category
import com.toufikforyou.mytodoapp.domain.model.Todo
import kotlinx.coroutines.flow.Flow

interface TodoRepository {
    fun observeTodos(): Flow<List<Todo>>

    fun observeTodo(id: Long): Flow<Todo?>

    suspend fun getTodo(id: Long): Todo?

    /** Inserts when [todo] has id 0, updates otherwise. Returns the persisted id. */
    suspend fun save(todo: Todo): Long

    suspend fun delete(id: Long)

    /** Re-inserts a previously deleted task, keeping its id so undo restores it exactly. */
    suspend fun restore(todo: Todo)

    suspend fun setCompleted(id: Long, completed: Boolean)

    /** Returns the tasks that were cleared so the caller can offer an undo. */
    suspend fun clearCompleted(): List<Todo>
}

interface CategoryRepository {
    fun observeCategories(): Flow<List<Category>>

    suspend fun save(category: Category): Long

    /** Deleting a category leaves its tasks in place, unfiled. */
    suspend fun delete(id: Long)
}

interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>

    suspend fun update(transform: (AppSettings) -> AppSettings)
}
