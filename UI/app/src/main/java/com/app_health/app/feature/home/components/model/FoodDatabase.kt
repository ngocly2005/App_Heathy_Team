package com.app_health.app.feature.home.model

object FoodDatabase {
    // Dữ liệu được tính trên khẩu phần chuẩn mặc định là 100 gram hoặc 1 suất tiêu chuẩn
    val allFoods = listOf(
        // --- 1. Món Nước & Cơm (Bữa chính) ---
        FoodItem(name = "Phở bò chín", calories = 350, protein = 18, carbs = 45, fat = 10),
        FoodItem(name = "Phở gà", calories = 300, protein = 16, carbs = 42, fat = 7),
        FoodItem(name = "Bún bò Huế", calories = 400, protein = 20, carbs = 50, fat = 12),
        FoodItem(name = "Bún chả Hà Nội", calories = 450, protein = 22, carbs = 40, fat = 20),
        FoodItem(name = "Bún riêu cua", calories = 320, protein = 15, carbs = 45, fat = 10),
        FoodItem(name = "Cơm trắng (1 bát)", calories = 130, protein = 3, carbs = 28, fat = 0),
        FoodItem(name = "Cơm tấm sườn bì chả", calories = 600, protein = 25, carbs = 65, fat = 22),
        FoodItem(name = "Cơm rang dưa bò", calories = 550, protein = 20, carbs = 55, fat = 25),
        FoodItem(name = "Hủ tiếu Nam Vang", calories = 380, protein = 18, carbs = 48, fat = 12),
        FoodItem(name = "Mì xào hải sản", calories = 420, protein = 18, carbs = 52, fat = 15),

        // --- 2. Thịt & Hải Sản (Đạm) ---
        FoodItem(name = "Ức gà luộc (100g)", calories = 165, protein = 31, carbs = 0, fat = 3),
        FoodItem(name = "Đùi gà quay", calories = 220, protein = 20, carbs = 0, fat = 15),
        FoodItem(name = "Thịt bò xào (100g)", calories = 250, protein = 26, carbs = 5, fat = 14),
        FoodItem(name = "Bò bít tết (100g)", calories = 271, protein = 25, carbs = 0, fat = 19),
        FoodItem(name = "Thịt heo quay (100g)", calories = 350, protein = 14, carbs = 0, fat = 32),
        FoodItem(name = "Thịt ba chỉ luộc (100g)", calories = 260, protein = 16, carbs = 0, fat = 21),
        FoodItem(name = "Cá hồi áp chảo (100g)", calories = 208, protein = 20, carbs = 0, fat = 13),
        FoodItem(name = "Cá chép rán", calories = 250, protein = 18, carbs = 0, fat = 18),
        FoodItem(name = "Tôm luộc (100g)", calories = 99, protein = 24, carbs = 0, fat = 0),
        FoodItem(name = "Mực xào cần tỏi", calories = 150, protein = 16, carbs = 8, fat = 5),
        FoodItem(name = "Trứng gà luộc (1 quả)", calories = 70, protein = 6, carbs = 0, fat = 5),
        FoodItem(name = "Trứng ốp la", calories = 90, protein = 6, carbs = 0, fat = 7),

        // --- 3. Rau củ & Đậu (Chất xơ & Tinh bột chậm) ---
        FoodItem(name = "Rau muống xào tỏi (100g)", calories = 110, protein = 3, carbs = 5, fat = 8),
        FoodItem(name = "Súp lơ xanh luộc (100g)", calories = 34, protein = 2, carbs = 7, fat = 0),
        FoodItem(name = "Bắp cải luộc (100g)", calories = 25, protein = 1, carbs = 6, fat = 0),
        FoodItem(name = "Salad rau trộn dầu giấm", calories = 80, protein = 1, carbs = 8, fat = 5),
        FoodItem(name = "Khoai lang luộc (100g)", calories = 86, protein = 1, carbs = 20, fat = 0),
        FoodItem(name = "Khoai tây chiên (100g)", calories = 312, protein = 3, carbs = 41, fat = 15),
        FoodItem(name = "Đậu phụ luộc (100g)", calories = 76, protein = 8, carbs = 1, fat = 4),
        FoodItem(name = "Đậu phộng rang (100g)", calories = 567, protein = 25, carbs = 16, fat = 49),

        // --- 4. Đồ ăn vặt, Bánh & Chè ---
        FoodItem(name = "Bánh mì pate trứng", calories = 350, protein = 12, carbs = 35, fat = 18),
        FoodItem(name = "Bánh cuốn nhân thịt", calories = 280, protein = 8, carbs = 40, fat = 10),
        FoodItem(name = "Bánh xèo", calories = 350, protein = 10, carbs = 35, fat = 18),
        FoodItem(name = "Bánh bao nhân thịt", calories = 250, protein = 8, carbs = 35, fat = 8),
        FoodItem(name = "Bánh chưng (1 miếng nhỏ)", calories = 400, protein = 12, carbs = 45, fat = 18),
        FoodItem(name = "Xúc xích chiên (1 cây)", calories = 200, protein = 8, carbs = 2, fat = 17),
        FoodItem(name = "Chè đậu đen (1 ly)", calories = 220, protein = 6, carbs = 45, fat = 2),
        FoodItem(name = "Sữa chua có đường (1 hộp)", calories = 100, protein = 3, carbs = 15, fat = 3),
        FoodItem(name = "Sữa chua không đường", calories = 60, protein = 3, carbs = 5, fat = 3),

        // --- 5. Trái cây (100g) ---
        FoodItem(name = "Chuối chín", calories = 89, protein = 1, carbs = 22, fat = 0),
        FoodItem(name = "Táo đỏ", calories = 52, protein = 0, carbs = 14, fat = 0),
        FoodItem(name = "Cam", calories = 47, protein = 1, carbs = 12, fat = 0),
        FoodItem(name = "Dưa hấu", calories = 30, protein = 0, carbs = 7, fat = 0),
        FoodItem(name = "Xoài chín", calories = 60, protein = 0, carbs = 15, fat = 0),
        FoodItem(name = "Bơ (Avocado)", calories = 160, protein = 2, carbs = 8, fat = 15),

        // --- 6. Thức uống ---
        FoodItem(name = "Trà sữa trân châu (1 ly)", calories = 350, protein = 2, carbs = 60, fat = 10),
        FoodItem(name = "Cà phê sữa đá", calories = 120, protein = 2, carbs = 20, fat = 3),
        FoodItem(name = "Cà phê đen không đường", calories = 5, protein = 0, carbs = 1, fat = 0),
        FoodItem(name = "Nước dừa tươi", calories = 45, protein = 1, carbs = 10, fat = 0),
        FoodItem(name = "Sữa tươi nguyên kem (100ml)", calories = 62, protein = 3, carbs = 4, fat = 3)
    )
}