package com.codeforcesvisualizer.shared.data

import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteDriver
import com.codeforcesvisualizer.shared.data.local.CFDatabase
import com.codeforcesvisualizer.shared.data.local.buildCFDatabase

/**
 * Base class for tests that use a real Room database. On Android it runs the test under
 * Robolectric, which provides the Context Room needs; on iOS it is a plain class.
 */
expect abstract class DatabaseTest()

expect fun inMemoryDatabaseBuilder(): RoomDatabase.Builder<CFDatabase>

/**
 * The bundled SQLite driver only ships Android and iOS native libraries, so Android host tests
 * use the framework driver that Robolectric supports.
 */
expect fun testSQLiteDriver(): SQLiteDriver

/** An empty database configured the same way as the app's, apart from the driver. */
fun inMemoryDatabase(): CFDatabase = buildCFDatabase(inMemoryDatabaseBuilder(), testSQLiteDriver())
