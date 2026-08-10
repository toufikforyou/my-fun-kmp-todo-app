package com.toufikforyou.mytodoapp.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Kotlin/Native does not expose `Dispatchers.IO`. `Default` is the dispatcher Room's own
 * multiplatform guidance uses on Apple targets — the bundled SQLite driver pools its own
 * connections, so the work here is short and does not starve the shared pool.
 */
actual val ioDispatcher: CoroutineDispatcher = Dispatchers.Default
