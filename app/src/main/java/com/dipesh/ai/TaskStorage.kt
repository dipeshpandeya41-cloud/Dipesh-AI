package com.dipesh.ai

import android.content.SharedPreferences
import org.json.JSONArray

/**
 * Small persistence boundary for the on-device task list.
 * Keeps JSON parsing and preference writes out of the activity.
 */
internal class TaskStorage(private val preferences: SharedPreferences) {
    fun load(): JSONArray {
        return try {
            JSONArray(preferences.getString(KEY_TASKS, EMPTY_TASKS))
        } catch (_: Exception) {
            JSONArray()
        }
    }

    fun save(tasks: JSONArray) {
        preferences.edit().putString(KEY_TASKS, tasks.toString()).apply()
    }

    private companion object {
        const val KEY_TASKS = "local_tasks"
        const val EMPTY_TASKS = "[]"
    }
}
