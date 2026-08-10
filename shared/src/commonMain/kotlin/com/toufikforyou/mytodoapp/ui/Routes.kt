package com.toufikforyou.mytodoapp.ui

import kotlinx.serialization.Serializable

@Serializable
object TasksRoute

/** [todoId] of 0 opens the editor for a brand-new task. */
@Serializable
data class EditorRoute(val todoId: Long = 0L)

@Serializable
object CategoriesRoute

@Serializable
object SettingsRoute
