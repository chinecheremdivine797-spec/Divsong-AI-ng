package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.DivLogo
import com.example.ui.components.SamplePromptCard
import com.example.ui.components.SongCard
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.DivBackground
import com.example.ui.theme.DivBorder
import com.example.ui.theme.DivCard
import com.example.ui.theme.DivCyan
import com.example.ui.theme.DivGradientBrand
import com.example.ui.theme.DivGradientPurpleCyan
import com.example.ui.theme.DivPink
import com.example.ui.theme.DivPurple
import com.example.ui.theme.DivPurpleLight
import com.example.ui.theme.DivSurfaceDark
import com.example.ui.theme.DivTextMuted
import com.example.ui.theme.DivTextSecondary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToExplore: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToPricing: () -> Unit,
    onSongClick: (String) -> Unit
) {
    val publicSongs by viewModel.publicSongs.collectAsStateWithLifecycle()
    val currentPlayingSong by viewModel.currentPlayingSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DivBackground)
            .padding(horizontal = 16.dp)
            .testTag("home_screen"),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DivLogo(size = 36.dp, showTagline = false)
                // Credits Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(DivSurfaceDark)
                        .border(1.dp, DivBorder, RoundedCornerShape(20.dp))
                        .clickable { onNavigateToPricing() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("user_credits_pill"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = DivCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${currentUser?.credits ?: 10} Credits",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Hero Section Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.5.dp, Brush.horizontalGradient(listOf(DivPurple, DivPink, DivCyan)), RoundedCornerShape(24.dp))
                    .testTag("hero_section"),
                colors = CardDefaults.cardColors(containerColor = DivSurfaceDark)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Tagline Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(DivPurple.copy(alpha = 0.25f))
                            .border(1.dp, DivPurpleLight.copy(alpha = 0.5f), RoundedCornerShape(30.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "CREATE YOUR SOUND. BRING YOUR IDEAS TO LIFE.",
                            color = DivCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Turn Your Ideas Into Music With AI",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 30.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Describe the song you imagine, choose your style, and let DIV SONG AI help bring your music idea to life.",
                        fontSize = 13.sp,
                        color = DivTextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Animated Waveform in Hero
                    WaveformVisualizer(
                        isPlaying = isPlaying,
                        progress = 0.45f,
                        height = 36.dp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onNavigateToCreate,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("hero_create_music_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = DivPurple),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create Music", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = onNavigateToExplore,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("hero_explore_songs_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(brush = Brush.horizontalGradient(listOf(DivCyan, DivPurpleLight)))
                        ) {
                            Text("Explore Songs", color = DivCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Quick Inspiration Prompts Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sample Inspiration Prompts",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Text(
                    text = "See More",
                    color = DivCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToCreate() }
                )
            }
        }

        item {
            SamplePromptCard(
                category = "Afrobeats",
                promptText = "Create an uplifting Afrobeats song about a young person from Lagos chasing their dreams and refusing to give up.",
                onClick = {
                    viewModel.populateSamplePrompt(
                        "Afrobeats",
                        "Energetic",
                        "Create an uplifting Afrobeats song about a young person from Lagos chasing their dreams and refusing to give up."
                    )
                    onNavigateToCreate()
                }
            )
        }

        item {
            SamplePromptCard(
                category = "Amapiano",
                promptText = "Create a soulful midnight Amapiano groove with warm piano chords, deep rolling log drums, and relaxing vocals.",
                onClick = {
                    viewModel.populateSamplePrompt(
                        "Amapiano",
                        "Calm",
                        "Create a soulful midnight Amapiano groove with warm piano chords, deep rolling log drums, and relaxing vocals."
                    )
                    onNavigateToCreate()
                }
            )
        }

        item {
            SamplePromptCard(
                category = "Hip-hop",
                promptText = "Create an energetic hip-hop song about ambition, discipline, and building a better future.",
                onClick = {
                    viewModel.populateSamplePrompt(
                        "Hip-hop",
                        "Inspirational",
                        "Create an energetic hip-hop song about ambition, discipline, and building a better future."
                    )
                    onNavigateToCreate()
                }
            )
        }

        // HOW IT WORKS Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DivSurfaceDark)
                    .border(1.dp, DivBorder, RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Text(
                    text = "HOW IT WORKS",
                    color = DivCyan,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                HowItWorksStepItem(
                    stepNumber = "1",
                    title = "Describe Your Song",
                    description = "Tell the AI what type of song you want in plain words.",
                    icon = Icons.Default.AutoAwesome
                )

                Spacer(modifier = Modifier.height(12.dp))

                HowItWorksStepItem(
                    stepNumber = "2",
                    title = "Customize Your Sound",
                    description = "Choose genre, mood, language, vocals, and song structure.",
                    icon = Icons.Default.Tune
                )

                Spacer(modifier = Modifier.height(12.dp))

                HowItWorksStepItem(
                    stepNumber = "3",
                    title = "Generate",
                    description = "The neural music engine synthesizes lyrics, chords, and audio.",
                    icon = Icons.Default.MusicNote
                )

                Spacer(modifier = Modifier.height(12.dp))

                HowItWorksStepItem(
                    stepNumber = "4",
                    title = "Listen and Manage",
                    description = "Play, save, organize, favorite, and export your creations.",
                    icon = Icons.Default.Headphones
                )
            }
        }

        // Featured Trending Songs Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Trending AI Creations",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Text(
                    text = "View All",
                    color = DivCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToExplore() }
                )
            }
        }

        items(publicSongs.take(4)) { song ->
            SongCard(
                song = song,
                isPlaying = isPlaying && currentPlayingSong?.id == song.id,
                onPlayClick = { viewModel.togglePlayPause(song) },
                onClick = { onSongClick(song.id) },
                onFavoriteToggle = { viewModel.toggleFavorite(song) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp)) // Miniplayer padding
        }
    }
}

@Composable
private fun HowItWorksStepItem(
    stepNumber: String,
    title: String,
    description: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(DivGradientBrand),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = description,
                color = DivTextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}
