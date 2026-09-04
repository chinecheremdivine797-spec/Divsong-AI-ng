package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Upload
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.DivLogo
import com.example.ui.components.SamplePromptCard
import com.example.ui.components.SongCard
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.DivBackground
import com.example.ui.theme.DivBorder
import com.example.ui.theme.DivCyan
import com.example.ui.theme.DivGradientBrand
import com.example.ui.theme.DivPink
import com.example.ui.theme.DivPurple
import com.example.ui.theme.DivPurpleLight
import com.example.ui.theme.DivSurfaceDark
import com.example.ui.theme.DivTextMuted
import com.example.ui.theme.DivTextSecondary
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToLyrics: () -> Unit,
    onNavigateToStudio: () -> Unit,
    onNavigateToVoice: () -> Unit,
    onNavigateToVideo: () -> Unit,
    onNavigateToProjects: () -> Unit,
    onNavigateToExplore: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToPricing: () -> Unit,
    onSongClick: (String) -> Unit
) {
    val publicSongs by viewModel.publicSongs.collectAsStateWithLifecycle()
    val userProjects by viewModel.userProjects.collectAsStateWithLifecycle()
    val currentPlayingSong by viewModel.currentPlayingSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val userPlan = (currentUser?.planId ?: "free").uppercase()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DivBackground)
            .padding(horizontal = 16.dp)
            .testTag("home_screen"),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Top Header Bar
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DivLogo(size = 36.dp, showTagline = false)

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Plan Tier Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                when (userPlan) {
                                    "PRO" -> DivPurple.copy(alpha = 0.3f)
                                    "STUDIO" -> DivPink.copy(alpha = 0.3f)
                                    else -> DivSurfaceDark
                                }
                            )
                            .border(1.dp, DivBorder, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = userPlan,
                            color = when (userPlan) {
                                "PRO" -> DivCyan
                                "STUDIO" -> DivPink
                                else -> DivTextSecondary
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Credits Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(DivSurfaceDark)
                            .border(1.dp, DivBorder, RoundedCornerShape(20.dp))
                            .clickable { onNavigateToPricing() }
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                            .testTag("user_credits_pill"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = DivCyan, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("${currentUser?.credits ?: 10} Credits", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Hero Studio Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.5.dp, Brush.horizontalGradient(listOf(DivPurple, DivPink, DivCyan)), RoundedCornerShape(22.dp))
                    .testTag("hero_section"),
                colors = CardDefaults.cardColors(containerColor = DivSurfaceDark)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(DivPurple.copy(alpha = 0.25f))
                            .border(1.dp, DivPurpleLight.copy(alpha = 0.5f), RoundedCornerShape(30.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "DIV SONG AI • CREATIVE STUDIO",
                            color = DivCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Turn Your Creative Vision Into Music & Video",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Complete AI Music Production, Vocal Studio, 5-Track Multitrack DAW, and 4K Music Videos in one app.",
                        fontSize = 12.sp,
                        color = DivTextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    WaveformVisualizer(
                        isPlaying = isPlaying,
                        progress = 0.45f,
                        height = 32.dp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onNavigateToCreate,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("hero_create_music_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = DivPurple),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create Song", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = onNavigateToStudio,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                brush = Brush.horizontalGradient(listOf(DivCyan, DivPurpleLight))
                            )
                        ) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = DivCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Studio DAW", color = DivCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Quick Access Studio Dashboard Hub
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Creative Studio Hub",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    maxItemsInEachRow = 3
                ) {
                    StudioHubTile(
                        title = "Create Song",
                        subtitle = "Full AI Generation",
                        icon = Icons.Default.MusicNote,
                        color = DivCyan,
                        onClick = onNavigateToCreate,
                        modifier = Modifier.weight(1f)
                    )
                    StudioHubTile(
                        title = "AI Lyrics",
                        subtitle = "Songwriter Tool",
                        icon = Icons.Default.AutoAwesome,
                        color = DivPink,
                        onClick = onNavigateToLyrics,
                        modifier = Modifier.weight(1f)
                    )
                    StudioHubTile(
                        title = "Voice Studio",
                        subtitle = "Record & Clean",
                        icon = Icons.Default.Mic,
                        color = DivPurpleLight,
                        onClick = onNavigateToVoice,
                        modifier = Modifier.weight(1f)
                    )
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    maxItemsInEachRow = 3
                ) {
                    StudioHubTile(
                        title = "DAW Studio",
                        subtitle = "5-Track Mixer",
                        icon = Icons.Default.GraphicEq,
                        color = Color(0xFF06D6A0),
                        onClick = onNavigateToStudio,
                        modifier = Modifier.weight(1f)
                    )
                    StudioHubTile(
                        title = "Music Video",
                        subtitle = "AI Visual Clip",
                        icon = Icons.Default.Movie,
                        color = Color(0xFFFFD166),
                        onClick = onNavigateToVideo,
                        modifier = Modifier.weight(1f)
                    )
                    StudioHubTile(
                        title = "My Projects",
                        subtitle = "${userProjects.size} sessions",
                        icon = Icons.Default.Folder,
                        color = DivCyan,
                        onClick = onNavigateToProjects,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Recent Projects Section (if any exist)
        if (userProjects.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Projects & Drafts",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "View All (${userProjects.size})",
                        color = DivCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onNavigateToProjects() }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    userProjects.take(5).forEach { project ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DivSurfaceDark),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .width(200.dp)
                                .border(1.dp, DivBorder, RoundedCornerShape(14.dp))
                                .clickable { onNavigateToStudio() }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = project.status.uppercase(),
                                        color = if (project.status == "ready") Color(0xFF06D6A0) else DivPink,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(Icons.Default.Folder, contentDescription = null, tint = DivTextMuted, modifier = Modifier.size(14.dp))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(project.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                                Text(project.genre, color = DivTextMuted, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Tap to resume editing", color = DivCyan, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
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
                Text("Sample Inspiration Prompts", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("See More", color = DivCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { onNavigateToCreate() })
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

        // Trending Songs Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Trending AI Creations", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("View All", color = DivCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { onNavigateToExplore() })
            }
        }

        items(publicSongs.take(3)) { song ->
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
private fun StudioHubTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DivSurfaceDark),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .height(96.dp)
            .border(1.dp, DivBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }

            Column {
                Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(subtitle, color = DivTextMuted, fontSize = 10.sp, maxLines = 1)
            }
        }
    }
}
