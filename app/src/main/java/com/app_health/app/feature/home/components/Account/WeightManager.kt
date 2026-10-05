package com.app_health.app.core.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.app_health.app.feature.home.components.LoginLogout.SharedDataStoreProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WeightManager(private val context: Context, private val userEmail: String? = null) {

    // 1. Chỉ gọi DataStore từ SharedDataStoreProvider, KHÔNG tạo cache hay factory riêng ở đây nữa
    private val dataStore: DataStore<Preferences> = SharedDataStoreProvider.get(context, userEmail)

    // 2. Chỉ giữ lại 1 companion object duy nhất
    companion object {
        private val KEY_CURRENT_WEIGHT = floatPreferencesKey("key_weight")
        private val KEY_INITIAL_WEIGHT = floatPreferencesKey("key_initial_weight")
        private val KEY_TARGET_WEIGHT = floatPreferencesKey("key_target_weight")
        private val KEY_WEIGHT_HISTORY = stringPreferencesKey("key_weight_history")
    }

    val currentWeightFlow: Flow<Float> = dataStore.data.map { it[KEY_CURRENT_WEIGHT] ?: 0f }
    val initialWeightFlow: Flow<Float> = dataStore.data.map { it[KEY_INITIAL_WEIGHT] ?: it[KEY_CURRENT_WEIGHT] ?: 0f }
    val targetWeightFlow: Flow<Float> = dataStore.data.map { it[KEY_TARGET_WEIGHT] ?: 0f }

    val weightHistoryFlow: Flow<List<Float>> = dataStore.data.map { preferences ->
        val historyStr = preferences[KEY_WEIGHT_HISTORY] ?: ""
        if (historyStr.isBlank()) {
            val current = preferences[KEY_CURRENT_WEIGHT] ?: 0f
            if (current > 0f) listOf(current) else emptyList()
        } else {
            historyStr.split(",").mapNotNull { it.toFloatOrNull() }
        }
    }

    suspend fun updateCurrentWeight(newWeight: Float) {
        dataStore.edit { preferences ->
            preferences[KEY_CURRENT_WEIGHT] = newWeight
            val currentInitial = preferences[KEY_INITIAL_WEIGHT] ?: 0f
            if (currentInitial == 0f && newWeight > 0f) {
                preferences[KEY_INITIAL_WEIGHT] = newWeight
            }
            val historyStr = preferences[KEY_WEIGHT_HISTORY] ?: ""
            val list = if (historyStr.isBlank()) mutableListOf(newWeight) else historyStr.split(",").mapNotNull { it.toFloatOrNull() }.toMutableList()
            if (list.lastOrNull() != newWeight) {
                list.add(newWeight)
            }
            preferences[KEY_WEIGHT_HISTORY] = list.joinToString(",")
        }
    }

    suspend fun saveWeightMetrics(currentWeight: Float, targetWeight: Float = 0f) {
        dataStore.edit { preferences ->
            preferences[KEY_CURRENT_WEIGHT] = currentWeight
            val currentInitial = preferences[KEY_INITIAL_WEIGHT] ?: 0f
            if (currentInitial == 0f && currentWeight > 0f) {
                preferences[KEY_INITIAL_WEIGHT] = currentWeight
            }
            if (targetWeight > 0f) {
                preferences[KEY_TARGET_WEIGHT] = targetWeight
            }
            val historyStr = preferences[KEY_WEIGHT_HISTORY] ?: ""
            val list = if (historyStr.isBlank()) mutableListOf(currentWeight) else historyStr.split(",").mapNotNull { it.toFloatOrNull() }.toMutableList()
            if (!list.contains(currentWeight)) {
                list.add(currentWeight)
                preferences[KEY_WEIGHT_HISTORY] = list.joinToString(",")
            }
        }
    }

    suspend fun setupNewGoal(initialWeight: Float, targetWeight: Float) {
        dataStore.edit { preferences ->
            preferences[KEY_CURRENT_WEIGHT] = initialWeight
            preferences[KEY_INITIAL_WEIGHT] = initialWeight
            preferences[KEY_TARGET_WEIGHT] = targetWeight
            preferences[KEY_WEIGHT_HISTORY] = initialWeight.toString()
        }
    }
}