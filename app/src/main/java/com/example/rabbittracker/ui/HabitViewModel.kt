package com.example.rabbittracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rabbittracker.data.Habit
import com.example.rabbittracker.data.HabitLog
import com.example.rabbittracker.data.HabitRepository
import com.example.rabbittracker.data.UserPreferencesRepository
import com.example.rabbittracker.data.room.AppDatabase
import com.example.rabbittracker.data.room.DayNoteEntity
import com.example.rabbittracker.data.toEntity
import com.example.rabbittracker.data.toHabit
import com.example.rabbittracker.data.toLog
import com.example.rabbittracker.utils.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HabitViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = HabitRepository(AppDatabase.getDatabase(app))
    private val prefsRepo = UserPreferencesRepository(app)

    val today: String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    val onboardingCompleted: StateFlow<Boolean?> = prefsRepo.onboardingCompletedFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userName: StateFlow<String> = prefsRepo.userNameFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val userPhotoPath: StateFlow<String> = prefsRepo.userPhotoPathFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val remindersEnabled: StateFlow<Boolean> = prefsRepo.remindersEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val reminderTime: StateFlow<String> = prefsRepo.reminderTimeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "08:00 AM")

    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    private val _logs = MutableStateFlow<Map<Int, HabitLog>>(emptyMap())
    val logs: StateFlow<Map<Int, HabitLog>> = _logs.asStateFlow()

    private val _dayNote = MutableStateFlow("")
    val dayNote: StateFlow<String> = _dayNote.asStateFlow()

    private val _dateLogs = MutableStateFlow<Pair<String, Map<Int, HabitLog>>>("" to emptyMap())
    val dateLogs: StateFlow<Pair<String, Map<Int, HabitLog>>> = _dateLogs.asStateFlow()

    private val _recentLogs = MutableStateFlow<Map<String, Map<Int, HabitLog>>>(emptyMap())
    val recentLogs: StateFlow<Map<String, Map<Int, HabitLog>>> = _recentLogs.asStateFlow()

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    private val _totalCompletions = MutableStateFlow(0)
    val totalCompletions: StateFlow<Int> = _totalCompletions.asStateFlow()

    init {
        viewModelScope.launch {
            logs.collect {
                try {
                    val db = AppDatabase.getDatabase(getApplication())
                    val cursor = db.openHelper.readableDatabase.query(
                        androidx.sqlite.db.SimpleSQLiteQuery("SELECT COUNT(*) FROM habit_logs WHERE completed = 1")
                    )
                    if (cursor.moveToFirst()) {
                        _totalCompletions.value = cursor.getInt(0)
                    }
                    cursor.close()
                } catch (_: Exception) {}
            }
        }
    }

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.setRemindersEnabled(enabled)
            val context = getApplication<Application>()
            if (enabled) {
                rescheduleAllReminders()
            } else {
                for (habit in _habits.value) {
                    NotificationHelper.cancelHabitAlarm(context, habit.id)
                }
                NotificationHelper.cancelHabitAlarm(context, 900001)
            }
        }
    }

    fun setReminderTime(time: String) {
        viewModelScope.launch {
            prefsRepo.setReminderTime(time)
            val context = getApplication<Application>()
            if (remindersEnabled.value) {
                val (hour, minute) = NotificationHelper.parseReminderTime(time)
                NotificationHelper.scheduleHabitAlarm(
                    context,
                    900001,
                    "Rabbit Habit Tracker",
                    hour,
                    minute
                )
            } else {
                NotificationHelper.cancelHabitAlarm(context, 900001)
            }
        }
    }

    fun rescheduleAllReminders() {
        val context = getApplication<Application>()
        if (!remindersEnabled.value) return
        for (habit in _habits.value) {
            if (habit.reminderEnabled) {
                val (hour, minute) = NotificationHelper.parseReminderTime(habit.reminderTime)
                NotificationHelper.scheduleHabitAlarm(
                    context,
                    habit.id,
                    habit.name,
                    hour,
                    minute
                )
            } else {
                NotificationHelper.cancelHabitAlarm(context, habit.id)
            }
        }
        val (gHour, gMinute) = NotificationHelper.parseReminderTime(reminderTime.value)
        NotificationHelper.scheduleHabitAlarm(
            context,
            900001,
            "Rabbit Habit Tracker",
            gHour,
            gMinute
        )
    }

    fun setOnboardingCompleted(completed: Boolean) {
        viewModelScope.launch {
            prefsRepo.setOnboardingCompleted(completed)
        }
    }

    fun saveProfile(name: String, photoUri: android.net.Uri?, context: android.content.Context) {
        viewModelScope.launch {
            prefsRepo.setUserName(name.trim())
            if (photoUri != null) {
                try {
                    val file = java.io.File(context.filesDir, "profile.jpg")
                    context.contentResolver.openInputStream(photoUri)?.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    prefsRepo.setUserPhotoPath(file.absolutePath)
                } catch (_: Exception) {}
            }
        }
    }

    fun setUserName(name: String) {
        viewModelScope.launch {
            prefsRepo.setUserName(name.trim())
        }
    }

    fun saveProfilePhoto(context: android.content.Context, photoUri: android.net.Uri) {
        saveProfile(userName.value, photoUri, context)
    }

    fun removeProfilePhoto(context: android.content.Context) {
        viewModelScope.launch {
            try {
                val file = java.io.File(context.filesDir, "profile.jpg")
                if (file.exists()) file.delete()
            } catch (_: Exception) {}
            prefsRepo.setUserPhotoPath("")
        }
    }

    fun testNotification(context: android.content.Context) {
        NotificationHelper.showReminderNotification(
            context,
            "Rabbit Tracker Reminder",
            "Time to check your habits and maintain your streak! 🐰"
        )
    }

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _habits.value = repo.getAllHabits().map { it.toHabit() }
            _logs.value = repo.getLogsForDate(today).associate { it.habitId to it.toLog() }
            _dayNote.value = repo.getDayNote(today)?.text ?: ""
            _loaded.value = true
            rescheduleAllReminders()
        }
    }

    fun loadLogsFor(date: String) {
        viewModelScope.launch {
            _dateLogs.value = date to repo.getLogsForDate(date).associate { it.habitId to it.toLog() }
        }
    }

    fun loadRecentLogs(days: Int = 7) {
        viewModelScope.launch {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val cal = java.util.Calendar.getInstance()
            val map = mutableMapOf<String, Map<Int, HabitLog>>()
            for (i in 0 until days) {
                val key = fmt.format(cal.time)
                map[key] = repo.getLogsForDate(key).associate { it.habitId to it.toLog() }
                cal.add(java.util.Calendar.DAY_OF_MONTH, -1)
            }
            _recentLogs.value = map
        }
    }

    fun addHabit(habit: Habit) {
        viewModelScope.launch {
            val rowId = repo.insertHabit(habit.toEntity())
            val newId = rowId.toInt()
            refresh()
            val context = getApplication<Application>()
            if (remindersEnabled.value && habit.reminderEnabled && newId > 0) {
                val (hour, minute) = NotificationHelper.parseReminderTime(habit.reminderTime)
                NotificationHelper.scheduleHabitAlarm(
                    context,
                    newId,
                    habit.name,
                    hour,
                    minute
                )
            } else if (newId > 0) {
                NotificationHelper.cancelHabitAlarm(context, newId)
            }
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            repo.updateHabit(habit.toEntity())
            refresh()
            val context = getApplication<Application>()
            if (remindersEnabled.value && habit.reminderEnabled) {
                val (hour, minute) = NotificationHelper.parseReminderTime(habit.reminderTime)
                NotificationHelper.scheduleHabitAlarm(
                    context,
                    habit.id,
                    habit.name,
                    hour,
                    minute
                )
            } else {
                NotificationHelper.cancelHabitAlarm(context, habit.id)
            }
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            NotificationHelper.cancelHabitAlarm(context, habit.id)
            repo.deleteLogsForHabit(habit.id)
            repo.deleteHabit(habit.toEntity())
            refresh()
        }
    }

    private val adaptiveStore by lazy { com.example.rabbittracker.data.AdaptiveReminderStore(getApplication()) }

    fun dismissAdaptiveSuggestion(habitId: Int) {
        viewModelScope.launch {
            adaptiveStore.dismissSuggestion(habitId)
        }
    }

    fun getAdaptiveSuggestionFlow(habit: Habit): kotlinx.coroutines.flow.Flow<com.example.rabbittracker.utils.AdaptiveSuggestion?> {
        return kotlinx.coroutines.flow.combine(
            adaptiveStore.getCompletionTimesFlow(habit.id),
            adaptiveStore.getDismissalTimestampFlow(habit.id),
            remindersEnabled
        ) { times, dismissedMillis, globalReminders ->
            com.example.rabbittracker.utils.AdaptiveReminderEngine.evaluateSuggestion(
                habit = habit,
                globalRemindersEnabled = globalReminders,
                completionTimes = times,
                lastDismissedMillis = dismissedMillis
            )
        }
    }

    fun applyAdaptiveSuggestion(habit: Habit, suggestedTime: String) {
        viewModelScope.launch {
            adaptiveStore.clearSuggestionData(habit.id)
            updateHabit(habit.copy(reminderTime = suggestedTime))
        }
    }

    fun toggleHabit(habitId: Int) {
        viewModelScope.launch {
            val current = repo.getLog(habitId, today)
            val completed = !(current?.completed ?: false)
            if (completed) {
                adaptiveStore.recordCompletion(habitId)
            }
            repo.saveLog(
                com.example.rabbittracker.data.room.HabitLogEntity(
                    habitId = habitId,
                    date = today,
                    completed = completed,
                    note = current?.note ?: ""
                )
            )
            refresh()
        }
    }

    fun saveDayNote(text: String) {
        viewModelScope.launch {
            repo.saveDayNote(DayNoteEntity(date = today, text = text))
            _dayNote.value = text
        }
    }

    fun habitById(id: Int): Habit? = _habits.value.find { it.id == id }

    fun logFor(habitId: Int): HabitLog? = _logs.value[habitId]
}
