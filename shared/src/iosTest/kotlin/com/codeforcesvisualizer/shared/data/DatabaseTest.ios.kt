package com.codeforcesvisualizer.shared.data

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.codeforcesvisualizer.shared.data.local.CFDatabase

actual abstract class DatabaseTest actual constructor()

actual fun inMemoryDatabaseBuilder(): RoomDatabase.Builder<CFDatabase> {
    return Room.inMemoryDatabaseBuilder<CFDatabase>()
}

actual fun testSQLiteDriver(): SQLiteDriver = BundledSQLiteDriver()
