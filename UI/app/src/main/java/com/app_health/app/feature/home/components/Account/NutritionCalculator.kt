package com.app_health.app.core.utils

data class NutritionGoals(
    val bmr: Float,
    val tdee: Float,
    val targetCalories: Float,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val proteinPercent: Float,
    val carbsPercent: Float,
    val fatPercent: Float
)

object NutritionCalculator {
    fun calculate(
        weight: Float,
        height: Float,
        age: Int,
        gender: String,
        activityLevel: String,
        customCalories: Float = 0f,
        proteinPct: Float = 0f,
        carbsPct: Float = 0f,
        fatPct: Float = 0f
    ): NutritionGoals {
        val validWeight = if (weight > 0f) weight else 60f
        val validHeight = if (height > 0f) height else 165f
        val validAge = if (age > 0) age else 25

        // ĐÃ SỬA LỖI: Thêm dấu * vào công thức tính toán
        val bmr = if (gender.trim().equals("Nữ", ignoreCase = true)) {
            (10f * validWeight) + (6.25f * validHeight) - (5f * validAge) - 161f
        } else {
            (10f * validWeight) + (6.25f * validHeight) - (5f * validAge) + 5f
        }

        val activityMultiplier = when (activityLevel.trim()) {
            "Ít vận động" -> 1.2f
            "Vận động nhẹ" -> 1.375f
            "Vận động vừa" -> 1.55f
            "Vận động nhiều" -> 1.725f
            "Vận động rất nhiều" -> 1.9f
            else -> 1.375f
        }

        val tdee = bmr * activityMultiplier
        val targetCalories = if (customCalories > 0f) customCalories else tdee

        val useValidCustomMacro = proteinPct > 0f && carbsPct > 0f && fatPct > 0f &&
                kotlin.math.abs((proteinPct + carbsPct + fatPct) - 100f) < 0.1f

        val finalProteinPct = if (useValidCustomMacro) proteinPct else 25f
        val finalCarbsPct = if (useValidCustomMacro) carbsPct else 50f
        val finalFatPct = if (useValidCustomMacro) fatPct else 25f

        val proteinCalories = targetCalories * (finalProteinPct / 100f)
        val carbsCalories = targetCalories * (finalCarbsPct / 100f)
        val fatCalories = targetCalories * (finalFatPct / 100f)

        return NutritionGoals(
            bmr = bmr,
            tdee = tdee,
            targetCalories = targetCalories,
            proteinGrams = (proteinCalories / 4f).toInt(),
            carbsGrams = (carbsCalories / 4f).toInt(),
            fatGrams = (fatCalories / 9f).toInt(),
            proteinPercent = finalProteinPct,
            carbsPercent = finalCarbsPct,
            fatPercent = finalFatPct
        )
    }
}