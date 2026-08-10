package com.toufikforyou.mytodoapp

import com.toufikforyou.mytodoapp.domain.DateTimes
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class DateTimesTest {

    private val now = Instant.parse("2026-08-10T12:00:00Z")
    private val utc = TimeZone.UTC

    @Test
    fun `relative day names are used for the days either side of today`() {
        assertEquals("Today", DateTimes.formatDate(now, now, utc))
        assertEquals("Tomorrow", DateTimes.formatDate(now + 1.days, now, utc))
        assertEquals("Yesterday", DateTimes.formatDate(now - 1.days, now, utc))
    }

    @Test
    fun `dates further out show the weekday and other years show the year`() {
        // 2026-08-17 is a Monday.
        assertEquals("Mon 17 Aug", DateTimes.formatDate(now + 7.days, now, utc))
        assertEquals("10 Aug 2027", DateTimes.formatDate(now + 365.days, now, utc))
    }

    @Test
    fun `times render on a 12-hour clock with a padded minute`() {
        assertEquals("12:00 AM", DateTimes.formatTime(LocalTime(0, 0)))
        assertEquals("9:05 AM", DateTimes.formatTime(LocalTime(9, 5)))
        assertEquals("12:30 PM", DateTimes.formatTime(LocalTime(12, 30)))
        assertEquals("11:59 PM", DateTimes.formatTime(LocalTime(23, 59)))
    }

    @Test
    fun `end of day is the following midnight so anything due today falls before it`() {
        val today = DateTimes.today(utc, now)
        val endOfToday = DateTimes.endOfDay(today, utc)

        assertEquals(Instant.parse("2026-08-11T00:00:00Z"), endOfToday)
        assertEquals(Instant.parse("2026-08-10T00:00:00Z"), DateTimes.startOfDay(today, utc))
    }

    @Test
    fun `picker conversions round-trip a date without drifting across time zones`() {
        // Deliberately a zone far from UTC: a naive conversion would land on the wrong day.
        val kathmandu = TimeZone.of("Asia/Kathmandu")
        val date = LocalDate(2026, 8, 10)

        val millis = DateTimes.toPickerMillis(date)

        assertEquals(date, DateTimes.fromPickerMillis(millis))
        assertEquals(date, DateTimes.fromPickerMillis(DateTimes.toPickerMillis(date)))
        // The instant built for storage does use the local zone, which is what the user means.
        assertEquals(
            LocalDate(2026, 8, 10),
            DateTimes.dateOf(DateTimes.instantOf(date, LocalTime(9, 0), kathmandu), kathmandu),
        )
    }

    @Test
    fun `days between counts forward and backward`() {
        assertEquals(1, DateTimes.daysBetween(LocalDate(2026, 8, 10), LocalDate(2026, 8, 11)))
        assertEquals(-1, DateTimes.daysBetween(LocalDate(2026, 8, 10), LocalDate(2026, 8, 9)))
        assertEquals(0, DateTimes.daysBetween(LocalDate(2026, 8, 10), LocalDate(2026, 8, 10)))
        // Across a month boundary.
        assertEquals(22, DateTimes.daysBetween(LocalDate(2026, 8, 10), LocalDate(2026, 9, 1)))
    }
}
