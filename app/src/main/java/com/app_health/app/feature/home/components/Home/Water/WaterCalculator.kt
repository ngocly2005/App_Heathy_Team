package com.app_health.app.feature.home.components.Home.Water

object WaterCalculator {
    fun calculateTargetLiters(weight: Float, activityLevel: String): Float {
        val validWeight = if (weight > 0f) weight else 60f

        // Cơ bản: 35ml cho mỗi kg cân nặng
        var baseMl = validWeight * 35f

        // Cộng thêm lượng nước dựa theo mức độ vận động
        when (activityLevel.trim()) {
            "Vận động vừa" -> baseMl += 300f
            "Chăm chỉ tập luyện", "Vận động nhiều" -> baseMl += 500f
            "Vận động rất nhiều" -> baseMl += 700f
        }

        // Chuyển đổi sang lít và làm tròn đến chữ số thập phân phù hợp (giới hạn từ 1.5L đến 4.5L)
        val liters = baseMl / 1000f
        return liters.coerceIn(1.5f, 4.5f)
    }
}