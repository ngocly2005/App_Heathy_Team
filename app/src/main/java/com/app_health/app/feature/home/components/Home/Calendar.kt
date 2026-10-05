package com.app_health.app.feature.home.components.Home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.app_health.app.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@Composable
fun WeeklyCalendarSwipeable(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val initialPage = 500
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { 1000 })

    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    Column {
        Text(
            text = if (selectedDate == today) "HÔM NAY, ${formatter.format(today)}" else formatter.format(selectedDate),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))

        HorizontalPager(state = pagerState) { page ->
            val weekOffset = page - initialPage
            val startOfWeek = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusWeeks(weekOffset.toLong())

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val daysOfWeekLabel = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

                (0..6).forEach { i ->
                    val currentDateInWeek = startOfWeek.plusDays(i.toLong())
                    val isSelected = currentDateInWeek == selectedDate
                    val isToday = currentDateInWeek == today

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onDateSelected(currentDateInWeek) }
                    ) {
                        Text(
                            text = daysOfWeekLabel[i],
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) AppPrimary else Color.Transparent)
                        ) {
                            Text(
                                text = currentDateInWeek.dayOfMonth.toString(),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Color.White else TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        if (isToday && !isSelected) {
                            Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(AppPrimary))
                        } else {
                            Spacer(modifier = Modifier.height(5.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthCalendarDialog(
    initialMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var currentMonth by remember { mutableStateOf(initialMonth) }
    val daysOfWeek = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = AppSurface,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Month", tint = TextPrimary)
                    }
                    Text(
                        text = "Tháng ${currentMonth.monthValue} năm ${currentMonth.year}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month", tint = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    daysOfWeek.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val daysInMonth = currentMonth.lengthOfMonth()
                val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value
                val emptyDaysBefore = firstDayOfMonth - 1

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(emptyDaysBefore) {
                        Spacer(modifier = Modifier.size(40.dp))
                    }

                    items(daysInMonth) { dayOffset ->
                        val day = dayOffset + 1
                        val date = currentMonth.atDay(day)
                        val isSelected = date == selectedDate
                        val isToday = date == LocalDate.now()

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> AppPrimary
                                        isToday -> AppPrimaryLight
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable { onDateSelected(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.toString(),
                                color = if (isSelected) Color.White else TextPrimary,
                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

fun LocalDate.toFriendlyString(): String {
    val today = LocalDate.now()
    return when (this) {
        today -> "Hôm nay"
        today.minusDays(1) -> "Hôm qua"
        today.plusDays(1) -> "Ngày mai"
        else -> this.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }
}