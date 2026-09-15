package com.codeforcesvisualizer.inject

import androidx.room.Room
import androidx.room.RoomDatabase
import com.codeforcesvisualizer.Application
import com.codeforcesvisualizer.shared.data.local.CFDatabase
import com.codeforcesvisualizer.shared.data.local.DATABASE_FILE_NAME

actual fun createDatabaseBuilder(): RoomDatabase.Builder<CFDatabase> {
    val context = Application.context
    return Room.databaseBuilder<CFDatabase>(
        context = context,
        name = context.getDatabasePath(DATABASE_FILE_NAME).absolutePath
    )
}
