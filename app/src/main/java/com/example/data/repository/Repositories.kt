package com.example.data.repository

import android.content.Context
import com.example.data.ai.MusicProviderManager
import com.example.data.auth.FirebaseAuthService
import com.example.data.local.AppDatabase
import com.example.data.local.entities.GenerationJobEntity
import com.example.data.local.entities.PlanEntity
import com.example.data.local.entities.ReportEntity
import com.example.data.local.entities.SongEntity
import com.example.data.local.entities.SubscriptionEntity
import com.example.data.local.entities.TransactionEntity
import com.example.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class AuthRepository(private val database: AppDatabase) {
    private val userDao = database.userDao()

    private val _currentUserId = MutableStateFlow("usr_admin_001") // Default logged in as admin user
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    fun getCurrentUser(): Flow<UserEntity?> {
        return userDao.getUserById(_currentUserId.value)
    }

    suspend fun login(email: String, password: String): Result<UserEntity> {
        val cleanEmail = email.trim().lowercase()

        // 1. Try Firebase Authentication if initialized
        if (FirebaseAuthService.isFirebaseInitialized) {
            val fbResult = FirebaseAuthService.login(cleanEmail, password)
            if (fbResult.isSuccess) {
                val fbUser = fbResult.getOrThrow()
                var localUser = userDao.getUserByIdSync(fbUser.uid)
                if (localUser == null) {
                    localUser = UserEntity(
                        userId = fbUser.uid,
                        fullName = fbUser.displayName ?: cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                        username = cleanEmail.substringBefore("@").lowercase(),
                        email = cleanEmail,
                        role = if (cleanEmail == "divstudio03@gmail.com" || cleanEmail.contains("admin")) "admin" else "user",
                        credits = 15,
                        planId = "free",
                        isEmailVerified = fbUser.isEmailVerified
                    )
                    userDao.insertOrUpdate(localUser)
                } else {
                    userDao.insertOrUpdate(localUser.copy(isEmailVerified = fbUser.isEmailVerified))
                }
                _currentUserId.value = fbUser.uid
                return Result.success(localUser)
            } else {
                return Result.failure(fbResult.exceptionOrNull() ?: Exception("Firebase login failed."))
            }
        }

        // 2. Local Database Fallback (when Firebase google-services.json is pending)
        val user = userDao.getUserByEmail(cleanEmail)
        return if (user != null) {
            _currentUserId.value = user.userId
            Result.success(user)
        } else {
            val newUser = UserEntity(
                userId = "usr_" + UUID.randomUUID().toString().take(8),
                fullName = cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                username = cleanEmail.substringBefore("@").lowercase(),
                email = cleanEmail,
                role = if (cleanEmail.contains("admin") || cleanEmail == "divstudio03@gmail.com") "admin" else "user",
                credits = 15,
                planId = "free",
                isEmailVerified = true
            )
            userDao.insertOrUpdate(newUser)
            _currentUserId.value = newUser.userId
            Result.success(newUser)
        }
    }

    suspend fun signUp(fullName: String, username: String, email: String, password: String): Result<UserEntity> {
        val cleanEmail = email.trim().lowercase()
        val cleanUsername = username.trim().lowercase()
        val cleanName = fullName.trim()

        // 1. Try Firebase Authentication if initialized
        if (FirebaseAuthService.isFirebaseInitialized) {
            val fbResult = FirebaseAuthService.register(cleanEmail, password, cleanName)
            if (fbResult.isSuccess) {
                val fbUser = fbResult.getOrThrow()
                val newUser = UserEntity(
                    userId = fbUser.uid,
                    fullName = cleanName,
                    username = cleanUsername,
                    email = cleanEmail,
                    role = if (cleanEmail == "divstudio03@gmail.com" || cleanEmail.contains("admin")) "admin" else "user",
                    credits = 15,
                    planId = "free",
                    isEmailVerified = fbUser.isEmailVerified
                )
                userDao.insertOrUpdate(newUser)
                _currentUserId.value = fbUser.uid
                return Result.success(newUser)
            } else {
                return Result.failure(fbResult.exceptionOrNull() ?: Exception("Firebase sign-up failed."))
            }
        }

        // 2. Local Database Fallback
        val existing = userDao.getUserByEmail(cleanEmail)
        if (existing != null) {
            _currentUserId.value = existing.userId
            return Result.success(existing)
        }
        val newUser = UserEntity(
            userId = "usr_" + UUID.randomUUID().toString().take(8),
            fullName = cleanName,
            username = cleanUsername,
            email = cleanEmail,
            role = if (cleanEmail == "divstudio03@gmail.com") "admin" else "user",
            credits = 15,
            planId = "free",
            isEmailVerified = false
        )
        userDao.insertOrUpdate(newUser)
        _currentUserId.value = newUser.userId
        return Result.success(newUser)
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return if (FirebaseAuthService.isFirebaseInitialized) {
            FirebaseAuthService.sendPasswordReset(email)
        } else {
            // Local simulation confirmation
            Result.success(Unit)
        }
    }

    suspend fun resendVerificationEmail(): Result<Unit> {
        return if (FirebaseAuthService.isFirebaseInitialized) {
            FirebaseAuthService.resendVerificationEmail()
        } else {
            val user = userDao.getUserByIdSync(_currentUserId.value)
            if (user != null) {
                userDao.insertOrUpdate(user.copy(isEmailVerified = true))
            }
            Result.success(Unit)
        }
    }

    fun logout() {
        if (FirebaseAuthService.isFirebaseInitialized) {
            FirebaseAuthService.signOut()
        }
        _currentUserId.value = "usr_admin_001"
    }

    suspend fun updateProfile(fullName: String, username: String, bio: String, isPublic: Boolean, allowPublicSongs: Boolean) {
        val user = userDao.getUserByIdSync(_currentUserId.value) ?: return
        val updated = user.copy(
            fullName = fullName.trim(),
            username = username.trim().lowercase(),
            bio = bio.trim(),
            isPublicProfile = isPublic,
            allowPublicSongs = allowPublicSongs,
            updatedAt = System.currentTimeMillis()
        )
        userDao.insertOrUpdate(updated)
    }

    suspend fun deleteAccount() {
        val userId = _currentUserId.value
        userDao.deleteUser(userId)
        database.songDao().deleteSongsByUserId(userId)
        logout()
    }
}

class SongRepository(private val database: AppDatabase) {
    private val songDao = database.songDao()
    private val jobDao = database.generationJobDao()
    private val userDao = database.userDao()

    fun getUserSongs(userId: String): Flow<List<SongEntity>> = songDao.getSongsByUserId(userId)
    fun getPublicSongs(): Flow<List<SongEntity>> = songDao.getPublicSongs()
    fun getAllSongs(): Flow<List<SongEntity>> = songDao.getAllSongs()
    fun getSongById(songId: String): Flow<SongEntity?> = songDao.getSongById(songId)
    fun getUserJobs(userId: String): Flow<List<GenerationJobEntity>> = jobDao.getJobsByUser(userId)

    suspend fun toggleFavorite(songId: String, currentFavorite: Boolean) {
        songDao.toggleFavorite(songId, !currentFavorite)
    }

    suspend fun renameSong(songId: String, newTitle: String) {
        songDao.renameSong(songId, newTitle)
    }

    suspend fun updateVisibility(songId: String, visibility: String) {
        songDao.updateVisibility(songId, visibility)
    }

    suspend fun deleteSong(songId: String) {
        songDao.deleteSong(songId)
    }

    suspend fun reportSong(reporterId: String, reporterName: String, songId: String, songTitle: String, reason: String, description: String) {
        val report = ReportEntity(
            id = "rep_" + UUID.randomUUID().toString().take(8),
            reporterId = reporterId,
            reporterName = reporterName,
            songId = songId,
            songTitle = songTitle,
            reason = reason,
            description = description,
            status = "pending"
        )
        database.reportDao().insert(report)
    }

    suspend fun startSongGeneration(
        context: Context,
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
        energy: String,
        onProgressUpdate: suspend (progressPercent: Int, stage: String) -> Unit
    ): Result<SongEntity> {
        val provider = MusicProviderManager.activeProvider

        // Strict Check: Never pretend generation succeeded if no provider is configured!
        if (!provider.isConfigured) {
            return Result.failure(
                IllegalStateException("Music generation is currently being configured.")
            )
        }

        // 1. Check & deduct credits
        val user = userDao.getUserByIdSync(userId)
        if (user == null || user.credits < 1) {
            return Result.failure(Exception("Insufficient generation credits. Please upgrade your plan or top up credits."))
        }
        userDao.deductCredits(userId, 1)

        // 2. Create job record
        val job = provider.createGenerationJob(
            userId = userId,
            prompt = prompt,
            genre = genre,
            mood = mood,
            language = language,
            vocalStyle = vocalStyle,
            lyricsOption = lyricsOption,
            lyrics = lyrics,
            isInstrumental = isInstrumental,
            structure = structure,
            tempo = tempo,
            energy = energy
        )
        jobDao.insertOrUpdate(job)

        return try {
            val completedSong = provider.executeGenerationPipeline(
                context = context,
                job = job,
                onProgress = { progress, stage ->
                    jobDao.updateProgress(job.id, "processing", progress, stage)
                    onProgressUpdate(progress, stage)
                }
            )

            // Save completed song in database
            songDao.insertOrUpdate(completedSong)
            jobDao.completeJob(job.id, System.currentTimeMillis())

            Result.success(completedSong)
        } catch (e: Exception) {
            jobDao.failJob(job.id, e.message ?: "Generation error")
            // Refund credit on failure
            userDao.addCredits(userId, 1)
            Result.failure(e)
        }
    }
}

class PlanRepository(private val database: AppDatabase) {
    private val planDao = database.planDao()
    private val subDao = database.subscriptionDao()
    private val txDao = database.transactionDao()
    private val userDao = database.userDao()

    fun getActivePlans(): Flow<List<PlanEntity>> = planDao.getActivePlans()
    fun getAllPlans(): Flow<List<PlanEntity>> = planDao.getAllPlans()
    fun getUserTransactions(userId: String): Flow<List<TransactionEntity>> = txDao.getTransactionsByUser(userId)
    fun getAllTransactions(): Flow<List<TransactionEntity>> = txDao.getAllTransactions()

    suspend fun savePlan(plan: PlanEntity) {
        planDao.insertOrUpdate(plan)
    }

    suspend fun processSubscriptionPayment(
        userId: String,
        plan: PlanEntity,
        provider: String // "Paystack", "Flutterwave", "Stripe"
    ): Result<TransactionEntity> {
        val txId = "tx_" + UUID.randomUUID().toString().take(8)
        val reference = "DIV-" + provider.take(3).uppercase() + "-" + (1000000..9999999).random()

        val transaction = TransactionEntity(
            id = txId,
            userId = userId,
            provider = provider,
            reference = reference,
            amount = plan.priceMonthlyUsd,
            currency = plan.currency,
            status = "successful",
            planName = plan.name,
            creditsAdded = plan.generationCredits
        )
        txDao.insert(transaction)

        // Add credits & update user plan
        userDao.addCredits(userId, plan.generationCredits)
        val user = userDao.getUserByIdSync(userId)
        if (user != null) {
            userDao.insertOrUpdate(user.copy(planId = plan.planId))
        }

        // Add Subscription record
        val sub = SubscriptionEntity(
            id = "sub_" + UUID.randomUUID().toString().take(8),
            userId = userId,
            planId = plan.planId,
            planName = plan.name,
            provider = provider,
            status = "active",
            startsAt = System.currentTimeMillis(),
            endsAt = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
        )
        subDao.insertOrUpdate(sub)

        return Result.success(transaction)
    }
}

class AdminRepository(private val database: AppDatabase) {
    private val userDao = database.userDao()
    private val songDao = database.songDao()
    private val jobDao = database.generationJobDao()
    private val reportDao = database.reportDao()
    private val txDao = database.transactionDao()
    private val planDao = database.planDao()

    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()
    fun getAllSongs(): Flow<List<SongEntity>> = songDao.getAllSongs()
    fun getAllReports(): Flow<List<ReportEntity>> = reportDao.getAllReports()
    fun getAllJobs(): Flow<List<GenerationJobEntity>> = jobDao.getAllJobs()
    fun getAllTransactions(): Flow<List<TransactionEntity>> = txDao.getAllTransactions()

    suspend fun updateUserRoleOrStatus(userId: String, newRole: String) {
        val user = userDao.getUserByIdSync(userId) ?: return
        userDao.insertOrUpdate(user.copy(role = newRole))
    }

    suspend fun resolveReport(reportId: String, status: String) {
        reportDao.updateStatus(reportId, status)
    }

    suspend fun deleteSongByAdmin(songId: String) {
        songDao.deleteSong(songId)
    }
}
