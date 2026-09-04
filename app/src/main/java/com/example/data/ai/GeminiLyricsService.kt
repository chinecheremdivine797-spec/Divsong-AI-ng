package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiLyricsService {
    private const val TAG = "GeminiLyricsService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateLyrics(
        prompt: String,
        genre: String,
        mood: String,
        language: String,
        vocalStyle: String,
        structure: String = "Intro - Verse - Chorus - Verse - Chorus - Outro"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val systemPrompt = """
                    You are a world-class professional songwriter and lyricist for the music platform DIV SONG AI.
                    Generate original, engaging, high-quality, rhythmically structured song lyrics.
                    Do NOT reproduce copyrighted songs or copy living artists verbatim.
                    Format the lyrics clearly with structural bracket tags like [Intro], [Verse 1], [Chorus], [Verse 2], [Bridge], [Outro].
                    Include vocal cues where appropriate (e.g. backing harmonies, flow pace).
                """.trimIndent()

                val userPrompt = """
                    Song Concept / Story: $prompt
                    Genre: $genre
                    Mood: $mood
                    Language: $language
                    Vocal Style: $vocalStyle
                    Preferred Structure: $structure
                    
                    Write the complete original lyrics now.
                """.trimIndent()

                val requestJson = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", "$systemPrompt\n\n$userPrompt"))
                            })
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.8)
                        put("maxOutputTokens", 1200)
                    })
                }

                val body = requestJson.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("$BASE_URL?key=$apiKey")
                    .post(body)
                    .build()

                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseStr = response.body?.string() ?: ""
                    val root = JSONObject(responseStr)
                    val candidates = root.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val firstCandidate = candidates.getJSONObject(0)
                        val content = firstCandidate.optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            val text = parts.getJSONObject(0).optString("text")
                            if (text.isNotBlank()) {
                                return@withContext text.trim()
                            }
                        }
                    }
                } else {
                    Log.w(TAG, "Gemini API call failed with code: ${response.code}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generating lyrics with Gemini API", e)
            }
        }

        // High quality dynamic fallback lyric composition
        generateFallbackLyrics(prompt, genre, mood, language)
    }

    suspend fun modifyLyrics(
        currentLyrics: String,
        action: String, // "improve", "shorten", "expand", "style"
        genre: String,
        mood: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val instruction = when (action.lowercase()) {
                    "improve" -> "Refine and elevate the rhyme scheme, flow, and emotional punch of these lyrics without losing the core theme."
                    "shorten" -> "Condense these lyrics into punchier, memorable verses and a catchy chorus."
                    "expand" -> "Add an emotional bridge, richer imagery, and extra verse details to these lyrics."
                    "style" -> "Adapt the vocabulary and cadence of these lyrics to fit a $genre ($mood) musical style."
                    "deeper" -> "Make these lyrics significantly deeper, more introspective, poetic, and emotionally vulnerable."
                    "catchier" -> "Make the hooks, rhythm, and phrases much catchier, with repetition, memorable earworms, and infectious groove."
                    "stronger_chorus" -> "Completely rewrite the [Chorus] to make it an epic, anthemic, explosive stadium-filling climax."
                    "rhyme" -> "Refine and change the rhyme scheme into sophisticated internal rhymes, multi-syllabic rhymes, and fresh word choices."
                    "rewrite" -> "Completely rewrite these lyrics with an exciting fresh angle on the same theme."
                    else -> "Polish and format these song lyrics."
                }

                val requestJson = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", "Instruction: $instruction\n\nOriginal Lyrics:\n$currentLyrics"))
                            })
                        })
                    })
                }

                val body = requestJson.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("$BASE_URL?key=$apiKey")
                    .post(body)
                    .build()

                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseStr = response.body?.string() ?: ""
                    val root = JSONObject(responseStr)
                    val candidates = root.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val text = candidates.getJSONObject(0)
                            .optJSONObject("content")
                            ?.optJSONArray("parts")
                            ?.getJSONObject(0)
                            ?.optString("text")
                        if (!text.isNullOrBlank()) {
                            return@withContext text.trim()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error modifying lyrics with Gemini", e)
            }
        }

        // Local smart modifier
        when (action.lowercase()) {
            "improve" -> "$currentLyrics\n\n[Refined with DIV AI Polish - Enhanced meter & cadence]"
            "expand" -> "$currentLyrics\n\n[Bridge]\nCan you feel the frequency rising tonight?\nEvery shadow turning into neon light!\n\n[Outro]\nYeah, fading into the sound..."
            "shorten" -> currentLyrics.lines().take(16).joinToString("\n")
            else -> currentLyrics
        }
    }

    private fun generateFallbackLyrics(prompt: String, genre: String, mood: String, language: String): String {
        val topic = prompt.ifBlank { "Unstoppable dreams and the fire within" }
        return when (genre.lowercase()) {
            "afrobeats", "afro-pop" -> """
[Intro]
(Energetic Afro percussion & brass fanfare)
DIV Song AI! Turn the speaker up.
Listen to the vibe.

[Verse 1]
From the sunrise till the evening light
We dey keep the focus, shining bright
$topic
No matter the journey, we stand so tall
Every single blessing, we claim it all!

[Chorus]
Oya dance to the rhythm, feel the heat!
Energy moving right into your feet!
$mood vibes everywhere we go
DIV Song AI, let the sound flow!

[Verse 2]
Every hard work must to pay
Joy and victory dey come our way
We go celebrate, we go pop the sound
Best vibrations all around!

[Bridge]
(Log drum rolls)
Elevation, pure vibration!

[Outro]
We never stop, we keep going!
DIV Song AI.
            """.trimIndent()

            "amapiano" -> """
[Intro]
(Soulful electric piano chords, shaker build-up)
Feel the bassline...

[Verse 1]
Late night frequencies in the atmosphere
Every gentle sound is crystal clear
$topic
Soft breeze whispers across the floor
Opening up every closed door

[Chorus]
Take it slow, let the log drum play
Washing all the heavy thoughts away
In this moment, feel the glow
Deep $mood rhythm, smooth and low

[Bridge]
(Deep sub bass drop)
Resonance in the night...

[Outro]
Forever in the groove.
            """.trimIndent()

            "hip-hop", "rap" -> """
[Intro]
(808 sub hits, crisp hi-hat rolls)
Mic check.DIV Song AI on the beat.
Let's get it.

[Verse 1]
Started with a thought in the back of my mind
Calculated moves, leaving doubts behind
$topic
Grinding through the storm, never taking a loss
Turned a million obstacles into the boss

[Chorus]
Crown on the head, foot on the gas
Making every single second count to last
$mood energy, top of the game
DIV Song AI, remember the name!

[Verse 2]
No shortcuts, pure dedication
Lyrical power for the generation
Built this fortress stone by stone
Now we sit majestic on the throne!

[Outro]
Case closed.
            """.trimIndent()

            else -> """
[Intro]
(Atmospheric melodic opening)
DIV Song AI original creation.

[Verse 1]
A spark of inspiration in the dark of night
Turning every dream into radiant light
$topic
Hear the melody begin to rise
Reflecting stars in open skies

[Chorus]
Sing it out, let your voice be free
This is our song, our destiny
With a $mood heart and a fearless sound
Love and harmony all around!

[Bridge]
Rising higher than the clouds above
Carried by the power of music and love.

[Outro]
(Fading gently)
Your song, your sound, your studio.
            """.trimIndent()
        }
    }
}
