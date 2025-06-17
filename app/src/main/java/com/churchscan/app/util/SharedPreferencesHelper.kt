package com.churchscan.app.util

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesHelper(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("search_history", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_RECENT = "recent_searches"
    }

    // ✅ 최근 검색어 저장 (중복 제거)
    fun saveRecentSearch(query: String) {
        val recentSet = getRecentSearches().toMutableSet()
        recentSet.add(query)
        prefs.edit().putStringSet(KEY_RECENT, recentSet).apply()
    }

    // ✅ 전체 목록 불러오기
    fun getRecentSearches(): Set<String> {
        return prefs.getStringSet(KEY_RECENT, emptySet()) ?: emptySet()
    }

    // ✅ 개별 검색어 삭제
    fun removeSearch(query: String) {
        val recentSet = getRecentSearches().toMutableSet()
        recentSet.remove(query)
        prefs.edit().putStringSet(KEY_RECENT, recentSet).apply()
    }

    // ✅ 전체 검색어 삭제
    fun clearAllSearches() {
        prefs.edit().remove(KEY_RECENT).apply()
    }
}
