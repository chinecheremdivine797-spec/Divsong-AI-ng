package com.example.data.ai

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DivAudioSynthesizerTest {

    @Test
    fun testAudioSynthesizerGeneratesValidWavFile() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val outputFile = File(context.cacheDir, "test_synth.wav")
        if (outputFile.exists()) outputFile.delete()

        DivAudioSynthesizer.synthesizeSong(
            outputFile = outputFile,
            genre = "Afrobeats",
            mood = "Energetic",
            tempoBpm = 118,
            durationSeconds = 2
        )

        assertTrue("Output file must exist", outputFile.exists())
        assertTrue("Output file size should be greater than 1000 bytes", outputFile.length() > 1000)
    }
}
