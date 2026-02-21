package dev.gopes.hinducalendar.engine

import android.content.Context
import dev.gopes.hinducalendar.data.model.SacredTextType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.zip.ZipInputStream

class AudioDownloadManager(private val context: Context) {

    companion object {
        private const val BASE_URL =
            "https://github.com/Gopes13/hindu-calendar-audio/releases/download/v1-audio/"
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _downloadProgress = MutableStateFlow<Map<SacredTextType, Double>>(emptyMap())
    val downloadProgress: StateFlow<Map<SacredTextType, Double>> = _downloadProgress.asStateFlow()

    private val _downloadedTexts = MutableStateFlow<Set<SacredTextType>>(emptySet())
    val downloadedTexts: StateFlow<Set<SacredTextType>> = _downloadedTexts.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    private val downloadsDir: File
        get() = File(context.filesDir, "Audio").also { it.mkdirs() }

    init {
        scanDownloadedTexts()
    }

    // MARK: - Query

    fun isDownloaded(textType: SacredTextType): Boolean {
        return _downloadedTexts.value.contains(textType)
    }

    fun diskUsage(textType: SacredTextType): String {
        val dir = File(downloadsDir, textType.jsonFileName)
        if (!dir.exists()) return "0 MB"
        val total = dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        return formatBytes(total)
    }

    fun totalDiskUsage(): String {
        val total = downloadsDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        return formatBytes(total)
    }

    fun estimatedSize(textType: SacredTextType): String {
        return when (textType) {
            SacredTextType.GITA -> "~92 MB"
            SacredTextType.HANUMAN_CHALISA -> "~4 MB"
            SacredTextType.JAPJI_SAHIB -> "~2 MB"
            SacredTextType.BHAGAVATA -> "~7 MB"
            SacredTextType.VISHNU_SAHASRANAMA -> "~15 MB"
            SacredTextType.SHIVA_PURANA -> "~2 MB"
            SacredTextType.RUDRAM -> "~18 MB"
            SacredTextType.DEVI_MAHATMYA -> "~8 MB"
            SacredTextType.SOUNDARYA_LAHARI -> "~23 MB"
            SacredTextType.SHIKSHAPATRI -> "~26 MB"
            SacredTextType.SUKHMANI -> "~3 MB"
            SacredTextType.GURBANI -> "~9 MB"
            SacredTextType.TATTVARTHA_SUTRA -> "~17 MB"
            SacredTextType.JAIN_PRAYERS -> "~1 MB"
            else -> "~5 MB"
        }
    }

    // MARK: - Download

    fun downloadText(textType: SacredTextType) {
        if (isDownloaded(textType)) return
        if (!textType.hasAudio) return

        scope.launch {
            downloadTextAwait(textType)
        }
    }

    /** Download and extract audio zip, suspending until complete. */
    suspend fun downloadTextAwait(textType: SacredTextType) {
        if (isDownloaded(textType)) return
        if (!textType.hasAudio) return

        withContext(Dispatchers.IO) {
            _isDownloading.value = true
            updateProgress(textType, 0.0)

            try {
                val zipName = "audio-${textType.jsonFileName}.zip"
                val url = URL(BASE_URL + zipName)
                val destDir = File(downloadsDir, textType.jsonFileName)
                destDir.mkdirs()

                // Download and unzip
                val connection = url.openConnection()
                val totalSize = connection.contentLengthLong
                val inputStream = BufferedInputStream(connection.getInputStream())

                val tempFile = File.createTempFile("audio_", ".zip", context.cacheDir)
                FileOutputStream(tempFile).use { out ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Long = 0
                    var len: Int
                    while (inputStream.read(buffer).also { len = it } != -1) {
                        out.write(buffer, 0, len)
                        bytesRead += len
                        if (totalSize > 0) {
                            updateProgress(textType, (bytesRead.toDouble() / totalSize) * 0.9)
                        }
                    }
                }
                inputStream.close()

                // Unzip
                ZipInputStream(tempFile.inputStream()).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        if (!entry.isDirectory) {
                            val fileName = File(entry.name).name // Strip directory prefix
                            val outFile = File(destDir, fileName)
                            FileOutputStream(outFile).use { out ->
                                zip.copyTo(out)
                            }
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }

                tempFile.delete()
                updateProgress(textType, 1.0)
                _downloadedTexts.value = _downloadedTexts.value + textType

            } catch (e: Exception) {
                Timber.e("Download failed for ${textType.jsonFileName}: ${e.message}")
                _downloadProgress.value = _downloadProgress.value - textType
            }

            _isDownloading.value = false
        }
    }

    fun downloadAll() {
        scope.launch {
            SacredTextType.entries
                .filter { it.hasAudio && !isDownloaded(it) }
                .forEach { downloadText(it) }
        }
    }

    fun deleteDownloads(textType: SacredTextType) {
        val dir = File(downloadsDir, textType.jsonFileName)
        dir.deleteRecursively()
        _downloadedTexts.value = _downloadedTexts.value - textType
    }

    fun deleteAllDownloads() {
        _downloadedTexts.value.toSet().forEach { deleteDownloads(it) }
    }

    // MARK: - Private

    private fun scanDownloadedTexts() {
        val found = mutableSetOf<SacredTextType>()
        for (textType in SacredTextType.entries) {
            if (!textType.hasAudio) continue
            val dir = File(downloadsDir, textType.jsonFileName)
            if (dir.exists() && dir.isDirectory) {
                val hasM4a = dir.listFiles()?.any { it.extension == "m4a" } == true
                if (hasM4a) found.add(textType)
            }
        }
        _downloadedTexts.value = found
    }

    private fun updateProgress(textType: SacredTextType, progress: Double) {
        _downloadProgress.value = _downloadProgress.value + (textType to progress)
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        }
    }
}
