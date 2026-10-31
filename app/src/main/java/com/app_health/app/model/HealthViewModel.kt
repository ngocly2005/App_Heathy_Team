package com.app_health.app.feature.home.components.model

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app_health.app.core.utils.WeightManager
import com.app_health.app.feature.home.components.LoginLogout.DataAuthManager
import com.app_health.app.feature.home.model.FoodItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.app_health.app.feature.home.components.Home.Water.WaterCalculator

data class ActivityRecord(
    val name: String,
    val param: String,
    val time: String,
    val calories: Int
)

class HealthViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("HealthApp_Prefs", Context.MODE_PRIVATE)
    private var currentUserEmail = ""
    private var dataSyncJob: Job? = null // ĐÃ THÊM: Quản lý luồng lắng nghe dữ liệu

    // ==========================================
    // 1. CÁC BIẾN QUẢN LÝ DỮ LIỆU
    // ==========================================
    private val _userProfile = MutableStateFlow(UserProfileState())
    private val _customGoals = MutableStateFlow(CustomGoalsState())
    private val _selectedDate = MutableStateFlow(LocalDate.now())

    private val _favoriteFoods = mutableStateListOf<FoodItem>()
    val favoriteFoods: List<FoodItem> get() = _favoriteFoods

    private val _customFoods = mutableStateListOf<FoodItem>()
    val customFoods: List<FoodItem> get() = _customFoods

    private val _recentFoods = MutableStateFlow<List<FoodItem>>(emptyList())
    val recentFoods: StateFlow<List<FoodItem>> = _recentFoods.asStateFlow()

    private val _loggedFoodMap = MutableStateFlow<Map<String, List<FoodItem>>>(emptyMap())
    val loggedFoodMap: StateFlow<Map<String, List<FoodItem>>> = _loggedFoodMap.asStateFlow()

    private val _plannedFoodMap = MutableStateFlow<Map<String, List<FoodItem>>>(emptyMap())
    val plannedFoodMap: StateFlow<Map<String, List<FoodItem>>> = _plannedFoodMap.asStateFlow()

    private val _loggedActivityMap = MutableStateFlow<Map<String, List<ActivityRecord>>>(emptyMap())
    val loggedActivityMap: StateFlow<Map<String, List<ActivityRecord>>> = _loggedActivityMap.asStateFlow()

    private val _loggedWaterMap = MutableStateFlow<Map<String, Int>>(emptyMap())
    val loggedWaterMap: StateFlow<Map<String, Int>> = _loggedWaterMap.asStateFlow()

    // ==========================================
    // 2. CẦU NỐI ĐỂ UI GỌI CẬP NHẬT DỮ LIỆU
    // ==========================================
    fun updateSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    // ==========================================
    // 3. STATE TỔNG HỢP CHO GIAO DIỆN
    // ==========================================
    private val profileAndGoalsFlow = combine(_userProfile, _customGoals) { profile, custom ->
        Pair(profile, custom)
    }

    private val mapsAndDateFlow = combine(_loggedFoodMap, _loggedActivityMap, _loggedWaterMap, _selectedDate) { food, activity, water, date ->
        MapStateHolder(food, activity, water, date)
    }

    val homeUiState: StateFlow<HomeUiState> = combine(profileAndGoalsFlow, mapsAndDateFlow) { (profile, custom), maps ->
        val goals = if (profile.isProfileUpdated) {
            com.app_health.app.core.utils.NutritionCalculator.calculate(
                weight = profile.weight,
                height = profile.height,
                age = profile.age,
                gender = profile.gender,
                activityLevel = profile.activityLevel,
                customCalories = custom.customCal,
                proteinPct = if (custom.customProtein > 0) custom.customProtein else 25f,
                carbsPct = if (custom.customCarbs > 0) custom.customCarbs else 50f,
                fatPct = if (custom.customFat > 0) custom.customFat else 25f
            )
        } else {
            com.app_health.app.core.utils.NutritionGoals(0f, 0f, 0f, 0, 0, 0, 0f, 0f, 0f)
        }
        val targetCal = if (goals.targetCalories > 0f) goals.targetCalories.toInt() else 0

        val waterLiters = if (profile.isProfileUpdated) {
            WaterCalculator.calculateTargetLiters(profile.weight, profile.activityLevel)
        } else 2f
        val targetWater = (waterLiters * 1000).toInt()

        val timeSlots = (0..23).map { String.format("%02d:00", it) }
        val dailyFoods = timeSlots.flatMap { time -> maps.foodMap["${maps.selectedDate}_$time"] ?: emptyList() }
        val consumedCal = dailyFoods.sumOf { it.calories }
        val dailyActivities = maps.activityMap["${maps.selectedDate}"] ?: emptyList()
        val currentWater = maps.waterMap["${maps.selectedDate}"] ?: 0

        HomeUiState(
            profile = profile,
            nutritionGoals = goals,
            targetCalories = targetCal,
            targetWaterMl = targetWater,
            isOverCalories = targetCal > 0 && consumedCal > targetCal,
            showWaterWarning = profile.isProfileUpdated && currentWater > targetWater,
            dailyConsumedCalories = consumedCal,
            dailyProtein = dailyFoods.sumOf { it.protein },
            dailyCarbs = dailyFoods.sumOf { it.carbs },
            dailyFat = dailyFoods.sumOf { it.fat },
            dailyBurnedCalories = dailyActivities.sumOf { it.calories },
            currentWaterMl = currentWater,
            consumedFoods = dailyFoods // ĐÃ THÊM DÒNG NÀY ĐỂ TRUYỀN DANH SÁCH MÓN ĂN VÀO UISTATE
        )
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = HomeUiState())

    // ==========================================
    // 4. HÀM KHÔI PHỤC VÀ ĐỒNG BỘ DỮ LIỆU CHÍNH (FIX LỖI KẸT ĐỒNG BỘ)
    // ==========================================
    fun loadDataForUser(email: String) {
        currentUserEmail = email

        // 1. Tải dữ liệu từ SharedPreferences (Món ăn, Nước)
        _favoriteFoods.clear()
        _favoriteFoods.addAll(loadList("$email-favorites"))
        _customFoods.clear()
        _customFoods.addAll(loadList("$email-customs"))
        _loggedFoodMap.value = loadFoodMap("$email-diary")
        _plannedFoodMap.value = loadFoodMap("$email-plan")
        _loggedActivityMap.value = loadActivityMap("$email-activity")
        _loggedWaterMap.value = loadWaterMap("$email-water")

        // 2. KHỞI CHẠY TRẠM LẮNG NGHE TỪ DATASTORE
        dataSyncJob?.cancel() // Hủy luồng cũ nếu đổi tài khoản
        dataSyncJob = viewModelScope.launch {
            val authManager = DataAuthManager(getApplication(), email)
            val weightManager = WeightManager(getApplication(), email)

            // TÁCH RỜI LUỒNG LẮNG NGHE ĐỘC LẬP: Dữ liệu nào có là cập nhật ngay lập tức!
            launch { authManager.savedAge.collect { age -> _userProfile.value = _userProfile.value.copy(age = age) } }
            launch { authManager.savedHeight.collect { height -> _userProfile.value = _userProfile.value.copy(height = height) } }
            launch { weightManager.currentWeightFlow.collect { weight -> _userProfile.value = _userProfile.value.copy(weight = weight) } }
            launch { authManager.savedGender.collect { gender -> _userProfile.value = _userProfile.value.copy(gender = gender) } }
            launch { authManager.savedActivityLevel.collect { activity -> _userProfile.value = _userProfile.value.copy(activityLevel = activity) } }

            // Lắng nghe độc lập cho phần Tùy chỉnh mục tiêu Macro
            launch { authManager.savedCustomCalories.collect { cal -> _customGoals.value = _customGoals.value.copy(customCal = cal) } }
            launch { authManager.savedProteinPercent.collect { p -> _customGoals.value = _customGoals.value.copy(customProtein = p) } }
            launch { authManager.savedCarbsPercent.collect { c -> _customGoals.value = _customGoals.value.copy(customCarbs = c) } }
            launch { authManager.savedFatPercent.collect { f -> _customGoals.value = _customGoals.value.copy(customFat = f) } }
        }
    }

    fun clearAllSessionData() {
        currentUserEmail = ""
        dataSyncJob?.cancel() // Tắt luồng lắng nghe khi đăng xuất
        _userProfile.value = UserProfileState()
        _customGoals.value = CustomGoalsState()
        _favoriteFoods.clear()
        _customFoods.clear()
        _recentFoods.value = emptyList()
        _loggedFoodMap.value = emptyMap()
        _plannedFoodMap.value = emptyMap()
        _loggedActivityMap.value = emptyMap()
        _loggedWaterMap.value = emptyMap()
    }

    // ==========================================
    // 5. CÁC HÀM XỬ LÝ DỮ LIỆU KHÁC (GIỮ NGUYÊN)
    // ==========================================
    fun toggleFavorite(food: FoodItem) {
        if (_favoriteFoods.contains(food)) _favoriteFoods.remove(food) else _favoriteFoods.add(food)
        if (currentUserEmail.isNotBlank()) saveList("$currentUserEmail-favorites", _favoriteFoods)
    }

    fun addCustomFood(food: FoodItem) {
        _customFoods.add(food)
        if (currentUserEmail.isNotBlank()) saveList("$currentUserEmail-customs", _customFoods)
    }

    fun addFoodToTimeSlot(dailyKey: String, food: FoodItem) {
        val currentMap = _loggedFoodMap.value.toMutableMap()
        val currentList = currentMap[dailyKey]?.toMutableList() ?: mutableListOf()
        currentList.add(food)
        currentMap[dailyKey] = currentList
        _loggedFoodMap.value = currentMap
        updateRecentFoods(food)
        if (currentUserEmail.isNotBlank()) saveFoodMap("$currentUserEmail-diary", currentMap)
    }

    fun removeFoodFromTimeSlot(dailyKey: String, food: FoodItem) {
        val currentMap = _loggedFoodMap.value.toMutableMap()
        val currentList = currentMap[dailyKey]?.toMutableList() ?: return
        currentList.remove(food)
        if (currentList.isEmpty()) currentMap.remove(dailyKey) else currentMap[dailyKey] = currentList
        _loggedFoodMap.value = currentMap
        if (currentUserEmail.isNotBlank()) saveFoodMap("$currentUserEmail-diary", currentMap)
    }

    fun addFoodToPlan(key: String, food: FoodItem) {
        val currentMap = _plannedFoodMap.value.toMutableMap()
        val currentList = currentMap[key]?.toMutableList() ?: mutableListOf()
        currentList.add(food)
        currentMap[key] = currentList
        _plannedFoodMap.value = currentMap
        updateRecentFoods(food)
        if (currentUserEmail.isNotBlank()) saveFoodMap("$currentUserEmail-plan", currentMap)
    }

    fun addActivityToDate(date: String, record: ActivityRecord) {
        val currentMap = _loggedActivityMap.value.toMutableMap()
        val currentList = currentMap[date]?.toMutableList() ?: mutableListOf()
        currentList.add(record)
        currentMap[date] = currentList
        _loggedActivityMap.value = currentMap
        if (currentUserEmail.isNotBlank()) saveActivityMap("$currentUserEmail-activity", currentMap)
    }

    fun updateWaterForDate(date: String, waterMl: Int) {
        val currentMap = _loggedWaterMap.value.toMutableMap()
        currentMap[date] = waterMl
        _loggedWaterMap.value = currentMap
        if (currentUserEmail.isNotBlank()) saveWaterMap("$currentUserEmail-water", currentMap)
    }

    private fun updateRecentFoods(food: FoodItem) {
        val currentList = _recentFoods.value.toMutableList()
        currentList.removeAll { it.name == food.name }
        currentList.add(0, food)
        if (currentList.size > 20) currentList.removeAt(currentList.lastIndex)
        _recentFoods.value = currentList
    }

    // ==========================================
    // 6. CÔNG CỤ LƯU / ĐỌC FILE BỘ NHỚ (GIỮ NGUYÊN)
    // ==========================================
    private fun saveList(key: String, list: List<FoodItem>) {
        val stringData = list.joinToString(separator = "||") { "${it.name}|${it.calories}|${it.protein}|${it.carbs}|${it.fat}" }
        prefs.edit().putString(key, stringData).apply()
    }

    private fun loadList(key: String): List<FoodItem> {
        val stringData = prefs.getString(key, "") ?: ""
        if (stringData.isEmpty()) return emptyList()
        return stringData.split("||").mapNotNull { itemStr ->
            val parts = itemStr.split("|")
            if (parts.size >= 5) FoodItem(parts[0], parts[1].toIntOrNull() ?: 0, parts[2].toIntOrNull() ?: 0, parts[3].toIntOrNull() ?: 0, parts[4].toIntOrNull() ?: 0) else null
        }
    }

    private fun saveFoodMap(prefKey: String, map: Map<String, List<FoodItem>>) {
        val sb = java.lang.StringBuilder()
        for ((key, list) in map) {
            for (food in list) sb.append("$key|${food.name}|${food.calories}|${food.protein}|${food.carbs}|${food.fat}||")
        }
        prefs.edit().putString(prefKey, sb.toString()).apply()
    }

    private fun loadFoodMap(prefKey: String): Map<String, List<FoodItem>> {
        val stringData = prefs.getString(prefKey, "") ?: ""
        if (stringData.isEmpty()) return emptyMap()
        val resultMap = mutableMapOf<String, MutableList<FoodItem>>()
        for (itemStr in stringData.split("||")) {
            if (itemStr.isBlank()) continue
            val parts = itemStr.split("|")
            if (parts.size >= 6) {
                val key = parts[0]
                if (resultMap[key] == null) resultMap[key] = mutableListOf()
                resultMap[key]!!.add(FoodItem(parts[1], parts[2].toIntOrNull() ?: 0, parts[3].toIntOrNull() ?: 0, parts[4].toIntOrNull() ?: 0, parts[5].toIntOrNull() ?: 0))
            }
        }
        return resultMap
    }

    private fun saveActivityMap(prefKey: String, map: Map<String, List<ActivityRecord>>) {
        val sb = java.lang.StringBuilder()
        for ((key, list) in map) {
            for (record in list) sb.append("$key|${record.name}|${record.param}|${record.time}|${record.calories}||")
        }
        prefs.edit().putString(prefKey, sb.toString()).apply()
    }

    private fun loadActivityMap(prefKey: String): Map<String, List<ActivityRecord>> {
        val stringData = prefs.getString(prefKey, "") ?: ""
        if (stringData.isEmpty()) return emptyMap()
        val resultMap = mutableMapOf<String, MutableList<ActivityRecord>>()
        for (itemStr in stringData.split("||")) {
            if (itemStr.isBlank()) continue
            val parts = itemStr.split("|")
            if (parts.size >= 5) {
                val key = parts[0]
                if (resultMap[key] == null) resultMap[key] = mutableListOf()
                resultMap[key]!!.add(ActivityRecord(parts[1], parts[2], parts[3], parts[4].toIntOrNull() ?: 0))
            }
        }
        return resultMap
    }

    private fun saveWaterMap(prefKey: String, map: Map<String, Int>) {
        val sb = java.lang.StringBuilder()
        for ((key, water) in map) sb.append("$key|$water||")
        prefs.edit().putString(prefKey, sb.toString()).apply()
    }

    private fun loadWaterMap(prefKey: String): Map<String, Int> {
        val stringData = prefs.getString(prefKey, "") ?: ""
        if (stringData.isEmpty()) return emptyMap()
        val resultMap = mutableMapOf<String, Int>()
        for (itemStr in stringData.split("||")) {
            if (itemStr.isBlank()) continue
            val parts = itemStr.split("|")
            if (parts.size >= 2) resultMap[parts[0]] = parts[1].toIntOrNull() ?: 0
        }
        return resultMap
    }
}

private data class MapStateHolder(
    val foodMap: Map<String, List<FoodItem>>,
    val activityMap: Map<String, List<ActivityRecord>>,
    val waterMap: Map<String, Int>,
    val selectedDate: LocalDate
)