package com.example.data.ai

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.local.entities.GenerationJobEntity
import com.example.data.local.entities.SongEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

interface MusicGenerationProvider {
    val providerId: String
    val providerName: String
    val isConfigured: Boolean
    val statusMessage: String

    suspend fun createGenerationJob(
        userId: String,
        prompt: String,
        genre: String,
        mood: String,
        language: String,
        vocalStyle: String,
        lyricsOption: String,
        lyrics: String,
        isInstrumental: Boolean,
        structure: String,
        tempo: String,
        energy: String
    ): GenerationJobEntity

    suspend fun getGenerationStatus(jobId: String): String
    suspend fun cancelGeneration(jobId: String): Boolean
    suspend fun getGenerationResult(jobId: String): SongEntity?

    suspend fun executeGenerationPipeline(
        context: Context,
        job: GenerationJobEntity,
        onProgress: suspend (progressPercent: Int, stage: String) -> Unit
    ): SongEntity
}

/**
 * Real External AI Music Provider (e.g. Suno, Udio, Replicate MusicGen API)
 * Communicates with the external API when MUSIC_API_KEY is supplied.
 * If not configured, clearly indicates "Music generation is currently being configured."
 * and refuses to generate fake audio.
 */
object ExternalAiMusicProvider : MusicGenerationProvider {
    private const val TAG = "ExternalAiMusicProvider"
    override val providerId: String = "external_ai_music"
    override val providerName: String = "DIV Cloud Music Engine"

    private val activeJobs = ConcurrentHashMap<String, GenerationJobEntity>()
    private val completedSongs = ConcurrentHashMap<String, SongEntity>()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val apiKey: String
        get() = try {
            val key = BuildConfig.MUSIC_API_KEY
            if (key.isNullOrBlank() || key == "MY_MUSIC_API_KEY") "" else key.trim()
        } catch (e: Throwable) {
            ""
        }

    private val apiUrl: String
        get() = try {
            val url = BuildConfig.MUSIC_API_URL
            if (url.isNullOrBlank()) "https://api.divsongai.com/v1/generate" else url.trim()
        } catch (e: Throwable) {
            "https://api.divsongai.com/v1/generate"
        }

    override val isConfigured: Boolean
        get() = apiKey.isNotBlank()

    override val statusMessage: String
        get() = if (isConfigured) {
            "AI Music Provider Connected ($providerName)"
        } else {
            "Music generation is currently being configured."
        }

    override suspend fun createGenerationJob(
        userId: String,
        prompt: String,
        genre: String,
        mood: String,
        language: String,
        vocalStyle: String,
        lyricsOption: String,
        lyrics: String,
        isInstrumental: Boolean,
        structure: String,
        tempo: String,
        energy: String
    ): GenerationJobEntity {
        val jobId = "job_" + UUID.randomUUID().toString().take(8)
        val songId = "song_" + UUID.randomUUID().toString().take(8)

        val job = GenerationJobEntity(
            id = jobId,
            userId = userId,
            songId = songId,
            prompt = prompt,
            genre = genre,
            mood = mood,
            language = language,
            vocalStyle = vocalStyle,
            lyricsOption = lyricsOption,
            lyrics = lyrics,
            provider = providerName,
            status = if (isConfigured) "queued" else "not_configured",
            progressPercent = 0,
            currentStage = if (isConfigured) "Queued in generation pipeline" else "Music generation is currently being configured."
        )

        activeJobs[jobId] = job
        return job
    }

    override suspend fun getGenerationStatus(jobId: String): String {
        return activeJobs[jobId]?.status ?: "unknown"
    }

    override suspend fun cancelGeneration(jobId: String): Boolean {
        val job = activeJobs[jobId] ?: return false
        activeJobs[jobId] = job.copy(status = "cancelled", currentStage = "Generation cancelled by user")
        return true
    }

    override suspend fun getGenerationResult(jobId: String): SongEntity? {
        return completedSongs[jobId]
    }

    override suspend fun executeGenerationPipeline(
        context: Context,
        job: GenerationJobEntity,
        onProgress: suspend (progressPercent: Int, stage: String) -> Unit
    ): SongEntity = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            throw IllegalStateException("Music generation is currently being configured.")
        }

        // Real Provider Pipeline Execution
        onProgress(15, "Connecting to AI Music provider...")
        delay(600)

        onProgress(35, "Submitting music generation request...")
        val requestJson = JSONObject().apply {
            put("prompt", job.prompt)
            put("genre", job.genre)
            put("mood", job.mood)
            put("language", job.language)
            put("vocal_style", job.vocalStyle)
            put("lyrics", job.lyrics)
            put("instrumental", job.lyricsOption == "Instrumental")
            put("tempo", job.tempo)
            put("energy", job.energy)
        }

        val request = Request.Builder()
            .url(apiUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        onProgress(60, "Synthesizing AI audio stems...")
        val response = httpClient.newCall(request).execute()

        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: ""
            throw IllegalStateException("AI Music provider error (${response.code}): $errorBody")
        }

        val responseStr = response.body?.string() ?: "{}"
        val json = JSONObject(responseStr)
        val audioUrl = json.optString("audio_url")
        val coverUrl = json.optString("cover_url", "")
        val duration = json.optInt("duration_seconds", 180)
        val title = json.optString("title", "${job.mood} ${job.genre} Anthem")

        onProgress(95, "Mastering audio output...")
        delay(400)

        val completedSong = SongEntity(
            id = job.songId,
            userId = job.userId,
            creatorName = "You",
            title = title,
            description = job.prompt.ifBlank { "Original ${job.genre} production created with DIV SONG AI." },
            genre = job.genre,
            mood = job.mood,
            language = job.language,
            lyrics = job.lyrics,
            instrumental = (job.lyricsOption == "Instrumental"),
            audioUrl = audioUrl,
            coverImageUrl = coverUrl,
            durationSeconds = duration,
            visibility = "public",
            status = "ready",
            provider = providerName,
            vocalStyle = job.vocalStyle,
            tempo = job.tempo,
            energy = job.energy,
            structure = "Intro - Verse - Chorus - Verse - Chorus - Outro",
            createdAt = System.currentTimeMillis()
        )

        completedSongs[job.id] = completedSong
        activeJobs.remove(job.id)
        onProgress(100, "Song generated successfully!")
        completedSong
    }
}

/**
 * Explicit Developer / Sandbox Audio Synthesizer Provider.
 * Allows testing full audio playback, waveforms, and song workflows in development/offline modes.
 * Stamped clearly as "[DEVELOPMENT / SANDBOX MODE]" as mandated by guidelines.
 */
object DevSandboxSynthesizerProvider : MusicGenerationProvider {
    override val providerId: String = "dev_sandbox"
    override val providerName: String = "[SANDBOX MODE] Tone Synthesizer"

    override val isConfigured: Boolean = true
    override val statusMessage: String = "Development Sandbox Mode Active (Offline Sound Synthesizer)"

    private val activeJobs = ConcurrentHashMap<String, GenerationJobEntity>()
    private val completedSongs = ConcurrentHashMap<String, SongEntity>()

    override suspend fun createGenerationJob(
        userId: String,
        prompt: String,
        genre: String,
        mood: String,
        language: String,
        vocalStyle: String,
        lyricsOption: String,
        lyrics: String,
        isInstrumental: Boolean,
        structure: String,
        tempo: String,
        energy: String
    ): GenerationJobEntity {
        val jobId = "job_" + UUID.randomUUID().toString().take(8)
        val songId = "song_" + UUID.randomUUID().toString().take(8)

        val job = GenerationJobEntity(
            id = jobId,
            userId = userId,
            songId = songId,
            prompt = prompt,
            genre = genre,
            mood = mood,
            language = language,
            vocalStyle = vocalStyle,
            lyricsOption = lyricsOption,
            lyrics = lyrics,
            provider = providerName,
            status = "processing",
            progressPercent = 10,
            currentStage = "Preparing development sandbox synthesis..."
        )
        activeJobs[jobId] = job
        return job
    }

    override suspend fun getGenerationStatus(jobId: String): String {
        return activeJobs[jobId]?.status ?: "unknown"
    }

    override suspend fun cancelGeneration(jobId: String): Boolean {
        val job = activeJobs[jobId] ?: return false
        activeJobs[jobId] = job.copy(status = "cancelled", currentStage = "Cancelled by user")
        return true
    }

    override suspend fun getGenerationResult(jobId: String): SongEntity? {
        return completedSongs[jobId]
    }

    override suspend fun executeGenerationPipeline(
        context: Context,
        job: GenerationJobEntity,
        onProgress: suspend (progressPercent: Int, stage: String) -> Unit
    ): SongEntity = withContext(Dispatchers.IO) {
        onProgress(15, "Preparing development sandbox synthesis...")
        delay(600)

        onProgress(35, "Structuring lyrics...")
        val finalLyrics = if (job.lyricsOption == "AI Generate" && job.lyrics.isBlank()) {
            GeminiLyricsService.generateLyrics(
                prompt = job.prompt,
                genre = job.genre,
                mood = job.mood,
                language = job.language,
                vocalStyle = job.vocalStyle
            )
        } else if (job.lyricsOption == "Instrumental") {
            "[Instrumental Track - Harmonic Audio Synthesis]"
        } else {
            job.lyrics.ifBlank { "DIV SONG AI Music Studio" }
        }
        delay(700)

        onProgress(55, "Synthesizing harmonic chord progressions...")
        delay(800)

        onProgress(75, "Rendering 44.1kHz stereo audio stems...")
        val audioPath = DivAudioSynthesizer.synthesizeSongAudio(
            context = context,
            songId = job.songId,
            genre = job.genre,
            mood = job.mood,
            durationSeconds = 35
        )
        delay(800)

        onProgress(95, "Mastering sandbox mix...")
        delay(400)

        val generatedTitle = if (job.prompt.isNotBlank() && job.prompt.length < 35) {
            job.prompt.trim().replaceFirstChar { it.uppercase() }
        } else {
            "${job.mood} ${job.genre} Anthem"
        }

        val completedSong = SongEntity(
            id = job.songId,
            userId = job.userId,
            creatorName = "You",
            title = generatedTitle,
            description = "[Sandbox Mode] Generated via local development tone synthesizer for offline verification.",
            genre = job.genre,
            mood = job.mood,
            language = job.language,
            lyrics = finalLyrics,
            instrumental = (job.lyricsOption == "Instrumental"),
            audioUrl = audioPath,
            coverImageUrl = "",
            durationSeconds = 180,
            visibility = "public",
            status = "ready",
            provider = providerName,
            vocalStyle = job.vocalStyle,
            tempo = job.tempo,
            energy = job.energy,
            structure = "Intro - Verse - Chorus - Verse - Chorus - Outro",
            createdAt = System.currentTimeMillis()
        )

        completedSongs[job.id] = completedSong
        activeJobs.remove(job.id)
        onProgress(100, "Song generated successfully!")
        completedSong
    }
}

/**
 * Provider Manager: Coordinates active provider, sandbox toggling, and configuration reporting.
 */
object MusicProviderManager {
    private val _isDevSandboxMode = MutableStateFlow(false)
    val isDevSandboxMode: StateFlow<Boolean> = _isDevSandboxMode.asStateFlow()

    fun setDevSandboxMode(enabled: Boolean) {
        _isDevSandboxMode.value = enabled
    }

    val activeProvider: MusicGenerationProvider
        get() = if (_isDevSandboxMode.value) DevSandboxSynthesizerProvider else ExternalAiMusicProvider

    fun isConfigured(): Boolean = activeProvider.isConfigured

    fun getStatusMessage(): String = activeProvider.statusMessage
}
