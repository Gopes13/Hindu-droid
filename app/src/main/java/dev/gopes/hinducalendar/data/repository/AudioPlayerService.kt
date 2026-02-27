package dev.gopes.hinducalendar.data.repository

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import dev.gopes.hinducalendar.data.engine.AudioDownloadManager
import dev.gopes.hinducalendar.domain.model.AudioFileInfo
import dev.gopes.hinducalendar.domain.model.AudioManifest
import dev.gopes.hinducalendar.data.service.KirtanPlaybackService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import dev.gopes.hinducalendar.domain.model.SacredTextType
import java.io.File
import java.net.HttpURLConnection
import dev.gopes.hinducalendar.domain.repository.AudioPlaybackRepository
import dev.gopes.hinducalendar.domain.repository.AudioPlaybackState
import java.net.URL

class AudioPlayerService(
    private val context: Context,
    private val downloadManager: AudioDownloadManager
) : AudioPlaybackRepository {

    companion object {
        private const val BASE_URL =
            "https://github.com/Gopes13/hindu-calendar-audio/releases/download/v1-audio/"
        private const val MANIFEST_FILE = "manifest.json"
    }

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    private val _currentlyPlayingId = MutableStateFlow<String?>(null)
    override val currentlyPlayingId: StateFlow<String?> = _currentlyPlayingId.asStateFlow()

    private val _state = MutableStateFlow(AudioPlaybackState.IDLE)
    override val state: StateFlow<AudioPlaybackState> = _state.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0.0)
    override val playbackProgress: StateFlow<Double> = _playbackProgress.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0)
    override val currentPositionMs: StateFlow<Int> = _currentPositionMs.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    override val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private var manifest: AudioManifest? = null

    private val downloadsDir: File
        get() = File(context.filesDir, "Audio").also { it.mkdirs() }

    private val cacheDir: File
        get() = File(context.cacheDir, "Audio").also { it.mkdirs() }

    // MARK: - Manifest

    override fun loadManifest() {
        scope.launch(Dispatchers.IO) {
            // Try local bundled manifest
            try {
                val json = context.assets.open("audio_manifest.json").bufferedReader().readText()
                manifest = Gson().fromJson(json, AudioManifest::class.java)
                Timber.d("Loaded bundled manifest: ${manifest?.files?.size} entries")
                return@launch
            } catch (_: Exception) { }

            // Try cached manifest
            val cachedManifest = File(downloadsDir, MANIFEST_FILE)
            if (cachedManifest.exists()) {
                try {
                    manifest = Gson().fromJson(cachedManifest.readText(), AudioManifest::class.java)
                    Timber.d("Loaded cached manifest: ${manifest?.files?.size} entries")
                    return@launch
                } catch (_: Exception) { }
            }

            // Fetch from remote with timeout
            try {
                val connection = URL(BASE_URL + MANIFEST_FILE).openConnection() as HttpURLConnection
                connection.connectTimeout = 15_000
                connection.readTimeout = 15_000
                val json = connection.inputStream.bufferedReader().readText()
                connection.disconnect()
                manifest = Gson().fromJson(json, AudioManifest::class.java)
                cachedManifest.writeText(json)
                Timber.d("Fetched remote manifest: ${manifest?.files?.size} entries")
            } catch (e: Exception) {
                Timber.w("Failed to load audio manifest: ${e.message}")
            }
        }
    }

    // MARK: - Query

    override fun hasAudio(audioId: String): Boolean {
        return manifest?.files?.containsKey(audioId) == true
    }

    override fun isPlaying(audioId: String): Boolean {
        return _currentlyPlayingId.value == audioId && _state.value == AudioPlaybackState.PLAYING
    }

    override fun audioState(audioId: String): AudioPlaybackState {
        return if (_currentlyPlayingId.value == audioId) _state.value else AudioPlaybackState.IDLE
    }

    override fun duration(audioId: String): Int? {
        return manifest?.files?.get(audioId)?.durationMs
    }

    // MARK: - Playback

    override fun toggle(audioId: String, displayTitle: String?) {
        when {
            _currentlyPlayingId.value == audioId && _state.value == AudioPlaybackState.PLAYING -> pause()
            _currentlyPlayingId.value == audioId && _state.value == AudioPlaybackState.PAUSED -> resume()
            else -> play(audioId, displayTitle)
        }
    }

    override fun play(audioId: String, displayTitle: String?) {
        stop()

        val fileInfo = manifest?.files?.get(audioId) ?: run {
            Timber.w("Audio not in manifest: $audioId")
            _state.value = AudioPlaybackState.ERROR
            return
        }

        _currentlyPlayingId.value = audioId
        _state.value = AudioPlaybackState.LOADING
        _playbackProgress.value = 0.0
        _currentPositionMs.value = 0

        // Start foreground service for kirtans (background playback + notification)
        if (audioId.startsWith("kirtans_")) {
            try {
                val intent = Intent(context, KirtanPlaybackService::class.java)
                    .putExtra(KirtanPlaybackService.EXTRA_AUDIO_ID, audioId)
                    .putExtra(KirtanPlaybackService.EXTRA_TITLE, displayTitle)
                ContextCompat.startForegroundService(context, intent)
            } catch (e: Exception) {
                Timber.w("Could not start playback service: ${e.message}")
            }
        }

        scope.launch(Dispatchers.IO) {
            val file = findLocalFile(audioId, fileInfo) ?: downloadOnDemand(audioId, fileInfo)
            if (file == null) {
                withContext(Dispatchers.Main) {
                    _state.value = AudioPlaybackState.ERROR
                }
                return@launch
            }

            withContext(Dispatchers.Main) {
                // Guard on Main thread to avoid race with stop()
                if (_currentlyPlayingId.value != audioId) return@withContext
                startPlayback(file, audioId)
            }
        }
    }

    override fun pause() {
        mediaPlayer?.pause()
        _state.value = AudioPlaybackState.PAUSED
        progressJob?.cancel()
    }

    override fun resume() {
        val player = mediaPlayer ?: return
        player.start()
        _state.value = AudioPlaybackState.PLAYING
        startProgressTracking()
    }

    override fun stop() {
        progressJob?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
        _currentlyPlayingId.value = null
        _state.value = AudioPlaybackState.IDLE
        _playbackProgress.value = 0.0
        _currentPositionMs.value = 0
        _playbackSpeed.value = 1.0f

        // Stop foreground service
        try {
            context.stopService(Intent(context, KirtanPlaybackService::class.java))
        } catch (_: Exception) { }
    }

    // MARK: - Seek & Speed

    override fun seekTo(positionMs: Int) {
        mediaPlayer?.seekTo(positionMs)
        _currentPositionMs.value = positionMs
    }

    override fun skipForward(ms: Int) {
        val player = mediaPlayer ?: return
        val target = (player.currentPosition + ms).coerceAtMost(player.duration)
        seekTo(target)
    }

    override fun skipBackward(ms: Int) {
        val player = mediaPlayer ?: return
        val target = (player.currentPosition - ms).coerceAtLeast(0)
        seekTo(target)
    }

    override fun setSpeed(speed: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mediaPlayer?.let {
                try {
                    it.playbackParams = it.playbackParams.setSpeed(speed)
                    _playbackSpeed.value = speed
                } catch (e: Exception) {
                    Timber.w("Could not set playback speed: ${e.message}")
                }
            }
        }
    }

    // MARK: - Private

    private fun findLocalFile(audioId: String, fileInfo: AudioFileInfo): File? {
        // Check bulk downloads first
        val bulkFile = File(downloadsDir, fileInfo.path)
        if (bulkFile.exists()) return bulkFile

        // Check on-demand cache
        val cachedFile = File(cacheDir, "$audioId.m4a")
        if (cachedFile.exists()) return cachedFile

        return null
    }

    private suspend fun downloadOnDemand(audioId: String, fileInfo: AudioFileInfo): File? {
        // Kirtans: download single file directly (not bulk ZIP)
        if (audioId.startsWith("kirtans_")) {
            return downloadSingleFile(audioId, fileInfo.path)
        }

        // Derive text type from manifest path (e.g. "gita/gita_1_1.m4a" → "gita")
        val textDir = fileInfo.path.substringBefore("/")
        val textType = SacredTextType.entries.firstOrNull { it.jsonFileName == textDir }
        if (textType != null) {
            try {
                downloadManager.downloadTextAwait(textType)
                // After zip extracted, check for local file again
                val bulkFile = File(downloadsDir, fileInfo.path)
                if (bulkFile.exists()) return bulkFile
            } catch (e: Exception) {
                Timber.e("Failed to download audio pack for $textDir: ${e.message}")
            }
        }
        return null
    }

    private suspend fun downloadSingleFile(audioId: String, path: String): File? {
        val destFile = File(cacheDir, "$audioId.m4a")
        if (destFile.exists()) return destFile
        return try {
            val url = URL(BASE_URL + path)
            url.openStream().use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            Timber.d("Downloaded single file: $path")
            destFile
        } catch (e: Exception) {
            Timber.e("Failed to download $path: ${e.message}")
            destFile.delete()
            null
        }
    }

    private fun startPlayback(file: File, audioId: String) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, Uri.fromFile(file))
                setOnPreparedListener { player ->
                    if (_currentlyPlayingId.value != audioId) {
                        player.release()
                        return@setOnPreparedListener
                    }
                    player.start()
                    _state.value = AudioPlaybackState.PLAYING
                    startProgressTracking()
                }
                setOnErrorListener { _, what, extra ->
                    Timber.e("MediaPlayer error: what=$what extra=$extra")
                    _state.value = AudioPlaybackState.ERROR
                    true
                }
                setOnCompletionListener {
                    stop()
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Timber.e("Playback error: ${e.message}")
            _state.value = AudioPlaybackState.ERROR
        }
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                val player = mediaPlayer ?: break
                if (player.isPlaying && player.duration > 0) {
                    _currentPositionMs.value = player.currentPosition
                    _playbackProgress.value = player.currentPosition.toDouble() / player.duration
                }
                delay(50)
            }
        }
    }
}
