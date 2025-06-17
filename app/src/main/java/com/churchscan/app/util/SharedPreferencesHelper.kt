package com.churchscan.app.util

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesHelper(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("church_prefs", Context.MODE_PRIVATE)

    fun saveRecentSearch(query: String) {
        val current = getRecentSearches().toMutableList()
        current.remove(query)
        current.add(0, query)
        while (current.size > 5) current.removeLast()

        prefs.edit().putString("recent_searches", current.joinToString("|")).apply()
    }

    fun getRecentSearches(): List<String> {
        val saved = prefs.getString("recent_searches", "") ?: ""
        return if (saved.isNotBlank()) saved.split("|") else emptyList()
    }
}
