package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
fun ProfileSettingsScreen(
    viewModel: MainViewModel,
    onNavigateToPricing: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onLogout: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val userSongs by viewModel.userSongs.collectAsStateWithLifecycle()

    var fullName by remember(currentUser) { mutableStateOf(currentUser?.fullName ?: "") }
    var username by remember(currentUser) { mutableStateOf(currentUser?.username ?: "") }
    var bio by remember(currentUser) { mutableStateOf(currentUser?.bio ?: "") }
    var isPublicProfile by remember(currentUser) { mutableStateOf(currentUser?.isPublicProfile ?: true) }
    var allowPublicSongs by remember(currentUser) { mutableStateOf(currentUser?.allowPublicSongs ?: true) }

    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete Account", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to permanently delete your account and all generated songs? This action cannot be reversed.", color = DivTextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAccount {
                            showDeleteAccountDialog = false
                            onLogout()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Permanently Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
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
            .testTag("profile_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Profile & Studio Settings",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp
            )
        }

        // Profile Avatar & Identity Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, DivBorder, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = DivSurfaceDark)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(DivGradientBrand),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (currentUser?.fullName?.take(1) ?: "D").uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentUser?.fullName ?: "Creator",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = Color.White
                                )
                                if (currentUser?.role == "admin") {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(DivPink)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("ADMIN", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                            Text(
                                text = "@${currentUser?.username ?: "creator"} • ${currentUser?.email}",
                                color = DivTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats row: Credits, Songs, Plan
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DivCard)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${currentUser?.credits ?: 0}", color = DivCyan, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                Text("Credits", color = DivTextSecondary, fontSize = 11.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DivCard)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${userSongs.size}", color = DivPink, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                Text("Songs", color = DivTextSecondary, fontSize = 11.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DivCard)
                                .clickable { onNavigateToPricing() }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = (currentUser?.planId ?: "Free").replaceFirstChar { it.uppercase() },
                                    color = DivPurpleLight,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                                Text("Upgrade →", color = DivCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Profile Editor Fields
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, DivBorder, RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = DivSurfaceDark)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Edit Profile Details", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        modifier = Modifier.fillMaxWidth().testTag("profile_name_input"),
                        label = { Text("Display Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DivCyan,
                            unfocusedBorderColor = DivBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier.fillMaxWidth().testTag("profile_username_input"),
                        label = { Text("Username") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DivCyan,
                            unfocusedBorderColor = DivBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        modifier = Modifier.fillMaxWidth().height(90.dp).testTag("profile_bio_input"),
                        label = { Text("Bio / Producer Profile") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DivCyan,
                            unfocusedBorderColor = DivBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Button(
                        onClick = {
                            viewModel.updateProfile(fullName, username, bio, isPublicProfile, allowPublicSongs)
                        },
                        modifier = Modifier.fillMaxWidth().testTag("save_profile_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = DivPurple)
                    ) {
                        Text("Save Profile Changes")
                    }
                }
            }
        }

        // Privacy & Preferences
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, DivBorder, RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = DivSurfaceDark)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Privacy & Discovery", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Public Creator Profile", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Allow others to view your producer profile in Explore", color = DivTextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = isPublicProfile,
                            onCheckedChange = { isPublicProfile = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = DivPurple)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Default Songs to Public", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Automatically publish generated songs to Explore feed", color = DivTextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = allowPublicSongs,
                            onCheckedChange = { allowPublicSongs = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = DivCyan)
                        )
                    }
                }
            }
        }

        // Navigation Quick Links (Admin, Help, Pricing)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (currentUser?.role == "admin") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onNavigateToAdmin() }
                            .testTag("admin_dashboard_link"),
                        colors = CardDefaults.cardColors(containerColor = DivCardElevated)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = DivPink)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Admin Control Console", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Manage users, songs, plans, and reports", color = DivTextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onNavigateToHelp() }
                        .testTag("help_support_link"),
                    colors = CardDefaults.cardColors(containerColor = DivCard)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.HelpOutline, contentDescription = null, tint = DivCyan)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Help & Support Center", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("FAQs, guidelines, contact support, legal policies", color = DivTextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Logout & Delete Account
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.authRepo.logout()
                        onLogout()
                    },
                    modifier = Modifier.weight(1f).testTag("logout_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Out", color = Color.White, fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { showDeleteAccountDialog = true },
                    modifier = Modifier.weight(1f).testTag("delete_account_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete Account", fontSize = 12.sp)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
