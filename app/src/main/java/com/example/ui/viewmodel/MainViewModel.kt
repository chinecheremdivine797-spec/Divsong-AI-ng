package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPlayerManager
import com.example.data.ai.GeminiLyricsService
import com.example.data.ai.MusicProviderManager
import com.example.data.local.AppDatabase
import com.example.data.local.entities.GenerationJobEntity
import com.example.data.local.entities.PlanEntity
import com.example.data.local.entities.ReportEntity
import com.example.data.local.entities.SongEntity
import com.example.data.local.entities.TransactionEntity
import com.example.data.local.entities.UserEntity
import com.example.data.repository.AdminRepository
import com.example.data.repository.AuthRepository
import com.example.data.repository.PlanRepository
import com.example.data.repository.SongRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    val authRepo = AuthRepository(database)
    val songRepo = SongRepository(database)
    val planRepo = PlanRepository(database)
    val adminRepo = AdminRepository(database)

    // Current User
    val currentUser: StateFlow<UserEntity?> = authRepo.getCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Public explore songs
    val publicSongs: StateFlow<List<SongEntity>> = songRepo.getPublicSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User's own library songs
    val userSongs: StateFlow<List<SongEntity>> = authRepo.currentUserId.flatMapLatest { uid ->
        songRepo.getUserSongs(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All active plans
    val plans: StateFlow<List<PlanEntity>> = planRepo.getActivePlans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All plans for admin
    val allPlans: StateFlow<List<PlanEntity>> = planRepo.getAllPlans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User transactions
    val userTransactions: StateFlow<List<TransactionEntity>> = authRepo.currentUserId.flatMapLatest { uid ->
        planRepo.getUserTransactions(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin streams
    val adminUsers: StateFlow<List<UserEntity>> = adminRepo.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminSongs: StateFlow<List<SongEntity>> = adminRepo.getAllSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminJobs: StateFlow<List<GenerationJobEntity>> = adminRepo.getAllJobs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminTransactions: StateFlow<List<TransactionEntity>> = adminRepo.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminReports: StateFlow<List<ReportEntity>> = adminRepo.getAllReports()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Create Music State
    var promptState = MutableStateFlow("")
    var genreState = MutableStateFlow("Afrobeats")
    var customGenreState = MutableStateFlow("")
    var moodState = MutableStateFlow("Energetic")
    var languageState = MutableStateFlow("English")
    var vocalStyleState = MutableStateFlow("Male vocal")
    var lyricsOptionState = MutableStateFlow("AI Generate") // "AI Generate", "Write My Own", "Instrumental"
    var lyricsTextState = MutableStateFlow("")
    var structureState = MutableStateFlow("Intro - Verse - Pre-Chorus - Chorus - Verse - Chorus - Outro")
    var tempoState = MutableStateFlow("Medium (118 BPM)")
    var energyState = MutableStateFlow("High")
    var creativityLevel = MutableStateFlow(0.8f)
    var isSimpleMode = MutableStateFlow(false)

    // Generation in-progress
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generationProgress = MutableStateFlow(0)
    val generationProgress: StateFlow<Int> = _generationProgress.asStateFlow()

    private val _generationStage = MutableStateFlow("Preparing...")
    val generationStage: StateFlow<String> = _generationStage.asStateFlow()

    private val _lastGeneratedSong = MutableStateFlow<SongEntity?>(null)
    val lastGeneratedSong: StateFlow<SongEntity?> = _lastGeneratedSong.asStateFlow()

    private val _isAiWritingLyrics = MutableStateFlow(false)
    val isAiWritingLyrics: StateFlow<Boolean> = _isAiWritingLyrics.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private var activeGenerationJob: Job? = null

    // Provider Configuration & Sandbox Mode
    val isDevSandboxMode = MusicProviderManager.isDevSandboxMode
    val isMusicProviderConfigured: Boolean get() = MusicProviderManager.isConfigured()
    val musicProviderStatusMessage: String get() = MusicProviderManager.getStatusMessage()

    fun toggleDevSandboxMode(enabled: Boolean) {
        MusicProviderManager.setDevSandboxMode(enabled)
        showToast(if (enabled) "Sandbox Mode Active (Local Synthesizer)" else "Cloud Music Engine Active")
    }

    // Audio Playback
    val currentPlayingSong = AudioPlayerManager.currentSong
    val isPlaying = AudioPlayerManager.isPlaying
    val currentPositionMs = AudioPlayerManager.currentPositionMs
    val durationMs = AudioPlayerManager.durationMs
    val isBuffering = AudioPlayerManager.isBuffering

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun playSong(song: SongEntity) {
        AudioPlayerManager.playSong(getApplication(), song)
        viewModelScope.launch {
            database.songDao().incrementPlayCount(song.id)
        }
    }

    fun togglePlayPause(song: SongEntity? = null) {
        AudioPlayerManager.togglePlayPause(getApplication(), song)
    }

    fun seekTo(progress: Float) {
        val targetMs = (progress * durationMs.value).toInt()
        AudioPlayerManager.seekTo(targetMs)
    }

    fun seekRelative(deltaMs: Int) {
        AudioPlayerManager.seekRelative(deltaMs)
    }

    fun toggleFavorite(song: SongEntity) {
        viewModelScope.launch {
            songRepo.toggleFavorite(song.id, song.isFavorite)
        }
    }

    fun renameSong(songId: String, newTitle: String) {
        viewModelScope.launch {
            songRepo.renameSong(songId, newTitle)
            showToast("Song renamed")
        }
    }

    fun deleteSong(songId: String) {
        viewModelScope.launch {
            songRepo.deleteSong(songId)
            showToast("Song deleted from library")
        }
    }

    fun reportSong(song: SongEntity, reason: String, description: String) {
        viewModelScope.launch {
            val user = currentUser.value
            songRepo.reportSong(
                reporterId = user?.userId ?: "guest",
                reporterName = user?.fullName ?: "Anonymous User",
                songId = song.id,
                songTitle = song.title,
                reason = reason,
                description = description
            )
            showToast("Report submitted for review")
        }
    }

    fun generateLyricsWithAI() {
        viewModelScope.launch {
            _isAiWritingLyrics.value = true
            val effectiveGenre = if (genreState.value == "Custom") customGenreState.value.ifBlank { "Afrobeats" } else genreState.value
            val lyrics = GeminiLyricsService.generateLyrics(
                prompt = promptState.value,
                genre = effectiveGenre,
                mood = moodState.value,
                language = languageState.value,
                vocalStyle = vocalStyleState.value,
                structure = structureState.value
            )
            lyricsTextState.value = lyrics
            _isAiWritingLyrics.value = false
            showToast("Lyrics generated successfully!")
        }
    }

    fun modifyLyrics(action: String) {
        viewModelScope.launch {
            _isAiWritingLyrics.value = true
            val effectiveGenre = if (genreState.value == "Custom") customGenreState.value.ifBlank { "Afrobeats" } else genreState.value
            val modified = GeminiLyricsService.modifyLyrics(
                currentLyrics = lyricsTextState.value,
                action = action,
                genre = effectiveGenre,
                mood = moodState.value
            )
            lyricsTextState.value = modified
            _isAiWritingLyrics.value = false
            showToast("Lyrics updated ($action)")
        }
    }

    fun startMusicGeneration(onSuccess: (SongEntity) -> Unit) {
        val user = currentUser.value
        if (user == null) {
            showToast("Please log in to generate music")
            return
        }

        if (user.credits < 1) {
            showToast("You have 0 credits. Please upgrade your plan in Pricing.")
            return
        }

        if (promptState.value.isBlank() && lyricsTextState.value.isBlank() && !isSimpleMode.value) {
            showToast("Please enter a description or prompt for your song")
            return
        }

        if (!isMusicProviderConfigured && !isDevSandboxMode.value) {
            showToast("Music generation is currently being configured.")
            return
        }

        _isGenerating.value = true
        _generationProgress.value = 10
        _generationStage.value = "Preparing your song..."

        activeGenerationJob = viewModelScope.launch {
            val effectiveGenre = if (genreState.value == "Custom") customGenreState.value.ifBlank { "Afrobeats" } else genreState.value
            val result = songRepo.startSongGeneration(
                context = getApplication(),
                userId = user.userId,
                prompt = promptState.value,
                genre = effectiveGenre,
                mood = moodState.value,
                language = languageState.value,
                vocalStyle = vocalStyleState.value,
                lyricsOption = lyricsOptionState.value,
                lyrics = lyricsTextState.value,
                isInstrumental = (lyricsOptionState.value == "Instrumental"),
                structure = structureState.value,
                tempo = tempoState.value,
                energy = energyState.value,
                onProgressUpdate = { progress, stage ->
                    _generationProgress.value = progress
                    _generationStage.value = stage
                }
            )

            result.onSuccess { newSong ->
                _isGenerating.value = false
                _lastGeneratedSong.value = newSong
                showToast("Song generated and added to your library!")
                onSuccess(newSong)
            }.onFailure { err ->
                _isGenerating.value = false
                showToast("Generation failed: ${err.message}")
            }
        }
    }

    fun cancelGeneration() {
        activeGenerationJob?.cancel()
        _isGenerating.value = false
        showToast("Generation cancelled")
    }

    fun subscribeToPlan(plan: PlanEntity, provider: String, onComplete: () -> Unit) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = planRepo.processSubscriptionPayment(user.userId, plan, provider)
            result.onSuccess {
                showToast("Subscribed to ${plan.name}! ${plan.generationCredits} credits activated.")
                onComplete()
            }.onFailure {
                showToast("Payment error: ${it.message}")
            }
        }
    }

    fun updateProfile(fullName: String, username: String, bio: String, isPublic: Boolean, allowPublicSongs: Boolean) {
        viewModelScope.launch {
            authRepo.updateProfile(fullName, username, bio, isPublic, allowPublicSongs)
            showToast("Profile saved successfully")
        }
    }

    fun deleteAccount(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepo.deleteAccount()
            showToast("Account deleted")
            onComplete()
        }
    }

    fun saveAdminPlan(plan: PlanEntity) {
        viewModelScope.launch {
            planRepo.savePlan(plan)
            showToast("Plan updated")
        }
    }

    fun updateAdminUserRole(user: UserEntity, newRole: String) {
        viewModelScope.launch {
            adminRepo.updateUserRoleOrStatus(user.userId, newRole)
            showToast("User role updated to $newRole")
        }
    }

    fun deleteSongByAdmin(songId: String) {
        viewModelScope.launch {
            adminRepo.deleteSongByAdmin(songId)
            showToast("Song deleted by admin")
        }
    }

    fun resolveReport(reportId: String, status: String) {
        viewModelScope.launch {
            adminRepo.resolveReport(reportId, status)
            showToast("Report marked as $status")
        }
    }

    fun populateSamplePrompt(genre: String, mood: String, prompt: String) {
        genreState.value = genre
        moodState.value = mood
        promptState.value = prompt
    }
}
