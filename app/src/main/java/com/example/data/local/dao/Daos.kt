package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.AppSettingEntity
import com.example.data.local.entities.GenerationJobEntity
import com.example.data.local.entities.PlanEntity
import com.example.data.local.entities.ReportEntity
import com.example.data.local.entities.SongEntity
import com.example.data.local.entities.SubscriptionEntity
import com.example.data.local.entities.TransactionEntity
import com.example.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM profiles WHERE userId = :userId LIMIT 1")
    fun getUserById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM profiles WHERE userId = :userId LIMIT 1")
    suspend fun getUserByIdSync(userId: String): UserEntity?

    @Query("SELECT * FROM profiles WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM profiles ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(user: UserEntity)

    @Query("UPDATE profiles SET credits = credits + :amount WHERE userId = :userId")
    suspend fun addCredits(userId: String, amount: Int)

    @Query("UPDATE profiles SET credits = credits - :amount WHERE userId = :userId AND credits >= :amount")
    suspend fun deductCredits(userId: String, amount: Int): Int

    @Query("DELETE FROM profiles WHERE userId = :userId")
    suspend fun deleteUser(userId: String)
}

@Dao
interface SongDao {
    @Query("SELECT * FROM songs WHERE userId = :userId ORDER BY createdAt DESC")
    fun getSongsByUserId(userId: String): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE visibility = 'public' ORDER BY createdAt DESC")
    fun getPublicSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE id = :songId LIMIT 1")
    fun getSongById(songId: String): Flow<SongEntity?>

    @Query("SELECT * FROM songs WHERE id = :songId LIMIT 1")
    suspend fun getSongByIdSync(songId: String): SongEntity?

    @Query("SELECT * FROM songs ORDER BY createdAt DESC")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(song: SongEntity)

    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE id = :songId")
    suspend fun toggleFavorite(songId: String, isFavorite: Boolean)

    @Query("UPDATE songs SET title = :newTitle WHERE id = :songId")
    suspend fun renameSong(songId: String, newTitle: String)

    @Query("UPDATE songs SET visibility = :visibility WHERE id = :songId")
    suspend fun updateVisibility(songId: String, visibility: String)

    @Query("UPDATE songs SET playCount = playCount + 1 WHERE id = :songId")
    suspend fun incrementPlayCount(songId: String)

    @Query("DELETE FROM songs WHERE id = :songId")
    suspend fun deleteSong(songId: String)

    @Query("DELETE FROM songs WHERE userId = :userId")
    suspend fun deleteSongsByUserId(userId: String)
}

@Dao
interface GenerationJobDao {
    @Query("SELECT * FROM generation_jobs WHERE userId = :userId ORDER BY createdAt DESC")
    fun getJobsByUser(userId: String): Flow<List<GenerationJobEntity>>

    @Query("SELECT * FROM generation_jobs WHERE id = :jobId LIMIT 1")
    fun getJobById(jobId: String): Flow<GenerationJobEntity?>

    @Query("SELECT * FROM generation_jobs ORDER BY createdAt DESC")
    fun getAllJobs(): Flow<List<GenerationJobEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(job: GenerationJobEntity)

    @Query("UPDATE generation_jobs SET status = :status, progressPercent = :progress, currentStage = :stage WHERE id = :jobId")
    suspend fun updateProgress(jobId: String, status: String, progress: Int, stage: String)

    @Query("UPDATE generation_jobs SET status = 'failed', errorMessage = :error WHERE id = :jobId")
    suspend fun failJob(jobId: String, error: String)

    @Query("UPDATE generation_jobs SET status = 'completed', progressPercent = 100, completedAt = :completedAt WHERE id = :jobId")
    suspend fun completeJob(jobId: String, completedAt: Long)
}

@Dao
interface PlanDao {
    @Query("SELECT * FROM plans WHERE active = 1")
    fun getActivePlans(): Flow<List<PlanEntity>>

    @Query("SELECT * FROM plans")
    fun getAllPlans(): Flow<List<PlanEntity>>

    @Query("SELECT * FROM plans WHERE planId = :planId LIMIT 1")
    suspend fun getPlanById(planId: String): PlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(plan: PlanEntity)
}

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions WHERE userId = :userId LIMIT 1")
    fun getSubscriptionByUser(userId: String): Flow<SubscriptionEntity?>

    @Query("SELECT * FROM subscriptions ORDER BY startsAt DESC")
    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(sub: SubscriptionEntity)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY createdAt DESC")
    fun getTransactionsByUser(userId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)
}

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    fun getAllReports(): Flow<List<ReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(report: ReportEntity)

    @Query("UPDATE reports SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)
}

@Dao
interface AppSettingDao {
    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setValue(setting: AppSettingEntity)
}
