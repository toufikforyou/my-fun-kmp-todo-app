package com.toufikforyou.mytodoapp.core

import kotlinx.coroutines.CoroutineDispatcher

/** `Dispatchers.IO` is not part of the common coroutines API, so each platform supplies it. */
expect val ioDispatcher: CoroutineDispatcher
