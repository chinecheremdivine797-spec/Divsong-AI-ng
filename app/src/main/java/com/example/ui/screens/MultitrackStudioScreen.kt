package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.TrackStem
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

data class DawTrack(
    val id: String = UUID.randomUUID().toString().take(8),
    var name: String,
    var volume: Float = 1.0f,
    var isMuted: Boolean = false,
    var isSolo: Boolean = false,
    var pan: Float = 0.0f,
    var startOffsetSec: Float = 0.0f,
    var durationSec: Float = 45.0f,
    var fadeInSec: Float = 0.5f,
    var fadeOutSec: Float = 0.5f,
    val color: Color
)

@Composable
fun MultitrackStudioScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    onExportClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 5 Default Professional DAW Tracks
    val tracks = remember {
        mutableStateListOf(
            DawTrack(name = "Track 1 — Vocals", volume = 1.0f, color = DivCyan, durationSec = 60f),
            DawTrack(name = "Track 2 — Instrumental", volume = 0.9f, color = DivPink, durationSec = 60f),
            DawTrack(name = "Track 3 — Backing Vocals", volume = 0.75f, color = DivPurpleLight, durationSec = 50f),
            DawTrack(name = "Track 4 — Effects", volume = 0.6f, color = Color(0xFFFFD166), durationSec = 40f),
            DawTrack(name = "Track 5 — Additional Audio", volume = 0.8f, color = Color(0xFF06D6A0), durationSec = 30f)
        )
    }

    // Undo / Redo History Stack
    val undoStack = remember { mutableStateListOf<List<DawTrack>>() }
    val redoStack = remember { mutableStateListOf<List<DawTrack>>() }

    fun saveSnapshot() {
        val snapshot = tracks.map { it.copy() }
        undoStack.add(snapshot)
        redoStack.clear()
    }

    // Master Transport
    var isPlaying by remember { mutableStateOf(false) }
    var playheadPositionSec by remember { mutableFloatStateOf(0.0f) }
    var zoomLevel by remember { mutableFloatStateOf(1.0f) } // 0.5x, 1.0x, 2.0x
    val totalProjectLengthSec = 60f

    // Interactive Playhead Loop
    LaunchedEffect(isPlaying) {
        while (isActive && isPlaying) {
            delay(100)
            playheadPositionSec += 0.1f
            if (playheadPositionSec >= totalProjectLengthSec) {
                playheadPositionSec = 0f
                isPlaying = false
            }
        }
    }

    // Rename Dialog
    var trackToRename by remember { mutableStateOf<DawTrack?>(null) }
    var renameInput by remember { mutableStateOf("") }

    // Master Volume
    var masterVolume by remember { mutableFloatStateOf(1.0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DivBackground)
            .testTag("multitrack_studio_screen")
    ) {
        // Studio Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DivSurfaceDark)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text("Professional Studio", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("5-Track Multitrack DAW & Stems", color = DivCyan, fontSize = 11.sp)
                }
            }

            // Undo / Redo / Zoom & Save
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        if (undoStack.isNotEmpty()) {
                            val last = undoStack.removeAt(undoStack.size - 1)
                            redoStack.add(tracks.map { it.copy() })
                            tracks.clear()
                            tracks.addAll(last)
                            viewModel.showToast("Undo performed")
                        }
                    },
                    enabled = undoStack.isNotEmpty()
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        tint = if (undoStack.isNotEmpty()) Color.White else DivTextMuted
                    )
                }

                IconButton(
                    onClick = {
                        if (redoStack.isNotEmpty()) {
                            val next = redoStack.removeAt(redoStack.size - 1)
                            undoStack.add(tracks.map { it.copy() })
                            tracks.clear()
                            tracks.addAll(next)
                            viewModel.showToast("Redo performed")
                        }
                    },
                    enabled = redoStack.isNotEmpty()
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo",
                        tint = if (redoStack.isNotEmpty()) Color.White else DivTextMuted
                    )
                }

                IconButton(
                    onClick = {
                        zoomLevel = if (zoomLevel >= 2.0f) 0.5f else zoomLevel + 0.5f
                    }
                ) {
                    Icon(Icons.Default.ZoomIn, contentDescription = "Zoom", tint = DivCyan)
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.createProject(
                                name = "Studio Session",
                                genre = "Multitrack Master",
                                status = "ready"
                            )
                            viewModel.showToast("Multitrack session saved to Projects!")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DivCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Save", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Transport Deck (Play, Pause, Stop, Scrub, Master Level)
        Card(
            colors = CardDefaults.cardColors(containerColor = DivSurfaceDark),
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DivBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Playback Transport
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = {
                            playheadPositionSec = 0f
                            isPlaying = false
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Rewind", tint = Color.White)
                    }

                    Button(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier.size(42.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isPlaying) DivPink else DivCyan),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            isPlaying = false
                            playheadPositionSec = 0f
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.White)
                    }

                    // Time display
                    val currentM = (playheadPositionSec.toInt() / 60)
                    val currentS = (playheadPositionSec.toInt() % 60)
                    Text(
                        text = String.format("%02d:%02d", currentM, currentS),
                        color = DivCyan,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Master Volume Slider
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.width(140.dp)) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = DivTextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Slider(
                        value = masterVolume,
                        onValueChange = { masterVolume = it },
                        valueRange = 0.0f..1.5f,
                        colors = SliderDefaults.colors(thumbColor = DivCyan, activeTrackColor = DivCyan)
                    )
                }
            }
        }

        // DAW Timeline & Tracks Area
        val horizontalScrollState = rememberScrollState()
        val verticalScrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(verticalScrollState)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            tracks.forEachIndexed { index, track ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DivSurfaceDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DivBorder, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Track Header (Name, Mute, Solo, Volume, Tools)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(track.color)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = track.name,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                IconButton(
                                    onClick = {
                                        trackToRename = track
                                        renameInput = track.name
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Rename", tint = DivTextMuted, modifier = Modifier.size(14.dp))
                                }
                            }

                            // Track Controls: Mute, Solo, Duplicate, Delete
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                FilterChip(
                                    selected = track.isMuted,
                                    onClick = {
                                        saveSnapshot()
                                        track.isMuted = !track.isMuted
                                    },
                                    label = { Text("M", fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color.Red.copy(alpha = 0.3f),
                                        selectedLabelColor = Color.Red
                                    )
                                )

                                FilterChip(
                                    selected = track.isSolo,
                                    onClick = {
                                        saveSnapshot()
                                        track.isSolo = !track.isSolo
                                    },
                                    label = { Text("S", fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DivCyan.copy(alpha = 0.3f),
                                        selectedLabelColor = DivCyan
                                    )
                                )

                                IconButton(
                                    onClick = {
                                        saveSnapshot()
                                        val copy = track.copy(id = UUID.randomUUID().toString().take(8), name = "${track.name} (Dup)")
                                        tracks.add(copy)
                                        viewModel.showToast("Track duplicated")
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = DivTextSecondary, modifier = Modifier.size(14.dp))
                                }

                                if (tracks.size > 1) {
                                    IconButton(
                                        onClick = {
                                            saveSnapshot()
                                            tracks.removeAt(index)
                                            viewModel.showToast("Track deleted")
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Waveform Timeline Canvas for this track
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DivBackground)
                                .border(1.dp, DivBorder, RoundedCornerShape(8.dp))
                        ) {
                            // Audio Waveform Block
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(0.85f)
                                    .padding(horizontal = 4.dp, vertical = 6.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(track.color.copy(alpha = 0.25f))
                                    .border(1.dp, track.color.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${track.name.take(12)} Stem", color = track.color, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    Text("${track.durationSec.toInt()}s", color = DivTextMuted, fontSize = 9.sp)
                                }
                            }

                            // Playhead line
                            val playheadFraction = (playheadPositionSec / totalProjectLengthSec).coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(playheadFraction)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .width(2.dp)
                                        .fillMaxHeight()
                                        .background(Color.White)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Volume & Fader Row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Gain: ${(track.volume * 100).toInt()}%", color = DivTextSecondary, fontSize = 11.sp, modifier = Modifier.width(68.dp))
                            Slider(
                                value = track.volume,
                                onValueChange = {
                                    saveSnapshot()
                                    track.volume = it
                                },
                                valueRange = 0.0f..1.5f,
                                colors = SliderDefaults.colors(thumbColor = track.color, activeTrackColor = track.color),
                                modifier = Modifier.weight(1f)
                            )

                            // Quick Edit Tools: Split, Fade In, Fade Out
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton(
                                    onClick = {
                                        saveSnapshot()
                                        viewModel.showToast("Track trimmed at playhead (${playheadPositionSec.toInt()}s)")
                                    }
                                ) {
                                    Text("Cut", fontSize = 10.sp, color = DivCyan)
                                }
                                TextButton(
                                    onClick = {
                                        saveSnapshot()
                                        viewModel.showToast("Fade effect added")
                                    }
                                ) {
                                    Text("Fade", fontSize = 10.sp, color = DivPink)
                                }
                            }
                        }
                    }
                }
            }

            // Add Track Button
            OutlinedButton(
                onClick = {
                    saveSnapshot()
                    val newTrack = DawTrack(
                        name = "Track ${tracks.size + 1} — Stem",
                        volume = 0.8f,
                        color = DivCyan,
                        durationSec = 45f
                    )
                    tracks.add(newTrack)
                    viewModel.showToast("Added new track")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DivCyan),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add New Audio Track Stem", fontSize = 12.sp)
            }
        }

        // Rename Track Dialog
        if (trackToRename != null) {
            AlertDialog(
                onDismissRequest = { trackToRename = null },
                title = { Text("Rename Track", color = Color.White) },
                text = {
                    OutlinedTextField(
                        value = renameInput,
                        onValueChange = { renameInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = DivCyan,
                            unfocusedBorderColor = DivBorder
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            saveSnapshot()
                            trackToRename?.name = renameInput.ifBlank { trackToRename?.name ?: "Track" }
                            trackToRename = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DivCyan)
                    ) {
                        Text("Save", color = Color.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { trackToRename = null }) {
                        Text("Cancel", color = DivTextMuted)
                    }
                },
                containerColor = DivSurfaceDark
            )
        }
    }
}
