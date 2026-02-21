package dev.gopes.hinducalendar.data.model

data class AudioManifest(
    val version: Int = 1,
    val files: Map<String, AudioFileInfo> = emptyMap()
)

data class AudioFileInfo(
    val path: String,
    val durationMs: Int,
    val sizeBytes: Int
)
