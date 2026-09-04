package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.entities.SongEntity
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

data class VideoScene(
    val id: String = UUID.randomUUID().toString().take(8),
    val sceneTitle: String,
    val visualDescription: String,
    val imageUrl: String,
    val durationSeconds: Int = 4,
    val lyricSubtitle: String = "",
    val transition: String = "Cross Dissolve"
)

@Composable
fun MusicVideoStudioScreen(
    viewModel: MainViewModel,
    initialSongId: String = "",
    onBackClick: () -> Unit,
    onOpenProjects: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val userSongs by viewModel.userSongs.collectAsStateWithLifecycle()
    var selectedSong by remember {
        mutableStateOf<SongEntity?>(
            userSongs.find { it.id == initialSongId } ?: userSongs.firstOrNull()
        )
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = AI Generator, 1 = Video Studio Editor

    // AI Generator State
    var videoPrompt by remember {
        mutableStateOf("Create a motivational music video for a Nigerian Afrobeats song about success and celebration.")
    }
    var videoStyle by remember { mutableStateOf("Cinematic Golden Hour") }
    var aspectRatio by remember { mutableStateOf("16:9 Landscape") }
    var isGeneratingVideo by remember { mutableStateOf(false) }
    var generationProgress by remember { mutableFloatStateOf(0f) }
    var generationStepText by remember { mutableStateOf("") }

    // Video Scenes List
    val scenes = remember {
        mutableStateListOf(
            VideoScene(
                sceneTitle = "Scene 1: Intro Hustle",
                visualDescription = "Golden Lagos sunrise over the city skyline, bustling energy and determination.",
                imageUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?auto=format&fit=crop&w=600&q=80",
                durationSeconds = 4,
                lyricSubtitle = "DIV Song AI... Turn the speaker up!",
                transition = "Fade In"
            ),
            VideoScene(
                sceneTitle = "Scene 2: Overcoming Obstacles",
                visualDescription = "Late night recording studio with warm analog neon glow and audio meters jumping.",
                imageUrl = "https://images.unsplash.com/photo-1598488035139-bdbb2231ce04?auto=format&fit=crop&w=600&q=80",
                durationSeconds = 4,
                lyricSubtitle = "From the sunrise till the evening light",
                transition = "Cross Dissolve"
            ),
            VideoScene(
                sceneTitle = "Scene 3: The Breakthrough",
                visualDescription = "Energetic street dancers in vibrant modern African prints performing choreography.",
                imageUrl = "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?auto=format&fit=crop&w=600&q=80",
                durationSeconds = 5,
                lyricSubtitle = "We dey keep the focus, shining bright!",
                transition = "Beat Pulse"
            ),
            VideoScene(
                sceneTitle = "Scene 4: Victory Celebration",
                visualDescription = "Stadium concert cheering crowd under dazzling laser lights and golden sparks.",
                imageUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?auto=format&fit=crop&w=600&q=80",
                durationSeconds = 5,
                lyricSubtitle = "Oya dance to the rhythm, feel the heat!",
                transition = "Zoom In"
            )
        )
    }

    // Video Playback Simulation
    var isVideoPlaying by remember { mutableStateOf(false) }
    var activeSceneIndex by remember { mutableIntStateOf(0) }
    var currentPlaybackSec by remember { mutableFloatStateOf(0f) }

    val activeScene = scenes.getOrNull(activeSceneIndex) ?: scenes.firstOrNull()

    LaunchedEffect(isVideoPlaying) {
        while (isActive && isVideoPlaying) {
            delay(1000)
            currentPlaybackSec += 1f
            val current = scenes.getOrNull(activeSceneIndex)
            val sceneDur = current?.durationSeconds ?: 4
            if (currentPlaybackSec >= sceneDur) {
                currentPlaybackSec = 0f
                if (activeSceneIndex < scenes.size - 1) {
                    activeSceneIndex += 1
                } else {
                    activeSceneIndex = 0
                    isVideoPlaying = false
                }
            }
        }
    }

    // Media Picker for Custom Clip
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val newScene = VideoScene(
                sceneTitle = "Scene ${scenes.size + 1}: Custom Clip",
                visualDescription = "User uploaded custom footage",
                imageUrl = uri.toString(),
                durationSeconds = 4,
                lyricSubtitle = "Your rhythm, your story",
                transition = "Fade In"
            )
            scenes.add(newScene)
            viewModel.showToast("New video clip added!")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DivBackground)
            .testTag("music_video_studio_screen")
    ) {
        // Top Bar
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
                    Text("Music Video Studio", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("AI Scene Direction & 4K Video Composition", color = DivCyan, fontSize = 11.sp)
                }
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        viewModel.createProject(
                            name = selectedSong?.title ?: "Music Video Project",
                            genre = selectedSong?.genre ?: "Afrobeats",
                            videoUrl = "https://assets.divsong.ai/sample_video.mp4",
                            status = "ready"
                        )
                        viewModel.showToast("Music Video project saved!")
                        onOpenProjects()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DivCyan),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Tabs: AI Generator vs Studio Editor
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DivSurfaceDark,
            contentColor = DivCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = DivCyan
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("AI Video Generator", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Video Studio Editor", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Video Preview Canvas
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .border(1.dp, DivBorder, RoundedCornerShape(16.dp))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (activeScene != null) {
                        AsyncImage(
                            model = activeScene.imageUrl,
                            contentDescription = activeScene.sceneTitle,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Dark gradient scrim for subtitles
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                )
                            )
                    )

                    // Active Scene Badge & Song Info
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.7f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = activeScene?.sceneTitle ?: "Scene",
                                color = DivCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        selectedSong?.let { song ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black.copy(alpha = 0.7f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = DivPink, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(song.title, color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // Synchronized Subtitle / Lyric Overlay
                    if (activeScene != null && activeScene.lyricSubtitle.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 20.dp, start = 16.dp, end = 16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = activeScene.lyricSubtitle,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Center Play / Pause Floating Control
                    IconButton(
                        onClick = { isVideoPlaying = !isVideoPlaying },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(54.dp)
                            .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                            .border(1.dp, DivCyan.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isVideoPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause Video",
                            tint = DivCyan,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            if (selectedTab == 0) {
                // AI Video Generator Form
                Card(
                    colors = CardDefaults.cardColors(containerColor = DivSurfaceDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DivBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = DivCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Visual Scene Direction Prompt", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedTextField(
                            value = videoPrompt,
                            onValueChange = { videoPrompt = it },
                            placeholder = { Text("e.g. Create a motivational music video for a Nigerian Afrobeats song about success.", color = DivTextMuted, fontSize = 13.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(88.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = DivCyan,
                                unfocusedBorderColor = DivBorder,
                                focusedContainerColor = DivBackground,
                                unfocusedContainerColor = DivBackground
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Visual Style Presets
                        Text("Visual Style & Cinematography", color = DivTextSecondary, fontSize = 12.sp)
                        val styles = listOf("Cinematic Golden Hour", "Afrobeats Street Energy", "Cyberpunk Neon", "Soulful Acoustic", "Vintage 35mm Film")
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            styles.forEach { style ->
                                FilterChip(
                                    selected = videoStyle == style,
                                    onClick = { videoStyle = style },
                                    label = { Text(style, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DivCyan.copy(alpha = 0.2f),
                                        selectedLabelColor = DivCyan
                                    )
                                )
                            }
                        }

                        // Select Song Track to Synchronize
                        Text("Synchronize With Song", color = DivTextSecondary, fontSize = 12.sp)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            userSongs.forEach { song ->
                                FilterChip(
                                    selected = selectedSong?.id == song.id,
                                    onClick = { selectedSong = song },
                                    label = { Text(song.title, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DivPink.copy(alpha = 0.2f),
                                        selectedLabelColor = DivPink
                                    )
                                )
                            }
                        }

                        // Generate Button
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isGeneratingVideo = true
                                    generationStepText = "Analyzing audio cadence and lyrical beats..."
                                    generationProgress = 0.25f
                                    delay(1200)

                                    generationStepText = "Synthesizing cinematic visual sequences..."
                                    generationProgress = 0.6f
                                    delay(1400)

                                    generationStepText = "Rendering subtitle typography & transitions..."
                                    generationProgress = 0.9f
                                    delay(1000)

                                    generationProgress = 1.0f
                                    isGeneratingVideo = false
                                    viewModel.showToast("AI Music Video Generated Successfully!")
                                }
                            },
                            enabled = !isGeneratingVideo,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("generate_music_video_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(DivGradientBrand, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isGeneratingVideo) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(generationStepText, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { generationProgress },
                                            modifier = Modifier.width(180.dp),
                                            color = DivCyan
                                        )
                                    }
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Generate Complete Music Video", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Video Studio Timeline Editor
                Card(
                    colors = CardDefaults.cardColors(containerColor = DivSurfaceDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DivBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Timeline Scenes (${scenes.size})", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                            OutlinedButton(
                                onClick = { mediaPickerLauncher.launch("image/*") },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DivCyan),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Clip", fontSize = 11.sp)
                            }
                        }

                        // Scenes list
                        scenes.forEachIndexed { index, scene ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (activeSceneIndex == index) DivCyan.copy(alpha = 0.12f) else DivBackground
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        if (activeSceneIndex == index) DivCyan else DivBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        activeSceneIndex = index
                                        currentPlaybackSec = 0f
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        AsyncImage(
                                            model = scene.imageUrl,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(6.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(scene.sceneTitle, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text("${scene.durationSeconds}s • ${scene.transition}", color = DivTextMuted, fontSize = 11.sp)
                                            if (scene.lyricSubtitle.isNotBlank()) {
                                                Text("\"${scene.lyricSubtitle}\"", color = DivCyan, fontSize = 10.sp, maxLines = 1)
                                            }
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            if (scenes.size > 1) {
                                                scenes.removeAt(index)
                                                activeSceneIndex = 0
                                            }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
