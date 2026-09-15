package com.codeforcesvisualizer.core.ratingalerts

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** The last rated contest the user was told about, for one handle. */
data class SeenRating(val handle: String, val contestId: Int)

/** Whether rating change alerts are on, and which change was seen last. */
class RatingAlertSettingsRepository(
    private val dataStore: DataStore<Preferences>
) {
    private val enabledKey = booleanPreferencesKey("rating_alerts_enabled")
    private val lastSeenKey = stringPreferencesKey("rating_alerts_last_seen")

    val enabled: Flow<Boolean> = dataStore.data.map { prefs -> prefs[enabledKey] ?: false }

    val lastSeen: Flow<SeenRating?> = dataStore.data.map { prefs ->
        val value = prefs[lastSeenKey] ?: return@map null
        val handle = value.substringBeforeLast(SEPARATOR)
        val contestId = value.substringAfterLast(SEPARATOR).toIntOrNull() ?: return@map null
        SeenRating(handle, contestId)
    }

    suspend fun setEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[enabledKey] = enabled }
    }

    suspend fun setLastSeen(seen: SeenRating) {
        dataStore.edit { prefs -> prefs[lastSeenKey] = "${seen.handle}$SEPARATOR${seen.contestId}" }
    }

    private companion object {
        // Codeforces handles can't contain a space.
        const val SEPARATOR = " "
    }
}
