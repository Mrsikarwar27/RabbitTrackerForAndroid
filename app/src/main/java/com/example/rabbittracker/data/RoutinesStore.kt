package com.example.rabbittracker.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class Routine(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val habitIds: List<Int>
)

class RoutinesStore(private val context: Context) {
    private val ROUTINES_KEY = stringPreferencesKey("saved_routines_json")

    val routinesFlow: Flow<List<Routine>> = context.dataStore.data.map { prefs ->
        val raw = prefs[ROUTINES_KEY] ?: ""
        if (raw.isBlank()) emptyList()
        else {
            try {
                val jsonArray = JSONArray(raw)
                val list = mutableListOf<Routine>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val id = obj.optString("id", UUID.randomUUID().toString())
                    val name = obj.optString("name", "")
                    val idsArr = obj.getJSONArray("habitIds")
                    val habitIds = mutableListOf<Int>()
                    for (j in 0 until idsArr.length()) {
                        habitIds.add(idsArr.getInt(j))
                    }
                    if (name.isNotBlank() && habitIds.isNotEmpty()) {
                        list.add(Routine(id = id, name = name, habitIds = habitIds))
                    }
                }
                list
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    suspend fun saveRoutine(routine: Routine) {
        context.dataStore.edit { prefs ->
            val raw = prefs[ROUTINES_KEY] ?: ""
            val jsonArray = if (raw.isBlank()) JSONArray() else JSONArray(raw)
            var found = false
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.optString("id", "")
                if (id == routine.id) {
                    found = true
                    val updatedObj = JSONObject().apply {
                        put("id", routine.id)
                        put("name", routine.name)
                        put("habitIds", JSONArray(routine.habitIds))
                    }
                    jsonArray.put(i, updatedObj)
                }
            }
            if (!found) {
                val newObj = JSONObject().apply {
                    put("id", routine.id)
                    put("name", routine.name)
                    put("habitIds", JSONArray(routine.habitIds))
                }
                jsonArray.put(newObj)
            }
            prefs[ROUTINES_KEY] = jsonArray.toString()
        }
    }

    suspend fun deleteRoutine(routineId: String) {
        context.dataStore.edit { prefs ->
            val raw = prefs[ROUTINES_KEY] ?: ""
            if (raw.isNotBlank()) {
                try {
                    val jsonArray = JSONArray(raw)
                    val newArray = JSONArray()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        if (obj.optString("id") != routineId) {
                            newArray.put(obj)
                        }
                    }
                    prefs[ROUTINES_KEY] = newArray.toString()
                } catch (_: Exception) {}
            }
        }
    }
}
