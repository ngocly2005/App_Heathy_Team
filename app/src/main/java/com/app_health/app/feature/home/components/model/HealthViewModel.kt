package com.app_health.app.feature.home.components.model

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import com.app_health.app.feature.home.model.FoodItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ActivityRecord(
    val name: String,
    val param: String,
    val time: String,
    val calories: Int
)

class HealthViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("HealthApp_Prefs", Context.MODE_PRIVATE)
    private var currentUserEmail = ""

    // ==========================================
    // 1. DANH SÁCH MÓN ĂN YÊU THÍCH VÀ TỰ TẠO
    // ==========================================
    private val _favoriteFoods = mutableStateListOf<FoodItem>()
    val favoriteFoods: List<FoodItem> get() = _favoriteFoods

    private val _customFoods = mutableStateListOf<FoodItem>()
    val customFoods: List<FoodItem> get() = _customFoods

    fun toggleFavorite(food: FoodItem) {
        if (_favoriteFoods.contains(food)) _favoriteFoods.remove(food)
        else _favoriteFoods.add(food)
        if (currentUserEmail.isNotBlank()) saveList("$currentUserEmail-favorites", _favoriteFoods)
    }

    fun addCustomFood(food: FoodItem) {
        _customFoods.add(food)
        if (currentUserEmail.isNotBlank()) saveList("$currentUserEmail-customs", _customFoods)
    }

    // ==========================================
    // 2. CÁC BIẾN QUẢN LÝ DỮ LIỆU CHÍNH TỪNG NGÀY
    // ==========================================
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
    // 3. HÀM KHÔI PHỤC VÀ XÓA DỮ LIỆU KHI ĐĂNG NHẬP
    // ==========================================
    fun loadDataForUser(email: String) {
        currentUserEmail = email

        _favoriteFoods.clear()
        _favoriteFoods.addAll(loadList("$email-favorites"))

        _customFoods.clear()
        _customFoods.addAll(loadList("$email-customs"))

        // ĐÃ BỔ SUNG: Khôi phục lại toàn bộ dữ liệu ăn uống, nước, tập luyện từ bộ nhớ
        _loggedFoodMap.value = loadFoodMap("$email-diary")
        _plannedFoodMap.value = loadFoodMap("$email-plan")
        _loggedActivityMap.value = loadActivityMap("$email-activity")
        _loggedWaterMap.value = loadWaterMap("$email-water")
    }

    fun clearAllSessionData() {
        currentUserEmail = ""
        _favoriteFoods.clear()
        _customFoods.clear()
        _recentFoods.value = emptyList()
        _loggedFoodMap.value = emptyMap()
        _plannedFoodMap.value = emptyMap()
        _loggedActivityMap.value = emptyMap()
        _loggedWaterMap.value = emptyMap()
    }

    // ==========================================
    // 4. CÁC HÀM XỬ LÝ DỮ LIỆU (ĐÃ THÊM LỆNH LƯU)
    // ==========================================
    fun addFoodToTimeSlot(dailyKey: String, food: FoodItem) {
        val currentMap = _loggedFoodMap.value.toMutableMap()
        val currentList = currentMap[dailyKey]?.toMutableList() ?: mutableListOf()
        currentList.add(food)
        currentMap[dailyKey] = currentList
        _loggedFoodMap.value = currentMap

        updateRecentFoods(food)
        if (currentUserEmail.isNotBlank()) saveFoodMap("$currentUserEmail-diary", currentMap) // LƯU VÀO MÁY
    }

    fun removeFoodFromTimeSlot(dailyKey: String, food: FoodItem) {
        val currentMap = _loggedFoodMap.value.toMutableMap()
        val currentList = currentMap[dailyKey]?.toMutableList() ?: return
        currentList.remove(food)
        if (currentList.isEmpty()) currentMap.remove(dailyKey) else currentMap[dailyKey] = currentList
        _loggedFoodMap.value = currentMap

        if (currentUserEmail.isNotBlank()) saveFoodMap("$currentUserEmail-diary", currentMap) // LƯU VÀO MÁY
    }

    fun addFoodToPlan(key: String, food: FoodItem) {
        val currentMap = _plannedFoodMap.value.toMutableMap()
        val currentList = currentMap[key]?.toMutableList() ?: mutableListOf()
        currentList.add(food)
        currentMap[key] = currentList
        _plannedFoodMap.value = currentMap

        updateRecentFoods(food)
        if (currentUserEmail.isNotBlank()) saveFoodMap("$currentUserEmail-plan", currentMap) // LƯU VÀO MÁY
    }

    fun addActivityToDate(date: String, record: ActivityRecord) {
        val currentMap = _loggedActivityMap.value.toMutableMap()
        val currentList = currentMap[date]?.toMutableList() ?: mutableListOf()
        currentList.add(record)
        currentMap[date] = currentList
        _loggedActivityMap.value = currentMap

        if (currentUserEmail.isNotBlank()) saveActivityMap("$currentUserEmail-activity", currentMap) // LƯU VÀO MÁY
    }

    fun updateWaterForDate(date: String, waterMl: Int) {
        val currentMap = _loggedWaterMap.value.toMutableMap()
        currentMap[date] = waterMl
        _loggedWaterMap.value = currentMap

        if (currentUserEmail.isNotBlank()) saveWaterMap("$currentUserEmail-water", currentMap) // LƯU VÀO MÁY
    }

    private fun updateRecentFoods(food: FoodItem) {
        val currentList = _recentFoods.value.toMutableList()
        currentList.removeAll { it.name == food.name }
        currentList.add(0, food)
        if (currentList.size > 20) currentList.removeAt(currentList.lastIndex)
        _recentFoods.value = currentList
    }

    // ==========================================
    // 5. CÔNG CỤ LƯU / ĐỌC FILE BỘ NHỚ
    // ==========================================

    // Lưu/Đọc List thông thường
    private fun saveList(key: String, list: List<FoodItem>) {
        val stringData = list.joinToString(separator = "||") {
            "${it.name}|${it.calories}|${it.protein}|${it.carbs}|${it.fat}"
        }
        prefs.edit().putString(key, stringData).apply()
    }

    private fun loadList(key: String): List<FoodItem> {
        val stringData = prefs.getString(key, "") ?: ""
        if (stringData.isEmpty()) return emptyList()

        return stringData.split("||").mapNotNull { itemStr ->
            val parts = itemStr.split("|")
            if (parts.size >= 5) {
                FoodItem(parts[0], parts[1].toIntOrNull() ?: 0, parts[2].toIntOrNull() ?: 0, parts[3].toIntOrNull() ?: 0, parts[4].toIntOrNull() ?: 0)
            } else null
        }
    }

    // ĐÃ BỔ SUNG: Lưu/Đọc Bản đồ (Map) Nhật ký & Thực đơn
    private fun saveFoodMap(prefKey: String, map: Map<String, List<FoodItem>>) {
        val sb = java.lang.StringBuilder()
        for ((key, list) in map) {
            for (food in list) {
                sb.append("$key|${food.name}|${food.calories}|${food.protein}|${food.carbs}|${food.fat}||")
            }
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
                val food = FoodItem(parts[1], parts[2].toIntOrNull() ?: 0, parts[3].toIntOrNull() ?: 0, parts[4].toIntOrNull() ?: 0, parts[5].toIntOrNull() ?: 0)
                if (resultMap[key] == null) resultMap[key] = mutableListOf()
                resultMap[key]!!.add(food)
            }
        }
        return resultMap
    }

    // ĐÃ BỔ SUNG: Lưu/Đọc Bản đồ (Map) Hoạt động tập thể dục
    private fun saveActivityMap(prefKey: String, map: Map<String, List<ActivityRecord>>) {
        val sb = java.lang.StringBuilder()
        for ((key, list) in map) {
            for (record in list) {
                sb.append("$key|${record.name}|${record.param}|${record.time}|${record.calories}||")
            }
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
                val record = ActivityRecord(parts[1], parts[2], parts[3], parts[4].toIntOrNull() ?: 0)
                if (resultMap[key] == null) resultMap[key] = mutableListOf()
                resultMap[key]!!.add(record)
            }
        }
        return resultMap
    }

    // ĐÃ BỔ SUNG: Lưu/Đọc Bản đồ (Map) Lượng Nước
    private fun saveWaterMap(prefKey: String, map: Map<String, Int>) {
        val sb = java.lang.StringBuilder()
        for ((key, water) in map) {
            sb.append("$key|$water||")
        }
        prefs.edit().putString(prefKey, sb.toString()).apply()
    }

    private fun loadWaterMap(prefKey: String): Map<String, Int> {
        val stringData = prefs.getString(prefKey, "") ?: ""
        if (stringData.isEmpty()) return emptyMap()

        val resultMap = mutableMapOf<String, Int>()
        for (itemStr in stringData.split("||")) {
            if (itemStr.isBlank()) continue
            val parts = itemStr.split("|")
            if (parts.size >= 2) {
                resultMap[parts[0]] = parts[1].toIntOrNull() ?: 0
            }
        }
        return resultMap
    }
}