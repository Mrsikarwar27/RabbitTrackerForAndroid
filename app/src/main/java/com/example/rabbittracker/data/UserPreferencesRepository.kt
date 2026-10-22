package com.example.rabbittracker.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_PHOTO_PATH = stringPreferencesKey("user_photo_path")
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val REMINDER_TIME = stringPreferencesKey("reminder_time")
    }

    val onboardingCompletedFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
        }

    val userNameFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_NAME] ?: ""
        }

    val userPhotoPathFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_PHOTO_PATH] ?: ""
        }

    val remindersEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.REMINDERS_ENABLED] ?: true
        }

    val reminderTimeFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.REMINDER_TIME] ?: "08:00 AM"
        }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
        }
    }

    suspend fun setUserPhotoPath(path: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_PHOTO_PATH] = path
        }
    }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDERS_ENABLED] = enabled
        }
    }

    suspend fun setReminderTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDER_TIME] = time
        }
    }
}
