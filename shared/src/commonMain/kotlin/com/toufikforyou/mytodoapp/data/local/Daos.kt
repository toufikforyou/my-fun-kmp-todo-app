package com.toufikforyou.mytodoapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {

    @Query("SELECT * FROM todos")
    fun observeAll(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE id = :id")
    fun observeById(id: Long): Flow<TodoEntity?>

    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getById(id: Long): TodoEntity?

    @Query("SELECT * FROM todos WHERE isCompleted = 1")
    suspend fun getCompleted(): List<TodoEntity>

    /** Returns the new row id; for an update Room returns the existing id. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todo: TodoEntity): Long

    @Update
    suspend fun update(todo: TodoEntity)

    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM todos WHERE isCompleted = 1")
    suspend fun deleteCompleted()

    @Query(
        """
        UPDATE todos
        SET isCompleted = :completed,
            completedAt = :completedAt,
            updatedAt = :updatedAt
        WHERE id = :id
        """,
    )
    suspend fun setCompleted(id: Long, completed: Boolean, completedAt: Long?, updatedAt: Long)
}

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY sortIndex ASC, name ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int

    @Upsert
    suspend fun upsert(category: CategoryEntity): Long

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface SettingsDao {

    @Query("SELECT * FROM settings WHERE id = :id")
    fun observe(id: Long): Flow<SettingsEntity?>

    @Query("SELECT * FROM settings WHERE id = :id")
    suspend fun get(id: Long): SettingsEntity?

    @Upsert
    suspend fun upsert(settings: SettingsEntity)
}
