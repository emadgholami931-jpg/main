package com.vazheyar.app.review

import com.vazheyar.app.data.ReviewLogEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class ReviewHistoryRange {
    WEEKLY,
    MONTHLY,
    YEARLY
}

data class ReviewHistoryBucket(
    val key: String,
    val label: String,
    val again: Int = 0,
    val hard: Int = 0,
    val good: Int = 0,
    val easy: Int = 0
) {
    val total: Int get() = again + hard + good + easy
}

object ReviewHistoryAggregator {
    fun build(
        logs: List<ReviewLogEntity>,
        range: ReviewHistoryRange,
        now: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault()
    ): List<ReviewHistoryBucket> {
        val buckets = createBuckets(range, now, timeZone).toMutableList()
        val indexByKey = buckets.mapIndexed { index, bucket -> bucket.key to index }.toMap()

        logs.forEach { log ->
            val key = when (range) {
                ReviewHistoryRange.WEEKLY,
                ReviewHistoryRange.MONTHLY -> dayKey(log.reviewedAt, timeZone)
                ReviewHistoryRange.YEARLY -> monthKey(log.reviewedAt, timeZone)
            }
            val index = indexByKey[key] ?: return@forEach
            val bucket = buckets[index]
            buckets[index] = when (log.rating) {
                ReviewRating.AGAIN.value -> bucket.copy(again = bucket.again + 1)
                ReviewRating.HARD.value -> bucket.copy(hard = bucket.hard + 1)
                ReviewRating.GOOD.value -> bucket.copy(good = bucket.good + 1)
                ReviewRating.EASY.value -> bucket.copy(easy = bucket.easy + 1)
                else -> bucket
            }
        }

        return buckets
    }

    private fun createBuckets(
        range: ReviewHistoryRange,
        now: Long,
        timeZone: TimeZone
    ): List<ReviewHistoryBucket> {
        val current = Calendar.getInstance(timeZone).apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (range == ReviewHistoryRange.YEARLY) {
                set(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val (count, field, firstOffset) = when (range) {
            ReviewHistoryRange.WEEKLY -> Triple(7, Calendar.DAY_OF_YEAR, -6)
            ReviewHistoryRange.MONTHLY -> Triple(30, Calendar.DAY_OF_YEAR, -29)
            ReviewHistoryRange.YEARLY -> Triple(12, Calendar.MONTH, -11)
        }

        return List(count) { index ->
            val calendar = current.clone() as Calendar
            calendar.add(field, firstOffset + index)
            val timestamp = calendar.timeInMillis
            val key = when (range) {
                ReviewHistoryRange.WEEKLY,
                ReviewHistoryRange.MONTHLY -> dayKey(timestamp, timeZone)
                ReviewHistoryRange.YEARLY -> monthKey(timestamp, timeZone)
            }
            val label = when (range) {
                ReviewHistoryRange.WEEKLY -> format(timestamp, "EEE", timeZone)
                ReviewHistoryRange.MONTHLY -> format(timestamp, "d", timeZone)
                ReviewHistoryRange.YEARLY -> format(timestamp, "MMM", timeZone)
            }
            ReviewHistoryBucket(key = key, label = label)
        }
    }

    private fun dayKey(timestamp: Long, timeZone: TimeZone): String {
        val calendar = Calendar.getInstance(timeZone).apply { timeInMillis = timestamp }
        return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.DAY_OF_YEAR)}"
    }

    private fun monthKey(timestamp: Long, timeZone: TimeZone): String {
        val calendar = Calendar.getInstance(timeZone).apply { timeInMillis = timestamp }
        return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}"
    }

    private fun format(timestamp: Long, pattern: String, timeZone: TimeZone): String =
        SimpleDateFormat(pattern, Locale.ENGLISH).apply {
            this.timeZone = timeZone
        }.format(Date(timestamp))
}
