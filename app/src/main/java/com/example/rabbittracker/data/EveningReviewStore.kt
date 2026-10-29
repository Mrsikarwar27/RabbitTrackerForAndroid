package com.example.rabbittracker.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EveningReviewStore(private val context: Context) {

    private object Keys {
        val FIRST_STEP_HABIT_ID = intPreferencesKey("evening_first_step_habit_id")
        val FIRST_STEP_DATE = stringPreferencesKey("evening_first_step_date")
        val LAST_REVIEWED_DATE = stringPreferencesKey("evening_last_reviewed_date")
    }

    val firstStepHabitIdFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.FIRST_STEP_HABIT_ID] ?: -1
    }

    val firstStepDateFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.FIRST_STEP_DATE] ?: ""
    }

    val lastReviewedDateFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.LAST_REVIEWED_DATE] ?: ""
    }

    suspend fun saveReview(firstStepHabitId: Int, tomorrowDate: String, todayDate: String) {
        context.dataStore.edit { prefs ->
            if (firstStepHabitId > 0 && tomorrowDate.isNotBlank()) {
                prefs[Keys.FIRST_STEP_HABIT_ID] = firstStepHabitId
                prefs[Keys.FIRST_STEP_DATE] = tomorrowDate
            }
            prefs[Keys.LAST_REVIEWED_DATE] = todayDate
        }
    }
}
