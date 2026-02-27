package dev.gopes.hinducalendar.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import dagger.hilt.android.AndroidEntryPoint
import dev.gopes.hinducalendar.MainActivity
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.engine.AudioPlaybackState
import dev.gopes.hinducalendar.engine.AudioPlayerService
import dev.gopes.hinducalendar.engine.KirtanService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@AndroidEntryPoint
class KirtanPlaybackService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 9001
        private const val CHANNEL_ID = "kirtan_playback"
        const val EXTRA_AUDIO_ID = "audioId"
        const val EXTRA_TITLE = "displayTitle"
        private const val ACTION_TOGGLE = "dev.gopes.hinducalendar.ACTION_TOGGLE"
        private const val ACTION_SKIP_FORWARD = "dev.gopes.hinducalendar.ACTION_SKIP_FORWARD"
        private const val ACTION_SKIP_BACKWARD = "dev.gopes.hinducalendar.ACTION_SKIP_BACKWARD"
    }

    @Inject lateinit var audioPlayerService: AudioPlayerService
    @Inject lateinit var kirtanService: KirtanService

    private lateinit var mediaSession: MediaSessionCompat
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var observerJob: Job? = null
    private var currentTitle: String = ""

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannel()
        mediaSession = MediaSessionCompat(this, "KirtanPlayback").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    audioPlayerService.currentlyPlayingId.value?.let {
                        audioPlayerService.resume()
                    }
                }
                override fun onPause() {
                    audioPlayerService.pause()
                }
                override fun onStop() {
                    audioPlayerService.stop()
                }
                override fun onSeekTo(pos: Long) {
                    audioPlayerService.seekTo(pos.toInt())
                }
                override fun onSkipToNext() {
                    audioPlayerService.skipForward()
                }
                override fun onSkipToPrevious() {
                    audioPlayerService.skipBackward()
                }
                override fun onSetPlaybackSpeed(speed: Float) {
                    audioPlayerService.setSpeed(speed)
                }
            })
            isActive = true
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle notification button actions
        when (intent?.action) {
            ACTION_TOGGLE -> {
                audioPlayerService.currentlyPlayingId.value?.let { audioId ->
                    audioPlayerService.toggle(audioId)
                }
                return START_NOT_STICKY
            }
            ACTION_SKIP_FORWARD -> {
                audioPlayerService.skipForward()
                return START_NOT_STICKY
            }
            ACTION_SKIP_BACKWARD -> {
                audioPlayerService.skipBackward()
                return START_NOT_STICKY
            }
        }

        val audioId = intent?.getStringExtra(EXTRA_AUDIO_ID) ?: run {
            stopSelf()
            return START_NOT_STICKY
        }

        // Use localized title from intent, fall back to Sanskrit
        val displayTitle = intent.getStringExtra(EXTRA_TITLE)
        val kirtanId = audioId.removePrefix("kirtans_")
        val kirtan = kirtanService.kirtanById(kirtanId)
        currentTitle = displayTitle ?: kirtan?.titleSanskrit ?: kirtanId

        // Set media metadata
        val durationMs = audioPlayerService.duration(audioId)?.toLong() ?: 0L
        mediaSession.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentTitle)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Kiraan")
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, durationMs)
                .build()
        )

        // Build initial notification and start foreground immediately
        val notification = buildNotification(currentTitle, isPlaying = false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        // Observe playback state and update notification
        observerJob?.cancel()
        observerJob = scope.launch {
            combine(
                audioPlayerService.state,
                audioPlayerService.currentlyPlayingId
            ) { state, playingId -> state to playingId }.collect { (state, playingId) ->
                when {
                    state == AudioPlaybackState.IDLE || playingId == null -> {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }
                    else -> {
                        val isPlaying = state == AudioPlaybackState.PLAYING
                        updatePlaybackState(isPlaying)
                        val notif = buildNotification(currentTitle, isPlaying)
                        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                        nm.notify(NOTIFICATION_ID, notif)
                    }
                }
            }
        }

        return START_NOT_STICKY
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID, "Kirtan Playback", NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Controls for kirtan audio playback"
                    setSound(null, null)
                }
                nm.createNotificationChannel(channel)
            }
        }
    }

    private fun updatePlaybackState(isPlaying: Boolean) {
        val position = audioPlayerService.currentPositionMs.value.toLong()
        val speed = audioPlayerService.playbackSpeed.value
        val actions = PlaybackStateCompat.ACTION_SEEK_TO or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_SET_PLAYBACK_SPEED
        val state = if (isPlaying) {
            PlaybackStateCompat.Builder()
                .setActions(actions or PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_STOP)
                .setState(PlaybackStateCompat.STATE_PLAYING, position, speed)
                .build()
        } else {
            PlaybackStateCompat.Builder()
                .setActions(actions or PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_STOP)
                .setState(PlaybackStateCompat.STATE_PAUSED, position, 0f)
                .build()
        }
        mediaSession.setPlaybackState(state)
    }

    private fun buildNotification(title: String, isPlaying: Boolean): android.app.Notification {
        // Tap notification → open app
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Skip backward action
        val skipBackIntent = Intent(this, KirtanPlaybackService::class.java)
            .setAction(ACTION_SKIP_BACKWARD)
        val skipBackPendingIntent = PendingIntent.getService(
            this, 2, skipBackIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val skipBackAction = NotificationCompat.Action.Builder(
            R.drawable.ic_skip_backward, "Rewind", skipBackPendingIntent
        ).build()

        // Play/Pause action
        val actionIcon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        val actionTitle = if (isPlaying) "Pause" else "Play"
        val toggleIntent = Intent(this, KirtanPlaybackService::class.java)
            .setAction(ACTION_TOGGLE)
        val togglePendingIntent = PendingIntent.getService(
            this, 1, toggleIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val playPauseAction = NotificationCompat.Action.Builder(
            actionIcon, actionTitle, togglePendingIntent
        ).build()

        // Skip forward action
        val skipFwdIntent = Intent(this, KirtanPlaybackService::class.java)
            .setAction(ACTION_SKIP_FORWARD)
        val skipFwdPendingIntent = PendingIntent.getService(
            this, 3, skipFwdIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val skipFwdAction = NotificationCompat.Action.Builder(
            R.drawable.ic_skip_forward, "Forward", skipFwdPendingIntent
        ).build()

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("Kiraan")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(contentIntent)
            .addAction(skipBackAction)
            .addAction(playPauseAction)
            .addAction(skipFwdAction)
            .setStyle(
                MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setOngoing(isPlaying)
            .setSilent(true)
            .build()
    }

    override fun onDestroy() {
        observerJob?.cancel()
        scope.cancel()
        audioPlayerService.stop()
        mediaSession.isActive = false
        mediaSession.release()
        super.onDestroy()
    }
}
