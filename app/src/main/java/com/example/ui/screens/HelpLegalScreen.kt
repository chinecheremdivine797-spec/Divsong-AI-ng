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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DivBackground
import com.example.ui.theme.DivBorder
import com.example.ui.theme.DivCard
import com.example.ui.theme.DivCyan
import com.example.ui.theme.DivPink
import com.example.ui.theme.DivPurple
import com.example.ui.theme.DivSurfaceDark
import com.example.ui.theme.DivTextMuted
import com.example.ui.theme.DivTextSecondary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun HelpLegalScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: FAQs & Support, 1: Privacy Policy, 2: Terms of Service
    var expandedFaqIndex by remember { mutableStateOf<Int?>(0) }

    var contactSubject by remember { mutableStateOf("") }
    var contactMessage by remember { mutableStateOf("") }

    val faqs = listOf(
        Pair(
            "How does DIV SONG AI generate songs?",
            "DIV SONG AI utilizes neural audio synthesis models combined with Gemini AI to convert your prompt descriptions into structured lyrics, harmonic chords, rhythmic drums, and mastered audio files."
        ),
        Pair(
            "Do I own the rights to the generated music?",
            "Yes! On Creator and Pro subscription tiers, you obtain full commercial usage rights to the generated songs and lyrics for streaming platforms, YouTube, videos, and commercial productions."
        ),
        Pair(
            "Can I write my own lyrics?",
            "Absolutely. In the 'Create Music' studio, switch to Custom Mode and select 'Write My Own' to input your original lyrics with structural cues like [Intro], [Chorus], and [Verse]."
        ),
        Pair(
            "How do generation credits work?",
            "Each song generation consumes 1 credit. Free accounts receive 10 initial credits, while Creator plans get 100 credits/mo and Pro gets 300 credits/mo. Credits can also be topped up anytime."
        ),
        Pair(
            "What genres and styles are supported?",
            "We support Afrobeats, Amapiano, Hip-hop, R&B, Gospel, Dancehall, Reggae, Pop, EDM, Rock, Soul, Highlife, Fuji, Classical, Acoustic, and custom user-defined styles."
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DivBackground)
            .padding(horizontal = 16.dp)
            .testTag("help_legal_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("Help & Legal Center", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
        }

        // Tabs
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DivSurfaceDark,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = DivCyan
                    )
                }
            ) {
                listOf("Support & FAQs", "Privacy Policy", "Terms of Service").forEachIndexed { index, name ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(name, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }

        when (selectedTab) {
            0 -> {
                // FAQs
                item {
                    Text("Frequently Asked Questions", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                items(faqs.indices.toList()) { index ->
                    val (question, answer) = faqs[index]
                    val isExpanded = expandedFaqIndex == index

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { expandedFaqIndex = if (isExpanded) null else index },
                        colors = CardDefaults.cardColors(containerColor = DivCard)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(question, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = DivCyan
                                )
                            }
                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(answer, color = DivTextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
                            }
                        }
                    }
                }

                // Contact Support Form
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = DivSurfaceDark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Contact Studio Support", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Have a question or custom enterprise request? Send our team a message.", color = DivTextSecondary, fontSize = 11.sp)

                            OutlinedTextField(
                                value = contactSubject,
                                onValueChange = { contactSubject = it },
                                label = { Text("Subject") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DivCyan,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            OutlinedTextField(
                                value = contactMessage,
                                onValueChange = { contactMessage = it },
                                label = { Text("Message") },
                                modifier = Modifier.fillMaxWidth().height(90.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DivCyan,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            Button(
                                onClick = {
                                    if (contactSubject.isNotBlank() && contactMessage.isNotBlank()) {
                                        viewModel.showToast("Message sent to DIV Studio Support!")
                                        contactSubject = ""
                                        contactMessage = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = DivPurple)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Send Message")
                            }
                        }
                    }
                }
            }

            1 -> {
                // Privacy Policy
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = DivSurfaceDark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Privacy Policy", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Effective Date: September 2026", color = DivCyan, fontSize = 11.sp)
                            Text(
                                "1. Information Collection: DIV SONG AI collects account information (email, name), song generation prompts, and custom lyrics solely to provide the AI music generation service.\n\n" +
                                        "2. Data Protection: All data transmission is encrypted using industry standard TLS. User audio files and personal prompts are stored securely.\n\n" +
                                        "3. AI Model Training: Your private song prompts and lyrics are not used to train public generative models without explicit opt-in consent.\n\n" +
                                        "4. Payments & Billing: Payment credentials are processed directly through certified PCI-DSS compliant providers (Paystack, Flutterwave, Stripe).",
                                color = DivTextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            2 -> {
                // Terms of Service
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = DivSurfaceDark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Terms of Service", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Effective Date: September 2026", color = DivCyan, fontSize = 11.sp)
                            Text(
                                "1. Acceptance of Terms: By using DIV SONG AI, you agree to comply with all applicable local and international copyright laws.\n\n" +
                                        "2. Prohibited Content: Users may not generate songs containing hate speech, targeted harassment, illegal material, or verbatim replicas of copyrighted lyrics.\n\n" +
                                        "3. Credits & Subscriptions: Subscriptions renew on a monthly basis. Unused credits rollover within active subscription terms.\n\n" +
                                        "4. Service Availability: DIV SONG AI strives for 99.9% uptime for AI generation pipelines and audio streaming servers.",
                                color = DivTextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
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
