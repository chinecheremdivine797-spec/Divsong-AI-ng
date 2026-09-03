package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
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
import com.example.data.local.entities.SongEntity
import com.example.ui.components.SongCard
import com.example.ui.theme.DivBackground
import com.example.ui.theme.DivBorder
import com.example.ui.theme.DivCard
import com.example.ui.theme.DivCardElevated
import com.example.ui.theme.DivCyan
import com.example.ui.theme.DivGradientBrand
import com.example.ui.theme.DivPink
import com.example.ui.theme.DivPurple
import com.example.ui.theme.DivPurpleLight
import com.example.ui.theme.DivSurfaceDark
import com.example.ui.theme.DivTextMuted
import com.example.ui.theme.DivTextSecondary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun LibraryScreen(
    viewModel: MainViewModel,
    onNavigateToCreate: () -> Unit,
    onSongClick: (String) -> Unit
) {
    val userSongs by viewModel.userSongs.collectAsStateWithLifecycle()
    val currentPlayingSong by viewModel.currentPlayingSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var sortOrder by remember { mutableStateOf("Newest") } // "Newest", "Oldest", "A-Z"

    var songToDelete by remember { mutableStateOf<SongEntity?>(null) }
    var songToRename by remember { mutableStateOf<SongEntity?>(null) }
    var renameTitleInput by remember { mutableStateOf("") }

    // Filter and Sort Logic
    val filteredSongs = userSongs.filter { song ->
        val matchesSearch = song.title.contains(searchQuery, ignoreCase = true) ||
                song.genre.contains(searchQuery, ignoreCase = true) ||
                song.mood.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "All" -> true
            "Favorites" -> song.isFavorite
            "Public" -> song.visibility == "public"
            "Private" -> song.visibility == "private"
            else -> song.genre.equals(selectedFilter, ignoreCase = true)
        }

        matchesSearch && matchesFilter
    }.sortedWith { a, b ->
        when (sortOrder) {
            "Newest" -> b.createdAt.compareTo(a.createdAt)
            "Oldest" -> a.createdAt.compareTo(b.createdAt)
            "A-Z" -> a.title.compareTo(b.title, ignoreCase = true)
            else -> 0
        }
    }

    // Delete Confirmation Dialog
    if (songToDelete != null) {
        AlertDialog(
            onDismissRequest = { songToDelete = null },
            title = { Text("Delete Song", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to permanently delete \"${songToDelete?.title}\"? This action cannot be undone.", color = DivTextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        songToDelete?.let { viewModel.deleteSong(it.id) }
                        songToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { songToDelete = null }) {
                    Text("Cancel", color = DivTextSecondary)
                }
            },
            containerColor = DivSurfaceDark,
            modifier = Modifier.border(1.dp, DivBorder, RoundedCornerShape(16.dp))
        )
    }

    // Rename Dialog
    if (songToRename != null) {
        AlertDialog(
            onDismissRequest = { songToRename = null },
            title = { Text("Rename Song", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameTitleInput,
                    onValueChange = { renameTitleInput = it },
                    modifier = Modifier.fillMaxWidth().testTag("rename_song_input"),
                    label = { Text("Song Title") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DivCyan,
                        unfocusedBorderColor = DivBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameTitleInput.isNotBlank()) {
                            songToRename?.let { viewModel.renameSong(it.id, renameTitleInput) }
                            songToRename = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DivPurple)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { songToRename = null }) {
                    Text("Cancel", color = DivTextSecondary)
                }
            },
            containerColor = DivSurfaceDark,
            modifier = Modifier.border(1.dp, DivBorder, RoundedCornerShape(16.dp))
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DivBackground)
            .padding(horizontal = 16.dp)
            .testTag("library_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "My Song Library",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp
                    )
                    Text(
                        text = "${userSongs.size} tracks created",
                        color = DivTextSecondary,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = onNavigateToCreate,
                    colors = ButtonDefaults.buttonColors(containerColor = DivPurple),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("library_create_new_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Song", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Search Field
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("library_search_input"),
                placeholder = { Text("Search your songs by title, genre, mood...", color = DivTextMuted, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DivTextSecondary) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = DivTextSecondary)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DivCyan,
                    unfocusedBorderColor = DivBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = DivSurfaceDark,
                    unfocusedContainerColor = DivSurfaceDark
                ),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
        }

        // Filter & Sort Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Favorites", "Afrobeats", "Amapiano", "Hip-hop", "Gospel", "R&B", "Public", "Private").forEach { filterName ->
                        val isSelected = selectedFilter == filterName
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = filterName },
                            label = { Text(filterName, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
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
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Sort, contentDescription = null, tint = DivTextMuted, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Sort: $sortOrder",
                        color = DivCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable {
                            sortOrder = when (sortOrder) {
                                "Newest" -> "Oldest"
                                "Oldest" -> "A-Z"
                                else -> "Newest"
                            }
                        }
                    )
                }
            }
        }

        // Empty State or Song List
        if (filteredSongs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, DivBorder, RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = DivSurfaceDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(DivCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LibraryMusic,
                                contentDescription = null,
                                tint = DivCyan,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (userSongs.isEmpty()) "You haven't created any songs yet" else "No songs matching filter",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Turn your creative ideas into full music tracks in seconds.",
                            fontSize = 12.sp,
                            color = DivTextSecondary
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = onNavigateToCreate,
                            colors = ButtonDefaults.buttonColors(containerColor = DivPurple),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Create Your First Song", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        } else {
            items(filteredSongs, key = { it.id }) { song ->
                SongCard(
                    song = song,
                    isPlaying = isPlaying && currentPlayingSong?.id == song.id,
                    onPlayClick = { viewModel.togglePlayPause(song) },
                    onClick = { onSongClick(song.id) },
                    onFavoriteToggle = { viewModel.toggleFavorite(song) },
                    onDeleteClick = { songToDelete = song },
                    onShareClick = { viewModel.showToast("Sharing \"${song.title}\" link copied!") }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
