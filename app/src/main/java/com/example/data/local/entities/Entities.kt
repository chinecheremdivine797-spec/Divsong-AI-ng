package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class UserEntity(
    @PrimaryKey val userId: String,
    val fullName: String,
    val username: String,
    val email: String,
    val avatarUrl: String = "",
    val bio: String = "",
    val role: String = "user", // "user" or "admin"
    val credits: Int = 10,
    val planId: String = "free",
    val isEmailVerified: Boolean = false,
    val isPublicProfile: Boolean = true,
    val allowPublicSongs: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val creatorName: String,
    val title: String,
    val description: String,
    val genre: String,
    val mood: String,
    val language: String,
    val lyrics: String,
    val instrumental: Boolean = false,
    val audioUrl: String = "",
    val coverImageUrl: String = "",
    val durationSeconds: Int = 180,
    val visibility: String = "public", // "public" or "private"
    val status: String = "ready",      // "ready", "generating", "failed"
    val provider: String = "DIV AI Audio Studio",
    val vocalStyle: String = "Male vocal",
    val tempo: String = "Medium (110 BPM)",
    val energy: String = "High",
    val structure: String = "Intro - Verse - Chorus - Verse - Chorus - Outro",
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "generation_jobs")
data class GenerationJobEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val songId: String,
    val prompt: String,
    val genre: String,
    val mood: String,
    val language: String,
    val vocalStyle: String,
    val lyricsOption: String, // "AI Generate", "Custom", "Instrumental"
    val lyrics: String,
    val tempo: String = "Medium (118 BPM)",
    val energy: String = "High",
    val structure: String = "Intro - Verse - Chorus - Verse - Chorus - Outro",
    val provider: String = "DIV Sound Engine",
    val providerJobId: String = "",
    val status: String = "processing", // "queued", "processing", "completed", "failed", "cancelled"
    val progressPercent: Int = 0,
    val currentStage: String = "Initializing...",
    val errorMessage: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@Entity(tableName = "plans")
data class PlanEntity(
    @PrimaryKey val planId: String, // "free", "creator", "pro"
    val name: String,
    val description: String,
    val priceMonthlyUsd: Double,
    val currency: String = "USD",
    val generationCredits: Int,
    val featuresJson: String,
    val active: Boolean = true
)

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val planId: String,
    val planName: String = "",
    val status: String = "active", // "active", "cancelled", "expired"
    val startsAt: Long = System.currentTimeMillis(),
    val endsAt: Long = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
    val provider: String = "Paystack",
    val providerSubscriptionId: String = ""
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val provider: String, // "Paystack", "Flutterwave", "Stripe"
    val reference: String,
    val amount: Double,
    val currency: String,
    val status: String,   // "successful", "pending", "failed"
    val planName: String,
    val creditsAdded: Int,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val id: String,
    val reporterId: String,
    val reporterName: String,
    val songId: String,
    val songTitle: String,
    val reason: String,
    val description: String,
    val status: String = "pending", // "pending", "resolved", "dismissed"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val coverImageUrl: String = "",
    val genre: String = "Afrobeats",
    val mood: String = "Inspiring",
    val lyrics: String = "",
    val vocalTrackUrl: String = "",
    val instrumentalTrackUrl: String = "",
    val masterAudioUrl: String = "",
    val videoUrl: String = "",
    val tracksJson: String = "",       // serialized list of audio stems/tracks
    val videoClipsJson: String = "",   // serialized list of video clips & transitions
    val subtitlesJson: String = "",    // timed lyrics/subtitles
    val effectsJson: String = "",      // audio & video effects configuration
    val status: String = "draft",      // "draft", "ready", "rendering", "exported"
    val durationSeconds: Int = 180,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
