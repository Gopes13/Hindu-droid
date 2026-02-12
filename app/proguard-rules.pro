# Hindu Calendar ProGuard Rules

# Keep Gson serialization classes
-keepclassmembers class dev.gopes.hinducalendar.engine.** { *; }
-keep class com.google.gson.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
