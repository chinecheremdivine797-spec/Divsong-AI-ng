package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.GenerationProgressModal
import com.example.ui.theme.DivBackground
import com.example.ui.theme.DivBorder
import com.example.ui.theme.DivCard
import com.example.ui.theme.DivCardElevated
import com.example.ui.theme.DivCyan
import com.example.ui.theme.DivCyanDark
import com.example.ui.theme.DivGradientBrand
import com.example.ui.theme.DivPink
import com.example.ui.theme.DivPurple
import com.example.ui.theme.DivPurpleDark
import com.example.ui.theme.DivPurpleLight
import com.example.ui.theme.DivSurfaceDark
import com.example.ui.theme.DivTextMuted
import com.example.ui.theme.DivTextSecondary
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateMusicScreen(
    viewModel: MainViewModel,
    onSongGenerated: (String) -> Unit
) {
    val prompt by viewModel.promptState.collectAsStateWithLifecycle()
    val genre by viewModel.genreState.collectAsStateWithLifecycle()
    val customGenre by viewModel.customGenreState.collectAsStateWithLifecycle()
    val mood by viewModel.moodState.collectAsStateWithLifecycle()
    val language by viewModel.languageState.collectAsStateWithLifecycle()
    val vocalStyle by viewModel.vocalStyleState.collectAsStateWithLifecycle()
    val lyricsOption by viewModel.lyricsOptionState.collectAsStateWithLifecycle()
    val lyricsText by viewModel.lyricsTextState.collectAsStateWithLifecycle()
    val structure by viewModel.structureState.collectAsStateWithLifecycle()
    val tempo by viewModel.tempoState.collectAsStateWithLifecycle()
    val energy by viewModel.energyState.collectAsStateWithLifecycle()
    val creativity by viewModel.creativityLevel.collectAsStateWithLifecycle()
    val isSimpleMode by viewModel.isSimpleMode.collectAsStateWithLifecycle()

    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val generationProgress by viewModel.generationProgress.collectAsStateWithLifecycle()
    val generationStage by viewModel.generationStage.collectAsStateWithLifecycle()
    val isAiWritingLyrics by viewModel.isAiWritingLyrics.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val genresList = listOf(
        "Afrobeats", "Amapiano", "Hip-hop", "R&B", "Gospel", "Dancehall",
        "Pop", "EDM", "Reggae", "Highlife", "Soul", "Rock", "Acoustic", "Custom"
    )

    val moodsList = listOf(
        "Energetic", "Happy", "Emotional", "Romantic", "Calm", "Inspirational",
        "Hopeful", "Aggressive", "Dark", "Peaceful", "Epic"
    )

    val languagesList = listOf(
        "English", "Nigerian Pidgin", "Yoruba", "Igbo", "Hausa", "French", "Spanish", "Swahili"
    )

    val vocalStylesList = listOf(
        "Male vocal", "Female vocal", "Group vocal", "Natural acoustic", "Powerful belt", "Soft melodious"
    )

    var showProgressDialog by remember { mutableStateOf(false) }

    if (isGenerating) {
        showProgressDialog = true
    }

    if (showProgressDialog && isGenerating) {
        GenerationProgressModal(
            progressPercent = generationProgress,
            currentStage = generationStage,
            genre = genre,
            mood = mood,
            onCancel = {
                viewModel.cancelGeneration()
                showProgressDialog = false
            },
            onBackground = {
                showProgressDialog = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DivBackground)
            .padding(horizontal = 16.dp)
            .testTag("create_music_screen"),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            // Screen Header & Mode Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AI Sound Studio",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp
                    )
                    Text(
                        text = "Craft original songs with neural intelligence",
                        color = DivTextSecondary,
                        fontSize = 12.sp
                    )
                }

                // Simple / Custom Mode Switcher
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(DivSurfaceDark)
                        .border(1.dp, DivBorder, RoundedCornerShape(20.dp))
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSimpleMode) DivPurple else Color.Transparent)
                            .clickable { viewModel.isSimpleMode.value = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("mode_simple")
                    ) {
                        Text(
                            text = "Simple",
                            color = if (isSimpleMode) Color.White else DivTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (!isSimpleMode) DivPurple else Color.Transparent)
                            .clickable { viewModel.isSimpleMode.value = false }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("mode_custom")
                    ) {
                        Text(
                            text = "Custom",
                            color = if (!isSimpleMode) Color.White else DivTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Music Provider Status / Sandbox Banner
        item {
            val isConfigured = viewModel.isMusicProviderConfigured
            val isSandbox by viewModel.isDevSandboxMode.collectAsStateWithLifecycle()

            if (!isConfigured && !isSandbox) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, DivPink.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                        .testTag("provider_not_configured_card"),
                    colors = CardDefaults.cardColors(containerColor = DivCardElevated)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = DivPink,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Music generation is currently being configured.",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "To generate music with an external AI provider, supply MUSIC_API_KEY in the Secrets panel or environment. Or switch to Dev Sandbox Mode to test the audio pipeline.",
                            color = DivTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Testing the UI & sound?",
                                color = DivTextMuted,
                                fontSize = 11.sp
                            )
                            OutlinedButton(
                                onClick = { viewModel.toggleDevSandboxMode(true) },
                                modifier = Modifier.testTag("enable_dev_sandbox_button"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DivCyan)
                            ) {
                                Text("Enable Dev Sandbox Mode", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            } else if (isSandbox) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, DivCyan.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                        .testTag("dev_sandbox_banner_card"),
                    colors = CardDefaults.cardColors(containerColor = DivCardElevated)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "⚡ [DEVELOPMENT / SANDBOX MODE]",
                                color = DivCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Using offline wave synthesizer for development testing.",
                                color = DivTextMuted,
                                fontSize = 11.sp
                            )
                        }
                        TextButton(
                            onClick = { viewModel.toggleDevSandboxMode(false) },
                            modifier = Modifier.testTag("disable_sandbox_button")
                        ) {
                            Text("Switch to Prod", color = DivPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Song Description Prompt Input
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, DivBorder, RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = DivSurfaceDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = DivCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Describe Your Song Idea",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        if (prompt.isNotBlank()) {
                            Text(
                                text = "Clear",
                                color = DivTextMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.clickable { viewModel.promptState.value = "" }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { viewModel.promptState.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("song_prompt_input"),
                        placeholder = {
                            Text(
                                text = "e.g., An energetic Afrobeats anthem about triumph and celebration with heavy drums, joyful melodies, and uplifting choruses...",
                                color = DivTextMuted,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DivPurpleLight,
                            unfocusedBorderColor = DivBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = DivCard,
                            unfocusedContainerColor = DivCard
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${prompt.length} / 500 characters",
                        color = DivTextMuted,
                        fontSize = 10.sp,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }

        // Genre Selector Chips
        item {
            Column {
                Text(
                    text = "Select Music Genre",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    genresList.forEach { g ->
                        val isSelected = genre == g
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.genreState.value = g },
                            label = { Text(g, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DivPurple,
                                selectedLabelColor = Color.White,
                                containerColor = DivCard,
                                labelColor = DivTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) DivCyan else DivBorder
                            ),
                            modifier = Modifier.testTag("genre_chip_$g")
                        )
                    }
                }

                if (genre == "Custom") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customGenre,
                        onValueChange = { viewModel.customGenreState.value = it },
                        modifier = Modifier.fillMaxWidth().testTag("custom_genre_input"),
                        placeholder = { Text("Type custom genre (e.g., Afro-fusion, Synthwave)", color = DivTextMuted, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DivCyan,
                            unfocusedBorderColor = DivBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // Mood Selector Chips
        item {
            Column {
                Text(
                    text = "Song Mood & Vibe",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    moodsList.forEach { m ->
                        val isSelected = mood == m
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.moodState.value = m },
                            label = { Text(m, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DivPink,
                                selectedLabelColor = Color.White,
                                containerColor = DivCard,
                                labelColor = DivTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) DivPink else DivBorder
                            ),
                            modifier = Modifier.testTag("mood_chip_$m")
                        )
                    }
                }
            }
        }

        // Custom Mode Extended Options
        if (!isSimpleMode) {
            // Language Selection
            item {
                Column {
                    Text(
                        text = "Language / Dialect",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        languagesList.forEach { lang ->
                            val isSelected = language == lang
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.languageState.value = lang },
                                label = { Text(lang, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DivCyanDark,
                                    selectedLabelColor = Color.White,
                                    containerColor = DivCard,
                                    labelColor = DivTextSecondary
                                ),
                                modifier = Modifier.testTag("lang_chip_$lang")
                            )
                        }
                    }
                }
            }

            // Vocal Style Selection
            item {
                Column {
                    Text(
                        text = "Vocal Style & Tone",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        vocalStylesList.forEach { v ->
                            val isSelected = vocalStyle == v
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.vocalStyleState.value = v },
                                label = { Text(v, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DivPurpleDark,
                                    selectedLabelColor = Color.White,
                                    containerColor = DivCard,
                                    labelColor = DivTextSecondary
                                ),
                                modifier = Modifier.testTag("vocal_chip_$v")
                            )
                        }
                    }
                }
            }

            // Lyrics Generation / Custom Lyrics Studio
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, DivBorder, RoundedCornerShape(18.dp)),
                    colors = CardDefaults.cardColors(containerColor = DivSurfaceDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Lyrics Engine",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Tab options: AI Generate, Write My Own, Instrumental
                        val lyricTabs = listOf("AI Generate", "Write My Own", "Instrumental")
                        val selectedTabIndex = lyricTabs.indexOf(lyricsOption).coerceAtLeast(0)

                        TabRow(
                            selectedTabIndex = selectedTabIndex,
                            containerColor = DivCard,
                            contentColor = Color.White,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                    color = DivPink
                                )
                            }
                        ) {
                            lyricTabs.forEachIndexed { index, tabName ->
                                Tab(
                                    selected = selectedTabIndex == index,
                                    onClick = { viewModel.lyricsOptionState.value = tabName },
                                    text = { Text(tabName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                    modifier = Modifier.testTag("lyrics_tab_$tabName")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        when (lyricsOption) {
                            "AI Generate" -> {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Preview / AI Lyrics",
                                            color = DivCyan,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        OutlinedButton(
                                            onClick = { viewModel.generateLyricsWithAI() },
                                            modifier = Modifier.testTag("generate_lyrics_ai_button"),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DivCyan)
                                        ) {
                                            if (isAiWritingLyrics) {
                                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = DivCyan, strokeWidth = 2.dp)
                                            } else {
                                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Generate Lyrics", fontSize = 11.sp)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = lyricsText,
                                        onValueChange = { viewModel.lyricsTextState.value = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                            .testTag("ai_lyrics_preview_input"),
                                        placeholder = {
                                            Text(
                                                text = "Click 'Generate Lyrics' to have Gemini craft structured lyrics or let the generator create them during full song rendering...",
                                                color = DivTextMuted,
                                                fontSize = 12.sp
                                            )
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = DivPurpleLight,
                                            unfocusedBorderColor = DivBorder,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedContainerColor = DivCard,
                                            unfocusedContainerColor = DivCard
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    if (lyricsText.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(text = "AI Refine Tools:", color = DivTextSecondary, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf("Improve Rhymes" to "improve", "Expand Bridge" to "expand", "Shorten" to "shorten").forEach { (label, action) ->
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(DivCardElevated)
                                                        .border(1.dp, DivBorder, RoundedCornerShape(6.dp))
                                                        .clickable { viewModel.modifyLyrics(action) }
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(label, color = DivPurpleLight, fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            "Write My Own" -> {
                                Column {
                                    Text(
                                        text = "Insert structural tags into your custom lyrics:",
                                        color = DivTextSecondary,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf("[Intro]", "[Verse 1]", "[Chorus]", "[Verse 2]", "[Bridge]", "[Outro]").forEach { tag ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(DivPurple.copy(alpha = 0.3f))
                                                    .clickable { viewModel.lyricsTextState.value = "${lyricsText}\n$tag\n" }
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(tag, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = lyricsText,
                                        onValueChange = { viewModel.lyricsTextState.value = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp)
                                            .testTag("custom_lyrics_input"),
                                        placeholder = { Text("Write your original lyrics here...", color = DivTextMuted, fontSize = 12.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = DivCyan,
                                            unfocusedBorderColor = DivBorder,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedContainerColor = DivCard,
                                            unfocusedContainerColor = DivCard
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }

                            "Instrumental" -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(DivCard)
                                        .padding(14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "🎶 Instrumental Mode Active: The sound engine will generate pure musical harmonies, beats, and melodies with no vocals.",
                                        color = DivCyan,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Advanced Tuning Sliders: Creativity Level & Tempo
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, DivBorder, RoundedCornerShape(18.dp)),
                    colors = CardDefaults.cardColors(containerColor = DivSurfaceDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Advanced Studio Controls",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Creativity Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("AI Creativity (Temperature)", color = DivTextSecondary, fontSize = 12.sp)
                            Text("${(creativity * 100).toInt()}%", color = DivCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = creativity,
                            onValueChange = { viewModel.creativityLevel.value = it },
                            valueRange = 0.2f..1.0f,
                            colors = SliderDefaults.colors(thumbColor = DivCyan, activeTrackColor = DivCyan, inactiveTrackColor = Color(0xFF2E2E4E))
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Tempo selector
                        Text("Tempo (BPM)", color = DivTextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Slow (75 BPM)", "Medium (118 BPM)", "Fast (140 BPM)").forEach { t ->
                                val isSelected = tempo == t
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) DivPurple else DivCard)
                                        .border(1.dp, if (isSelected) DivCyan else DivBorder, RoundedCornerShape(8.dp))
                                        .clickable { viewModel.tempoState.value = t }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = t.substringBefore(" "),
                                        color = if (isSelected) Color.White else DivTextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // GENERATE BUTTON CTA
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        viewModel.startMusicGeneration(
                            onSuccess = { generatedSong ->
                                onSongGenerated(generatedSong.id)
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("generate_my_song_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = DivPurple),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Synthesizing Sound ($generationProgress%)...", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Generate My Song (1 Credit)",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Balance: ${currentUser?.credits ?: 10} Credits remaining • Instant WAV Generation",
                    color = DivTextMuted,
                    fontSize = 11.sp
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp)) // Miniplayer bottom padding
        }
    }
}
