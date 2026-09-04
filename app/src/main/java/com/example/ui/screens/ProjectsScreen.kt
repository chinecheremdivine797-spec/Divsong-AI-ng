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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.ProjectEntity
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProjectsScreen(
    viewModel: MainViewModel,
    onOpenProjectInStudio: (ProjectEntity) -> Unit,
    onOpenProjectInVoice: (ProjectEntity) -> Unit,
    onOpenProjectInVideo: (ProjectEntity) -> Unit,
    onCreateNewProject: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val projects by viewModel.userProjects.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Drafts", "Ready", "Exported"

    // Dialog state
    var projectToRename by remember { mutableStateOf<ProjectEntity?>(null) }
    var renameInput by remember { mutableStateOf("") }
    var projectToDelete by remember { mutableStateOf<ProjectEntity?>(null) }

    val filteredProjects = projects.filter { project ->
        val matchesQuery = project.name.contains(searchQuery, ignoreCase = true) ||
                project.genre.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "Drafts" -> project.status == "draft"
            "Ready" -> project.status == "ready"
            "Exported" -> project.status == "exported"
            else -> true
        }
        matchesQuery && matchesFilter
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DivBackground)
            .testTag("projects_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("My Studio Projects", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("${projects.size} active studio sessions", color = DivTextMuted, fontSize = 13.sp)
                }

                Button(
                    onClick = onCreateNewProject,
                    colors = ButtonDefaults.buttonColors(containerColor = DivCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(38.dp).testTag("new_project_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search songs, lyrics, or sessions...", color = DivTextMuted, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DivTextSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = DivCyan,
                    unfocusedBorderColor = DivBorder,
                    focusedContainerColor = DivSurfaceDark,
                    unfocusedContainerColor = DivSurfaceDark
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("All", "Drafts", "Ready", "Exported")
                filters.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DivCyan.copy(alpha = 0.25f),
                            selectedLabelColor = DivCyan,
                            containerColor = DivSurfaceDark,
                            labelColor = DivTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (selectedFilter == filter) DivCyan else DivBorder,
                            enabled = true,
                            selected = selectedFilter == filter
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Project List
            if (filteredProjects.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(DivSurfaceDark)
                                .border(1.dp, DivBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = DivTextMuted, modifier = Modifier.size(32.dp))
                        }
                        Text(
                            text = if (searchQuery.isNotBlank()) "No projects match '$searchQuery'" else "No projects created yet",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Create songs, record vocals, or write lyrics to start your first session.",
                            color = DivTextMuted,
                            fontSize = 13.sp
                        )
                        Button(
                            onClick = onCreateNewProject,
                            colors = ButtonDefaults.buttonColors(containerColor = DivCyan),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Create First Project", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredProjects, key = { it.id }) { project ->
                        ProjectCard(
                            project = project,
                            onOpenStudio = { onOpenProjectInStudio(project) },
                            onOpenVoice = { onOpenProjectInVoice(project) },
                            onOpenVideo = { onOpenProjectInVideo(project) },
                            onRename = {
                                projectToRename = project
                                renameInput = project.name
                            },
                            onDuplicate = {
                                coroutineScope.launch {
                                    viewModel.duplicateProject(project)
                                    viewModel.showToast("Project duplicated")
                                }
                            },
                            onDelete = {
                                projectToDelete = project
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Rename Dialog
        if (projectToRename != null) {
            AlertDialog(
                onDismissRequest = { projectToRename = null },
                title = { Text("Rename Project", color = Color.White) },
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
                            val id = projectToRename?.id ?: return@Button
                            coroutineScope.launch {
                                viewModel.renameProject(id, renameInput)
                                projectToRename = null
                                viewModel.showToast("Project renamed")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DivCyan)
                    ) {
                        Text("Save", color = Color.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { projectToRename = null }) {
                        Text("Cancel", color = DivTextMuted)
                    }
                },
                containerColor = DivSurfaceDark
            )
        }

        // Delete Confirm Dialog
        if (projectToDelete != null) {
            AlertDialog(
                onDismissRequest = { projectToDelete = null },
                title = { Text("Delete Project?", color = Color.White) },
                text = { Text("Are you sure you want to delete '${projectToDelete?.name}'? This cannot be undone.", color = DivTextSecondary) },
                confirmButton = {
                    Button(
                        onClick = {
                            val id = projectToDelete?.id ?: return@Button
                            coroutineScope.launch {
                                viewModel.deleteProject(id)
                                projectToDelete = null
                                viewModel.showToast("Project deleted")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { projectToDelete = null }) {
                        Text("Cancel", color = DivTextMuted)
                    }
                },
                containerColor = DivSurfaceDark
            )
        }
    }
}

@Composable
fun ProjectCard(
    project: ProjectEntity,
    onOpenStudio: () -> Unit,
    onOpenVoice: () -> Unit,
    onOpenVideo: () -> Unit,
    onRename: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()) }
    val formattedDate = remember(project.updatedAt) { dateFormat.format(Date(project.updatedAt)) }

    Card(
        colors = CardDefaults.cardColors(containerColor = DivSurfaceDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DivBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Title, Status Badge, Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = project.name,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when (project.status) {
                                        "ready" -> Color(0xFF06D6A0).copy(alpha = 0.2f)
                                        "exported" -> DivCyan.copy(alpha = 0.2f)
                                        else -> DivPink.copy(alpha = 0.2f)
                                    }
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = project.status.uppercase(),
                                color = when (project.status) {
                                    "ready" -> Color(0xFF06D6A0)
                                    "exported" -> DivCyan
                                    else -> DivPink
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${project.genre} • $formattedDate",
                        color = DivTextMuted,
                        fontSize = 11.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onRename, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Rename", tint = DivTextSecondary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDuplicate, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = DivTextSecondary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Component Badges
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (project.lyrics.isNotBlank()) {
                    ComponentPill(icon = Icons.Default.MusicNote, label = "Lyrics Ready", tint = DivCyan)
                }
                if (project.vocalTrackUrl.isNotBlank()) {
                    ComponentPill(icon = Icons.Default.Mic, label = "Vocals Recorded", tint = DivPink)
                }
                if (project.masterAudioUrl.isNotBlank()) {
                    ComponentPill(icon = Icons.Default.GraphicEq, label = "Mastered Audio", tint = DivPurpleLight)
                }
                if (project.videoUrl.isNotBlank()) {
                    ComponentPill(icon = Icons.Default.Movie, label = "Music Video", tint = Color(0xFFFFD166))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Studio Launch Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenStudio,
                    modifier = Modifier.weight(1f).height(38.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DivCyan),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("DAW Studio", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onOpenVoice,
                    modifier = Modifier.weight(1f).height(38.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DivPink),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Voice Studio", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onOpenVideo,
                    modifier = Modifier.weight(1f).height(38.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD166)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Video Studio", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ComponentPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(tint.copy(alpha = 0.15f))
            .border(1.dp, tint.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, color = tint, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}
