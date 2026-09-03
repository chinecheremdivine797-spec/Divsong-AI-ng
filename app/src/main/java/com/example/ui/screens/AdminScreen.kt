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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.PlanEntity
import com.example.data.local.entities.UserEntity
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
fun AdminScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    val users by viewModel.adminUsers.collectAsStateWithLifecycle()
    val songs by viewModel.adminSongs.collectAsStateWithLifecycle()
    val jobs by viewModel.adminJobs.collectAsStateWithLifecycle()
    val transactions by viewModel.adminTransactions.collectAsStateWithLifecycle()
    val reports by viewModel.adminReports.collectAsStateWithLifecycle()
    val plans by viewModel.allPlans.collectAsStateWithLifecycle()

    var selectedAdminTab by remember { mutableStateOf(0) } // 0: Metrics, 1: Users, 2: Songs, 3: Plans, 4: Reports

    var editingPlan by remember { mutableStateOf<PlanEntity?>(null) }
    var editPriceInput by remember { mutableStateOf("") }
    var editCreditsInput by remember { mutableStateOf("") }

    val totalRevenue = transactions.filter { it.status == "successful" }.sumOf { it.amount }

    // Plan Edit Dialog
    if (editingPlan != null) {
        AlertDialog(
            onDismissRequest = { editingPlan = null },
            title = { Text("Edit Plan: ${editingPlan?.name}", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editPriceInput,
                        onValueChange = { editPriceInput = it },
                        label = { Text("Price Monthly (USD)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DivCyan,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = editCreditsInput,
                        onValueChange = { editCreditsInput = it },
                        label = { Text("Generation Credits") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DivCyan,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = editingPlan ?: return@Button
                        val price = editPriceInput.toDoubleOrNull() ?: p.priceMonthlyUsd
                        val credits = editCreditsInput.toIntOrNull() ?: p.generationCredits
                        viewModel.saveAdminPlan(p.copy(priceMonthlyUsd = price, generationCredits = credits))
                        editingPlan = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DivPurple)
                ) {
                    Text("Save Plan")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingPlan = null }) {
                    Text("Cancel", color = DivTextSecondary)
                }
            },
            containerColor = DivSurfaceDark
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DivBackground)
            .padding(horizontal = 16.dp)
            .testTag("admin_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "DIV SONG AI Console",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Platform Administration & Moderation",
                        color = DivCyan,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Admin Tabs
        item {
            TabRow(
                selectedTabIndex = selectedAdminTab,
                containerColor = DivSurfaceDark,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedAdminTab]),
                        color = DivPink
                    )
                }
            ) {
                listOf("Overview", "Users", "Songs", "Plans", "Reports", "System").forEachIndexed { index, tabName ->
                    Tab(
                        selected = selectedAdminTab == index,
                        onClick = { selectedAdminTab = index },
                        text = { Text(tabName, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("admin_tab_$tabName")
                    )
                }
            }
        }

        when (selectedAdminTab) {
            0 -> {
                // Overview & Key Metrics
                item {
                    Text("Live System Analytics", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AdminStatCard(
                            title = "Total Users",
                            value = "${users.size}",
                            icon = Icons.Default.People,
                            color = DivCyan,
                            modifier = Modifier.weight(1f)
                        )
                        AdminStatCard(
                            title = "Total Songs",
                            value = "${songs.size}",
                            icon = Icons.Default.LibraryMusic,
                            color = DivPink,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AdminStatCard(
                            title = "Jobs Executed",
                            value = "${jobs.size}",
                            icon = Icons.Default.Memory,
                            color = DivPurpleLight,
                            modifier = Modifier.weight(1f)
                        )
                        AdminStatCard(
                            title = "Platform Revenue",
                            value = "$${totalRevenue.toInt()}",
                            icon = Icons.Default.AttachMoney,
                            color = Color(0xFF10B981),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = DivSurfaceDark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("AI Generation Engine Status", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Primary Provider", color = DivTextSecondary, fontSize = 12.sp)
                                Text("DIV Neural Audio Engine", color = DivCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Lyrics Provider", color = DivTextSecondary, fontSize = 12.sp)
                                Text("Gemini 3.5 Flash", color = DivPink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Active Pending Reports", color = DivTextSecondary, fontSize = 12.sp)
                                Text("${reports.count { it.status == "pending" }}", color = if (reports.any { it.status == "pending" }) Color(0xFFEF4444) else Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            1 -> {
                // Users Management
                item {
                    Text("Registered Users (${users.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                items(users) { user ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = DivCard)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.fullName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${user.email} • ${user.credits} Credits • Plan: ${user.planId}", color = DivTextSecondary, fontSize = 11.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (user.role == "admin") DivPink else DivPurple)
                                    .clickable {
                                        val newRole = if (user.role == "admin") "user" else "admin"
                                        viewModel.updateAdminUserRole(user, newRole)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(user.role.uppercase(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            2 -> {
                // Songs Moderation
                item {
                    Text("All Songs Moderation (${songs.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                items(songs) { s ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = DivCard)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(s.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("By: ${s.creatorName} • ${s.genre} • ${s.visibility}", color = DivTextSecondary, fontSize = 11.sp)
                            }
                            IconButton(onClick = { viewModel.deleteSongByAdmin(s.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                            }
                        }
                    }
                }
            }

            3 -> {
                // Plans & Pricing Editor
                item {
                    Text("Subscription Plans Config", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                items(plans) { plan ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = DivCard)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(plan.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("$${plan.priceMonthlyUsd}/mo • ${plan.generationCredits} Credits", color = DivCyan, fontSize = 12.sp)
                            }
                            IconButton(onClick = {
                                editingPlan = plan
                                editPriceInput = plan.priceMonthlyUsd.toString()
                                editCreditsInput = plan.generationCredits.toString()
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Plan", tint = Color.White)
                            }
                        }
                    }
                }
            }

            4 -> {
                // Reports
                item {
                    Text("Content Reports (${reports.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                if (reports.isEmpty()) {
                    item {
                        Text("No reports filed yet.", color = DivTextSecondary, fontSize = 13.sp)
                    }
                } else {
                    items(reports) { rep ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = DivCard)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(rep.songTitle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(rep.status.uppercase(), color = if (rep.status == "pending") Color(0xFFF59E0B) else Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("Reason: ${rep.reason}", color = DivPink, fontSize = 11.sp)
                                if (rep.description.isNotBlank()) {
                                    Text(rep.description, color = DivTextSecondary, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { viewModel.resolveReport(rep.id, "resolved") },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("Resolve", fontSize = 10.sp)
                                    }
                                    Button(
                                        onClick = { viewModel.resolveReport(rep.id, "dismissed") },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64748B)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("Dismiss", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            5 -> {
                // System & Provider Settings
                item {
                    Text("Platform & Infrastructure Configuration", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = DivCard)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Music Generation Pipeline", color = DivCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                            Text(
                                text = "Current Status: " + viewModel.musicProviderStatusMessage,
                                color = Color.White,
                                fontSize = 12.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Developer Sandbox Mode", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Enables local wave synthesizer for offline pipeline tests.", color = DivTextSecondary, fontSize = 11.sp)
                                }
                                Button(
                                    onClick = {
                                        val current = viewModel.isDevSandboxMode.value
                                        viewModel.toggleDevSandboxMode(!current)
                                    },
                                    modifier = Modifier.testTag("admin_toggle_sandbox"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (viewModel.isDevSandboxMode.value) DivPink else DivPurple
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(if (viewModel.isDevSandboxMode.value) "Active" else "Enable", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = DivCard)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Payment Gateways", color = DivCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("• Paystack Gateway: Ready (Credit card, USSD, Bank transfer)", color = Color.White, fontSize = 12.sp)
                            Text("• Flutterwave Gateway: Ready (Cards, Mobile money, Barter)", color = Color.White, fontSize = 12.sp)
                            Text("• Stripe Billing: Ready (Global cards, Apple Pay, Google Pay)", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = DivCard)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Storage & Local Room Database", color = DivCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("• Database Engine: Room 2.6.1 SQLite local persistence", color = Color.White, fontSize = 12.sp)
                            Text("• Indexed Tracks: ${songs.size} registered songs", color = Color.White, fontSize = 12.sp)
                            Text("• User Profiles: ${users.size} registered accounts", color = Color.White, fontSize = 12.sp)
                            Text("• Generation Jobs: ${jobs.size} tracked jobs", color = Color.White, fontSize = 12.sp)
                            Text("• Superadmin Email: divstudio03@gmail.com", color = DivCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun AdminStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clip(RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = DivSurfaceDark)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
            Text(title, color = DivTextSecondary, fontSize = 11.sp)
        }
    }
}
