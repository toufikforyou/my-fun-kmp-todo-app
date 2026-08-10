package com.toufikforyou.mytodoapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.toufikforyou.mytodoapp.domain.model.AppSettings
import com.toufikforyou.mytodoapp.domain.model.Category
import com.toufikforyou.mytodoapp.ui.CategoriesRoute
import com.toufikforyou.mytodoapp.ui.EditorRoute
import com.toufikforyou.mytodoapp.ui.SettingsRoute
import com.toufikforyou.mytodoapp.ui.TasksRoute
import com.toufikforyou.mytodoapp.ui.categories.CategoriesScreen
import com.toufikforyou.mytodoapp.ui.editor.TaskEditorScreen
import com.toufikforyou.mytodoapp.ui.editor.TaskEditorViewModel
import com.toufikforyou.mytodoapp.ui.settings.SettingsScreen
import com.toufikforyou.mytodoapp.ui.tasks.TaskListRoute
import com.toufikforyou.mytodoapp.ui.tasks.TaskListViewModel
import com.toufikforyou.mytodoapp.ui.theme.AppTheme
import com.toufikforyou.mytodoapp.ui.theme.supportsDynamicColor
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Root of the shared UI. Both `MainActivity` on Android and `MainViewController` on iOS render
 * exactly this, so the two apps stay in lockstep by construction.
 */
@Composable
fun App(container: AppContainer) {
    LaunchedEffect(container) { container.initialize() }

    val settings by container.settingsRepository.observeSettings()
        .collectAsStateWithLifecycle(AppSettings())

    AppTheme(themeMode = settings.themeMode, dynamicColor = settings.dynamicColor) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            AppNavHost(container = container, settings = settings)
        }
    }
}

@Composable
private fun AppNavHost(container: AppContainer, settings: AppSettings) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    NavHost(navController = navController, startDestination = TasksRoute) {
        composable<TasksRoute> {
            val viewModel = viewModel {
                TaskListViewModel(
                    todos = container.todoRepository,
                    categories = container.categoryRepository,
                    settings = container.settingsRepository,
                    reminders = container.reminderScheduler,
                )
            }
            val state by viewModel.state.collectAsStateWithLifecycle()

            TaskListRoute(
                viewModel = viewModel,
                state = state,
                snackbarHostState = snackbarHostState,
                onOpenTask = { navController.navigate(EditorRoute(it)) },
                onAddTask = { navController.navigate(EditorRoute()) },
                onOpenCategories = { navController.navigate(CategoriesRoute) },
                onOpenSettings = { navController.navigate(SettingsRoute) },
            )
        }

        composable<EditorRoute> { backStackEntry ->
            val todoId = backStackEntry.toRoute<EditorRoute>().todoId
            val viewModel = viewModel(key = "editor-$todoId") {
                TaskEditorViewModel(
                    todoId = todoId,
                    todos = container.todoRepository,
                    categories = container.categoryRepository,
                    settings = container.settingsRepository,
                    reminders = container.reminderScheduler,
                )
            }
            val state by viewModel.state.collectAsStateWithLifecycle()
            val saved by viewModel.saved.collectAsStateWithLifecycle()

            // Saving and deleting both finish the screen; the view model reports when it is done.
            LaunchedEffect(saved) {
                if (saved) navController.popBackStack()
            }

            TaskEditorScreen(
                state = state,
                onTitleChange = viewModel::onTitleChange,
                onNotesChange = viewModel::onNotesChange,
                onPriorityChange = viewModel::onPriorityChange,
                onCategoryChange = viewModel::onCategoryChange,
                onDueDateChange = viewModel::onDueDateChange,
                onDueTimeChange = viewModel::onDueTimeChange,
                onReminderEnabledChange = viewModel::onReminderEnabledChange,
                onReminderLeadChange = viewModel::onReminderLeadChange,
                onToggleCompleted = viewModel::onToggleCompleted,
                onSave = viewModel::onSave,
                onDelete = viewModel::onDelete,
                onBack = { navController.popBackStack() },
            )
        }

        composable<CategoriesRoute> {
            val categories by container.categoryRepository.observeCategories()
                .collectAsStateWithLifecycle(emptyList())
            // Remembered so the derived flow is not rebuilt on every recomposition.
            val countsFlow = remember(container) {
                container.todoRepository.observeTodos().map { todos ->
                    todos.mapNotNull { it.categoryId }
                        .groupingBy { it }
                        .eachCount()
                }
            }
            val taskCounts by countsFlow.collectAsStateWithLifecycle(emptyMap())

            CategoriesScreen(
                categories = categories,
                taskCounts = taskCounts,
                onSave = { category ->
                    scope.launch {
                        container.categoryRepository.save(withSortIndex(category, categories))
                    }
                },
                onDelete = { category ->
                    scope.launch { container.categoryRepository.delete(category.id) }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable<SettingsRoute> {
            SettingsScreen(
                settings = settings,
                supportsDynamicColor = supportsDynamicColor(),
                onThemeModeChange = { mode ->
                    scope.launch {
                        container.settingsRepository.update { it.copy(themeMode = mode) }
                    }
                },
                onDynamicColorChange = { enabled ->
                    scope.launch {
                        container.settingsRepository.update { it.copy(dynamicColor = enabled) }
                    }
                },
                onSortOrderChange = { order ->
                    scope.launch {
                        container.settingsRepository.update { it.copy(sortOrder = order) }
                    }
                },
                onShowCompletedChange = { show ->
                    scope.launch {
                        container.settingsRepository.update { it.copy(showCompleted = show) }
                    }
                },
                onDefaultReminderLeadChange = { lead ->
                    scope.launch {
                        container.settingsRepository.update {
                            it.copy(defaultReminderLeadMinutes = lead.minutesBefore)
                        }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
    }
}

/** New lists go to the end; existing ones keep the position they already had. */
private fun withSortIndex(category: Category, existing: List<Category>): Category =
    if (category.id == 0L) category.copy(sortIndex = existing.size) else category
