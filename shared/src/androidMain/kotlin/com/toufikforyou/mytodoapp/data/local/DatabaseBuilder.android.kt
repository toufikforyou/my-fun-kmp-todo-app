package com.toufikforyou.mytodoapp.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

/** Called from the Android entry point, which is the only place with a [Context]. */
fun createDatabaseBuilder(context: Context): RoomDatabase.Builder<TodoDatabase> {
    val dbFile = context.applicationContext.getDatabasePath(DATABASE_NAME)
    return Room.databaseBuilder<TodoDatabase>(
        context = context.applicationContext,
        name = dbFile.absolutePath,
    )
}
