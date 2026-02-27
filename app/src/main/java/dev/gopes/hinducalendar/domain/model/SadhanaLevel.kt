package dev.gopes.hinducalendar.domain.model

data class SadhanaLevel(
    val level: Int,
    val titleKey: String,
    val xpRequired: Int,
    val icon: String
) {
    companion object {
        val allLevels = listOf(
            SadhanaLevel(1, "level_1", 0, "visibility"),
            SadhanaLevel(2, "level_2", 50, "menu_book"),
            SadhanaLevel(3, "level_3", 120, "wb_sunny"),
            SadhanaLevel(4, "level_4", 220, "auto_stories"),
            SadhanaLevel(5, "level_5", 350, "spa"),
            SadhanaLevel(6, "level_6", 520, "explore"),
            SadhanaLevel(7, "level_7", 730, "bookmark"),
            SadhanaLevel(8, "level_8", 1000, "star"),
            SadhanaLevel(9, "level_9", 1350, "graphic_eq"),
            SadhanaLevel(10, "level_10", 1800, "nightlight"),
            SadhanaLevel(11, "level_11", 2400, "lightbulb"),
            SadhanaLevel(12, "level_12", 3100, "local_fire_department"),
            SadhanaLevel(13, "level_13", 4000, "account_balance"),
            SadhanaLevel(14, "level_14", 5100, "format_quote"),
            SadhanaLevel(15, "level_15", 6500, "volunteer_activism"),
            SadhanaLevel(16, "level_16", 8200, "school"),
            SadhanaLevel(17, "level_17", 10200, "self_improvement"),
            SadhanaLevel(18, "level_18", 12800, "emoji_events"),
            SadhanaLevel(19, "level_19", 16000, "auto_awesome"),
            SadhanaLevel(20, "level_20", 20000, "wb_sunny")
        )

        fun forLevel(level: Int): SadhanaLevel =
            allLevels.find { it.level == level } ?: allLevels.first()

        fun forLevelOrNull(level: Int): SadhanaLevel? =
            allLevels.find { it.level == level }

        fun levelForPoints(points: Int): Int =
            allLevels.lastOrNull { points >= it.xpRequired }?.level ?: 1
    }
}
