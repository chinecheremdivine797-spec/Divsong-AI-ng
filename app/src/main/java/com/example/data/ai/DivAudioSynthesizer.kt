package com.example.data.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

object DivAudioSynthesizer {

    /**
     * Synthesizes an audio track based on genre, tempo, and mood.
     * Returns the absolute file path of the generated WAV file.
     */
    suspend fun synthesizeSongAudio(
        context: Context,
        songId: String,
        genre: String,
        mood: String,
        durationSeconds: Int = 30 // Generates a seamless 30-45s musical preview loop
    ): String = withContext(Dispatchers.IO) {
        val outputDir = File(context.cacheDir, "div_songs")
        if (!outputDir.exists()) outputDir.mkdirs()
        val outputFile = File(outputDir, "song_${songId}.wav")

        if (outputFile.exists() && outputFile.length() > 44) {
            return@withContext outputFile.absolutePath
        }

        val sampleRate = 44100
        val totalSamples = sampleRate * durationSeconds.coerceIn(15, 60)
        val channels = 2 // Stereo

        // Genre-based scale and BPM parameters
        val (bpm, scaleNotes, baseFreq) = when (genre.lowercase()) {
            "afrobeats", "afro-pop" -> Triple(120, listOf(0, 2, 4, 7, 9, 12), 220.0) // A major pentatonic
            "amapiano" -> Triple(113, listOf(0, 3, 5, 7, 10, 12), 174.61) // F minor pentatonic
            "hip-hop", "rap" -> Triple(140, listOf(0, 3, 5, 6, 7, 10, 12), 130.81) // C blues
            "r&b", "soul" -> Triple(95, listOf(0, 2, 4, 7, 9, 11, 12), 196.0) // G major 7th vibe
            "gospel" -> Triple(80, listOf(0, 2, 4, 5, 7, 9, 11, 12), 261.63) // C major gospel
            "edm" -> Triple(128, listOf(0, 3, 5, 7, 10, 12), 220.0) // A minor drive
            "rock", "alternative" -> Triple(135, listOf(0, 3, 5, 7, 10, 12), 164.81) // E minor
            "dancehall", "reggae" -> Triple(100, listOf(0, 2, 4, 5, 7, 9, 11, 12), 220.0)
            "classical", "acoustic" -> Triple(90, listOf(0, 2, 4, 7, 9, 12), 261.63)
            else -> Triple(115, listOf(0, 2, 4, 7, 9, 12), 220.0)
        }

        val samplesPerBeat = (sampleRate * 60) / bpm
        val samplesPerBar = samplesPerBeat * 4

        FileOutputStream(outputFile).use { fos ->
            // Write placeholder WAV header (44 bytes)
            writeWavHeaderPlaceholder(fos)

            val buffer = ByteBuffer.allocate(sampleRate * 4).order(ByteOrder.LITTLE_ENDIAN)
            var currentNoteIndex = 0

            for (sample in 0 until totalSamples) {
                val beatPosition = sample % samplesPerBeat
                val barPosition = sample % samplesPerBar
                val beatNumber = (sample / samplesPerBeat) % 4
                val time = sample.toDouble() / sampleRate

                // 1. Percussion / Beat Kick & Snare
                var drumSample = 0.0
                if (beatNumber == 0 || (genre.lowercase() == "amapiano" && (beatNumber == 0 || beatNumber == 2))) {
                    // Kick drum sine drop
                    val kickProgress = beatPosition.toDouble() / (sampleRate * 0.15)
                    if (kickProgress < 1.0) {
                        val kickFreq = 140.0 * (1.0 - kickProgress * 0.8) + 40.0
                        val kickEnvelope = (1.0 - kickProgress) * (1.0 - kickProgress)
                        drumSample += sin(2.0 * PI * kickFreq * (beatPosition.toDouble() / sampleRate)) * kickEnvelope * 0.4
                    }
                }

                if (beatNumber == 2 || (genre.lowercase() == "reggae" && beatNumber == 3)) {
                    // Snare / Clap
                    val snareProgress = beatPosition.toDouble() / (sampleRate * 0.12)
                    if (snareProgress < 1.0) {
                        val noise = (Math.random() * 2.0 - 1.0)
                        val snareEnvelope = 1.0 - snareProgress
                        drumSample += noise * snareEnvelope * 0.25
                    }
                }

                // Hi-Hat on 8th notes
                val eighthBeatPosition = sample % (samplesPerBeat / 2)
                if (eighthBeatPosition < sampleRate * 0.03) {
                    val hatEnvelope = 1.0 - (eighthBeatPosition.toDouble() / (sampleRate * 0.03))
                    drumSample += (Math.random() * 2.0 - 1.0) * hatEnvelope * 0.12
                }

                // 2. Bassline
                val bassNoteOffset = when ((sample / samplesPerBar) % 4) {
                    0 -> 0
                    1 -> 5
                    2 -> 3
                    else -> 7
                }
                val bassFreq = (baseFreq / 2.0) * Math.pow(2.0, bassNoteOffset / 12.0)
                val bassSample = sin(2.0 * PI * bassFreq * time) * 0.25

                // 3. Melodic & Harmonic Arpeggio / Chords
                val noteDuration = samplesPerBeat / 2
                if (sample % noteDuration == 0) {
                    currentNoteIndex = (currentNoteIndex + (1..3).random()) % scaleNotes.size
                }
                val noteSemitone = scaleNotes[currentNoteIndex] + bassNoteOffset
                val melodyFreq = baseFreq * Math.pow(2.0, noteSemitone / 12.0)

                // Soft synth bell tone
                val noteProgress = (sample % noteDuration).toDouble() / noteDuration
                val melodyEnvelope = Math.exp(-3.5 * noteProgress)
                val melodySample = (
                    sin(2.0 * PI * melodyFreq * time) * 0.2 +
                    sin(2.0 * PI * (melodyFreq * 2.0) * time) * 0.08 +
                    sin(2.0 * PI * (melodyFreq * 1.5) * time) * 0.04
                ) * melodyEnvelope

                // 4. Pad / Ambient Texture
                val padFreq1 = baseFreq * Math.pow(2.0, bassNoteOffset / 12.0)
                val padFreq2 = padFreq1 * 1.25 // Major/Minor 3rd
                val padSample = (sin(2.0 * PI * padFreq1 * time) + sin(2.0 * PI * padFreq2 * time)) * 0.06

                // Total Mix
                val left = (drumSample * 0.9 + bassSample * 0.8 + melodySample * 0.7 + padSample * 0.5).coerceIn(-0.95, 0.95)
                val right = (drumSample * 0.9 + bassSample * 0.8 + melodySample * 0.75 + padSample * 0.5).coerceIn(-0.95, 0.95)

                val leftShort = (left * 32767.0).toInt().toShort()
                val rightShort = (right * 32767.0).toInt().toShort()

                buffer.putShort(leftShort)
                buffer.putShort(rightShort)

                if (!buffer.hasRemaining()) {
                    fos.write(buffer.array())
                    buffer.clear()
                }
            }

            if (buffer.position() > 0) {
                fos.write(buffer.array(), 0, buffer.position())
            }
        }

        // Finalize WAV Header with correct sizes
        updateWavHeader(outputFile)

        outputFile.absolutePath
    }

    private fun writeWavHeaderPlaceholder(fos: FileOutputStream) {
        val placeholder = ByteArray(44)
        fos.write(placeholder)
    }

    private fun updateWavHeader(file: File) {
        val fileSize = file.length()
        val dataSize = fileSize - 44
        val totalSize = fileSize - 8
        val sampleRate = 44100
        val channels = 2
        val byteRate = sampleRate * channels * 2
        val blockAlign = channels * 2

        RandomAccessFile(file, "rw").use { raf ->
            raf.seek(0)
            raf.writeBytes("RIFF")
            raf.writeInt(Integer.reverseBytes(totalSize.toInt()))
            raf.writeBytes("WAVE")
            raf.writeBytes("fmt ")
            raf.writeInt(Integer.reverseBytes(16)) // Subchunk1Size (16 for PCM)
            raf.writeShort(java.lang.Short.reverseBytes(1.toShort()).toInt()) // AudioFormat (1 for PCM)
            raf.writeShort(java.lang.Short.reverseBytes(channels.toShort()).toInt())
            raf.writeInt(Integer.reverseBytes(sampleRate))
            raf.writeInt(Integer.reverseBytes(byteRate))
            raf.writeShort(java.lang.Short.reverseBytes(blockAlign.toShort()).toInt())
            raf.writeShort(java.lang.Short.reverseBytes(16.toShort()).toInt()) // BitsPerSample
            raf.writeBytes("data")
            raf.writeInt(Integer.reverseBytes(dataSize.toInt()))
        }
    }
}
