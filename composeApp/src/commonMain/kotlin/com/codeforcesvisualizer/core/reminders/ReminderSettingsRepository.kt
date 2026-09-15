package com.codeforcesvisualizer.core.reminders

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Which contests the user wants reminders for, and how early. These are user choices, so they
 * live in DataStore rather than the API cache, which is rebuilt whenever its schema changes.
 */
class ReminderSettingsRepository(
    private val dataStore: DataStore<Preferences>
) {
    private val contestIdsKey = stringSetPreferencesKey("reminder_contest_ids")
    private val leadTimesKey = stringSetPreferencesKey("reminder_lead_times")

    val remindedContestIds: Flow<Set<Int>> = dataStore.data.map { prefs ->
        prefs[contestIdsKey].orEmpty().mapNotNull { it.toIntOrNull() }.toSet()
    }

    /** [ReminderLeadTime.Default] until the user changes it; can be empty. */
    val leadTimes: Flow<Set<ReminderLeadTime>> = dataStore.data.map { prefs ->
        prefs[leadTimesKey]
            ?.mapNotNull { name -> ReminderLeadTime.entries.find { it.name == name } }
            ?.toSet()
            ?: ReminderLeadTime.Default
    }

    suspend fun setReminder(contestId: Int, enabled: Boolean) {
        dataStore.edit { prefs ->
            val ids = prefs[contestIdsKey].orEmpty()
            prefs[contestIdsKey] = if (enabled) ids + contestId.toString() else ids - contestId.toString()
        }
    }

    suspend fun removeReminders(contestIds: Set<Int>) {
        dataStore.edit { prefs ->
            prefs[contestIdsKey] = prefs[contestIdsKey].orEmpty() - contestIds.map { it.toString() }.toSet()
        }
    }

    suspend fun setLeadTime(leadTime: ReminderLeadTime, enabled: Boolean) {
        dataStore.edit { prefs ->
            val current = prefs[leadTimesKey] ?: ReminderLeadTime.Default.map { it.name }.toSet()
            prefs[leadTimesKey] = if (enabled) current + leadTime.name else current - leadTime.name
        }
    }
}
