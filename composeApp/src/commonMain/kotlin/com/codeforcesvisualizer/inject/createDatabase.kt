package com.codeforcesvisualizer.inject

import androidx.room.RoomDatabase
import com.codeforcesvisualizer.shared.data.local.CFDatabase

/** Creates the platform-specific builder; `buildCFDatabase` finishes the configuration. */
expect fun createDatabaseBuilder(): RoomDatabase.Builder<CFDatabase>
