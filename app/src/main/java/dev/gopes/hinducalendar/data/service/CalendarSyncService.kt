package dev.gopes.hinducalendar.data.service

import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import dev.gopes.hinducalendar.domain.model.*
import timber.log.Timber
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Syncs Hindu calendar events to the Android Calendar Provider (Google Calendar).
 * Respects CalendarSyncOption (which events) and ReminderTiming (when to alert).
 */
@Singleton
class CalendarSyncService @Inject constructor(
    private val context: Context
) {
    private val calendarName = "Hindu Calendar"
    private val accountName = "dev.gopes.hinducalendar"

    /** Important recurring tithis to include when syncing "Festivals + Tithis". */
    private val importantTithis = setOf(
        "Ekadashi", "Purnima", "Amavasya", "Chaturdashi" // Pradosh falls on Trayodashi/Chaturdashi
    )

    fun getOrCreateCalendarId(): Long? {
        return try {
            val resolver = context.contentResolver

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

    fun syncDay(panchang: PanchangDay, syncOption: CalendarSyncOption, reminderTimings: List<ReminderTiming>, language: AppLanguage = AppLanguage.ENGLISH) {
        val calendarId = getOrCreateCalendarId() ?: return

        // Sync festivals based on option
        val festivalsToSync = filterFestivals(panchang.festivals, syncOption)
        for (occurrence in festivalsToSync) {
            insertEvent(calendarId, occurrence, panchang, reminderTimings, language)
        }

        // For FESTIVALS_AND_TITHIS: also add important tithi events
        if (syncOption == CalendarSyncOption.FESTIVALS_AND_TITHIS ||
            syncOption == CalendarSyncOption.FULL_PANCHANG) {
            if (isImportantTithi(panchang.tithiInfo.name)) {
                insertTithiEvent(calendarId, panchang, reminderTimings)
            }
        }

        // For FULL_PANCHANG: add daily panchang summary event
        if (syncOption == CalendarSyncOption.FULL_PANCHANG) {
            insertPanchangSummaryEvent(calendarId, panchang)
        }
    }

    fun syncMonth(panchangDays: List<PanchangDay>, syncOption: CalendarSyncOption, reminderTimings: List<ReminderTiming>, language: AppLanguage = AppLanguage.ENGLISH) {
        val calendarId = getOrCreateCalendarId() ?: return
        for (day in panchangDays) {
            val festivalsToSync = filterFestivals(day.festivals, syncOption)
            for (occurrence in festivalsToSync) {
                insertEvent(calendarId, occurrence, day, reminderTimings, language)
            }

            if (syncOption == CalendarSyncOption.FESTIVALS_AND_TITHIS ||
                syncOption == CalendarSyncOption.FULL_PANCHANG) {
                if (isImportantTithi(day.tithiInfo.name)) {
                    insertTithiEvent(calendarId, day, reminderTimings)
                }
            }

            if (syncOption == CalendarSyncOption.FULL_PANCHANG) {
                insertPanchangSummaryEvent(calendarId, day)
            }
        }
    }

    private fun filterFestivals(
        festivals: List<FestivalOccurrence>,
        syncOption: CalendarSyncOption
    ): List<FestivalOccurrence> {
        return when (syncOption) {
            CalendarSyncOption.FESTIVALS_ONLY -> festivals.filter {
                it.festival.category == FestivalCategory.MAJOR ||
                it.festival.category == FestivalCategory.MODERATE ||
                it.festival.category == FestivalCategory.REGIONAL ||
                it.festival.category == FestivalCategory.VRAT
            }
            CalendarSyncOption.FESTIVALS_AND_TITHIS,
            CalendarSyncOption.FULL_PANCHANG -> festivals // include all
        }
    }

    private fun isImportantTithi(tithiName: String): Boolean {
        return importantTithis.any { tithiName.contains(it, ignoreCase = true) }
    }

    /** Compute reminder offset in minutes for all-day events based on ReminderTiming. */
    private fun reminderMinutes(timing: ReminderTiming): Int = when (timing) {
        ReminderTiming.MORNING_OF -> 0              // alert at start of all-day event
        ReminderTiming.EVENING_BEFORE -> 18 * 60    // 6 PM day before (18 hours before midnight)
        ReminderTiming.DAY_BEFORE -> 24 * 60        // 24 hours before
        ReminderTiming.TWO_DAYS_BEFORE -> 48 * 60   // 48 hours before
    }

    private fun insertEvent(
        calendarId: Long,
        occurrence: FestivalOccurrence,
        panchang: PanchangDay,
        reminderTimings: List<ReminderTiming>,
        language: AppLanguage = AppLanguage.ENGLISH
    ) {
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
                put(CalendarContract.Events.TITLE, occurrence.festival.displayName(language))
                put(CalendarContract.Events.DESCRIPTION, description)
                put(CalendarContract.Events.DTSTART, startMillis)
                put(CalendarContract.Events.DTEND, endMillis)
                put(CalendarContract.Events.ALL_DAY, 1)
                put(CalendarContract.Events.EVENT_TIMEZONE, panchang.location.timeZoneId)
                put(CalendarContract.Events.HAS_ALARM, 1)
            }

            val eventUri = resolver.insert(CalendarContract.Events.CONTENT_URI, values)
            eventUri?.lastPathSegment?.toLongOrNull()?.let { insertedEventId ->
                addReminders(resolver, insertedEventId, reminderTimings)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to insert calendar event: %s", occurrence.festival.displayName(language))
        }
    }

    private fun insertTithiEvent(calendarId: Long, panchang: PanchangDay, reminderTimings: List<ReminderTiming>) {
        try {
            val resolver = context.contentResolver
            val eventId = "hindu_tithi_${panchang.tithiInfo.name}_${panchang.id}"

            val cursor = resolver.query(
                CalendarContract.Events.CONTENT_URI,
                arrayOf(CalendarContract.Events._ID),
                "${CalendarContract.Events.CALENDAR_ID} = ? AND ${CalendarContract.Events.DESCRIPTION} LIKE ?",
                arrayOf(calendarId.toString(), "%$eventId%"),
                null
            )
            if (cursor?.count ?: 0 > 0) { cursor?.close(); return }
            cursor?.close()

            val startMillis = panchang.date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endMillis = panchang.date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val paksha = panchang.hinduDate.paksha.displayName
            val title = "$paksha ${panchang.tithiInfo.name}"
            val description = buildString {
                appendLine("Hindu Date: ${panchang.hinduDate.displayString}")
                panchang.tithiInfo.timeRangeString.takeIf { it.isNotEmpty() }?.let {
                    appendLine("Tithi timing: $it")
                }
                appendLine("[$eventId]")
            }

            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, description)
                put(CalendarContract.Events.DTSTART, startMillis)
                put(CalendarContract.Events.DTEND, endMillis)
                put(CalendarContract.Events.ALL_DAY, 1)
                put(CalendarContract.Events.EVENT_TIMEZONE, panchang.location.timeZoneId)
                put(CalendarContract.Events.HAS_ALARM, 1)
            }

            val eventUri = resolver.insert(CalendarContract.Events.CONTENT_URI, values)
            eventUri?.lastPathSegment?.toLongOrNull()?.let { insertedEventId ->
                addReminders(resolver, insertedEventId, reminderTimings)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to insert tithi event for %s", panchang.date)
        }
    }

    private fun insertPanchangSummaryEvent(calendarId: Long, panchang: PanchangDay) {
        try {
            val resolver = context.contentResolver
            val eventId = "hindu_panchang_${panchang.id}"

            val cursor = resolver.query(
                CalendarContract.Events.CONTENT_URI,
                arrayOf(CalendarContract.Events._ID),
                "${CalendarContract.Events.CALENDAR_ID} = ? AND ${CalendarContract.Events.DESCRIPTION} LIKE ?",
                arrayOf(calendarId.toString(), "%$eventId%"),
                null
            )
            if (cursor?.count ?: 0 > 0) { cursor?.close(); return }
            cursor?.close()

            val startMillis = panchang.date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endMillis = panchang.date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val timeFmt = DateTimeFormatter.ofPattern("h:mm a")
            val title = "Panchang: ${panchang.hinduDate.displayString}"
            val description = buildString {
                appendLine("Tithi: ${panchang.tithiInfo.name}")
                appendLine("Nakshatra: ${panchang.nakshatraInfo.name}")
                appendLine("Yoga: ${panchang.yogaInfo.name}")
                appendLine("Karana: ${panchang.karanaInfo.name}")
                appendLine()
                appendLine("Sunrise: ${panchang.sunrise.format(timeFmt)}")
                appendLine("Sunset: ${panchang.sunset.format(timeFmt)}")
                panchang.moonrise?.let { appendLine("Moonrise: ${it.format(timeFmt)}") }
                appendLine()
                panchang.rahuKaal?.let { appendLine("Rahu Kaal: ${it.displayString}") }
                panchang.yamaghanda?.let { appendLine("Yamaghanda: ${it.displayString}") }
                panchang.gulikaKaal?.let { appendLine("Gulika Kaal: ${it.displayString}") }
                panchang.abhijitMuhurta?.let { appendLine("Abhijit Muhurta: ${it.displayString}") }
                appendLine()
                appendLine("[$eventId]")
            }

            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, description)
                put(CalendarContract.Events.DTSTART, startMillis)
                put(CalendarContract.Events.DTEND, endMillis)
                put(CalendarContract.Events.ALL_DAY, 1)
                put(CalendarContract.Events.EVENT_TIMEZONE, panchang.location.timeZoneId)
                put(CalendarContract.Events.HAS_ALARM, 0) // No alarm for daily summaries
            }

            resolver.insert(CalendarContract.Events.CONTENT_URI, values)
        } catch (e: Exception) {
            Timber.e(e, "Failed to insert panchang summary for %s", panchang.date)
        }
    }

    private fun addReminders(
        resolver: android.content.ContentResolver,
        eventId: Long,
        timings: List<ReminderTiming>
    ) {
        for (timing in timings) {
            val reminder = ContentValues().apply {
                put(CalendarContract.Reminders.EVENT_ID, eventId)
                put(CalendarContract.Reminders.MINUTES, reminderMinutes(timing))
                put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
            }
            resolver.insert(CalendarContract.Reminders.CONTENT_URI, reminder)
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
