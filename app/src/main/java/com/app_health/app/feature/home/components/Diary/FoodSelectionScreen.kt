package com.app_health.app.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app_health.app.feature.home.model.FoodItem
import com.app_health.app.feature.home.model.FoodDatabase
import com.app_health.app.feature.home.components.model.HealthViewModel
import com.app_health.app.ui.theme.*

// 1. Stateful Composable
@Composable
fun FoodSelectScreen(
    viewModel: HealthViewModel,
    timeSlot: String,
    onBack: () -> Unit
) {
    FoodSelectContent(
        foodList = FoodDatabase.allFoods, // Lấy dữ liệu từ FoodDatabase
        onFoodClick = { food ->
            val today = java.time.LocalDate.now() // Hoặc lấy selectedDate nếu bạn truyền xuống
            val dailyKey = "${today}_$timeSlot"
            viewModel.addFoodToTimeSlot(dailyKey, food)
            onBack()
        },
        onBack = onBack
    )
}

// 2. Stateless Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSelectContent(
    foodList: List<FoodItem>,
    onFoodClick: (FoodItem) -> Unit,
    onBack: () -> Unit
) {
    // Biến trạng thái quản lý việc hiển thị màn hình chi tiết
    var selectedFood by remember { mutableStateOf<FoodItem?>(null) }

    if (selectedFood != null) {
        // Nếu có món ăn được chọn, hiển thị FoodDetailScreen
        FoodDetailScreen(
            food = selectedFood!!,
            onBack = { selectedFood = null }, // Nhấn nút back trên màn chi tiết thì quay lại danh sách
            onAdd = { adjustedFood ->
                onFoodClick(adjustedFood) // Thêm món ăn đã điều chỉnh gram và thoát
                selectedFood = null
            }
        )
    } else {
        // Nếu không có món nào được chọn, hiển thị màn hình danh sách
        var searchQuery by remember { mutableStateOf("") }
        var selectedTab by remember { mutableStateOf(0) }
        val tabs = listOf("Gần đây", "Tạo bởi tôi", "Yêu thích")

        // Lọc danh sách món ăn dựa trên từ khóa tìm kiếm
        val filteredFoods = remember(searchQuery, foodList) {
            if (searchQuery.isBlank()) {
                foodList
            } else {
                foodList.filter { food ->
                    food.name.contains(searchQuery, ignoreCase = true)
                }
            }
        }

        Scaffold(
            topBar = {
                Column(modifier = Modifier.background(AppBackground)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Hôm nay • 12:00",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = TextPrimary
                            )
                        }
                        IconButton(onClick = { /* Thông tin */ }) {
                            Icon(Icons.Default.Info, contentDescription = "Info", tint = TextPrimary)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .shadow(2.dp, RoundedCornerShape(25.dp)),
                            placeholder = { Text("Tìm thực phẩm hoặc món ăn", color = TextSecondary) },
                            leadingIcon = { Icon(Icons.Default.Search, tint = TextSecondary, contentDescription = null) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = AppSurface,
                                unfocusedContainerColor = AppSurface,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(25.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = AppBackground,
                        contentColor = AppPrimary,
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = AppPrimary
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTab == index) AppPrimary else TextSecondary
                                    )
                                }
                            )
                        }
                    }
                }
            },
            containerColor = AppBackground
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                if (filteredFoods.isEmpty()) {
                    item {
                        Text(
                            text = "Không tìm thấy món ăn nào",
                            color = TextSecondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    items(filteredFoods) { food ->
                        FoodItemCard(
                            food = food,
                            onItemClick = { selectedFood = food }, // Nhấn vào item để mở trang chi tiết
                            onAddClick = { onFoodClick(food) }     // Nhấn nút + để thêm nhanh mặc định 100g
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun FoodItemCard(
    food: FoodItem,
    onItemClick: () -> Unit,
    onAddClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .clickable { onItemClick() }, // Click vào card để xem chi tiết
        color = AppSurface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppBackground),
                contentAlignment = Alignment.Center
            ) {
                Text("🍲", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = food.name,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified",
                        tint = AppPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Khẩu phần • ${food.calories} cal",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("⚡ ${food.protein}g", fontSize = 11.sp, color = ProteinColor)
                    Text("💧 ${food.carbs}g", fontSize = 11.sp, color = CarbsColor)
                    Text("🛢️ ${food.fat}g", fontSize = 11.sp, color = FatColor)
                }
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AppPrimaryLight)
                    .clickable { onAddClick() }, // Giữ nguyên sự kiện click thêm nhanh cho icon +
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = AppPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FoodSelectScreenPreview() {
    // Truyền trực tiếp dữ liệu từ Database vào Preview để kiểm tra giao diện
    FoodSelectContent(
        foodList = FoodDatabase.allFoods.take(5), // Chỉ lấy 5 món đầu tiên để render Preview cho nhẹ
        onFoodClick = {},
        onBack = {}
    )
}