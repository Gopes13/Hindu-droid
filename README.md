# Hindu Calendar - Android

A native Kotlin/Jetpack Compose app that brings the complete Hindu Panchang to Android. Syncs Hindu calendar events directly into Google Calendar.

## Features

- Full daily Panchang (Tithi, Nakshatra, Yoga, Karana)
- All major Hindu festivals with tradition-based filtering
- Location-aware astronomical calculations
- Sync to Google Calendar via CalendarProvider
- Support for Purnimant and Amant calendar systems
- Offline-first — all computations happen on-device
- Festival reminders and notifications

## Tech Stack

- **Jetpack Compose** for modern declarative UI
- **Kotlin Coroutines** for async computation
- **Hilt** for dependency injection
- **Room** for local caching
- **WorkManager** for background sync

## Requirements

- Android 8.0+ (API 26+)
- Android Studio Hedgehog or later

## Hindu Calendar Concepts

- **Tithi**: Lunar day (30 per month), based on Moon-Sun angular distance
- **Nakshatra**: Lunar mansion (27 total), based on Moon's sidereal position
- **Yoga**: Combination of Sun and Moon positions (27 total)
- **Karana**: Half of a tithi (11 types)
- **Panchang**: The five elements (tithi, nakshatra, yoga, karana, vara/weekday)
