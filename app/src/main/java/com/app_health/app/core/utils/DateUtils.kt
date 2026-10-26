package com.app_health.app.core.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Extension function cho LocalDate để chuyển đổi ngày thành chuỗi thân thiện với người dùng.
 * Ví dụ: "Hôm nay", "Hôm qua", "Ngày mai", hoặc định dạng "dd/MM/yyyy".
 */
fun LocalDate.toFriendlyString(): String {
    val today = LocalDate.now()
    return when (this) {
        today -> "Hôm nay"
        today.minusDays(1) -> "Hôm qua"
        today.plusDays(1) -> "Ngày mai"
        else -> this.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }
}