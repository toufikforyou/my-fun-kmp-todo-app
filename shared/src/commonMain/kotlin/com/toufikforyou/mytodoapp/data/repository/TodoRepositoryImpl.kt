package com.toufikforyou.mytodoapp.data.repository

import com.toufikforyou.mytodoapp.data.local.TodoDao
import com.toufikforyou.mytodoapp.data.toDomain
import com.toufikforyou.mytodoapp.data.toEntity
import com.toufikforyou.mytodoapp.domain.model.Todo
import com.toufikforyou.mytodoapp.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

class TodoRepositoryImpl(
    private val dao: TodoDao,
    private val clock: Clock = Clock.System,
) : TodoRepository {

    override fun observeTodos(): Flow<List<Todo>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeTodo(id: Long): Flow<Todo?> =
        dao.observeById(id).map { it?.toDomain() }

    override suspend fun getTodo(id: Long): Todo? = dao.getById(id)?.toDomain()

    override suspend fun save(todo: Todo): Long {
        val stamped = todo.copy(updatedAt = clock.now())
        return if (stamped.id == 0L) {
            dao.insert(stamped.toEntity())
        } else {
            dao.update(stamped.toEntity())
            stamped.id
        }
    }

    override suspend fun delete(id: Long) = dao.deleteById(id)

    override suspend fun restore(todo: Todo) {
        // Insert rather than update: the row is gone, and REPLACE keeps the original id.
        dao.insert(todo.toEntity())
    }

    override suspend fun setCompleted(id: Long, completed: Boolean) {
        val now = clock.now()
        dao.setCompleted(
            id = id,
            completed = completed,
            completedAt = if (completed) now.toEpochMilliseconds() else null,
            updatedAt = now.toEpochMilliseconds(),
        )
    }

    override suspend fun clearCompleted(): List<Todo> {
        val cleared = dao.getCompleted().map { it.toDomain() }
        dao.deleteCompleted()
        return cleared
    }
}
