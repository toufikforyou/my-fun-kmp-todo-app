package com.toufikforyou.mytodoapp.ui.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.toufikforyou.mytodoapp.domain.model.Category
import com.toufikforyou.mytodoapp.ui.components.ColorDot
import com.toufikforyou.mytodoapp.ui.components.EmptyState
import com.toufikforyou.mytodoapp.ui.icons.AppIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    categories: List<Category>,
    taskCounts: Map<Long, Int>,
    onSave: (Category) -> Unit,
    onDelete: (Category) -> Unit,
    onBack: () -> Unit,
) {
    var editing by remember { mutableStateOf<Category?>(null) }
    var confirmDelete by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lists") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(AppIcons.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editing = Category(name = "", colorArgb = Category.palette.first()) },
                icon = { Icon(AppIcons.Add, contentDescription = null) },
                text = { Text("New list") },
            )
        },
    ) { scaffoldPadding ->
        if (categories.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding),
                contentAlignment = Alignment.Center,
            ) {
                EmptyState(
                    icon = AppIcons.Label,
                    title = "No lists yet",
                    message = "Group related tasks by creating a list such as Work or Errands.",
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(categories, key = { it.id }) { category ->
                    CategoryCard(
                        category = category,
                        taskCount = taskCounts[category.id] ?: 0,
                        onEdit = { editing = category },
                        onDelete = { confirmDelete = category },
                    )
                }
            }
        }
    }

    editing?.let { draft ->
        CategoryEditorDialog(
            initial = draft,
            onDismiss = { editing = null },
            onConfirm = {
                onSave(it)
                editing = null
            },
        )
    }

    confirmDelete?.let { category ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text("Delete \"${category.name}\"?") },
            text = {
                Text(
                    "Tasks in this list are kept — they just stop being filed under it.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(category)
                        confirmDelete = null
                    },
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun CategoryCard(
    category: Category,
    taskCount: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        onClick = onEdit,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ColorDot(Color(category.colorArgb), size = 16.dp)
            Column(
                Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
            ) {
                Text(category.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = when (taskCount) {
                        0 -> "No tasks"
                        1 -> "1 task"
                        else -> "$taskCount tasks"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    AppIcons.Delete,
                    contentDescription = "Delete ${category.name}",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryEditorDialog(
    initial: Category,
    onDismiss: () -> Unit,
    onConfirm: (Category) -> Unit,
) {
    var name by remember { mutableStateOf(initial.name) }
    var color by remember { mutableStateOf(initial.colorArgb) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.id == 0L) "New list" else "Edit list") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Color", style = MaterialTheme.typography.titleSmall)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Category.palette.forEach { swatch ->
                            ColorSwatch(
                                color = Color(swatch),
                                selected = swatch == color,
                                onSelect = { color = swatch },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    onConfirm(initial.copy(name = name.trim(), colorArgb = color))
                },
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun ColorSwatch(color: Color, selected: Boolean, onSelect: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(color, CircleShape)
            .border(
                width = if (selected) 3.dp else 0.dp,
                color = if (selected) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    Color.Transparent
                },
                shape = CircleShape,
            )
            .selectable(selected = selected, onClick = onSelect),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                AppIcons.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
