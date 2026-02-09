package dev.gopes.hinducalendar.service

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.CalendarContract
import dev.gopes.hinducalendar.data.model.FestivalOccurrence
import dev.gopes.hinducalendar.data.model.PanchangDay
import java.time.ZoneId
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Syncs Hindu calendar events to the Android Calendar Provider (Google Calendar).
 */
@Singleton
class CalendarSyncService @Inject constructor(
    private val context: Context
) {
    private val calendarName = "Hindu Calendar"
    private val accountName = "dev.gopes.hinducalendar"

    fun getOrCreateCalendarId(): Long? {
        val resolver = context.contentResolver

        // Check existing
        val projection = arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
        val cursor = resolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            "${CalendarContract.Calendars.CALENDAR_DISPLAY_NAME} = ?",
            arrayOf(calendarName),
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                return it.getLong(0)
            }
        }

        // Create new calendar
        val values = ContentValues().apply {
            put(CalendarContract.Calendars.ACCOUNT_NAME, accountName)
            put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
            put(CalendarContract.Calendars.NAME, calendarName)
            put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, calendarName)
            put(CalendarContract.Calendars.CALENDAR_COLOR, 0xFFFF9933.toInt())
            put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER)
            put(CalendarContract.Calendars.OWNER_ACCOUNT, accountName)
            put(CalendarContract.Calendars.VISIBLE, 1)
            put(CalendarContract.Calendars.SYNC_EVENTS, 1)
            put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, TimeZone.getDefault().id)
        }

        val uri = CalendarContract.Calendars.CONTENT_URI.buildUpon()
            .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, accountName)
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
            .build()

        val result = resolver.insert(uri, values)
        return result?.lastPathSegment?.toLongOrNull()
    }

    fun syncDay(panchang: PanchangDay) {
        val calendarId = getOrCreateCalendarId() ?: return
        for (occurrence in panchang.festivals) {
            insertEvent(calendarId, occurrence, panchang)
        }
    }

    fun syncMonth(panchangDays: List<PanchangDay>) {
        val calendarId = getOrCreateCalendarId() ?: return
        for (day in panchangDays) {
            for (occurrence in day.festivals) {
                insertEvent(calendarId, occurrence, day)
            }
        }
    }

    private fun insertEvent(calendarId: Long, occurrence: FestivalOccurrence, panchang: PanchangDay) {
        val resolver = context.contentResolver
        val eventId = "hindu_${occurrence.festival.id}_${panchang.id}"

        // Check if already exists
        val cursor = resolver.query(
            CalendarContract.Events.CONTENT_URI,
            arrayOf(CalendarContract.Events._ID),
            "${CalendarContract.Events.CALENDAR_ID} = ? AND ${CalendarContract.Events.DESCRIPTION} LIKE ?",
            arrayOf(calendarId.toString(), "%$eventId%"),
            null
        )

        if (cursor?.count ?: 0 > 0) {
            cursor?.close()
            return
        }
        cursor?.close()

        val startMillis = occurrence.date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endDate = occurrence.endDate ?: occurrence.date.plusDays(1)
        val endMillis = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val description = buildString {
            appendLine(occurrence.festival.description.en)
            appendLine()
            appendLine("Hindu Date: ${panchang.hinduDate.displayString}")
            appendLine("Sunrise: ${panchang.sunrise}")
            appendLine("Sunset: ${panchang.sunset}")
            appendLine()
            appendLine("[$eventId]")
        }

        val values = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.TITLE, occurrence.festival.displayName)
            put(CalendarContract.Events.DESCRIPTION, description)
            put(CalendarContract.Events.DTSTART, startMillis)
            put(CalendarContract.Events.DTEND, endMillis)
            put(CalendarContract.Events.ALL_DAY, 1)
            put(CalendarContract.Events.EVENT_TIMEZONE, panchang.location.timeZoneId)
        }

        resolver.insert(CalendarContract.Events.CONTENT_URI, values)
    }

    fun removeAllEvents() {
        val calendarId = getOrCreateCalendarId() ?: return
        context.contentResolver.delete(
            CalendarContract.Events.CONTENT_URI,
            "${CalendarContract.Events.CALENDAR_ID} = ?",
            arrayOf(calendarId.toString())
        )
    }
}
