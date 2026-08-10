package com.toufikforyou.mytodoapp

import androidx.compose.ui.window.ComposeUIViewController

/**
 * Created lazily and kept for the process lifetime so the Room connection is opened once, not
 * every time SwiftUI rebuilds the hosting view.
 */
private val container: AppContainer by lazy { createAppContainer() }

fun MainViewController() = ComposeUIViewController { App(container) }
