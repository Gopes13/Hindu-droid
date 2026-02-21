# iOS Hindu Calendar — Feature Specification

Comprehensive documentation of all features in the iOS Dharmic Companion app, used as the reference for Android feature parity.

---

## 1. App Overview

- **Platform:** iOS (SwiftUI, iOS 16+)
- **Architecture:** MVVM with ObservableObject AppState
- **Navigation:** 5-tab layout (Today, Texts, Calendar, Festivals, Settings)
- **iPad Support:** NavigationSplitView sidebar layout
- **Persistence:** UserDefaults with Codable JSON encoding
- **Languages:** 13 supported (English, Hindi, Hinglish, Gujarati, Marathi, Tamil, Telugu, Kannada, Malayalam, Bengali, Punjabi, Odia, Assamese)

---

## 2. Today Tab

### 2.1 Today Panchang View
- **Greeting Banner:** Time-based (Good Morning/Afternoon/Evening) with Sanskrit text
- **Streak Badge:** Flame icon with color-coded levels (orange 7+, red 30+, purple 100+), pulsing animation
- **Sadhana Status Bar:** Level, PP count, streak (if gamification enabled)
- **Daily Challenge Card:** Quiz with 4 options, PP reward, daily reset
- **Daily Briefing Card:** Tap-to-reveal verse with progress tracking
- **Hindu Date Header:** Full Hindu date + Gregorian + location
- **Sun & Moon Times:** Sunrise, sunset, moonrise, moonset with colored icons
- **Panchang Elements:** Tithi, Nakshatra, Yoga, Karana with time ranges
- **Inauspicious Periods:** Rahu Kaal, Yamaghanda, Gulika Kaal (red accent)
- **Auspicious Period:** Abhijit Muhurta (green accent)
- **Today's Festivals:** Festival list with category badges, tap to view detail
- **Decorative Dividers:** OM, Diamond, Lotus patterns between sections
- **Pull-to-refresh** support
- **Staggered entrance animations** on all cards
- **Level-up & milestone celebration overlays**
- **PunyaPointsToast** overlay on point awards

### 2.2 Daily Briefing Card
- Hero card with verse reference badge
- Progressive reveal: original script > transliteration > translation > commentary
- Progress bar (current/total positions)
- "Mark as Read" button (+10 PP if gamified)
- Confetti on full reveal
- Text completion state with text picker for next reading
- Error/retry state

### 2.3 Streak Badge
- Flame icon with circular background
- Color-coded: default < 7, orange 7-29, red 30-99, purple 100+
- Pulsing scale animation (1.08x at 7+, 1.15x at 30+)
- Current/best streak display
- PP bonus display (streak x 2)

---

## 3. Sacred Texts Tab

### 3.1 Sacred Texts Library
Three-section layout in ScrollView:

1. **Continue Reading Hero Card**
   - Circular progress ring (52pt) with text icon
   - Text name + current position label
   - Full-width LinearProgressIndicator (gradient)
   - Tap to open reader

2. **Bookmarks Quick Access** (if bookmarks exist)
   - Heart icon + count + chevron

3. **"Your [Path] Texts" Section**
   - Path-specific text cards with:
     - Progress ring (44pt)
     - Text name (bold) + "Primary" badge
     - Theme tag capsule (e.g., "Philosophy", "Devotional Hymn")
     - Unit count (e.g., "700 verses")
     - Mini progress bar + position/total
     - Bookmark count badge

4. **"All Sacred Texts" Section**
   - Remaining 15 texts in same card format

### 3.2 Sacred Text Types (15 total)
| Text | Unit | Theme Tag | Audio |
|------|------|-----------|-------|
| Bhagavad Gita | 700 verses | Philosophy | Yes |
| Hanuman Chalisa | verses | Devotional Hymn | Yes |
| Japji Sahib | pauris | Morning Prayer | Yes |
| Bhagavata Purana | episodes | Epic Stories | Yes |
| Vishnu Sahasranama | shlokas | 1000 Names | Yes |
| Shiva Purana | episodes | Epic Stories | Yes |
| Sri Rudram | anuvakas | Vedic Chant | Yes |
| Devi Mahatmya | chapters | Goddess Glory | Yes |
| Soundarya Lahari | verses | Devotional Poetry | Yes |
| Shikshapatri | shlokas | Ethical Guide | Yes |
| Vachanamrut | discourses | Discourses | No |
| Sukhmani Sahib | ashtpadis | Meditation | Yes |
| Daily Gurbani | shabads | Daily Shabads | Yes |
| Tattvartha Sutra | sutras | Jain Philosophy | Yes |
| Jain Prayers | teachings | Prayers & Teachings | Yes |

### 3.3 Reader Features
- **Gita Reader:** Chapter picker (horizontal), verse cards with Sanskrit/transliteration/translation/explanation, audio, bookmarks, sharing
- **Chalisa Reader:** Doha/Chaupai/Closing Doha sections
- **Japji Sahib Reader:** Mool Mantar + Pauris + Salok
- **Generic Reader:** Adapts to episode/shloka/verse/chapter/discourse/sutra formats
- **Study Mode:** Full-screen, one verse at a time, progressive reveal buttons, deep study timer (15s = bonus PP)
- **Focus Mode:** Single verse display with swipe navigation, progress bar
- **Word-by-Word:** Tap-to-select word chips with meaning display
- **Verse Sharing:** Beautiful gradient share card with Sanskrit + transliteration + translation + app watermark
- **Bookmarking:** Heart toggle, reflection notes, grouped by text type

### 3.4 Bookmarks Screen
- Grouped by text type with section headers
- Bookmark cards: reference badge, date, note preview (3 lines)
- Edit/delete actions
- Reflection sheet: multi-line text editor with gamification hint (+5 PP for first reflection)

---

## 4. Calendar Tab

### 4.1 Month Calendar View
- Month navigation with chevron buttons + animated month transitions
- Weekday header row
- Calendar grid (7 columns, adaptive cell size for iPhone/iPad)
- Day cells: day number, today indicator (border), festival dots (colored by category)
- **Swipe gestures** for month navigation
- **AnimatedContent** slide transitions between months

### 4.2 Day Detail Panel
- **Hindu Date Header** (SacredHighlightCard): display string, Samvat year, tradition-specific year, Gregorian date
- **Panchang Elements** (SacredCard): Tithi + time range, Nakshatra + time range, Yoga, Karana
- **Sun/Moon Times:** Sunrise, sunset
- **Festivals:** List with star icons, category names, tappable

---

## 5. Festivals Tab

### 5.1 Festival List
- Scrollable list sorted by date (90-day lookahead)
- Festival cards with:
  - Date column (day + month + Hindu tithi)
  - Festival name + Hindi/Sanskrit name
  - **Countdown badges:** Green "Today!" (pulsing), Primary "Tomorrow", Subdued "In X days"
  - 2-line description preview
  - "Read Full Story >" hint
  - Category badge
- Entrance animations (staggered)

### 5.2 Festival Detail (Sheet)
- Header card: category icon, festival name, Sanskrit names, category badge
- About section: description (localized)
- Story section (highlighted card, sacred font)
- "Did You Know?" section (lightbulb icon)
- **Calendar Rule section:** Rule description, tithi/nakshatra, duration if multi-day
- Observed-in section: tradition badges
- Gamification: records festival story read event

---

## 6. Settings Tab

### 6.1 Settings Sections
1. **Calendar Tradition:** Picker (Purnimant, Amavasyant, Gujarati, Bengali, Tamil, Malayalam)
2. **Location:** City display + coordinates + "Change" button (LocationPickerSheet)
3. **Calendar Sync:** Toggle + sync option picker + "Sync Now" + "Remove All"
4. **Spiritual Path:** DharmaPath picker (9 paths)
5. **Daily Wisdom Text:** Text picker grouped by path/other
6. **Morning Briefing Content:** 4 toggles (Panchang, Primary Text, Festival Stories, Secondary Text)
7. **Notifications:** Festival reminders toggle + timing checkboxes + briefing time picker
8. **Reading Progress:** Description + "Reset All" button with confirmation
9. **Sadhana Journey:** Toggle + level display + PP count + link to journey screen
10. **Audio Downloads:** Total usage + per-text download/delete + batch download
11. **Language:** AppLanguage picker (13 languages with native script)
12. **About:** Version, Calculation Method (Drik Ganit), Ayanamsa (Lahiri)

### 6.2 Location Picker
- "Use My Location" button with GPS detection
- Search field for city filtering
- City list with checkmark for selected
- Timezone display per location

---

## 7. Gamification System

### 7.1 Sadhana Journey Screen
- **Animated Progress Ring:** Outer glow, gradient fill, level icon center, percentage
- **Level Display:** Level number (gradient text), title, PP to next level
- **Stats Row:** Punya Points, Days Active, Badges earned
- **Badge Collection:** 8 categories in 3-column grid (5 on iPad)
  - Earned: gradient circle with glow animation
  - Locked: gray circle with lock icon
  - Tap for BadgeDetailSheet

### 7.2 Levels (1-20)
Curious Seeker > Devoted Learner > Morning Pilgrim > Scripture Student > Mindful Practitioner > Dharma Explorer > Sacred Reader > Festival Keeper > Mantra Chanter > Panchang Knower > Wisdom Seeker > Devoted Sadhaka > Temple Guardian > Story Keeper > Tradition Bearer > Knowledge Master > Spiritual Guide > Dharma Champion > Sacred Luminary > Enlightened Sage

### 7.3 Badges (26 total, 8 categories)
- **Streak:** 7, 30, 100, 365 days
- **Text Completion:** 15 badges (one per text)
- **Explorer:** 2 & 5 paths explored
- **Festival:** 5 & 20 stories read
- **Language:** 3 & 6 languages used
- **Panchang:** 7, 30, 100 days checked
- **Challenge:** 7 & 30 challenges completed
- **Engagement:** 10/100 explanations viewed, 5/25 reflections, 7/30 deep study sessions

### 7.4 Punya Points Awards
- App open: +5 PP (daily)
- Verse view: +5 PP
- Verse read (marked): +10 PP
- Explanation view: +5 PP
- Challenge correct: +15 PP
- Streak bonus: streak x 2 PP
- Text completion: +100 PP
- Reflection written: +5 PP
- Deep study (15s+): +3 PP/session (max 50/day)

### 7.5 Celebrations
- **LevelUpCelebration:** Gradient "LEVEL UP!" text, old > new level transition, sparkle separator, level title, confetti, gradient "Continue Journey" button
- **MilestoneCelebration:** Pulsing glow ring, milestone-specific icon/color, confetti, "Continue" button
- **BadgeDetailSheet:** Rotating ring (earned), glow pulse, badge info, earned date
- **PunyaPointsToast:** Slide-in from top, sparkle icon, "+X PP", auto-dismiss 1.8s

### 7.6 Daily Challenges (4 types)
- Panchang Explorer: Tithi/Nakshatra questions
- Festival Knowledge: Festival-description matching
- Verse Reflection: Sacred text facts
- Mantra Match: Mantra-to-meaning matching

---

## 8. Audio System

- Play/pause/resume controls per verse
- Progress bar with seek
- Remote audio from GitHub Releases
- Dual caching: Documents/Audio/ (persistent) + Caches/Audio/ (temporary)
- Bulk download per text (ZIP extraction)
- Per-text download management in Settings
- Disk usage reporting
- Estimated sizes: Gita ~92MB, Rudram ~18MB, Shikshapatri ~26MB

---

## 9. Onboarding (5 steps)

1. **Language Selection:** 2-column grid, native script names, primary highlight
2. **Welcome:** Om symbol (80pt), app name, feature list (5 items), gradient background
3. **Dharma Path:** 9 paths with accent colors, descriptions, text count
4. **Calendar Tradition:** Purnimant/Amavasyant selection with descriptions
5. **Location Setup:** City picker with search

- **Step progress dots** at top (filled up to current)
- **Back navigation** arrow on steps 1-4
- **Animated transitions** (horizontal slide + fade between steps)

---

## 10. Dharma Paths (9)

| Path | Primary Text | Available Texts |
|------|-------------|-----------------|
| General Hindu | Bhagavad Gita | Gita, Hanuman Chalisa |
| Vaishnav | Bhagavad Gita | Gita, Bhagavata, Vishnu Sahasranama, Hanuman Chalisa |
| Shaiv | Bhagavad Gita | Gita, Shiva Purana, Sri Rudram |
| Shakta | Devi Mahatmya | Devi Mahatmya, Soundarya Lahari, Gita |
| Smarta | Bhagavad Gita | Gita, Vishnu Sahasranama, Sri Rudram, Soundarya Lahari |
| ISKCON | Bhagavad Gita | Gita, Bhagavata |
| Swaminarayan | Shikshapatri | Shikshapatri, Vachanamrut, Gita |
| Sikh | Japji Sahib | Japji Sahib, Sukhmani Sahib, Daily Gurbani |
| Jain | Tattvartha Sutra | Tattvartha Sutra, Jain Prayers |

---

## 11. Localization

- 13 languages with native script names and native numeral systems
- Localized digits: Devanagari, Bengali, Gujarati, Telugu, Kannada, Gurmukhi, Odia
- Per-element localization: Tithi, Paksha, Nakshatra, Yoga, Karana, Month names
- Festival descriptions, stories, significance in all languages
- UI string localization throughout

---

## 12. Accessibility

- VoiceOver labels and hints on all interactive elements
- Accessibility traits (headers, buttons, selected states)
- Element combination for grouped content
- Escape key support for dismissals
- Haptic feedback: success, selection, impact
- Content descriptions on all icons

---

## 13. Notifications

- **Daily Briefing:** Scheduled at user's preferred time (default 7 AM), includes Hindu date, Rahu Kaal, festivals, verse reference
- **Festival Reminders:** Morning of (7 AM), evening before (6 PM), 1 day before, 2 days before
- Category-based: Major festivals get extra alerts
- Multilingual notification content

---

## 14. Calendar Sync

- Creates "Hindu Calendar" calendar (orange)
- All-day events with rich notes (description, story excerpt, panchang details)
- Sync options: Festivals Only, Festivals + Tithis, Full Panchang
- Alarms based on festival category
- ±1 year sync range
- Deduplication by festival ID + date

---

## 15. UI Components

1. SacredCard / SacredHighlightCard
2. SacredButton / SacredOutlineButton
3. SacredProgressRing
4. GreetingBanner
5. DecorativeDivider (OM, Diamond, Lotus)
6. ConfettiOverlay
7. PunyaPointsToast
8. SadhanaStatusBar
9. StreakBadgeView
10. DailyChallengeCard
11. RevealableVerseCard
12. WordByWordView
13. VerseShareCard
14. MiniAudioProgressBar
15. VerseAudioButton
16. VerseExplanationView
17. EmptyStateView
18. FloatingSparkles
19. HapticManager
