package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AppSettingDao
import com.example.data.local.dao.GenerationJobDao
import com.example.data.local.dao.PlanDao
import com.example.data.local.dao.ReportDao
import com.example.data.local.dao.SongDao
import com.example.data.local.dao.SubscriptionDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entities.AppSettingEntity
import com.example.data.local.entities.GenerationJobEntity
import com.example.data.local.entities.PlanEntity
import com.example.data.local.entities.ReportEntity
import com.example.data.local.entities.SongEntity
import com.example.data.local.entities.SubscriptionEntity
import com.example.data.local.entities.TransactionEntity
import com.example.data.local.entities.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        SongEntity::class,
        GenerationJobEntity::class,
        PlanEntity::class,
        SubscriptionEntity::class,
        TransactionEntity::class,
        ReportEntity::class,
        AppSettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun songDao(): SongDao
    abstract fun generationJobDao(): GenerationJobDao
    abstract fun planDao(): PlanDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun transactionDao(): TransactionDao
    abstract fun reportDao(): ReportDao
    abstract fun appSettingDao(): AppSettingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "div_song_ai_database"
                )
                    .addCallback(DatabaseCallback(context))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val database = getDatabase(context)
                    seedInitialData(database)
                }
            }
        }

        suspend fun seedInitialData(database: AppDatabase) {
            // Seed Default Plans
            val plans = listOf(
                PlanEntity(
                    planId = "free",
                    name = "Free Tier",
                    description = "Perfect for testing the AI music generator and creating initial songs",
                    priceMonthlyUsd = 0.0,
                    currency = "USD",
                    generationCredits = 10,
                    featuresJson = "[\"10 AI Song Credits / mo\",\"Standard Generation Queue\",\"AI Lyrics Generator\",\"Full Studio Editor\",\"Audio Waveform Player\",\"Mobile & Web Access\"]",
                    active = true
                ),
                PlanEntity(
                    planId = "creator",
                    name = "Creator Studio",
                    description = "For dedicated creators, artists, and music producers seeking high fidelity",
                    priceMonthlyUsd = 19.0,
                    currency = "USD",
                    generationCredits = 100,
                    featuresJson = "[\"100 AI Song Credits / mo\",\"Priority Generation Speed\",\"Advanced Vocal Control\",\"High-Res Audio Download\",\"Custom Structure Builder\",\"Full Commercial Rights\"]",
                    active = true
                ),
                PlanEntity(
                    planId = "pro",
                    name = "Pro Studio Ultra",
                    description = "Maximum power with unlimited song iterations and priority server queues",
                    priceMonthlyUsd = 49.0,
                    currency = "USD",
                    generationCredits = 300,
                    featuresJson = "[\"300 AI Song Credits / mo\",\"Fastest Processing Engine\",\"Stem Export / Multi-track\",\"Instrumental Separation\",\"API Access Integration\",\"Dedicated 24/7 Support\"]",
                    active = true
                )
            )
            plans.forEach { database.planDao().insertOrUpdate(it) }

            // Seed Admin User (matching divstudio03@gmail.com from session metadata)
            val adminUser = UserEntity(
                userId = "usr_admin_001",
                fullName = "DIV Studio Admin",
                username = "divstudio",
                email = "divstudio03@gmail.com",
                avatarUrl = "",
                bio = "Founder & Sound Architect at DIV SONG AI. Crafting the next generation of intelligent music.",
                role = "admin",
                credits = 250,
                planId = "pro",
                createdAt = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            )
            database.userDao().insertOrUpdate(adminUser)

            // Seed Initial Public Explore Songs
            val seedSongs = listOf(
                SongEntity(
                    id = "song_001",
                    userId = "usr_admin_001",
                    creatorName = "DIV Studio",
                    title = "Lagos Sunrise Pulse",
                    description = "An uplifting energetic Afrobeats anthem about pursuing big city dreams in Nigeria with brass horns and heavy percussion.",
                    genre = "Afrobeats",
                    mood = "Energetic",
                    language = "Nigerian Pidgin",
                    lyrics = """[Intro]
(Energetic log drum kick)
Yeah, straight from the mainland to the island!
DIV Song AI, let the rhythm talk.

[Verse 1]
Early morning sun dey shine for Eko bridge
Every hustle get a story, every step na bridge
From Oshodi to the world, we dey elevate
No matter the pressure, we go celebrate!

[Chorus]
Oh we dey rise, we no go stop!
From the bottom straight to the very top!
E go make sense, the vibe is strong
DIV Song AI, na our song!

[Bridge]
Feel the groove, make your body shake
Every good dream wey we go make!

[Outro]
Lagos pulse, never fade away!""",
                    instrumental = false,
                    durationSeconds = 195,
                    visibility = "public",
                    status = "ready",
                    provider = "DIV AI Audio Studio",
                    vocalStyle = "Male vocal",
                    tempo = "Fast (124 BPM)",
                    energy = "High",
                    isFavorite = true,
                    playCount = 1420
                ),
                SongEntity(
                    id = "song_002",
                    userId = "usr_admin_001",
                    creatorName = "Aura Vibes",
                    title = "Midnight Horizon",
                    description = "A deep Amapiano track with soulful Rhodes piano chords, rolling shaker grooves, and heavy deep basslines.",
                    genre = "Amapiano",
                    mood = "Calm",
                    language = "English",
                    lyrics = """[Intro]
(Soulful electric piano chords, subtle shaker)

[Verse 1]
Walking under velvet skies
City lights reflect inside your eyes
Feel the vibration underneath our feet
Synchronized into the rhythm beat

[Chorus]
Midnight horizon, call my name
Music that washes away the pain
Soft bass rolling through the night
Everything is gonna be alright

[Outro]
Just let it breathe...""",
                    instrumental = false,
                    durationSeconds = 210,
                    visibility = "public",
                    status = "ready",
                    provider = "DIV AI Audio Studio",
                    vocalStyle = "Female vocal",
                    tempo = "Medium (113 BPM)",
                    energy = "Calm",
                    isFavorite = true,
                    playCount = 980
                ),
                SongEntity(
                    id = "song_003",
                    userId = "usr_admin_001",
                    creatorName = "K-Prime",
                    title = "Relentless Ambition",
                    description = "A hard-hitting hip-hop track with booming 808s, sharp trap hi-hats, and motivational lyrical cadence.",
                    genre = "Hip-hop",
                    mood = "Inspirational",
                    language = "English",
                    lyrics = """[Intro]
Count it up, level up.
We built this from the blueprint.

[Verse 1]
Late nights in the lab, clock ticking on the wall
They doubted the vision, but I stood up tall
Every setback was a setup for the major climb
Now we taking over territory in our prime

[Chorus]
Relentless, fearless, watch us go
Spark in the dark, let the fire glow
No looking back, got the crown in sight
Turning all our shadows into light!

[Outro]
Unstoppable.""",
                    instrumental = false,
                    durationSeconds = 175,
                    visibility = "public",
                    status = "ready",
                    provider = "DIV AI Audio Studio",
                    vocalStyle = "Male vocal",
                    tempo = "Fast (140 BPM)",
                    energy = "Epic",
                    isFavorite = false,
                    playCount = 820
                ),
                SongEntity(
                    id = "song_004",
                    userId = "usr_admin_001",
                    creatorName = "Grace Harmony",
                    title = "Higher Light",
                    description = "An uplifting spiritual Gospel melody with rich choir vocal harmonies and warm Hammond organ chords.",
                    genre = "Gospel",
                    mood = "Hopeful",
                    language = "English",
                    lyrics = """[Intro]
(Warm Hammond organ and acoustic piano)

[Verse 1]
Through the stormy wind and pouring rain
There is a promise that will heal the pain
Lift up your eyes to the mountain high
A brand new dawn is breaking in the sky

[Chorus]
Higher light, shining bright
Guiding our footsteps through the night
Peace like a river, joy so deep
Promises that heaven will keep

[Outro]
Hallelujah, we give praise.""",
                    instrumental = false,
                    durationSeconds = 230,
                    visibility = "public",
                    status = "ready",
                    provider = "DIV AI Audio Studio",
                    vocalStyle = "Group vocal",
                    tempo = "Slow (78 BPM)",
                    energy = "Inspirational",
                    isFavorite = true,
                    playCount = 1150
                )
            )

            seedSongs.forEach { database.songDao().insertOrUpdate(it) }

            // Seed Initial Transaction
            database.transactionDao().insert(
                TransactionEntity(
                    id = "tx_001",
                    userId = "usr_admin_001",
                    provider = "Paystack",
                    reference = "DIV-TX-9281749",
                    amount = 49.0,
                    currency = "USD",
                    status = "successful",
                    planName = "Pro Studio Ultra",
                    creditsAdded = 300,
                    createdAt = System.currentTimeMillis() - 5000000
                )
            )
        }
    }
}
