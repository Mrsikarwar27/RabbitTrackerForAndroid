package com.example.rabbittracker.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdaptiveReminderStore(private val context: Context) {

    suspend fun recordCompletion(habitId: Int) {
        val key = stringPreferencesKey("adaptive_completions_$habitId")
        val nowTimeStr = SimpleDateFormat("HH:mm", Locale.US).format(Date())
        context.dataStore.edit { prefs ->
            val existingRaw = prefs[key] ?: ""
            val list = if (existingRaw.isBlank()) mutableListOf() else existingRaw.split(",").toMutableList()
            list.add(nowTimeStr)
            val trimmed = if (list.size > 14) list.takeLast(14) else list
            prefs[key] = trimmed.joinToString(",")
        }
    }

    fun getCompletionTimesFlow(habitId: Int): Flow<List<String>> {
        val key = stringPreferencesKey("adaptive_completions_$habitId")
        return context.dataStore.data.map { prefs ->
            val raw = prefs[key] ?: ""
            if (raw.isBlank()) emptyList() else raw.split(",")
        }
    }

    suspend fun dismissSuggestion(habitId: Int) {
        val key = longPreferencesKey("adaptive_dismissal_$habitId")
        context.dataStore.edit { prefs ->
            prefs[key] = System.currentTimeMillis()
        }
    }

    fun getDismissalTimestampFlow(habitId: Int): Flow<Long> {
        val key = longPreferencesKey("adaptive_dismissal_$habitId")
        return context.dataStore.data.map { prefs ->
            prefs[key] ?: 0L
        }
    }

    suspend fun clearSuggestionData(habitId: Int) {
        val completionsKey = stringPreferencesKey("adaptive_completions_$habitId")
        val dismissalKey = longPreferencesKey("adaptive_dismissal_$habitId")
        context.dataStore.edit { prefs ->
            prefs.remove(completionsKey)
            prefs.remove(dismissalKey)
        }
    }
}
