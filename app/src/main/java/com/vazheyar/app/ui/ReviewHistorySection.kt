package com.vazheyar.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vazheyar.app.data.ReviewLogEntity
import com.vazheyar.app.review.ReviewHistoryAggregator
import com.vazheyar.app.review.ReviewHistoryBucket
import com.vazheyar.app.review.ReviewHistoryRange

private val AgainColor = Color(0xFFE53935)
private val HardColor = Color(0xFFFB8C00)
private val GoodColor = Color(0xFF43A047)
private val EasyColor = Color(0xFF1E88E5)

@Composable
internal fun ReviewHistorySection(logs: List<ReviewLogEntity>) {
    var range by remember { mutableStateOf(ReviewHistoryRange.WEEKLY) }
    val buckets = ReviewHistoryAggregator.build(logs, range)
    val total = buckets.sumOf { it.total }
    val peak = buckets.maxOfOrNull { it.total } ?: 0

    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        when (range) {
                            ReviewHistoryRange.WEEKLY -> "Last 7 days"
                            ReviewHistoryRange.MONTHLY -> "Last 30 days"
                            ReviewHistoryRange.YEARLY -> "Last 12 months"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text("$total reviews", style = MaterialTheme.typography.labelLarge)
            }

            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RangeChip("Weekly", range == ReviewHistoryRange.WEEKLY) { range = ReviewHistoryRange.WEEKLY }
                RangeChip("Monthly", range == ReviewHistoryRange.MONTHLY) { range = ReviewHistoryRange.MONTHLY }
                RangeChip("Yearly", range == ReviewHistoryRange.YEARLY) { range = ReviewHistoryRange.YEARLY }
            }

            if (peak == 0) {
                Box(
                    Modifier.fillMaxWidth().height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No review history in this period yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                HistoryBarChart(buckets = buckets, range = range, peak = peak)
            }

            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                LegendItem("Again", AgainColor)
                LegendItem("Hard", HardColor)
                LegendItem("Good", GoodColor)
                LegendItem("Easy", EasyColor)
            }
        }
    }
}

@Composable
private fun RangeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) }
    )
}

@Composable
private fun HistoryBarChart(
    buckets: List<ReviewHistoryBucket>,
    range: ReviewHistoryRange,
    peak: Int
) {
    val slotWidth = when (range) {
        ReviewHistoryRange.WEEKLY -> 44.dp
        ReviewHistoryRange.MONTHLY -> 28.dp
        ReviewHistoryRange.YEARLY -> 44.dp
    }
    val barWidth = when (range) {
        ReviewHistoryRange.MONTHLY -> 14.dp
        else -> 26.dp
    }
    val chartHeight = 132.dp

    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        buckets.forEach { bucket ->
            HistoryBar(
                bucket = bucket,
                peak = peak,
                slotWidth = slotWidth,
                barWidth = barWidth,
                chartHeight = chartHeight
            )
        }
    }
}

@Composable
private fun HistoryBar(
    bucket: ReviewHistoryBucket,
    peak: Int,
    slotWidth: Dp,
    barWidth: Dp,
    chartHeight: Dp
) {
    Column(
        Modifier.width(slotWidth),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            if (bucket.total > 0) bucket.total.toString() else "",
            fontSize = 9.sp,
            maxLines = 1
        )
        Spacer(Modifier.height(3.dp))
        Box(
            Modifier
                .height(chartHeight)
                .width(barWidth)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                Modifier.width(barWidth),
                verticalArrangement = Arrangement.Bottom
            ) {
                Segment(bucket.easy, peak, chartHeight, EasyColor)
                Segment(bucket.good, peak, chartHeight, GoodColor)
                Segment(bucket.hard, peak, chartHeight, HardColor)
                Segment(bucket.again, peak, chartHeight, AgainColor)
            }
        }
        Spacer(Modifier.height(5.dp))
        Text(
            bucket.label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun Segment(count: Int, peak: Int, chartHeight: Dp, color: Color) {
    if (count <= 0 || peak <= 0) return
    val height = (chartHeight.value * count.toFloat() / peak.toFloat()).dp
    Box(Modifier.fillMaxWidth().height(height).background(color))
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(color, CircleShape))
        Spacer(Modifier.width(5.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}
