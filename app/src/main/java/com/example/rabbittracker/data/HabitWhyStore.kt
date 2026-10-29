package com.example.rabbittracker.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

class HabitWhyStore(private val context: Context) {

    fun getWhyTextFlow(habitId: Int): Flow<String> {
        val key = stringPreferencesKey("habit_why_text_$habitId")
        return context.dataStore.data.map { prefs ->
            prefs[key] ?: ""
        }
    }

    fun getWhyPhotoPathFlow(habitId: Int): Flow<String> {
        val key = stringPreferencesKey("habit_why_photo_$habitId")
        return context.dataStore.data.map { prefs ->
            prefs[key] ?: ""
        }
    }

    suspend fun saveWhyText(habitId: Int, text: String) {
        val key = stringPreferencesKey("habit_why_text_$habitId")
        context.dataStore.edit { prefs ->
            prefs[key] = text.take(120).trim()
        }
    }

    suspend fun saveWhyPhoto(habitId: Int, photoUri: Uri?): String {
        val key = stringPreferencesKey("habit_why_photo_$habitId")
        if (photoUri == null) return ""

        val path = try {
            val inputStream = context.contentResolver.openInputStream(photoUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            if (bitmap != null) {
                val maxEdge = 1024
                val w = bitmap.width
                val h = bitmap.height
                val (newW, newH) = if (w > maxEdge || h > maxEdge) {
                    if (w >= h) {
                        maxEdge to (h * maxEdge / w)
                    } else {
                        (w * maxEdge / h) to maxEdge
                    }
                } else {
                    w to h
                }

                val scaled = Bitmap.createScaledBitmap(bitmap, newW, newH, true)
                val file = File(context.filesDir, "why_$habitId.jpg")
                file.outputStream().use { out ->
                    scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
                }
                if (scaled != bitmap) bitmap.recycle()
                file.absolutePath
            } else ""
        } catch (_: Exception) {
            ""
        }

        context.dataStore.edit { prefs ->
            prefs[key] = path
        }
        return path
    }

    suspend fun deleteWhyPhoto(habitId: Int) {
        val key = stringPreferencesKey("habit_why_photo_$habitId")
        context.dataStore.edit { prefs ->
            val oldPath = prefs[key]
            if (!oldPath.isNullOrEmpty()) {
                try {
                    val file = File(oldPath)
                    if (file.exists()) file.delete()
                } catch (_: Exception) {}
            }
            prefs.remove(key)
        }
    }

    suspend fun deleteWhy(habitId: Int) {
        val textKey = stringPreferencesKey("habit_why_text_$habitId")
        deleteWhyPhoto(habitId)
        context.dataStore.edit { prefs ->
            prefs.remove(textKey)
        }
    }
}
