package com.example.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
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

object AudioRecorderManager {
    private const val TAG = "AudioRecorderManager"

    private var mediaRecorder: MediaRecorder? = null
    private var currentRecordingFile: File? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _recordingDurationSeconds = MutableStateFlow(0)
    val recordingDurationSeconds: StateFlow<Int> = _recordingDurationSeconds.asStateFlow()

    private val _recordedAudioPath = MutableStateFlow<String?>(null)
    val recordedAudioPath: StateFlow<String?> = _recordedAudioPath.asStateFlow()

    private val _currentMaxAmplitude = MutableStateFlow(0)
    val currentMaxAmplitude: StateFlow<Int> = _currentMaxAmplitude.asStateFlow()

    fun startRecording(context: Context): Boolean {
        try {
            stopRecording() // Clean up any active session

            val outputDir = File(context.cacheDir, "recordings").apply { mkdirs() }
            val outputFile = File(outputDir, "vocal_rec_${System.currentTimeMillis()}.m4a")
            currentRecordingFile = outputFile

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(192000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }

            _isRecording.value = true
            _isPaused.value = false
            _recordingDurationSeconds.value = 0
            _recordedAudioPath.value = null

            startTimerAndAmplitudeTracker()
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error starting voice recording", e)
            _isRecording.value = false
            _isPaused.value = false
            return false
        }
    }

    fun pauseRecording() {
        if (_isRecording.value && !_isPaused.value) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mediaRecorder?.pause()
                    _isPaused.value = true
                    timerJob?.cancel()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error pausing recording", e)
            }
        }
    }

    fun resumeRecording() {
        if (_isRecording.value && _isPaused.value) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mediaRecorder?.resume()
                    _isPaused.value = false
                    startTimerAndAmplitudeTracker()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error resuming recording", e)
            }
        }
    }

    fun stopRecording(): String? {
        timerJob?.cancel()
        if (_isRecording.value) {
            try {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping recorder", e)
            } finally {
                mediaRecorder = null
                _isRecording.value = false
                _isPaused.value = false
            }
        }
        val path = currentRecordingFile?.absolutePath
        if (currentRecordingFile?.exists() == true && (currentRecordingFile?.length() ?: 0L) > 1000L) {
            _recordedAudioPath.value = path
            return path
        }
        return null
    }

    fun clearRecording() {
        stopRecording()
        currentRecordingFile?.delete()
        currentRecordingFile = null
        _recordedAudioPath.value = null
        _recordingDurationSeconds.value = 0
    }

    private fun startTimerAndAmplitudeTracker() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive && _isRecording.value && !_isPaused.value) {
                delay(200)
                try {
                    val amp = mediaRecorder?.maxAmplitude ?: 0
                    _currentMaxAmplitude.value = amp
                } catch (_: Exception) {
                }
                delay(800)
                _recordingDurationSeconds.value += 1
            }
        }
    }
}
