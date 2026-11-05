package com.app_health.app.feature.home.model

data class FoodItem(
    val name: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val salt: Float = 0f,
    val fiber: Float = 0f
)