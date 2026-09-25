package com.example.audio

import android.content.Context
import android.media.MediaMetadataRetriever
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeBackend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Gemini acts as the director: it recommends DSP settings from safe, non-audio
 * metadata while AudioProcessingEngine performs the actual signal processing locally.
 */
data class VoiceAiRecommendation(
    val summary: String,
    val pitchShiftSemitones: Int,
    val bassGain: Float,
    val midGain: Float,
    val trebleGain: Float,
    val compressionRatio: Float,
    val reverbAmount: Float,
    val delayAmount: Float,
    val noiseReduction: Boolean,
    val volumeNormalization: Boolean
)

object GeminiVoiceDirector {
    suspend fun analyzeVoice(context: Context, audioPath: String): VoiceAiRecommendation = withContext(Dispatchers.IO) {
        val metadata = MediaMetadataRetriever()
        try {
            metadata.setDataSource(audioPath)
            val durationMs = metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val sampleRate = metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)?.toIntOrNull() ?: 44100
            val channels = metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CHANNEL_COUNT)?.toIntOrNull() ?: 1
            val format = audioPath.substringAfterLast('.', "unknown").lowercase()

            val model = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel("gemini-3.8-flash")
            val prompt = """
You are the AI audio director for DIV SONG AI. Recommend conservative vocal DSP settings.
Do NOT claim to have heard the recording. Use only this metadata:
format=$format, duration_ms=$durationMs, sample_rate=$sampleRate, channels=$channels.
The user wants a REVERSE VOICE effect. Reversal itself is performed locally by the app.
Return exactly these lines and nothing else:
SUMMARY=<short recommendation>
PITCH=<integer -4..4>
BASS=<decimal 0.7..1.3>
MID=<decimal 0.8..1.5>
TREBLE=<decimal 0.7..1.4>
COMP=<decimal 1.5..5.0>
REVERB=<decimal 0.0..0.45>
DELAY=<decimal 0.0..0.25>
NR=<true|false>
NORM=<true|false>
""".trimIndent()
            val response = model.generateContent(prompt).text.orEmpty()
            parse(response)
        } finally {
            metadata.release()
        }
    }

    private fun parse(text: String): VoiceAiRecommendation {
        fun value(key: String): String? = text.lineSequence().firstOrNull { it.startsWith("$key=") }?.substringAfter('=')?.trim()
        fun floatValue(key: String, fallback: Float, min: Float, max: Float): Float = value(key)?.toFloatOrNull()?.coerceIn(min, max) ?: fallback
        fun intValue(key: String, fallback: Int, min: Int, max: Int): Int = value(key)?.toIntOrNull()?.coerceIn(min, max) ?: fallback
        fun boolValue(key: String, fallback: Boolean): Boolean = value(key)?.toBooleanStrictOrNull() ?: fallback

        return VoiceAiRecommendation(
            summary = value("SUMMARY")?.take(140) ?: "Balanced reverse-vocal settings ready.",
            pitchShiftSemitones = intValue("PITCH", 0, -4, 4),
            bassGain = floatValue("BASS", 1.0f, 0.7f, 1.3f),
            midGain = floatValue("MID", 1.15f, 0.8f, 1.5f),
            trebleGain = floatValue("TREBLE", 1.05f, 0.7f, 1.4f),
            compressionRatio = floatValue("COMP", 3.0f, 1.5f, 5.0f),
            reverbAmount = floatValue("REVERB", 0.18f, 0.0f, 0.45f),
            delayAmount = floatValue("DELAY", 0.08f, 0.0f, 0.25f),
            noiseReduction = boolValue("NR", true),
            volumeNormalization = boolValue("NORM", true)
        )
    }
}
