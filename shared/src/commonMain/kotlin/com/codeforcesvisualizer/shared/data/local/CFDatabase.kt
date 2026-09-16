package com.codeforcesvisualizer.shared.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

const val DATABASE_FILE_NAME = "cfclimb.db"

@Database(
    entities = [
        ContestEntity::class,
        UserEntity::class,
        RatingChangeEntity::class,
        SubmissionEntity::class,
        FetchTimeEntity::class,
        ProblemEntity::class,
    ],
    version = 2,
)
@TypeConverters(StringListConverter::class)
@ConstructedBy(CFDatabaseConstructor::class)
abstract class CFDatabase : RoomDatabase() {
    abstract fun contestDao(): ContestDao
    abstract fun profileDao(): ProfileDao
    abstract fun problemDao(): ProblemDao
}

// Room generates the actual implementations for each platform.
@Suppress("KotlinNoActualForExpect")
expect object CFDatabaseConstructor : RoomDatabaseConstructor<CFDatabase> {
    override fun initialize(): CFDatabase
}

/**
 * Finishes a platform-specific [builder]. The database only caches API data, so a schema change
 * rebuilds it instead of requiring migrations; the next refresh fills it again.
 *
 * @param driver the bundled SQLite build by default, so every device runs the same SQLite version.
 */
fun buildCFDatabase(
    builder: RoomDatabase.Builder<CFDatabase>,
    driver: SQLiteDriver = BundledSQLiteDriver(),
): CFDatabase {
    return builder
        .setDriver(driver)
        .setQueryCoroutineContext(Dispatchers.IO)
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
}

internal class StringListConverter {
    private val serializer = ListSerializer(String.serializer())

    @TypeConverter
    fun fromList(values: List<String>): String = Json.encodeToString(serializer, values)

    @TypeConverter
    fun toList(json: String): List<String> = Json.decodeFromString(serializer, json)
}
