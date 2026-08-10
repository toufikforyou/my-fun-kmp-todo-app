package com.toufikforyou.mytodoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.toufikforyou.mytodoapp.domain.model.Category
import com.toufikforyou.mytodoapp.domain.model.TodoFilter
import com.toufikforyou.mytodoapp.ui.icons.AppIcons
import com.toufikforyou.mytodoapp.ui.theme.Accents

/** Rounded, filled search field that reads as a pill rather than a form input. */
@Composable
fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search tasks") },
        leadingIcon = {
            Icon(AppIcons.Search, contentDescription = null)
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(AppIcons.Close, contentDescription = "Clear search")
                }
            }
        },
        singleLine = true,
        shape = CircleShape,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
            // The pill shape carries the affordance; an underline would fight it.
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
    )
}

/** Horizontally scrolling status filters. */
@Composable
fun FilterChipRow(
    selected: TodoFilter,
    overdueCount: Int,
    onSelect: (TodoFilter) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp),
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = contentPadding,
    ) {
        items(TodoFilter.entries.toList(), key = { it.name }) { filter ->
            val accent = Accents.of(filter)
            val isSelected = filter == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(filter) },
                label = {
                    Text(
                        if (filter == TodoFilter.OVERDUE && overdueCount > 0) {
                            "${filter.label} · $overdueCount"
                        } else {
                            filter.label
                        },
                    )
                },
                shape = CircleShape,
                // Every filter carries its own accent: tinted when idle, solid when chosen.
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = accent.copy(alpha = 0.12f),
                    labelColor = accent,
                    selectedContainerColor = accent,
                    selectedLabelColor = Color.White,
                ),
                border = null,
            )
        }
    }
}

/** Category filters, with a leading "All lists" chip that clears the selection. */
@Composable
fun CategoryChipRow(
    categories: List<Category>,
    selectedId: Long?,
    onSelect: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp),
) {
    if (categories.isEmpty()) return
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = contentPadding,
    ) {
        item(key = "all") {
            val accent = MaterialTheme.colorScheme.primary
            FilterChip(
                selected = selectedId == null,
                onClick = { onSelect(null) },
                label = { Text("All lists") },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = accent.copy(alpha = 0.12f),
                    labelColor = accent,
                    selectedContainerColor = accent,
                    selectedLabelColor = Color.White,
                ),
                border = null,
            )
        }
        items(categories, key = { it.id }) { category ->
            val accent = Color(category.colorArgb)
            val isSelected = selectedId == category.id
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(if (isSelected) null else category.id) },
                label = { Text(category.name) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = accent.copy(alpha = 0.12f),
                    labelColor = accent,
                    selectedContainerColor = accent,
                    selectedLabelColor = Color.White,
                ),
                border = null,
                leadingIcon = if (!isSelected) {
                    { ColorDot(accent) }
                } else {
                    null
                },
            )
        }
    }
}

/** The small filled circle that stands in for a category everywhere it is referenced. */
@Composable
fun ColorDot(color: Color, modifier: Modifier = Modifier, size: Dp = 12.dp) {
    Box(
        modifier
            .size(size)
            .background(color, CircleShape),
    )
}
