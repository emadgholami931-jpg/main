package com.vazheyar.app.review

import com.vazheyar.app.data.ReviewLogEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class ReviewHistoryAggregatorTest {
    private val utc = TimeZone.getTimeZone("UTC")

    @Test
    fun weeklyHistoryCountsRatingsAndExcludesOlderReviews() {
        val now = millis(2026, Calendar.AUGUST, 17)
        val logs = listOf(
            log(ReviewRating.AGAIN, millis(2026, Calendar.AUGUST, 17)),
            log(ReviewRating.GOOD, millis(2026, Calendar.AUGUST, 17, 18)),
            log(ReviewRating.EASY, millis(2026, Calendar.AUGUST, 16)),
            log(ReviewRating.HARD, millis(2026, Calendar.AUGUST, 10))
        )

        val buckets = ReviewHistoryAggregator.build(
            logs = logs,
            range = ReviewHistoryRange.WEEKLY,
            now = now,
            timeZone = utc
        )

        assertEquals(7, buckets.size)
        assertEquals(1, buckets[buckets.lastIndex - 1].easy)
        assertEquals(1, buckets.last().again)
        assertEquals(1, buckets.last().good)
        assertEquals(2, buckets.last().total)
        assertEquals(3, buckets.sumOf { it.total })
    }


    @Test
    fun monthlyHistoryUsesTheLastThirtyCalendarDays() {
        val now = millis(2026, Calendar.AUGUST, 17)
        val logs = listOf(
            log(ReviewRating.GOOD, millis(2026, Calendar.JULY, 19)),
            log(ReviewRating.HARD, millis(2026, Calendar.JULY, 18)),
            log(ReviewRating.EASY, millis(2026, Calendar.AUGUST, 17))
        )

        val buckets = ReviewHistoryAggregator.build(
            logs = logs,
            range = ReviewHistoryRange.MONTHLY,
            now = now,
            timeZone = utc
        )

        assertEquals(30, buckets.size)
        assertEquals(1, buckets.first().good)
        assertEquals(1, buckets.last().easy)
        assertEquals(2, buckets.sumOf { it.total })
    }

    @Test
    fun yearlyHistoryAggregatesByCalendarMonth() {
        val now = millis(2026, Calendar.AUGUST, 17)
        val logs = listOf(
            log(ReviewRating.HARD, millis(2025, Calendar.SEPTEMBER, 3)),
            log(ReviewRating.GOOD, millis(2026, Calendar.AUGUST, 1)),
            log(ReviewRating.EASY, millis(2026, Calendar.AUGUST, 15)),
            log(ReviewRating.AGAIN, millis(2025, Calendar.AUGUST, 20))
        )

        val buckets = ReviewHistoryAggregator.build(
            logs = logs,
            range = ReviewHistoryRange.YEARLY,
            now = now,
            timeZone = utc
        )

        assertEquals(12, buckets.size)
        assertEquals(1, buckets.first().hard)
        assertEquals(1, buckets.last().good)
        assertEquals(1, buckets.last().easy)
        assertEquals(3, buckets.sumOf { it.total })
    }

    private fun log(rating: ReviewRating, reviewedAt: Long) = ReviewLogEntity(
        cardId = 1,
        rating = rating.value,
        reviewedAt = reviewedAt,
        elapsedDays = 0,
        scheduledDays = 0,
        stabilityBefore = 0.0,
        difficultyBefore = 0.0,
        stabilityAfter = 0.0,
        difficultyAfter = 0.0,
        stateBefore = "LEARNING",
        stateAfter = "LEARNING"
    )

    private fun millis(year: Int, month: Int, day: Int, hour: Int = 12): Long =
        Calendar.getInstance(utc).apply {
            clear()
            set(year, month, day, hour, 0, 0)
        }.timeInMillis
}
