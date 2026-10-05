package com.app_health.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app_health.app.feature.home.components.AddCameraFood.AddFoodBottomSheet
import com.app_health.app.ui.theme.*

// Định nghĩa các Route cho Navigation
enum class BottomNavRoute {
    HOME, DIARY, PLAN, ACCOUNT
}

@Composable
fun AppBottomNavigation(
    modifier: Modifier = Modifier,
    currentRoute: BottomNavRoute,
    onRouteSelected: (BottomNavRoute) -> Unit,
    onNavigateToSelectFood: () -> Unit,
    onNavigateToCaptureFood: () -> Unit
) {
    // Khai báo state để quản lý việc đóng/mở BottomSheet
    var showAddSheet by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp)
            .background(AppSurface)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Trang chủ
        BottomNavItem(
            title = "Trang chủ",
            icon = Icons.Filled.Home,
            isSelected = currentRoute == BottomNavRoute.HOME,
            selectedColor = AppPrimary,
            unselectedColor = TextSecondary,
            onClick = { onRouteSelected(BottomNavRoute.HOME) }
        )

        // 2. Nhật ký
        BottomNavItem(
            title = "Nhật ký",
            icon = Icons.Filled.Restaurant,
            isSelected = currentRoute == BottomNavRoute.DIARY,
            selectedColor = AppPrimary,
            unselectedColor = TextSecondary,
            onClick = { onRouteSelected(BottomNavRoute.DIARY) }
        )

        // 3. Nút Thêm ở giữa (+)
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(AppPrimary)
                .clickable { showAddSheet = true }, // Cập nhật sự kiện click
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Thêm",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        // 4. Thực đơn
        BottomNavItem(
            title = "Thực đơn",
            icon = Icons.AutoMirrored.Filled.Assignment,
            isSelected = currentRoute == BottomNavRoute.PLAN,
            selectedColor = AppPrimary,
            unselectedColor = TextSecondary,
            onClick = { onRouteSelected(BottomNavRoute.PLAN) }
        )

        // 5. Tài khoản
        BottomNavItem(
            title = "Tài khoản",
            icon = Icons.Filled.Person,
            isSelected = currentRoute == BottomNavRoute.ACCOUNT,
            selectedColor = AppPrimary,
            unselectedColor = TextSecondary,
            onClick = { onRouteSelected(BottomNavRoute.ACCOUNT) }
        )
    }

    // Hiển thị BottomSheet khi state là true
    if (showAddSheet) {
        AddFoodBottomSheet(
            onDismiss = { showAddSheet = false },
            onNavigateToSelectFood = onNavigateToSelectFood,
            onNavigateToCaptureFood = onNavigateToCaptureFood
        )
    }
}

@Composable
private fun BottomNavItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    selectedColor: Color,
    unselectedColor: Color,
    onClick: () -> Unit
) {
    val color = if (isSelected) selectedColor else unselectedColor

    Column(
        modifier = Modifier
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            color = color,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppBottomNavigationPreview() {
    AppBottomNavigation(
        currentRoute = BottomNavRoute.HOME,
        onRouteSelected = {},
        onNavigateToSelectFood = {},
        onNavigateToCaptureFood = {}
    )
}