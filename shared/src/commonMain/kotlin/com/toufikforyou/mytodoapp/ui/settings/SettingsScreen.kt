package com.toufikforyou.mytodoapp.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.toufikforyou.mytodoapp.domain.model.AppSettings
import com.toufikforyou.mytodoapp.domain.model.SortOrder
import com.toufikforyou.mytodoapp.domain.model.ThemeMode
import com.toufikforyou.mytodoapp.ui.editor.ReminderLead
import com.toufikforyou.mytodoapp.ui.icons.AppIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    supportsDynamicColor: Boolean,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    onShowCompletedChange: (Boolean) -> Unit,
    onDefaultReminderLeadChange: (ReminderLead) -> Unit,
    onBack: () -> Unit,
) {
    val currentLead = ReminderLead.closestTo(settings.defaultReminderLeadMinutes)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(AppIcons.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            SettingsGroup(title = "Appearance") {
                ChoiceRow(
                    icon = AppIcons.Palette,
                    title = "Theme",
                    value = settings.themeMode.label,
                    options = ThemeMode.entries.map { mode ->
                        mode.label to { onThemeModeChange(mode) }
                    },
                )
                if (supportsDynamicColor) {
                    HorizontalDivider()
                    ToggleRow(
                        icon = AppIcons.Palette,
                        title = "Dynamic color",
                        subtitle = "Match the app's palette to your wallpaper",
                        checked = settings.dynamicColor,
                        onCheckedChange = onDynamicColorChange,
                    )
                }
            }

            SettingsGroup(title = "Tasks") {
                ChoiceRow(
                    icon = AppIcons.Sort,
                    title = "Default sort",
                    value = settings.sortOrder.label,
                    options = SortOrder.entries.map { order ->
                        order.label to { onSortOrderChange(order) }
                    },
                )
                HorizontalDivider()
                ToggleRow(
                    icon = AppIcons.CheckCircle,
                    title = "Show completed",
                    subtitle = "Keep finished tasks visible at the bottom of the list",
                    checked = settings.showCompleted,
                    onCheckedChange = onShowCompletedChange,
                )
            }

            SettingsGroup(title = "Reminders") {
                ChoiceRow(
                    icon = AppIcons.Bell,
                    title = "Remind me by default",
                    value = currentLead.label,
                    options = ReminderLead.entries.map { lead ->
                        lead.label to { onDefaultReminderLeadChange(lead) }
                    },
                )
            }

            Text(
                text = "Tasks are stored on this device only. Nothing is uploaded anywhere.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        }
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp),
        )
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Column(
            Modifier
                .weight(1f)
                .padding(start = 16.dp, end = 8.dp),
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/** A row that opens a menu of mutually exclusive choices. */
@Composable
private fun ChoiceRow(
    icon: ImageVector,
    title: String,
    value: String,
    options: List<Pair<String, () -> Unit>>,
) {
    var open by remember { mutableStateOf(false) }

    Box {
        Surface(
            onClick = { open = true },
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Icon(
                    AppIcons.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(18.dp),
                )
            }
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { (label, select) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        select()
                        open = false
                    },
                    trailingIcon = {
                        if (label == value) Icon(AppIcons.Check, contentDescription = null)
                    },
                )
            }
        }
    }
}
