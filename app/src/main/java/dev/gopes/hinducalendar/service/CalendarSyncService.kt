package dev.gopes.hinducalendar.service

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.CalendarContract
import dev.gopes.hinducalendar.data.model.FestivalCategory
import dev.gopes.hinducalendar.data.model.FestivalOccurrence
import dev.gopes.hinducalendar.data.model.PanchangDay
import timber.log.Timber
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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
        return try {
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
            result?.lastPathSegment?.toLongOrNull()
        } catch (e: Exception) {
            Timber.e(e, "Failed to get or create Hindu Calendar in Calendar Provider")
            null
        }
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
        try {
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

            val timeFmt = DateTimeFormatter.ofPattern("h:mm a")
            val description = buildString {
                appendLine(occurrence.festival.description["en"] ?: "")

                occurrence.festival.significances?.get("en")?.let {
                    appendLine()
                    appendLine("Significance: $it")
                }

                appendLine()
                appendLine("--- Panchang ---")
                appendLine("Hindu Date: ${panchang.hinduDate.displayString}")
                appendLine("Tithi: ${panchang.tithiInfo.name}")
                appendLine("Nakshatra: ${panchang.nakshatraInfo.name}")
                appendLine("Yoga: ${panchang.yogaInfo.name}")
                appendLine("Karana: ${panchang.karanaInfo.name}")
                appendLine()
                appendLine("Sunrise: ${panchang.sunrise.format(timeFmt)}  |  Sunset: ${panchang.sunset.format(timeFmt)}")
                panchang.rahuKaal?.let { appendLine("Rahu Kaal: ${it.displayString}") }
                panchang.abhijitMuhurta?.let { appendLine("Abhijit Muhurta: ${it.displayString}") }
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
                put(CalendarContract.Events.HAS_ALARM, 1)
            }

            val eventUri = resolver.insert(CalendarContract.Events.CONTENT_URI, values)
            eventUri?.lastPathSegment?.toLongOrNull()?.let { insertedEventId ->
                // Day-before reminder for all festivals
                val dayBeforeReminder = ContentValues().apply {
                    put(CalendarContract.Reminders.EVENT_ID, insertedEventId)
                    put(CalendarContract.Reminders.MINUTES, 24 * 60)
                    put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
                }
                resolver.insert(CalendarContract.Reminders.CONTENT_URI, dayBeforeReminder)

                // Morning-of reminder for major festivals
                if (occurrence.festival.category == FestivalCategory.MAJOR) {
                    val morningReminder = ContentValues().apply {
                        put(CalendarContract.Reminders.EVENT_ID, insertedEventId)
                        put(CalendarContract.Reminders.MINUTES, 0)
                        put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
                    }
                    resolver.insert(CalendarContract.Reminders.CONTENT_URI, morningReminder)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to insert calendar event: %s", occurrence.festival.displayName)
        }
    }

    fun removeAllEvents() {
        try {
            val calendarId = getOrCreateCalendarId() ?: return
            context.contentResolver.delete(
                CalendarContract.Events.CONTENT_URI,
                "${CalendarContract.Events.CALENDAR_ID} = ?",
                arrayOf(calendarId.toString())
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to remove Hindu Calendar events")
        }
    }
}
