package com.toufikforyou.mytodoapp

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.toufikforyou.mytodoapp.core.ioDispatcher
import com.toufikforyou.mytodoapp.data.local.TodoDatabase
import com.toufikforyou.mytodoapp.data.repository.CategoryRepositoryImpl
import com.toufikforyou.mytodoapp.data.repository.SettingsRepositoryImpl
import com.toufikforyou.mytodoapp.data.repository.TodoRepositoryImpl
import com.toufikforyou.mytodoapp.domain.repository.CategoryRepository
import com.toufikforyou.mytodoapp.domain.repository.SettingsRepository
import com.toufikforyou.mytodoapp.domain.repository.TodoRepository
import com.toufikforyou.mytodoapp.notification.ReminderScheduler

/**
 * Hand-rolled dependency graph. Each platform entry point builds one of these and passes it to
 * [App]; there is no DI framework to keep the shared module dependency-light.
 */
class AppContainer(
    databaseBuilder: RoomDatabase.Builder<TodoDatabase>,
    val reminderScheduler: ReminderScheduler,
) {
    val database: TodoDatabase = buildDatabase(databaseBuilder)

    val todoRepository: TodoRepository = TodoRepositoryImpl(database.todoDao())

    private val categories = CategoryRepositoryImpl(database.categoryDao())
    val categoryRepository: CategoryRepository = categories

    val settingsRepository: SettingsRepository = SettingsRepositoryImpl(database.settingsDao())

    /** Runs once per launch, before the first frame needs data. */
    suspend fun initialize() {
        categories.seedDefaultsIfEmpty()
    }
}

/** Shared Room configuration, also used by the Android boot receiver. */
fun buildDatabase(builder: RoomDatabase.Builder<TodoDatabase>): TodoDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(ioDispatcher)
        .build()
