package com.app_health.app.feature.home.components.Home.Calculator

import com.app_health.app.feature.home.model.ActivityInputType
import com.app_health.app.feature.home.model.ExerciseDatabase

object ActivityCalculator {

    private const val DEFAULT_WEIGHT_KG = 65f

    fun calculateCaloriesBurned(
        activityName: String,
        distanceStr: String,
        timeStr: String,
        level: String
    ): Int {
        val distance = distanceStr.toFloatOrNull() ?: 0f
        val time = timeStr.toFloatOrNull() ?: 0f

        // BẮT BUỘC phải nhập thời gian
        if (time <= 0f) return 0

        // 1. Kết nối với Database để biết môn này dùng Khoảng cách hay Mức độ
        val activityData = ExerciseDatabase.allActivities.find { it.name == activityName }
        val inputType = activityData?.inputType ?: ActivityInputType.LEVEL

        var met = 0f

        if (inputType == ActivityInputType.DISTANCE) {
            // Môn KHOẢNG CÁCH: Ép buộc phải có dữ liệu quãng đường
            if (distance <= 0f) return 0

            val speedKmH = distance / (time / 60f) // Tự động tính Tốc độ (km/h)

            met = when {
                activityName.contains("Chạy") -> when {
                    speedKmH < 8f -> 7.0f   // Chạy chậm
                    speedKmH < 10.5f -> 9.8f // Chạy vừa
                    speedKmH < 12f -> 11.5f // Chạy nhanh
                    else -> 12.8f
                }
                activityName.contains("Đi bộ") -> when {
                    speedKmH < 4.5f -> 3.0f // Đi bộ chậm
                    speedKmH < 6f -> 4.3f   // Đi bộ nhanh
                    else -> 5.0f            // Đi bộ rất nhanh
                }
                activityName.contains("Đạp xe") -> when {
                    speedKmH < 15f -> 4.0f  // Đạp chậm
                    speedKmH < 20f -> 8.0f  // Đạp vừa
                    else -> 10.0f           // Đạp nhanh
                }
                activityName.contains("Bơi lội") -> 7.0f
                // BAO PHỦ TOÀN BỘ CÁC MÔN KHOẢNG CÁCH CÒN LẠI (Chèo thuyền, Trượt patin...)
                else -> if (speedKmH > 10f) 8.0f else 5.0f
            }
        } else {
            // Môn MỨC ĐỘ / BÀI TẬP CỤ THỂ
            met = when {
                activityName.contains("Gym") || activityName.contains("Tập tạ") -> when (level) {
                    "Deadlift", "Squat", "Tập chân với máy sled" -> 6.0f // Compound tốn sức
                    else -> 3.5f // Các bài cô lập
                }
                activityName.contains("Cầu lông") || activityName.contains("Pickleball") || activityName.contains("Quần vợt") -> {
                    if (level == "Chuyên nghiệp") 7.3f else 5.5f
                }
                activityName.contains("Yoga") -> when {
                    level.contains("Power") || level.contains("Vinyasa") || level.contains("Hot") -> 4.0f
                    else -> 2.5f
                }
                activityName.contains("Zumba") || activityName.contains("Aerobic") -> {
                    if (level.contains("Nâng cao")) 7.3f else 5.0f
                }
                activityName.contains("Đấm bốc") || activityName.contains("Võ thuật") -> {
                    if (level == "Chuyên nghiệp") 12.0f else 7.5f
                }
                activityName.contains("Bóng") -> { // Bóng chuyền, Bóng đá, Bóng bàn...
                    if (level == "Chuyên nghiệp") 8.0f else 6.0f
                }
                // BAO PHỦ TOÀN BỘ CÁC MÔN MỨC ĐỘ CÒN LẠI (Golf, Pilates, Dọn nhà, Leo núi...)
                else -> {
                    if (level == "Chuyên nghiệp" || level == "Nặng" || level == "Nâng cao") 7.0f else 5.0f
                }
            }
        }

        // 3. Áp dụng công thức MET chuẩn
        val caloriesPerMinute = (met * DEFAULT_WEIGHT_KG * 3.5f) / 200f
        return (caloriesPerMinute * time).toInt()
    }
}