package dev.gopes.hinducalendar.domain.model

data class SadhanaBadge(
    val id: String,
    val titleKey: String,
    val category: BadgeCategory,
    val icon: String,
    val requirement: (GamificationData, StreakData) -> Boolean
) {
    enum class BadgeCategory(val titleKey: String, val icon: String) {
        STREAK("badge_cat_streak", "local_fire_department"),
        TEXT_COMPLETION("badge_cat_text", "menu_book"),
        EXPLORER("badge_cat_explorer", "explore"),
        FESTIVAL("badge_cat_festival", "star"),
        LANGUAGE("badge_cat_language", "translate"),
        PANCHANG("badge_cat_panchang", "nightlight"),
        CHALLENGE("badge_cat_challenge", "help"),
        ENGAGEMENT("badge_cat_engagement", "lightbulb"),
        SANSKRIT("badge_cat_sanskrit", "translate"),
        JAPA("badge_cat_japa", "radio_button_checked"),
        DIYA("badge_cat_diya", "local_fire_department")
    }

    companion object {
        val allBadges: List<SadhanaBadge> = buildList {
            // Streak badges
            add(SadhanaBadge("streak_7", "badge_streak_7", BadgeCategory.STREAK, "local_fire_department") { _, s -> s.longestStreak >= 7 })
            add(SadhanaBadge("streak_30", "badge_streak_30", BadgeCategory.STREAK, "local_fire_department") { _, s -> s.longestStreak >= 30 })
            add(SadhanaBadge("streak_100", "badge_streak_100", BadgeCategory.STREAK, "emoji_events") { _, s -> s.longestStreak >= 100 })
            add(SadhanaBadge("streak_365", "badge_streak_365", BadgeCategory.STREAK, "workspace_premium") { _, s -> s.longestStreak >= 365 })

            // Text completion badges (one per sacred text)
            SacredTextType.entries.forEach { textType ->
                add(SadhanaBadge(
                    "text_${textType.name.lowercase()}",
                    "badge_text_${textType.name.lowercase()}",
                    BadgeCategory.TEXT_COMPLETION,
                    "menu_book"
                ) { g, _ -> textType.name in g.textsCompleted })
            }

            // Explorer badges
            add(SadhanaBadge("explorer_2", "badge_explorer_2", BadgeCategory.EXPLORER, "explore") { g, _ -> g.dharmaPathsExplored.size >= 2 })
            add(SadhanaBadge("explorer_5", "badge_explorer_5", BadgeCategory.EXPLORER, "public") { g, _ -> g.dharmaPathsExplored.size >= 5 })

            // Festival badges
            add(SadhanaBadge("festival_5", "badge_festival_5", BadgeCategory.FESTIVAL, "star") { g, _ -> g.festivalStoriesRead.size >= 5 })
            add(SadhanaBadge("festival_20", "badge_festival_20", BadgeCategory.FESTIVAL, "star") { g, _ -> g.festivalStoriesRead.size >= 20 })

            // Language badges
            add(SadhanaBadge("language_3", "badge_language_3", BadgeCategory.LANGUAGE, "translate") { g, _ -> g.languagesUsed.size >= 3 })
            add(SadhanaBadge("language_6", "badge_language_6", BadgeCategory.LANGUAGE, "public") { g, _ -> g.languagesUsed.size >= 6 })

            // Panchang badges
            add(SadhanaBadge("panchang_7", "badge_panchang_7", BadgeCategory.PANCHANG, "nightlight") { g, _ -> g.panchangDaysChecked >= 7 })
            add(SadhanaBadge("panchang_30", "badge_panchang_30", BadgeCategory.PANCHANG, "calendar_month") { g, _ -> g.panchangDaysChecked >= 30 })
            add(SadhanaBadge("panchang_100", "badge_panchang_100", BadgeCategory.PANCHANG, "event_available") { g, _ -> g.panchangDaysChecked >= 100 })

            // Challenge badges
            add(SadhanaBadge("challenge_7", "badge_challenge_7", BadgeCategory.CHALLENGE, "help") { g, _ -> g.challengesSolved >= 7 })
            add(SadhanaBadge("challenge_30", "badge_challenge_30", BadgeCategory.CHALLENGE, "psychology") { g, _ -> g.challengesSolved >= 30 })

            // Engagement badges
            add(SadhanaBadge("explained_10", "badge_explained_10", BadgeCategory.ENGAGEMENT, "lightbulb") { g, _ -> g.versesExplained >= 10 })
            add(SadhanaBadge("explained_100", "badge_explained_100", BadgeCategory.ENGAGEMENT, "lightbulb") { g, _ -> g.versesExplained >= 100 })
            add(SadhanaBadge("reflections_5", "badge_reflections_5", BadgeCategory.ENGAGEMENT, "edit_note") { g, _ -> g.reflectionsWritten >= 5 })
            add(SadhanaBadge("reflections_25", "badge_reflections_25", BadgeCategory.ENGAGEMENT, "edit_note") { g, _ -> g.reflectionsWritten >= 25 })
            add(SadhanaBadge("deep_study_7", "badge_deep_study_7", BadgeCategory.ENGAGEMENT, "psychology") { g, _ -> g.deepStudySessions >= 7 })
            add(SadhanaBadge("deep_study_30", "badge_deep_study_30", BadgeCategory.ENGAGEMENT, "psychology") { g, _ -> g.deepStudySessions >= 30 })

            // Sanskrit badges
            add(SadhanaBadge("badge_sanskrit_first_letters", "badge_sanskrit_first_letters", BadgeCategory.SANSKRIT, "translate") { g, _ -> g.hasBadge("badge_sanskrit_first_letters") })
            add(SadhanaBadge("badge_sanskrit_student", "badge_sanskrit_student", BadgeCategory.SANSKRIT, "school") { g, _ -> g.hasBadge("badge_sanskrit_student") })
            add(SadhanaBadge("badge_sanskrit_scholar", "badge_sanskrit_scholar", BadgeCategory.SANSKRIT, "workspace_premium") { g, _ -> g.hasBadge("badge_sanskrit_scholar") })
            add(SadhanaBadge("badge_sanskrit_mantra_reader", "badge_sanskrit_mantra_reader", BadgeCategory.SANSKRIT, "auto_stories") { g, _ -> g.hasBadge("badge_sanskrit_mantra_reader") })

            // Japa badges
            add(SadhanaBadge("badge_japa_10", "badge_japa_10", BadgeCategory.JAPA, "radio_button_checked") { g, _ -> g.hasBadge("badge_japa_10") })
            add(SadhanaBadge("badge_japa_108", "badge_japa_108", BadgeCategory.JAPA, "radio_button_checked") { g, _ -> g.hasBadge("badge_japa_108") })
            add(SadhanaBadge("badge_japa_1008", "badge_japa_1008", BadgeCategory.JAPA, "auto_awesome") { g, _ -> g.hasBadge("badge_japa_1008") })
            add(SadhanaBadge("badge_japa_streak_7", "badge_japa_streak_7", BadgeCategory.JAPA, "local_fire_department") { g, _ -> g.hasBadge("badge_japa_streak_7") })
            add(SadhanaBadge("badge_japa_streak_30", "badge_japa_streak_30", BadgeCategory.JAPA, "local_fire_department") { g, _ -> g.hasBadge("badge_japa_streak_30") })

            // Diya badges
            add(SadhanaBadge("badge_diya_7", "badge_diya_7", BadgeCategory.DIYA, "local_fire_department") { g, _ -> g.hasBadge("badge_diya_7") })
            add(SadhanaBadge("badge_diya_30", "badge_diya_30", BadgeCategory.DIYA, "local_fire_department") { g, _ -> g.hasBadge("badge_diya_30") })
            add(SadhanaBadge("badge_diya_108", "badge_diya_108", BadgeCategory.DIYA, "auto_awesome") { g, _ -> g.hasBadge("badge_diya_108") })
            add(SadhanaBadge("badge_diya_streak_7", "badge_diya_streak_7", BadgeCategory.DIYA, "local_fire_department") { g, _ -> g.hasBadge("badge_diya_streak_7") })
        }

        fun forId(id: String): SadhanaBadge? = allBadges.find { it.id == id }

        fun forCategory(category: BadgeCategory): List<SadhanaBadge> =
            allBadges.filter { it.category == category }
    }
}
