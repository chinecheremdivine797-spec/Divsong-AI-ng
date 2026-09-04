package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ai.GeminiLyricsService
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiLyricsScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    onUseInCreator: (String) -> Unit,
    onSendToVocalStudio: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var ideaPrompt by remember { mutableStateOf("Write an emotional Afrobeats song about a young Nigerian trying to succeed.") }
    var selectedGenre by remember { mutableStateOf("Afrobeats") }
    var selectedMood by remember { mutableStateOf("Inspiring") }
    var selectedTopic by remember { mutableStateOf("Success & Hustle") }
    var selectedLanguage by remember { mutableStateOf("English & Pidgin") }
    var selectedStructure by remember { mutableStateOf("Intro - Verse - Chorus - Verse - Chorus - Bridge - Outro") }
    var isExplicitClean by remember { mutableStateOf(true) } // true = Clean
    var selectedLength by remember { mutableStateOf("Standard (3 min)") }

    var lyricsText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var currentAiAction by remember { mutableStateOf("") }

    val genres = listOf("Afrobeats", "Afro-pop", "Amapiano", "Gospel", "Hip-hop", "R&B", "Pop", "Highlife", "Reggae", "Lo-fi")
    val moods = listOf("Inspiring", "Emotional", "Energetic", "Victorious", "Romantic", "Reflective", "Spiritual")
    val topics = listOf("Success & Hustle", "Overcoming Odds", "Gratitude & Grace", "True Love", "Celebration", "Faith")
    val languages = listOf("English & Pidgin", "English", "Yoruba & English", "Igbo & English", "French", "Spanish")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DivBackground)
            .testTag("ai_lyrics_screen")
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
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("lyrics_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "AI Lyrics Writer",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Professional songwriting & lyrical composition",
                        color = DivTextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            if (lyricsText.isNotBlank()) {
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("DIV Song AI Lyrics", lyricsText)
                        clipboard.setPrimaryClip(clip)
                        viewModel.showToast("Lyrics copied to clipboard!")
                    },
                    modifier = Modifier.testTag("copy_lyrics_button")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = DivCyan)
                }
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
            // Idea Input Card
            Card(
                colors = CardDefaults.cardColors(containerColor = DivSurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DivBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = DivCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Song Idea & Story Concept", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = ideaPrompt,
                        onValueChange = { ideaPrompt = it },
                        placeholder = { Text("e.g. Write an emotional Afrobeats song about a young Nigerian trying to succeed.", color = DivTextMuted, fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 90.dp)
                            .testTag("lyrics_idea_input"),
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
                }
            }

            // Style & Linguistic Controls
            Card(
                colors = CardDefaults.cardColors(containerColor = DivSurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DivBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Musical Style & Structure", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)

                    // Genres
                    Text("Genre", color = DivTextSecondary, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        genres.forEach { genre ->
                            FilterChip(
                                selected = selectedGenre == genre,
                                onClick = { selectedGenre = genre },
                                label = { Text(genre, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DivCyan.copy(alpha = 0.2f),
                                    selectedLabelColor = DivCyan,
                                    containerColor = DivBackground,
                                    labelColor = DivTextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (selectedGenre == genre) DivCyan else DivBorder,
                                    enabled = true,
                                    selected = selectedGenre == genre
                                )
                            )
                        }
                    }

                    // Moods
                    Text("Mood & Vibe", color = DivTextSecondary, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        moods.forEach { mood ->
                            FilterChip(
                                selected = selectedMood == mood,
                                onClick = { selectedMood = mood },
                                label = { Text(mood, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DivPink.copy(alpha = 0.2f),
                                    selectedLabelColor = DivPink,
                                    containerColor = DivBackground,
                                    labelColor = DivTextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (selectedMood == mood) DivPink else DivBorder,
                                    enabled = true,
                                    selected = selectedMood == mood
                                )
                            )
                        }
                    }

                    // Language
                    Text("Language / Dialect", color = DivTextSecondary, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        languages.forEach { lang ->
                            FilterChip(
                                selected = selectedLanguage == lang,
                                onClick = { selectedLanguage = lang },
                                label = { Text(lang, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DivPurpleLight.copy(alpha = 0.25f),
                                    selectedLabelColor = DivPurpleLight,
                                    containerColor = DivBackground,
                                    labelColor = DivTextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (selectedLanguage == lang) DivPurpleLight else DivBorder,
                                    enabled = true,
                                    selected = selectedLanguage == lang
                                )
                            )
                        }
                    }

                    // Clean Lyrics Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Radio Clean Lyrics", color = Color.White, fontSize = 13.sp)
                            Text("Ensure lyrics are suitable for all audiences", color = DivTextMuted, fontSize = 11.sp)
                        }
                        Switch(
                            checked = isExplicitClean,
                            onCheckedChange = { isExplicitClean = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DivCyan,
                                checkedTrackColor = DivCyan.copy(alpha = 0.3f),
                                uncheckedThumbColor = DivTextMuted,
                                uncheckedTrackColor = DivBorder
                            )
                        )
                    }
                }
            }

            // Primary Generate Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        isGenerating = true
                        currentAiAction = "Writing original lyrics..."
                        try {
                            val promptWithStyle = "$ideaPrompt (Topic: $selectedTopic, Clean: $isExplicitClean)"
                            val generated = GeminiLyricsService.generateLyrics(
                                prompt = promptWithStyle,
                                genre = selectedGenre,
                                mood = selectedMood,
                                language = selectedLanguage,
                                vocalStyle = "Lead vocal",
                                structure = selectedStructure
                            )
                            lyricsText = generated
                            viewModel.showToast("Lyrics generated successfully!")
                        } catch (e: Exception) {
                            viewModel.showToast("Error generating lyrics: ${e.message}")
                        } finally {
                            isGenerating = false
                            currentAiAction = ""
                        }
                    }
                },
                enabled = !isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("generate_lyrics_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DivGradientBrand, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isGenerating && currentAiAction.startsWith("Writing")) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Generating Lyrics...", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate Lyrics", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }

            // AI Refining Tools Toolbar (Visible when lyrics exist)
            if (lyricsText.isNotBlank()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DivSurfaceDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DivBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("AI Lyric Polish & Revision", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val refineActions = listOf(
                                Triple("Rewrite", "rewrite", Icons.Default.Refresh),
                                Triple("Make It Deeper", "deeper", Icons.Default.AutoAwesome),
                                Triple("Make It Catchier", "catchier", Icons.Default.MusicNote),
                                Triple("Stronger Chorus", "stronger_chorus", Icons.Default.Lightbulb),
                                Triple("Change Rhyme", "rhyme", Icons.Default.Edit)
                            )

                            refineActions.forEach { (label, actionKey, icon) ->
                                OutlinedButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            isGenerating = true
                                            currentAiAction = label
                                            try {
                                                val modified = GeminiLyricsService.modifyLyrics(
                                                    currentLyrics = lyricsText,
                                                    action = actionKey,
                                                    genre = selectedGenre,
                                                    mood = selectedMood
                                                )
                                                lyricsText = modified
                                                viewModel.showToast("$label applied!")
                                            } catch (e: Exception) {
                                                viewModel.showToast("Refining error: ${e.message}")
                                            } finally {
                                                isGenerating = false
                                                currentAiAction = ""
                                            }
                                        }
                                    },
                                    enabled = !isGenerating,
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = DivCyan,
                                        containerColor = DivBackground
                                    ),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(DivBorder)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    if (isGenerating && currentAiAction == label) {
                                        CircularProgressIndicator(color = DivCyan, modifier = Modifier.size(14.dp), strokeWidth = 1.5.dp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                    } else {
                                        Icon(icon, contentDescription = null, tint = DivCyan, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(label, fontSize = 12.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }

                // Interactive Lyrics Editor Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = DivSurfaceDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DivBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Generated Lyrics (Editable)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("${lyricsText.lines().size} lines", color = DivTextMuted, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = lyricsText,
                            onValueChange = { lyricsText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 280.dp, max = 500.dp)
                                .testTag("lyrics_editor_field"),
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

                        Spacer(modifier = Modifier.height(14.dp))

                        // Studio Integration Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.lyricsTextState.value = lyricsText
                                    viewModel.promptState.value = ideaPrompt
                                    onUseInCreator(lyricsText)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("use_in_song_creator_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = DivCyan),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Song Creator", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    onSendToVocalStudio(lyricsText)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("use_in_vocal_studio_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = DivPurpleLight),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Vocal Studio", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    val user = viewModel.currentUser.value
                                    if (user != null) {
                                        viewModel.createProject(
                                            name = ideaPrompt.take(28).ifBlank { "Lyrics Project" },
                                            genre = selectedGenre,
                                            mood = selectedMood,
                                            lyrics = lyricsText
                                        )
                                        viewModel.showToast("Saved to My Projects!")
                                    } else {
                                        viewModel.showToast("Please log in to save projects")
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("save_lyrics_project_btn"),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = DivPink,
                                containerColor = DivBackground
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(DivBorder)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = DivPink, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save as Project", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
