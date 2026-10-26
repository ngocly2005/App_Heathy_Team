package com.app_health.app.feature.home.components.model

import com.app_health.app.core.utils.NutritionGoals
import com.app_health.app.feature.home.model.FoodItem

// 1. Hộp chứa thông tin profile cá nhân
data class UserProfileState(
    val age: Int = 0,
    val height: Float = 0f,
    val weight: Float = 0f,
    val gender: String = "",
    val activityLevel: String = ""
) {
    val isProfileUpdated: Boolean
        get() = weight > 0f && height > 0f && age > 0
    // TỰ ĐỘNG TÍNH BMI KHI CÓ CHIỀU CAO VÀ CÂN NẶNG
    val bmi: Float
        get() = if (height > 0f && weight > 0f) {
            val heightInMeter = height / 100
            weight / (heightInMeter * heightInMeter)
        } else 0f
}

// 2. Hộp chứa thông tin tùy chỉnh Calo/Macro
data class CustomGoalsState(
    val customCal: Float = 0f,
    val customProtein: Float = 0f,
    val customCarbs: Float = 0f,
    val customFat: Float = 0f
)

// 3. Hộp bưu kiện tổng hợp UI State (Giao diện chỉ cần đọc cái này)
data class HomeUiState(
    val profile: UserProfileState = UserProfileState(),

    // ĐÃ SỬA LỖI Ở ĐÂY: Dùng named arguments để tránh nhầm lẫn Float/Int
    val nutritionGoals: NutritionGoals = NutritionGoals(
        bmr = 0f,
        tdee = 0f,
        targetCalories = 0f,
        proteinPercent = 0f,
        carbsPercent = 0f,
        fatPercent = 0f,
        proteinGrams = 0,
        carbsGrams = 0,
        fatGrams = 0
    ),

    val targetCalories: Int = 0,
    val targetWaterMl: Int = 2000,
    val isOverCalories: Boolean = false,
    val showWaterWarning: Boolean = false,

    // Dữ liệu ngày tháng / nhật ký
    val dailyConsumedCalories: Int = 0,
    val dailyProtein: Int = 0,
    val dailyCarbs: Int = 0,
    val dailyFat: Int = 0,
    val dailyBurnedCalories: Int = 0,
    val currentWaterMl: Int = 0,

    val consumedFoods: List<FoodItem> = emptyList()
)