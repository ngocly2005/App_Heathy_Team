package com.app_health.app.feature.home.model

enum class ActivityInputType {
    DISTANCE,
    LEVEL
}

data class ExerciseItem(
    val name: String,
    val category: String,
    val inputType: ActivityInputType = ActivityInputType.LEVEL,
    val levelOptions: List<String> = emptyList()
)

object ExerciseDatabase {
    val defaultLevels = listOf("Nghiệp dư", "Chuyên nghiệp")
    val yogaLevels = listOf("Nadisodhana yoga", "Hatha yoga", "Surya Namaskar yoga", "Power yoga", "Vinyasa yoga", "Hot yoga")

    // Thêm danh sách bài tập Gym cụ thể
    val gymExercises = listOf(
        "Tập cơ ngực với tạ đơn", "Tập cơ ngực với tạ đôi", "Deadlift", "Squat",
        "Tập cơ vai với thanh tạ", "Tập cơ bắp tay trước với tạ đôi",
        "Tập cơ bắp tay trước với thanh tạ", "Chống đẩy với tạ",
        "Tập cơ vai với tạ đôi", "Tập chân với máy sled"
    )

    val allActivities = listOf(
        ExerciseItem("Aerobic (Aerobics)", "A", ActivityInputType.LEVEL, defaultLevels),
        ExerciseItem("Bài tập thể chất (Calisthenics)", "B", ActivityInputType.LEVEL, defaultLevels),
        ExerciseItem("Bóng bàn (Table Tennis)", "B", ActivityInputType.LEVEL, defaultLevels),
        ExerciseItem("Bóng bầu dục (Rugby)", "B", ActivityInputType.LEVEL, defaultLevels),
        ExerciseItem("Bóng chày (Baseball)", "B", ActivityInputType.LEVEL, defaultLevels),
        ExerciseItem("Bóng chuyền (Volleyball)", "B", ActivityInputType.LEVEL, defaultLevels),
        ExerciseItem("Bóng đá (Football/Soccer)", "B", ActivityInputType.LEVEL, defaultLevels),
        ExerciseItem("Bóng rổ (Basketball)", "B", ActivityInputType.LEVEL, defaultLevels),
        ExerciseItem("Bơi lội (Swimming)", "B", ActivityInputType.DISTANCE),
        ExerciseItem("Billiards / Bida", "B", ActivityInputType.LEVEL, defaultLevels),
        ExerciseItem("Cầu lông (Badminton)", "C", ActivityInputType.LEVEL, defaultLevels),
        ExerciseItem("Chạy bộ (Running)", "C", ActivityInputType.DISTANCE),
        ExerciseItem("Chạy địa hình (Trail Running)", "C", ActivityInputType.DISTANCE),
        ExerciseItem("Chèo thuyền (Rowing)", "C", ActivityInputType.DISTANCE),
        ExerciseItem("Cử tạ (Weightlifting)", "C", ActivityInputType.LEVEL, defaultLevels),
        ExerciseItem("Dọn dẹp nhà cửa (House Cleaning)", "D", ActivityInputType.LEVEL, listOf("Nhẹ nhàng", "Trung bình", "Nặng")),
        ExerciseItem("Đạp xe (Cycling)", "Đ", ActivityInputType.DISTANCE),
        ExerciseItem("Đi bộ (Walking)", "Đ", ActivityInputType.DISTANCE),
        ExerciseItem("Đi bộ đường dài (Hiking)", "Đ", ActivityInputType.DISTANCE),
        ExerciseItem("Đấm bốc (Boxing)", "Đ", ActivityInputType.LEVEL, defaultLevels),
        ExerciseItem("Golf", "G", ActivityInputType.LEVEL, defaultLevels),

        // Chuyển Gym và Tập tạ sang LEVEL và dùng danh sách gymExercises
        ExerciseItem("Gym (Thể hình)", "G", ActivityInputType.LEVEL, gymExercises),

        ExerciseItem("HIIT (Cardio cường độ cao)", "H", ActivityInputType.LEVEL, listOf("Cơ bản", "Nâng cao")),
        ExerciseItem("Khiêu vũ (Dancing)", "K", ActivityInputType.LEVEL, defaultLevels),
        ExerciseItem("Leo núi (Climbing)", "L", ActivityInputType.LEVEL, defaultLevels),
        ExerciseItem("Lướt sóng (Surfing)", "L", ActivityInputType.LEVEL, defaultLevels),
        ExerciseItem("Martial Arts (Võ thuật)", "M", ActivityInputType.LEVEL, defaultLevels),
        ExerciseItem("Nhảy dây (Jumping Rope)", "N", ActivityInputType.LEVEL, defaultLevels),
        ExerciseItem("Pickleball", "P", ActivityInputType.LEVEL, defaultLevels),
        ExerciseItem("Pilates", "P", ActivityInputType.LEVEL, defaultLevels),
        ExerciseItem("Quần vợt (Tennis)", "Q", ActivityInputType.LEVEL, defaultLevels),

        ExerciseItem("Tập tạ (Weight training)", "T", ActivityInputType.LEVEL, gymExercises),

        ExerciseItem("Thể dục dụng cụ (Gymnastics)", "T", ActivityInputType.LEVEL, defaultLevels),
        ExerciseItem("Trượt băng (Ice Skating)", "T", ActivityInputType.LEVEL, defaultLevels),
        ExerciseItem("Trượt patin (Roller Skating)", "T", ActivityInputType.DISTANCE),
        ExerciseItem("Yoga", "Y", ActivityInputType.LEVEL, yogaLevels),
        ExerciseItem("Zumba", "Z", ActivityInputType.LEVEL, listOf("Cơ bản", "Nâng cao"))
    )
}