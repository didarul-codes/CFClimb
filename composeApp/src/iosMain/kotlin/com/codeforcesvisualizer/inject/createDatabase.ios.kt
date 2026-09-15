package com.codeforcesvisualizer.inject

import androidx.room.Room
import androidx.room.RoomDatabase
import com.codeforcesvisualizer.shared.data.local.CFDatabase
import com.codeforcesvisualizer.shared.data.local.DATABASE_FILE_NAME
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual fun createDatabaseBuilder(): RoomDatabase.Builder<CFDatabase> {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null
    )
    val path = requireNotNull(documentDirectory?.path) { "Documents directory is unavailable" }
    return Room.databaseBuilder<CFDatabase>(name = "$path/$DATABASE_FILE_NAME")
}
