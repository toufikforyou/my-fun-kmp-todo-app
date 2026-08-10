package com.toufikforyou.mytodoapp.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.toufikforyou.mytodoapp.domain.model.Category
import com.toufikforyou.mytodoapp.domain.model.SortOrder
import com.toufikforyou.mytodoapp.domain.model.Todo
import com.toufikforyou.mytodoapp.domain.model.TodoFilter
import com.toufikforyou.mytodoapp.ui.components.CategoryChipRow
import com.toufikforyou.mytodoapp.ui.components.EmptyState
import com.toufikforyou.mytodoapp.ui.components.FilterChipRow
import com.toufikforyou.mytodoapp.ui.components.ProgressHeader
import com.toufikforyou.mytodoapp.ui.components.SearchBar
import com.toufikforyou.mytodoapp.ui.components.TaskRow
import com.toufikforyou.mytodoapp.ui.icons.AppIcons
import com.toufikforyou.mytodoapp.ui.theme.SectionHeaderStyle
import kotlin.time.Instant

@Composable
fun TaskListScreen(
    state: TaskListState,
    snackbarHostState: SnackbarHostState,
    onSearchChange: (String) -> Unit,
    onFilterChange: (TodoFilter) -> Unit,
    onCategoryChange: (Long?) -> Unit,
    onSortChange: (SortOrder) -> Unit,
    onToggleComplete: (Todo) -> Unit,
    onDelete: (Todo) -> Unit,
    onClearCompleted: () -> Unit,
    onOpenTask: (Long) -> Unit,
    onAddTask: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val listState = rememberLazyListState()
    // The FAB shrinks to an icon once the user starts scrolling, giving the list more room.
    val fabExpanded by remember {
        derivedStateOf { listState.firstVisibleItemIndex == 0 }
    }
    val categoriesById = remember(state.categories) { state.categories.associateBy { it.id } }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTask,
                expanded = fabExpanded,
                icon = { Icon(AppIcons.Add, contentDescription = null) },
                text = { Text("New task") },
            )
        },
    ) { scaffoldPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
        ) {
            TaskListTopBar(
                state = state,
                onSortChange = onSortChange,
                onClearCompleted = onClearCompleted,
                onOpenCategories = onOpenCategories,
                onOpenSettings = onOpenSettings,
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 104.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item(key = "header") {
                    ProgressHeader(
                        total = state.totalCount,
                        completed = state.completedCount,
                        overdue = state.overdueCount,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    )
                }
                item(key = "search") {
                    SearchBar(
                        value = state.search,
                        onValueChange = onSearchChange,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
                item(key = "filters") {
                    FilterChipRow(
                        selected = state.filter,
                        overdueCount = state.overdueCount,
                        onSelect = onFilterChange,
                    )
                }
                if (state.categories.isNotEmpty()) {
                    item(key = "categories") {
                        CategoryChipRow(
                            categories = state.categories,
                            selectedId = state.selectedCategoryId,
                            onSelect = onCategoryChange,
                        )
                    }
                }

                if (state.isEmpty) {
                    item(key = "empty") {
                        EmptyStateFor(state)
                    }
                }

                state.sections.forEach { section ->
                    item(key = "section-${section.title}") {
                        SectionHeader(
                            title = section.title,
                            count = section.todos.size,
                            modifier = Modifier.padding(
                                start = 24.dp,
                                end = 24.dp,
                                top = 12.dp,
                                bottom = 2.dp,
                            ),
                        )
                    }
                    items(section.todos, key = { it.id }) { todo ->
                        SwipeableTaskRow(
                            todo = todo,
                            category = todo.categoryId?.let(categoriesById::get),
                            now = state.now,
                            onToggle = { onToggleComplete(todo) },
                            onClick = { onOpenTask(todo.id) },
                            onDelete = { onDelete(todo) },
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .animateItem(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskListTopBar(
    state: TaskListState,
    onSortChange: (SortOrder) -> Unit,
    onClearCompleted: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 8.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Tasks",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onOpenCategories) {
            Icon(AppIcons.Label, contentDescription = "Manage lists")
        }
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(AppIcons.MoreVert, contentDescription = "More options")
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                Text(
                    text = "SORT BY",
                    style = SectionHeaderStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                SortOrder.entries.forEach { order ->
                    DropdownMenuItem(
                        text = { Text(order.label) },
                        onClick = {
                            onSortChange(order)
                            menuOpen = false
                        },
                        trailingIcon = {
                            if (order == state.sortOrder) {
                                Icon(AppIcons.Check, contentDescription = null)
                            }
                        },
                    )
                }
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("Clear completed") },
                    enabled = state.completedCount > 0,
                    onClick = {
                        onClearCompleted()
                        menuOpen = false
                    },
                    leadingIcon = { Icon(AppIcons.Delete, contentDescription = null) },
                )
                DropdownMenuItem(
                    text = { Text("Settings") },
                    onClick = {
                        onOpenSettings()
                        menuOpen = false
                    },
                    leadingIcon = { Icon(AppIcons.Tune, contentDescription = null) },
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title.uppercase(),
            style = SectionHeaderStyle,
            color = if (title == "Overdue") {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = count.toString(),
            style = SectionHeaderStyle,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}

/** Wraps [TaskRow] in a swipe-to-delete gesture that only triggers on a full end-to-start swipe. */
@Composable
private fun SwipeableTaskRow(
    todo: Todo,
    category: Category?,
    now: Instant,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState()

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        onDismiss = { direction ->
            if (direction == SwipeToDismissBoxValue.EndToStart) onDelete()
        },
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val active = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.medium,
                    )
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = AppIcons.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier
                        .size(24.dp)
                        .alpha(if (active) 1f else 0.5f),
                )
            }
        },
    ) {
        TaskRow(
            todo = todo,
            category = category,
            now = now,
            onToggle = onToggle,
            onClick = onClick,
        )
    }
}

@Composable
private fun EmptyStateFor(state: TaskListState) {
    when {
        state.search.isNotBlank() -> EmptyState(
            icon = AppIcons.Search,
            title = "No matches",
            message = "Nothing here matches \"${state.search}\". Try a different word.",
        )

        !state.hasAnyTasks -> EmptyState(
            icon = AppIcons.Inbox,
            title = "Your list is clear",
            message = "Tap New task to add the first thing you want to get done.",
        )

        state.filter == TodoFilter.OVERDUE -> EmptyState(
            icon = AppIcons.CheckCircle,
            title = "Nothing overdue",
            message = "You are on top of every deadline.",
        )

        state.filter == TodoFilter.COMPLETED -> EmptyState(
            icon = AppIcons.CheckCircle,
            title = "Nothing completed yet",
            message = "Finished tasks will collect here.",
        )

        else -> EmptyState(
            icon = AppIcons.List,
            title = "Nothing in this view",
            message = "Try another filter to see the rest of your tasks.",
        )
    }
}

/** Hosts the list screen's state and turns view-model events into snackbars. */
@Composable
fun TaskListRoute(
    viewModel: TaskListViewModel,
    state: TaskListState,
    snackbarHostState: SnackbarHostState,
    onOpenTask: (Long) -> Unit,
    onAddTask: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is TaskListEvent.TodoDeleted -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "Deleted \"${event.todo.title}\"",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onUndoDelete(event.todo)
                    }
                }

                is TaskListEvent.CompletedCleared -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "Cleared ${event.todos.size} completed",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onUndoClearCompleted(event.todos)
                    }
                }
            }
        }
    }

    TaskListScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onSearchChange = viewModel::onSearchChange,
        onFilterChange = viewModel::onFilterChange,
        onCategoryChange = viewModel::onCategoryChange,
        onSortChange = viewModel::onSortChange,
        onToggleComplete = viewModel::onToggleComplete,
        onDelete = viewModel::onDelete,
        onClearCompleted = viewModel::onClearCompleted,
        onOpenTask = onOpenTask,
        onAddTask = onAddTask,
        onOpenCategories = onOpenCategories,
        onOpenSettings = onOpenSettings,
    )
}
