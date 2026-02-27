package dev.gopes.hinducalendar.domain.model

import java.util.TimeZone

data class HinduLocation(
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String,
    val cityName: String? = null
) {
    val timeZone: TimeZone get() = TimeZone.getTimeZone(timeZoneId)

    companion object {
        val DELHI = HinduLocation(28.6139, 77.2090, "Asia/Kolkata", "New Delhi")
        val HOUSTON = HinduLocation(29.7604, -95.3698, "America/Chicago", "Houston")
        val NEW_YORK = HinduLocation(40.7128, -74.0060, "America/New_York", "New York")
        val LONDON = HinduLocation(51.5074, -0.1278, "Europe/London", "London")
        val MUMBAI = HinduLocation(19.0760, 72.8777, "Asia/Kolkata", "Mumbai")
        val BANGALORE = HinduLocation(12.9716, 77.5946, "Asia/Kolkata", "Bangalore")
        val SAN_FRANCISCO = HinduLocation(37.7749, -122.4194, "America/Los_Angeles", "San Francisco")
        val LOS_ANGELES = HinduLocation(34.0522, -118.2437, "America/Los_Angeles", "Los Angeles")
        val CHICAGO = HinduLocation(41.8781, -87.6298, "America/Chicago", "Chicago")
        val DUBAI = HinduLocation(25.2048, 55.2708, "Asia/Dubai", "Dubai")
        val SINGAPORE = HinduLocation(1.3521, 103.8198, "Asia/Singapore", "Singapore")
        val SYDNEY = HinduLocation(-33.8688, 151.2093, "Australia/Sydney", "Sydney")
        val PHOENIX = HinduLocation(33.4484, -112.0740, "America/Phoenix", "Phoenix")
        val VANCOUVER = HinduLocation(49.2827, -123.1207, "America/Vancouver", "Vancouver")

        val ALL_PRESETS = listOf(
            DELHI, MUMBAI, BANGALORE, HOUSTON, NEW_YORK, SAN_FRANCISCO,
            LOS_ANGELES, CHICAGO, PHOENIX, LONDON, DUBAI, SINGAPORE, SYDNEY, VANCOUVER
        )
    }
}
