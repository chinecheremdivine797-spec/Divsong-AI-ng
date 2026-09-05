package com.example.ui.screens

import android.media.MediaPlayer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.VideoView
import com.example.ui.theme.DivBackground
import com.example.ui.theme.DivBorder
import com.example.ui.theme.DivCyan
import com.example.ui.theme.DivPink
import com.example.ui.theme.DivPurpleLight
import com.example.ui.theme.DivSurfaceDark
import com.example.ui.theme.DivTextMuted
import com.example.ui.theme.DivTextSecondary
import com.example.ui.viewmodel.MainViewModel

/** DIV SONG AI upload-and-edit workspace. */
@Composable
fun MusicVideoStudioScreen(
    viewModel: MainViewModel,
    initialSongId: String = "",
    onBackClick: () -> Unit,
    onOpenProjects: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var videoUri by remember { mutableStateOf<Uri?>(null) }
    var voiceUri by remember { mutableStateOf<Uri?>(null) }
    var selectedStyle by remember { mutableStateOf("Cinematic") }
    var trimStart by remember { mutableFloatStateOf(0f) }
    var trimEnd by remember { mutableFloatStateOf(100f) }
    var voiceVolume by remember { mutableFloatStateOf(1f) }
    var videoSpeed by remember { mutableFloatStateOf(1f) }
    var autoCaptions by remember { mutableStateOf(true) }
    var beatSync by remember { mutableStateOf(true) }
    var isPlayingVoice by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var editMessage by remember { mutableStateOf("") }

    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        videoUri = uri
        editMessage = if (uri != null) "Video loaded — ready to edit." else ""
    }
    val voicePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        voiceUri = uri
        editMessage = if (uri != null) "Voice track loaded — ready to mix." else ""
    }
    val mediaPlayer = remember(voiceUri) { voiceUri?.let { MediaPlayer.create(context, it) } }
    DisposableEffect(mediaPlayer) { onDispose { mediaPlayer?.release() } }

    Column(
        modifier = Modifier.fillMaxSize().background(DivBackground).verticalScroll(scrollState).padding(bottom = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().background(DivSurfaceDark).padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) }
            Column(modifier = Modifier.weight(1f)) {
                Text("AI Media Editor", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Upload your voice + video and edit them together", color = DivCyan, fontSize = 11.sp)
            }
            Icon(Icons.Default.AutoAwesome, null, tint = DivPink, modifier = Modifier.size(22.dp))
        }

        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Card(colors = CardDefaults.cardColors(containerColor = DivSurfaceDark), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().border(1.dp, DivBorder, RoundedCornerShape(16.dp))) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("1. Add your media", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Bring your own footage and voice into one editing workspace.", color = DivTextSecondary, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = { videoPicker.launch("video/*") }, modifier = Modifier.weight(1f).height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = DivCyan), shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Default.Movie, null, tint = Color.Black); Spacer(Modifier.width(6.dp)); Text(if (videoUri == null) "Upload Video" else "Video Added", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                        Button(onClick = { voicePicker.launch("audio/*") }, modifier = Modifier.weight(1f).height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = DivPink), shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Default.Mic, null, tint = Color.White); Spacer(Modifier.width(6.dp)); Text(if (voiceUri == null) "Upload Voice" else "Voice Added", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudUpload, null, tint = DivTextMuted, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp))
                        Text(if (videoUri != null && voiceUri != null) "Both tracks are loaded." else "Choose a video and your recorded voice.", color = DivTextMuted, fontSize = 11.sp)
                    }
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = Color.Black), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().height(220.dp).border(1.dp, DivBorder, RoundedCornerShape(16.dp))) {
                if (videoUri != null) {
                    AndroidView(factory = { VideoView(it) }, update = { view ->
                        view.setVideoURI(videoUri)
                        view.setOnPreparedListener { player -> player.isLooping = true }
                    }, modifier = Modifier.fillMaxSize())
                } else {
                    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.Movie, null, tint = DivTextMuted, modifier = Modifier.size(42.dp)); Spacer(Modifier.height(8.dp)); Text("Your video preview appears here", color = DivTextMuted, fontSize = 13.sp)
                    }
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = DivSurfaceDark), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().border(1.dp, DivBorder, RoundedCornerShape(16.dp))) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("2. Voice track", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MusicNote, null, tint = DivPink, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp))
                        Text(if (voiceUri != null) "Your uploaded voice is ready" else "Upload your voice to add it to the edit", color = if (voiceUri != null) Color.White else DivTextMuted, fontSize = 13.sp)
                        Spacer(Modifier.weight(1f))
                        if (voiceUri != null) IconButton(onClick = { mediaPlayer?.let { p -> if (p.isPlaying) { p.pause(); isPlayingVoice = false } else { p.start(); isPlayingVoice = true } } }) { Icon(if (isPlayingVoice) Icons.Default.Pause else Icons.Default.PlayArrow, "Preview voice", tint = DivCyan) }
                    }
                    Text("Voice volume", color = DivTextSecondary, fontSize = 12.sp)
                    Slider(value = voiceVolume, onValueChange = { voiceVolume = it }, valueRange = 0f..1.5f)
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = DivSurfaceDark), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().border(1.dp, DivBorder, RoundedCornerShape(16.dp))) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Tune, null, tint = DivCyan, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(7.dp)); Text("3. Edit controls", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                    Text("Trim: ${trimStart.toInt()}% — ${trimEnd.toInt()}%", color = DivTextSecondary, fontSize = 12.sp)
                    Slider(value = trimStart, onValueChange = { trimStart = it.coerceAtMost(trimEnd - 1f) }, valueRange = 0f..99f)
                    Slider(value = trimEnd, onValueChange = { trimEnd = it.coerceAtLeast(trimStart + 1f) }, valueRange = 1f..100f)
                    Text("Playback speed: ${"%.1f".format(videoSpeed)}x", color = DivTextSecondary, fontSize = 12.sp)
                    Slider(value = videoSpeed, onValueChange = { videoSpeed = it }, valueRange = 0.5f..2f)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf("Cinematic", "Music Video", "TikTok", "YouTube").forEach { style -> FilterChip(selected = selectedStyle == style, onClick = { selectedStyle = style }, label = { Text(style, fontSize = 10.sp) }) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        FilterChip(selected = autoCaptions, onClick = { autoCaptions = !autoCaptions }, label = { Text("Auto captions", fontSize = 11.sp) })
                        FilterChip(selected = beatSync, onClick = { beatSync = !beatSync }, label = { Text("Beat sync", fontSize = 11.sp) })
                    }
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = DivSurfaceDark), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().border(1.dp, DivBorder, RoundedCornerShape(16.dp))) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("4. AI edit", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Prepare an AI edit using your video, voice, style, trim and selected controls. The final provider rendering belongs on the secure backend.", color = DivTextSecondary, fontSize = 12.sp)
                    Button(onClick = {
                        if (videoUri == null) { editMessage = "Upload a video first."; return@Button }
                        isEditing = true; progress = 0.05f; editMessage = "Preparing your edit..."; viewModel.showToast("Edit job prepared")
                    }, enabled = !isEditing, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = DivPurpleLight), shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Default.AutoAwesome, null); Spacer(Modifier.width(7.dp)); Text(if (isEditing) "Preparing Edit..." else "✨ EDIT MY VIDEO WITH AI", fontWeight = FontWeight.Bold)
                    }
                    if (isEditing) {
                        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                        LaunchedEffect(isEditing) {
                            kotlinx.coroutines.delay(600); progress = 0.35f; kotlinx.coroutines.delay(600); progress = 0.7f; kotlinx.coroutines.delay(600); progress = 1f; isEditing = false
                            editMessage = "Edit plan prepared: ${selectedStyle}, ${trimStart.toInt()}–${trimEnd.toInt()}%, ${videoSpeed}x, captions ${if (autoCaptions) "on" else "off"}, beat sync ${if (beatSync) "on" else "off"}."
                        }
                    }
                    if (editMessage.isNotBlank()) Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Check, null, tint = DivCyan, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(6.dp)); Text(editMessage, color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Start) }
                }
            }

            OutlinedButton(onClick = { viewModel.showToast("Media edit project saved"); onOpenProjects() }, enabled = videoUri != null, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.Save, null); Spacer(Modifier.width(7.dp)); Text("Save Edit Project", fontWeight = FontWeight.Bold)
            }
        }
    }
}
