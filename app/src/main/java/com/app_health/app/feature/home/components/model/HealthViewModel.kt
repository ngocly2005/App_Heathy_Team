package com.app_health.app.feature.home.components.model

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app_health.app.feature.home.components.Home.StreakManager
import com.app_health.app.feature.home.components.LoginLogout.SharedDataStoreProvider
import com.app_health.app.feature.home.model.FoodItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ActivityRecord(
    val name: String,
    val param: String,
    val time: String,
    val calories: Int
)

class HealthViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val gson = Gson()
    private val streakManager = StreakManager(application)

    // Khóa DataStore để lưu JSON
    private val KEY_LOGGED_FOODS = stringPreferencesKey("logged_foods")
    private val KEY_PLANNED_FOODS = stringPreferencesKey("planned_foods")
    private val KEY_LOGGED_ACTIVITIES = stringPreferencesKey("logged_activities")
    private val KEY_LOGGED_WATER = stringPreferencesKey("logged_water")

    // Trạng thái Email hiện tại đang được load
    private var currentUserEmail: String? = null

    // StateFlows
    private val _recentFoods = MutableStateFlow<List<FoodItem>>(emptyList())
    val recentFoods = _recentFoods.asStateFlow()

    private val _loggedFoodMap = MutableStateFlow<Map<String, List<FoodItem>>>(emptyMap())
    val loggedFoodMap: StateFlow<Map<String, List<FoodItem>>> = _loggedFoodMap.asStateFlow()

    private val _plannedFoodMap = MutableStateFlow<Map<String, List<FoodItem>>>(emptyMap())
    val plannedFoodMap: StateFlow<Map<String, List<FoodItem>>> = _plannedFoodMap.asStateFlow()

    private val _loggedActivityMap = MutableStateFlow<Map<String, List<ActivityRecord>>>(emptyMap())
    val loggedActivityMap: StateFlow<Map<String, List<ActivityRecord>>> = _loggedActivityMap.asStateFlow()

    private val _loggedWaterMap = MutableStateFlow<Map<String, Int>>(emptyMap())
    val loggedWaterMap: StateFlow<Map<String, Int>> = _loggedWaterMap.asStateFlow()

    // --- HÀM TẢI DỮ LIỆU KHI ĐĂNG NHẬP ---
    fun loadDataForUser(email: String?) {
        if (email.isNullOrBlank()) {
            clearAllSessionData()
            return
        }

        currentUserEmail = email

        viewModelScope.launch {
            val dataStore = SharedDataStoreProvider.get(context, email)
            val preferences = dataStore.data.first() // Lấy snapshot hiện tại của DataStore

            // Load Food
            val foodJson = preferences[KEY_LOGGED_FOODS]
            val initialFoodMap = if (foodJson != null) {
                val type = object : TypeToken<Map<String, List<FoodItem>>>() {}.type
                gson.fromJson<Map<String, List<FoodItem>>>(foodJson, type)
            } else emptyMap()
            _loggedFoodMap.value = initialFoodMap

            // Tính Recent Food
            _recentFoods.value = initialFoodMap.values.flatten().distinct().take(2)

            // Load Plan
            val planJson = preferences[KEY_PLANNED_FOODS]
            _plannedFoodMap.value = if (planJson != null) {
                val type = object : TypeToken<Map<String, List<FoodItem>>>() {}.type
                gson.fromJson(planJson, type)
            } else emptyMap()

            // Load Activity
            val activityJson = preferences[KEY_LOGGED_ACTIVITIES]
            _loggedActivityMap.value = if (activityJson != null) {
                val type = object : TypeToken<Map<String, List<ActivityRecord>>>() {}.type
                gson.fromJson(activityJson, type)
            } else emptyMap()

            // Load Water
            val waterJson = preferences[KEY_LOGGED_WATER]
            _loggedWaterMap.value = if (waterJson != null) {
                val type = object : TypeToken<Map<String, Int>>() {}.type
                gson.fromJson(waterJson, type)
            } else emptyMap()
        }
    }

    // --- CÁC HÀM LƯU VÀO DATASTORE THEO EMAIL ---
    private fun saveFoodData(map: Map<String, List<FoodItem>>) {
        viewModelScope.launch {
            val dataStore = SharedDataStoreProvider.get(context, currentUserEmail)
            dataStore.edit { it[KEY_LOGGED_FOODS] = gson.toJson(map) }
        }
    }

    private fun savePlanData(map: Map<String, List<FoodItem>>) {
        viewModelScope.launch {
            val dataStore = SharedDataStoreProvider.get(context, currentUserEmail)
            dataStore.edit { it[KEY_PLANNED_FOODS] = gson.toJson(map) }
        }
    }

    private fun saveActivityData(map: Map<String, List<ActivityRecord>>) {
        viewModelScope.launch {
            val dataStore = SharedDataStoreProvider.get(context, currentUserEmail)
            dataStore.edit { it[KEY_LOGGED_ACTIVITIES] = gson.toJson(map) }
        }
    }

    private fun saveWaterData(map: Map<String, Int>) {
        viewModelScope.launch {
            val dataStore = SharedDataStoreProvider.get(context, currentUserEmail)
            dataStore.edit { it[KEY_LOGGED_WATER] = gson.toJson(map) }
        }
    }

    // --- CÁC HÀM CẬP NHẬT NHẬT KÝ ---
    fun addFoodToPlan(key: String, food: FoodItem) {
        val currentMap = _plannedFoodMap.value.toMutableMap()
        val currentList = currentMap[key]?.toMutableList() ?: mutableListOf()
        currentList.add(food)
        currentMap[key] = currentList
        _plannedFoodMap.value = currentMap
        savePlanData(currentMap)
    }

    fun addFoodToTimeSlot(timeKey: String, food: FoodItem) {
        val currentMap = _loggedFoodMap.value.toMutableMap()
        val currentList = currentMap[timeKey]?.toMutableList() ?: mutableListOf()
        currentList.add(food)
        currentMap[timeKey] = currentList
        _loggedFoodMap.value = currentMap

        saveFoodData(currentMap)

        val currentRecent = _recentFoods.value.toMutableList()
        currentRecent.removeAll { it.name == food.name }
        currentRecent.add(0, food)
        _recentFoods.value = if (currentRecent.size > 30) currentRecent.take(30) else currentRecent

        viewModelScope.launch {
            streakManager.checkAndUpdateStreak()
        }
    }

    fun removeFoodFromTimeSlot(timeKey: String, food: FoodItem) {
        val currentMap = _loggedFoodMap.value.toMutableMap()
        val currentList = currentMap[timeKey]?.toMutableList() ?: return

        if (currentList.remove(food)) {
            currentMap[timeKey] = currentList
            _loggedFoodMap.value = currentMap
            saveFoodData(currentMap)

            val currentRecent = _recentFoods.value.toMutableList()
            if (currentRecent.remove(food)) {
                _recentFoods.value = currentRecent
            }
        }
    }

    fun addActivityToDate(dateKey: String, activity: ActivityRecord) {
        val currentMap = _loggedActivityMap.value.toMutableMap()
        val currentList = currentMap[dateKey]?.toMutableList() ?: mutableListOf()
        currentList.add(activity)
        currentMap[dateKey] = currentList
        _loggedActivityMap.value = currentMap
        saveActivityData(currentMap)

        viewModelScope.launch {
            streakManager.checkAndUpdateStreak()
        }
    }

    fun updateWaterForDate(dateKey: String, waterMl: Int) {
        val currentMap = _loggedWaterMap.value.toMutableMap()
        currentMap[dateKey] = maxOf(0, waterMl)
        _loggedWaterMap.value = currentMap
        saveWaterData(currentMap)
    }

    fun clearAllSessionData() {
        currentUserEmail = null
        _loggedFoodMap.value = emptyMap()
        _plannedFoodMap.value = emptyMap()
        _loggedActivityMap.value = emptyMap()
        _loggedWaterMap.value = emptyMap()
        _recentFoods.value = emptyList()
    }
    private val _customFoods = mutableStateListOf<FoodItem>()
    val customFoods: List<FoodItem> get() = _customFoods

    // Hàm thêm món ăn mới vào danh sách custom
    fun addCustomFood(food: FoodItem) {
        _customFoods.add(0, food) // Thêm lên đầu danh sách
    }
}