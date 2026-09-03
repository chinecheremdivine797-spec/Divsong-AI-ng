package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.example.data.ai.DivAudioSynthesizer
import com.example.data.local.entities.SongEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

object AudioPlayerManager {
    private const val TAG = "AudioPlayerManager"

    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    private val _currentSong = MutableStateFlow<SongEntity?>(null)
    val currentSong: StateFlow<SongEntity?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0)
    val currentPositionMs: StateFlow<Int> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(1)
    val durationMs: StateFlow<Int> = _durationMs.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    fun playSong(context: Context, song: SongEntity) {
        val current = _currentSong.value
        if (current?.id == song.id && mediaPlayer != null) {
            if (!_isPlaying.value) {
                mediaPlayer?.start()
                _isPlaying.value = true
                startProgressTracker()
            }
            return
        }

        // Switching or playing new track
        _isBuffering.value = true
        _currentSong.value = song
        _currentPositionMs.value = 0
        _durationMs.value = (song.durationSeconds * 1000).coerceAtLeast(10000)

        scope.launch(Dispatchers.IO) {
            try {
                // If audioUrl is empty or local, synthesize or use file
                val audioPath = if (song.audioUrl.isNotEmpty() && File(song.audioUrl).exists()) {
                    song.audioUrl
                } else {
                    DivAudioSynthesizer.synthesizeSongAudio(
                        context = context,
                        songId = song.id,
                        genre = song.genre,
                        mood = song.mood,
                        durationSeconds = song.durationSeconds.coerceIn(20, 60)
                    )
                }

                launch(Dispatchers.Main) {
                    try {
                        releasePlayer()
                        mediaPlayer = MediaPlayer().apply {
                            setDataSource(audioPath)
                            isLooping = true
                            setOnPreparedListener { mp ->
                                _isBuffering.value = false
                                _durationMs.value = mp.duration.coerceAtLeast(1000)
                                mp.start()
                                _isPlaying.value = true
                                startProgressTracker()
                            }
                            setOnCompletionListener {
                                _isPlaying.value = false
                                _currentPositionMs.value = 0
                            }
                            setOnErrorListener { _, what, extra ->
                                Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                                _isBuffering.value = false
                                _isPlaying.value = false
                                true
                            }
                            prepareAsync()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error initializing player", e)
                        _isBuffering.value = false
                        _isPlaying.value = false
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error preparing audio file", e)
                _isBuffering.value = false
                _isPlaying.value = false
            }
        }
    }

    fun togglePlayPause(context: Context, song: SongEntity? = null) {
        val target = song ?: _currentSong.value ?: return
        if (_currentSong.value?.id == target.id) {
            if (_isPlaying.value) {
                pause()
            } else {
                mediaPlayer?.let {
                    it.start()
                    _isPlaying.value = true
                    startProgressTracker()
                } ?: playSong(context, target)
            }
        } else {
            playSong(context, target)
        }
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
        _isPlaying.value = false
        progressJob?.cancel()
    }

    fun seekTo(positionMs: Int) {
        mediaPlayer?.let {
            val validPos = positionMs.coerceIn(0, it.duration)
            it.seekTo(validPos)
            _currentPositionMs.value = validPos
        }
    }

    fun seekRelative(deltaMs: Int) {
        val current = _currentPositionMs.value
        seekTo(current + deltaMs)
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && _isPlaying.value) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        _currentPositionMs.value = mp.currentPosition
                        _durationMs.value = mp.duration.coerceAtLeast(1000)
                    }
                }
                delay(200)
            }
        }
    }

    private fun releasePlayer() {
        progressJob?.cancel()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing player", e)
        }
        mediaPlayer = null
    }
}
