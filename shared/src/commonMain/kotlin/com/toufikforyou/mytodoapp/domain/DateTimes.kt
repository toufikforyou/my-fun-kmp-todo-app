package com.toufikforyou.mytodoapp.domain

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/** Calendar helpers shared by filtering, grouping and the date labels shown in the UI. */
object DateTimes {

    fun today(tz: TimeZone = TimeZone.currentSystemDefault(), now: Instant): LocalDate =
        now.toLocalDateTime(tz).date

    fun startOfDay(date: LocalDate, tz: TimeZone = TimeZone.currentSystemDefault()): Instant =
        date.atStartOfDayIn(tz)

    fun endOfDay(date: LocalDate, tz: TimeZone = TimeZone.currentSystemDefault()): Instant =
        date.plus(1, DateTimeUnit.DAY).atStartOfDayIn(tz)

    fun dateOf(instant: Instant, tz: TimeZone = TimeZone.currentSystemDefault()): LocalDate =
        instant.toLocalDateTime(tz).date

    fun timeOf(instant: Instant, tz: TimeZone = TimeZone.currentSystemDefault()): LocalTime =
        instant.toLocalDateTime(tz).time

    /** Combines a calendar date and wall-clock time into an absolute instant in [tz]. */
    fun instantOf(
        date: LocalDate,
        time: LocalTime,
        tz: TimeZone = TimeZone.currentSystemDefault(),
    ): Instant = LocalDateTime(date, time).toInstant(tz)

    fun daysBetween(from: LocalDate, to: LocalDate): Int = from.daysUntil(to)

    /**
     * Material's date picker speaks in UTC-midnight milliseconds regardless of the user's zone,
     * so conversions in and out of it must not go through the local time zone.
     */
    fun toPickerMillis(date: LocalDate): Long =
        date.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

    fun fromPickerMillis(millis: Long): LocalDate =
        Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date

    /** "Today", "Tomorrow", "Yesterday", "Mon 12 Aug" or "12 Aug 2027" for other years. */
    fun formatDate(
        instant: Instant,
        now: Instant,
        tz: TimeZone = TimeZone.currentSystemDefault(),
    ): String {
        val date = dateOf(instant, tz)
        val todayDate = today(tz, now)
        return when (daysBetween(todayDate, date)) {
            0 -> "Today"
            1 -> "Tomorrow"
            -1 -> "Yesterday"
            else -> if (date.year == todayDate.year) {
                "${dayName(date)} ${date.day} ${monthName(date)}"
            } else {
                "${date.day} ${monthName(date)} ${date.year}"
            }
        }
    }

    /** 12-hour clock, e.g. "9:05 AM". */
    fun formatTime(instant: Instant, tz: TimeZone = TimeZone.currentSystemDefault()): String =
        formatTime(timeOf(instant, tz))

    fun formatTime(time: LocalTime): String {
        val hour = when {
            time.hour == 0 -> 12
            time.hour > 12 -> time.hour - 12
            else -> time.hour
        }
        val minute = time.minute.toString().padStart(2, '0')
        val suffix = if (time.hour < 12) "AM" else "PM"
        return "$hour:$minute $suffix"
    }

    fun formatDateTime(
        instant: Instant,
        now: Instant,
        tz: TimeZone = TimeZone.currentSystemDefault(),
    ): String = "${formatDate(instant, now, tz)} · ${formatTime(instant, tz)}"

    fun monthName(date: LocalDate): String = MONTHS[date.month.number - 1]

    fun dayName(date: LocalDate): String = DAYS[date.dayOfWeek.isoDayNumber - 1]

    private val MONTHS = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
    )

    private val DAYS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
}
