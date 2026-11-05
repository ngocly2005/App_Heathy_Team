package com.app_health.app.feature.home.components.LoginLogout

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 1. QUẢN LÝ DATASTORE DÙNG CHUNG CÓ KHÓA AN TOÀN
object SharedDataStoreProvider {
    private val cache = mutableMapOf<String, DataStore<Preferences>>()
    private val lock = Any()

    fun get(context: Context, email: String?): DataStore<Preferences> {
        val name = if (email.isNullOrBlank()) "auth_prefs_guest" else "auth_prefs_$email"
        synchronized(lock) {
            return cache.getOrPut(name) {
                PreferenceDataStoreFactory.create(
                    produceFile = { context.preferencesDataStoreFile(name) }
                )
            }
        }
    }
}

class DataAuthManager(private val context: Context, private val userEmail: String? = null) {
    private val dataStore: DataStore<Preferences> = SharedDataStoreProvider.get(context, userEmail)

    companion object {
        private val KEY_NAME = stringPreferencesKey("key_name")
        private val KEY_EMAIL = stringPreferencesKey("key_email")
        private val KEY_PASSWORD = stringPreferencesKey("key_password")
        private val KEY_AGE = intPreferencesKey("key_age")
        private val KEY_HEIGHT = floatPreferencesKey("key_height")
        private val KEY_WEIGHT = floatPreferencesKey("key_weight")
        private val KEY_INITIAL_WEIGHT = floatPreferencesKey("key_initial_weight")
        private val KEY_WEIGHT_HISTORY = stringPreferencesKey("key_weight_history")
        private val KEY_GENDER = stringPreferencesKey("key_gender")
        private val KEY_WAIST = floatPreferencesKey("key_waist")
        private val KEY_CHEST = floatPreferencesKey("key_chest")
        private val KEY_HIPS = floatPreferencesKey("key_hips")
        private val KEY_GOAL_TYPE = stringPreferencesKey("key_goal_type")
        private val KEY_TARGET_WEIGHT = floatPreferencesKey("key_target_weight")
        private val KEY_ACTIVITY_LEVEL = stringPreferencesKey("key_activity_level")
        private val KEY_CUSTOM_CALORIES = floatPreferencesKey("key_custom_calories")
        private val KEY_PROTEIN_PERCENT = floatPreferencesKey("key_protein_percent")
        private val KEY_CARBS_PERCENT = floatPreferencesKey("key_carbs_percent")
        private val KEY_FAT_PERCENT = floatPreferencesKey("key_fat_percent")
    }

    suspend fun saveUser(name: String, email: String, pass: String) {
        dataStore.edit { preferences ->
            preferences[KEY_NAME] = name
            preferences[KEY_EMAIL] = email
            preferences[KEY_PASSWORD] = pass
            preferences[KEY_AGE] = 0
            preferences[KEY_HEIGHT] = 0f
            preferences[KEY_WEIGHT] = 0f
            preferences[KEY_INITIAL_WEIGHT] = 0f
            preferences[KEY_WEIGHT_HISTORY] = ""
            preferences[KEY_GENDER] = ""
            preferences[KEY_WAIST] = 0f
            preferences[KEY_CHEST] = 0f
            preferences[KEY_HIPS] = 0f
            preferences[KEY_TARGET_WEIGHT] = 0f
            preferences[KEY_GOAL_TYPE] = "Giảm cân"
            preferences[KEY_ACTIVITY_LEVEL] = ""
        }
    }

    suspend fun saveFullMetrics(
        age: Int, height: Float, weight: Float, gender: String, waist: Float, chest: Float, hips: Float, activityLevel: String
    ) {
        dataStore.edit { preferences ->
            preferences[KEY_AGE] = age
            preferences[KEY_HEIGHT] = height

            val currentInitial = preferences[KEY_INITIAL_WEIGHT] ?: 0f
            if (currentInitial == 0f && weight > 0f) {
                preferences[KEY_INITIAL_WEIGHT] = weight
            }

            preferences[KEY_WEIGHT] = weight
            preferences[KEY_GENDER] = gender
            preferences[KEY_WAIST] = waist
            preferences[KEY_CHEST] = chest
            preferences[KEY_HIPS] = hips
            preferences[KEY_ACTIVITY_LEVEL] = activityLevel
        }
        if (weight > 0f) {
            addWeightHistory(weight)
        }
    }

    suspend fun saveUserMetrics(age: Int, height: Float, weight: Float) {
        dataStore.edit { preferences ->
            preferences[KEY_AGE] = age
            preferences[KEY_HEIGHT] = height
            val currentInitial = preferences[KEY_INITIAL_WEIGHT] ?: 0f
            if (currentInitial == 0f && weight > 0f) {
                preferences[KEY_INITIAL_WEIGHT] = weight
            }
            preferences[KEY_WEIGHT] = weight
        }
        if (weight > 0f) {
            addWeightHistory(weight)
        }
    }

    suspend fun savePhysicalGoals(goalType: String, targetWeight: Float, activityLevel: String) {
        dataStore.edit { preferences ->
            preferences[KEY_GOAL_TYPE] = goalType
            preferences[KEY_TARGET_WEIGHT] = targetWeight
            preferences[KEY_ACTIVITY_LEVEL] = activityLevel
        }
    }

    suspend fun addWeightHistory(newWeight: Float) {
        dataStore.edit { preferences ->
            val currentHistoryStr = preferences[KEY_WEIGHT_HISTORY] ?: ""
            val historyList = if (currentHistoryStr.isBlank()) {
                mutableListOf(newWeight)
            } else {
                val list = currentHistoryStr.split(",").mapNotNull { it.toFloatOrNull() }.toMutableList()
                if (list.lastOrNull() != newWeight) {
                    list.add(newWeight)
                }
                list
            }
            preferences[KEY_WEIGHT_HISTORY] = historyList.joinToString(",")
        }
    }

    suspend fun resetWeightHistory(initialWeight: Float) {
        dataStore.edit { preferences ->
            preferences[KEY_WEIGHT_HISTORY] = initialWeight.toString()
            preferences[KEY_INITIAL_WEIGHT] = initialWeight
        }
    }

    suspend fun updateCurrentWeightOnly(newWeight: Float) {
        dataStore.edit { preferences ->
            preferences[KEY_WEIGHT] = newWeight
        }
        if (newWeight > 0f) {
            addWeightHistory(newWeight)
        }
    }

    suspend fun saveCustomNutritionGoals(calories: Float, proteinPct: Float, carbsPct: Float, fatPct: Float) {
        dataStore.edit { preferences ->
            preferences[KEY_CUSTOM_CALORIES] = calories
            preferences[KEY_PROTEIN_PERCENT] = proteinPct
            preferences[KEY_CARBS_PERCENT] = carbsPct
            preferences[KEY_FAT_PERCENT] = fatPct
        }
    }

    // ==========================================
    // CÁC FLOW ĐỌC DỮ LIỆU
    // ==========================================
    val savedName: Flow<String?> = dataStore.data.map { it[KEY_NAME] }
    val savedEmail: Flow<String?> = dataStore.data.map { it[KEY_EMAIL] }
    val savedPassword: Flow<String?> = dataStore.data.map { it[KEY_PASSWORD] }
    val savedAge: Flow<Int> = dataStore.data.map { it[KEY_AGE] ?: 0 }
    val savedHeight: Flow<Float> = dataStore.data.map { it[KEY_HEIGHT] ?: 0f }
    val savedWeight: Flow<Float> = dataStore.data.map { it[KEY_WEIGHT] ?: 0f }
    val savedInitialWeight: Flow<Float> = dataStore.data.map { it[KEY_INITIAL_WEIGHT] ?: 0f }

    val savedWeightHistory: Flow<List<Float>> = dataStore.data.map { preferences ->
        val historyStr = preferences[KEY_WEIGHT_HISTORY] ?: ""
        if (historyStr.isBlank()) emptyList() else historyStr.split(",").mapNotNull { it.toFloatOrNull() }
    }

    val savedGender: Flow<String> = dataStore.data.map { it[KEY_GENDER] ?: "" }
    val savedWaist: Flow<Float> = dataStore.data.map { it[KEY_WAIST] ?: 0f }
    val savedChest: Flow<Float> = dataStore.data.map { it[KEY_CHEST] ?: 0f }
    val savedHips: Flow<Float> = dataStore.data.map { it[KEY_HIPS] ?: 0f }
    val savedGoalType: Flow<String> = dataStore.data.map { it[KEY_GOAL_TYPE] ?: "Giảm cân" }
    val savedTargetWeight: Flow<Float> = dataStore.data.map { it[KEY_TARGET_WEIGHT] ?: 0f }
    val savedActivityLevel: Flow<String> = dataStore.data.map { it[KEY_ACTIVITY_LEVEL] ?: "" }
    val savedCustomCalories: Flow<Float> = dataStore.data.map { it[KEY_CUSTOM_CALORIES] ?: 0f }
    val savedProteinPercent: Flow<Float> = dataStore.data.map { it[KEY_PROTEIN_PERCENT] ?: 0f }
    val savedCarbsPercent: Flow<Float> = dataStore.data.map { it[KEY_CARBS_PERCENT] ?: 0f }
    val savedFatPercent: Flow<Float> = dataStore.data.map { it[KEY_FAT_PERCENT] ?: 0f }
}