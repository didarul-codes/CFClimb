package com.codeforcesvisualizer.shared.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.codeforcesvisualizer.shared.data.local.CFDatabase
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

// SDK 34 keeps Robolectric on Java 17+; SDK 35 and up need Java 21.
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
actual abstract class DatabaseTest actual constructor()

actual fun inMemoryDatabaseBuilder(): RoomDatabase.Builder<CFDatabase> {
    return Room.inMemoryDatabaseBuilder<CFDatabase>(ApplicationProvider.getApplicationContext<Context>())
}

actual fun testSQLiteDriver(): SQLiteDriver = AndroidSQLiteDriver()
