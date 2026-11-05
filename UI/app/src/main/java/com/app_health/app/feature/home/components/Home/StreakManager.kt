package com.app_health.app.feature.home.components.Home


import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.streakDataStore by preferencesDataStore(name = "streak_prefs")

class StreakManager(private val context: Context, private val userEmail: String? = null) {

    private val userKeySuffix = userEmail?.let { "_$it" } ?: ""
    private val KEY_STREAK_COUNT = intPreferencesKey("key_streak_count$userKeySuffix")
    private val KEY_LAST_ACTIVE_DATE = stringPreferencesKey("key_last_active_date$userKeySuffix")

    val streakCountFlow: Flow<Int> = context.streakDataStore.data.map { preferences ->
        val lastDateStr = preferences[KEY_LAST_ACTIVE_DATE]
        val currentStreak = preferences[KEY_STREAK_COUNT] ?: 0

        if (lastDateStr != null) {
            val lastDate = LocalDate.parse(lastDateStr)
            val today = LocalDate.now()

            when {
                lastDate == today -> currentStreak
                lastDate == today.minusDays(1) -> currentStreak
                else -> 0 // Đứt chuỗi nếu cách >= 2 ngày
            }
        } else {
            0
        }
    }

    // Tự động kiểm tra và cập nhật streak khi mở app hoặc có hành động mới
    suspend fun checkAndUpdateStreak() {
        val today = LocalDate.now()
        context.streakDataStore.edit { preferences ->
            val lastDateStr = preferences[KEY_LAST_ACTIVE_DATE]
            val currentStreak = preferences[KEY_STREAK_COUNT] ?: 0

            if (lastDateStr == null) {
                preferences[KEY_STREAK_COUNT] = 1
                preferences[KEY_LAST_ACTIVE_DATE] = today.toString()
            } else {
                val lastDate = LocalDate.parse(lastDateStr)
                when {
                    lastDate == today -> {
                        // Đã xử lý trong ngày hôm nay rồi thì bỏ qua
                    }
                    lastDate == today.minusDays(1) -> {
                        // Sang ngày mới liên tiếp -> Tăng chuỗi lên 1 tự động
                        preferences[KEY_STREAK_COUNT] = currentStreak + 1
                        preferences[KEY_LAST_ACTIVE_DATE] = today.toString()
                    }
                    else -> {
                        // Bỏ lỡ quá 1 ngày -> Reset về 1
                        preferences[KEY_STREAK_COUNT] = 1
                        preferences[KEY_LAST_ACTIVE_DATE] = today.toString()
                    }
                }
            }
        }
    }
}