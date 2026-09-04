package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.AudioProcessingEngine
import com.example.audio.AudioRecorderManager
import com.example.audio.VocalEffectsConfig
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.DivBackground
import com.example.ui.theme.DivBorder
import com.example.ui.theme.DivCard
import com.example.ui.theme.DivCyan
import com.example.ui.theme.DivGradientBrand
import com.example.ui.theme.DivPink
import com.example.ui.theme.DivPurple
import com.example.ui.theme.DivPurpleLight
import com.example.ui.theme.DivSurfaceDark
import com.example.ui.theme.DivTextMuted
import com.example.ui.theme.DivTextSecondary
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocalStudioScreen(
    viewModel: MainViewModel,
    initialLyrics: String = "",
    onBackClick: () -> Unit,
    onNavigateToStudio: (vocalPath: String, instPath: String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Recorder State
    val isRecording by AudioRecorderManager.isRecording.collectAsStateWithLifecycle()
    val isPaused by AudioRecorderManager.isPaused.collectAsStateWithLifecycle()
    val recordingDuration by AudioRecorderManager.recordingDurationSeconds.collectAsStateWithLifecycle()
    val recordedAudioPath by AudioRecorderManager.recordedAudioPath.collectAsStateWithLifecycle()
    val maxAmplitude by AudioRecorderManager.currentMaxAmplitude.collectAsStateWithLifecycle()

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
        if (granted) {
            AudioRecorderManager.startRecording(context)
        } else {
            viewModel.showToast("Microphone permission required to record vocals")
        }
    }

    // Active vocal path (either newly recorded or imported)
    var activeVocalPath by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(recordedAudioPath) {
        if (recordedAudioPath != null) {
            activeVocalPath = recordedAudioPath
        }
    }

    // Audio Import Launcher
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val importedFile = File(context.cacheDir, "imported_vocal_${System.currentTimeMillis()}.m4a")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(importedFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        activeVocalPath = importedFile.absolutePath
                        viewModel.showToast("Audio track imported successfully")
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        viewModel.showToast("Failed to import audio: ${e.message}")
                    }
                }
            }
        }
    }

    // Playback state for previewing vocal / mix
    var isPreviewPlaying by remember { mutableStateOf(false) }
    var previewPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            previewPlayer?.release()
            previewPlayer = null
            AudioRecorderManager.stopRecording()
        }
    }

    // Vocal Cleanup & FX Controls
    var noiseReduction by remember { mutableStateOf(true) }
    var silenceRemoval by remember { mutableStateOf(false) }
    var volumeNormalization by remember { mutableStateOf(true) }
    var vocalEnhancement by remember { mutableStateOf(true) }

    var bassGain by remember { mutableFloatStateOf(1.0f) }
    var midGain by remember { mutableFloatStateOf(1.2f) }
    var trebleGain by remember { mutableFloatStateOf(1.1f) }
    var compressionRatio by remember { mutableFloatStateOf(3.0f) }
    var reverbAmount by remember { mutableFloatStateOf(0.25f) }
    var delayAmount by remember { mutableFloatStateOf(0.15f) }
    var pitchShiftSemitones by remember { mutableIntStateOf(0) }

    // Multi-track DAW Mixer Controls
    var vocalVolume by remember { mutableFloatStateOf(1.0f) }
    var isVocalMuted by remember { mutableStateOf(false) }
    var isVocalSolo by remember { mutableStateOf(false) }

    var instrumentalVolume by remember { mutableFloatStateOf(0.85f) }
    var isInstrumentalMuted by remember { mutableStateOf(false) }
    var isInstrumentalSolo by remember { mutableStateOf(false) }

    var isProcessingVocals by remember { mutableStateOf(false) }
    var processingStage by remember { mutableStateOf("") }
    var processingProgress by remember { mutableFloatStateOf(0f) }
    var mixedSongPath by remember { mutableStateOf<String?>(null) }

    // Instrumental Selection
    val userSongs by viewModel.userSongs.collectAsStateWithLifecycle()
    var selectedInstrumentalSongId by remember { mutableStateOf(userSongs.firstOrNull()?.id ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DivBackground)
            .testTag("vocal_studio_screen")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("AI Vocal Studio", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Recording, Cleanup, Multi-track Mixing", color = DivTextMuted, fontSize = 12.sp)
                }
            }

            IconButton(
                onClick = { audioPickerLauncher.launch("audio/*") },
                modifier = Modifier.testTag("import_audio_btn")
            ) {
                Icon(Icons.Default.Upload, contentDescription = "Import Audio", tint = DivCyan)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Recording Deck Card
            Card(
                colors = CardDefaults.cardColors(containerColor = DivSurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (isRecording) DivPink else DivBorder, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isRecording) {
                            if (isPaused) "RECORDING PAUSED" else "LIVE MICROPHONE RECORDING"
                        } else if (activeVocalPath != null) {
                            "VOCAL TRACK READY"
                        } else {
                            "VOCAL RECORDING DECK"
                        },
                        color = if (isRecording) DivPink else DivCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Timer Display
                    val minutes = recordingDuration / 60
                    val seconds = recordingDuration % 60
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Waveform / Amplitude Meter
                    WaveformVisualizer(
                        isPlaying = isRecording && !isPaused,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Recording Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isRecording) {
                            Button(
                                onClick = {
                                    if (hasAudioPermission) {
                                        AudioRecorderManager.startRecording(context)
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                modifier = Modifier
                                    .size(64.dp)
                                    .testTag("start_record_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = DivPink),
                                shape = CircleShape
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = "Record", tint = Color.White, modifier = Modifier.size(28.dp))
                            }
                        } else {
                            // Pause / Resume
                            IconButton(
                                onClick = {
                                    if (isPaused) AudioRecorderManager.resumeRecording()
                                    else AudioRecorderManager.pauseRecording()
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(DivBackground, CircleShape)
                                    .border(1.dp, DivBorder, CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = "Pause/Resume",
                                    tint = DivCyan
                                )
                            }

                            Spacer(modifier = Modifier.width(20.dp))

                            // Stop Recording
                            Button(
                                onClick = {
                                    val path = AudioRecorderManager.stopRecording()
                                    if (path != null) {
                                        activeVocalPath = path
                                        viewModel.showToast("Recording saved successfully!")
                                    }
                                },
                                modifier = Modifier
                                    .size(64.dp)
                                    .testTag("stop_record_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                shape = CircleShape
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.White, modifier = Modifier.size(28.dp))
                            }
                        }
                    }

                    // Playback Preview if vocal exists
                    if (activeVocalPath != null && !isRecording) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val path = activeVocalPath ?: return@OutlinedButton
                                    if (isPreviewPlaying) {
                                        previewPlayer?.stop()
                                        previewPlayer?.release()
                                        previewPlayer = null
                                        isPreviewPlaying = false
                                    } else {
                                        try {
                                            previewPlayer?.release()
                                            previewPlayer = MediaPlayer().apply {
                                                setDataSource(path)
                                                prepare()
                                                start()
                                                setOnCompletionListener {
                                                    isPreviewPlaying = false
                                                }
                                            }
                                            isPreviewPlaying = true
                                        } catch (e: Exception) {
                                            viewModel.showToast("Could not preview audio")
                                        }
                                    }
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DivCyan),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPreviewPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isPreviewPlaying) "Stop Preview" else "Preview Vocals", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    AudioRecorderManager.clearRecording()
                                    activeVocalPath = null
                                    previewPlayer?.release()
                                    previewPlayer = null
                                    isPreviewPlaying = false
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DivTextMuted),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Discard", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Voice Cleanup & DSP Audio Processing
            Card(
                colors = CardDefaults.cardColors(containerColor = DivSurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DivBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = DivCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Voice Cleanup & DSP Processing", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    // Toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Noise Reduction Gate", color = Color.White, fontSize = 13.sp)
                        Switch(
                            checked = noiseReduction,
                            onCheckedChange = { noiseReduction = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = DivCyan, checkedTrackColor = DivCyan.copy(alpha = 0.3f))
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Vocal Enhancement & Presence", color = Color.White, fontSize = 13.sp)
                        Switch(
                            checked = vocalEnhancement,
                            onCheckedChange = { vocalEnhancement = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = DivCyan, checkedTrackColor = DivCyan.copy(alpha = 0.3f))
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Dynamic Peak Normalization", color = Color.White, fontSize = 13.sp)
                        Switch(
                            checked = volumeNormalization,
                            onCheckedChange = { volumeNormalization = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = DivCyan, checkedTrackColor = DivCyan.copy(alpha = 0.3f))
                        )
                    }

                    // Sliders
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Equalizer: Bass", color = DivTextSecondary, fontSize = 12.sp)
                            Text("${String.format("%.1f", bassGain)}x", color = DivCyan, fontSize = 12.sp)
                        }
                        Slider(
                            value = bassGain,
                            onValueChange = { bassGain = it },
                            valueRange = 0.0f..2.0f,
                            colors = SliderDefaults.colors(thumbColor = DivCyan, activeTrackColor = DivCyan)
                        )
                    }

                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Equalizer: Mids (Vocal Warmth)", color = DivTextSecondary, fontSize = 12.sp)
                            Text("${String.format("%.1f", midGain)}x", color = DivCyan, fontSize = 12.sp)
                        }
                        Slider(
                            value = midGain,
                            onValueChange = { midGain = it },
                            valueRange = 0.0f..2.0f,
                            colors = SliderDefaults.colors(thumbColor = DivCyan, activeTrackColor = DivCyan)
                        )
                    }

                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Spatial Reverb & Room Ambience", color = DivTextSecondary, fontSize = 12.sp)
                            Text("${(reverbAmount * 100).toInt()}%", color = DivPink, fontSize = 12.sp)
                        }
                        Slider(
                            value = reverbAmount,
                            onValueChange = { reverbAmount = it },
                            valueRange = 0.0f..1.0f,
                            colors = SliderDefaults.colors(thumbColor = DivPink, activeTrackColor = DivPink)
                        )
                    }

                    // Process Vocals Button
                    if (activeVocalPath != null) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isProcessingVocals = true
                                    try {
                                        val config = VocalEffectsConfig(
                                            noiseReduction = noiseReduction,
                                            silenceRemoval = silenceRemoval,
                                            volumeNormalization = volumeNormalization,
                                            vocalEnhancement = vocalEnhancement,
                                            bassGain = bassGain,
                                            midGain = midGain,
                                            trebleGain = trebleGain,
                                            compressionRatio = compressionRatio,
                                            reverbAmount = reverbAmount,
                                            delayAmount = delayAmount,
                                            pitchShiftSemitones = pitchShiftSemitones
                                        )
                                        val processed = AudioProcessingEngine.processVocalTrack(
                                            context = context,
                                            inputAudioPath = activeVocalPath!!,
                                            config = config
                                        ) { progress, stage ->
                                            processingProgress = progress
                                            processingStage = stage
                                        }
                                        activeVocalPath = processed
                                        viewModel.showToast("Vocal enhancement & processing completed!")
                                    } catch (e: Exception) {
                                        viewModel.showToast("Processing failed: ${e.message}")
                                    } finally {
                                        isProcessingVocals = false
                                    }
                                }
                            },
                            enabled = !isProcessingVocals,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("process_vocals_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = DivPurpleLight),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isProcessingVocals) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(processingStage.ifBlank { "Processing DSP..." }, color = Color.White, fontSize = 12.sp)
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Apply Vocal Cleanup & Master FX", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Simplified DAW Mixer (Vocal Track + Instrumental Track = Mixed Song)
            Card(
                colors = CardDefaults.cardColors(containerColor = DivSurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DivBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = DivCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("2-Track Vocal + Instrumental Mixer", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    // Track 1: Vocals
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DivBackground),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DivCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (activeVocalPath != null) DivCyan else DivTextMuted)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Track 1: Vocals", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    FilterChip(
                                        selected = isVocalMuted,
                                        onClick = { isVocalMuted = !isVocalMuted },
                                        label = { Text("Mute", fontSize = 10.sp) },
                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color.Red.copy(alpha = 0.3f), selectedLabelColor = Color.Red)
                                    )
                                    FilterChip(
                                        selected = isVocalSolo,
                                        onClick = { isVocalSolo = !isVocalSolo },
                                        label = { Text("Solo", fontSize = 10.sp) },
                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = DivCyan.copy(alpha = 0.3f), selectedLabelColor = DivCyan)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = DivTextSecondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Slider(
                                    value = if (isVocalMuted) 0f else vocalVolume,
                                    onValueChange = { vocalVolume = it },
                                    valueRange = 0.0f..1.5f,
                                    colors = SliderDefaults.colors(thumbColor = DivCyan, activeTrackColor = DivCyan),
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${(vocalVolume * 100).toInt()}%", color = DivTextSecondary, fontSize = 11.sp)
                            }
                        }
                    }

                    // Track 2: Instrumental
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DivBackground),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DivPink.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(DivPink)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Track 2: Instrumental", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    FilterChip(
                                        selected = isInstrumentalMuted,
                                        onClick = { isInstrumentalMuted = !isInstrumentalMuted },
                                        label = { Text("Mute", fontSize = 10.sp) },
                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color.Red.copy(alpha = 0.3f), selectedLabelColor = Color.Red)
                                    )
                                    FilterChip(
                                        selected = isInstrumentalSolo,
                                        onClick = { isInstrumentalSolo = !isInstrumentalSolo },
                                        label = { Text("Solo", fontSize = 10.sp) },
                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = DivPink.copy(alpha = 0.3f), selectedLabelColor = DivPink)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = DivTextSecondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Slider(
                                    value = if (isInstrumentalMuted) 0f else instrumentalVolume,
                                    onValueChange = { instrumentalVolume = it },
                                    valueRange = 0.0f..1.5f,
                                    colors = SliderDefaults.colors(thumbColor = DivPink, activeTrackColor = DivPink),
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${(instrumentalVolume * 100).toInt()}%", color = DivTextSecondary, fontSize = 11.sp)
                            }
                        }
                    }

                    // Mix and Master Button
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val vPath = activeVocalPath ?: ""
                                val mixed = AudioProcessingEngine.mixTracks(
                                    context = context,
                                    vocalPath = vPath,
                                    instrumentalPath = "",
                                    vocalVolume = if (isVocalMuted) 0f else vocalVolume,
                                    instrumentalVolume = if (isInstrumentalMuted) 0f else instrumentalVolume,
                                    outputName = "DIV_SONG_AI_Master"
                                )
                                mixedSongPath = mixed
                                viewModel.showToast("Mixed Song Rendered Successfully!")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("mix_tracks_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(DivGradientBrand, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Mix & Master Final Song", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }

                    // Send to Multitrack Studio
                    OutlinedButton(
                        onClick = {
                            onNavigateToStudio(activeVocalPath ?: "", "")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DivCyan),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(DivBorder)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Open in 5-Track Multitrack Studio DAW", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
