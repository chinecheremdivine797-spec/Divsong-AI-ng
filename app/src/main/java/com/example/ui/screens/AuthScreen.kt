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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DivLogo
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

@Composable
fun AuthScreen(
    viewModel: MainViewModel,
    onAuthSuccess: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) } // 0: Login, 1: Sign Up

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf("") }

    var showVerificationNoticeDialog by remember { mutableStateOf(false) }
    var showOnboardingDialog by remember { mutableStateOf(false) }
    var onboardingStep by remember { mutableStateOf(1) }

    // Password strength calculation
    val passwordStrength = when {
        password.length >= 8 && password.any { it.isDigit() } && password.any { !it.isLetterOrDigit() } -> 1.0f
        password.length >= 6 -> 0.6f
        password.isNotEmpty() -> 0.3f
        else -> 0.0f
    }

    // Onboarding Walkthrough Dialog
    if (showOnboardingDialog) {
        AlertDialog(
            onDismissRequest = { showOnboardingDialog = false },
            title = {
                Text(
                    text = when (onboardingStep) {
                        1 -> "Welcome to DIV SONG AI"
                        2 -> "Describe & Customize"
                        else -> "Synthesize & Export"
                    },
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(DivGradientBrand),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (onboardingStep) {
                                1 -> Icons.Default.MusicNote
                                2 -> Icons.Default.AutoAwesome
                                else -> Icons.Default.Headphones
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = when (onboardingStep) {
                            1 -> "DIV SONG AI is your intelligent sound studio. Transform text descriptions into full original songs across Afrobeats, Amapiano, Hip-hop, Gospel, and more."
                            2 -> "Choose your preferred genre, mood, vocal style, and language. Let Gemini AI write original lyrics or bring your own verse!"
                            else -> "Listen with interactive waveforms, save tracks to your library, generate variations, and export high-res audio."
                        },
                        fontSize = 13.sp,
                        color = DivTextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(1, 2, 3).forEach { step ->
                            Box(
                                modifier = Modifier
                                    .size(if (step == onboardingStep) 16.dp else 8.dp, 8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (step == onboardingStep) DivCyan else Color(0xFF333355))
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (onboardingStep < 3) {
                            onboardingStep++
                        } else {
                            showOnboardingDialog = false
                            onAuthSuccess()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DivPurple)
                ) {
                    Text(if (onboardingStep < 3) "Next" else "Start Creating")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showOnboardingDialog = false
                    onAuthSuccess()
                }) {
                    Text("Skip", color = DivTextSecondary)
                }
            },
            containerColor = DivSurfaceDark,
            modifier = Modifier.border(1.dp, DivBorder, RoundedCornerShape(18.dp))
        )
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Reset Password", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter your email address to receive password reset instructions:", color = DivTextSecondary, fontSize = 12.sp)
                    OutlinedTextField(
                        value = forgotEmail,
                        onValueChange = { forgotEmail = it },
                        modifier = Modifier.fillMaxWidth().testTag("forgot_password_email_input"),
                        label = { Text("Email") },
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
                        viewModel.showToast("Password reset link sent to $forgotEmail")
                        showForgotPasswordDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DivPurple)
                ) {
                    Text("Send Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Cancel", color = DivTextSecondary)
                }
            },
            containerColor = DivSurfaceDark
        )
    }

    // Email Verification Notice Dialog
    if (showVerificationNoticeDialog) {
        AlertDialog(
            onDismissRequest = {
                showVerificationNoticeDialog = false
                showOnboardingDialog = true
            },
            title = {
                Text("Verification Email Sent", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "We've created your account and sent a verification email to $email.",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "You've been credited with 15 free song credits! You can verify your email anytime to unlock extra cloud sharing benefits.",
                        color = DivTextSecondary,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showVerificationNoticeDialog = false
                        showOnboardingDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DivPurple),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Continue to Studio")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.authRepo.resendVerificationEmail()
                            viewModel.showToast("Verification email re-sent to $email")
                        }
                    }
                ) {
                    Text("Resend Email", color = DivCyan)
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
            .padding(horizontal = 20.dp)
            .testTag("auth_screen"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(30.dp))
            DivLogo(size = 54.dp, showTagline = true, showSubtitle = true)
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, DivBorder, RoundedCornerShape(22.dp)),
                colors = CardDefaults.cardColors(containerColor = DivSurfaceDark)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Tabs
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = DivCard,
                        contentColor = Color.White,
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
                            text = { Text("Login", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("tab_login")
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Sign Up", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("tab_signup")
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (selectedTab == 1) {
                        // Full Name
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            modifier = Modifier.fillMaxWidth().testTag("auth_fullname_input"),
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = DivCyan) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DivCyan,
                                unfocusedBorderColor = DivBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Username
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            modifier = Modifier.fillMaxWidth().testTag("auth_username_input"),
                            label = { Text("Username") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = DivPink) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DivCyan,
                                unfocusedBorderColor = DivBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth().testTag("auth_email_input"),
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = DivCyan) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DivCyan,
                            unfocusedBorderColor = DivBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth().testTag("auth_password_input"),
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = DivPink) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password",
                                    tint = DivTextSecondary
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DivCyan,
                            unfocusedBorderColor = DivBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    if (selectedTab == 1 && password.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { passwordStrength },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = if (passwordStrength >= 0.8f) Color(0xFF10B981) else if (passwordStrength >= 0.5f) Color(0xFFF59E0B) else Color(0xFFEF4444),
                            trackColor = Color(0xFF2E2E4E)
                        )
                    }

                    if (selectedTab == 1) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // Confirm Password Field (Mandatory in Sign Up)
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            modifier = Modifier.fillMaxWidth().testTag("auth_confirm_password_input"),
                            label = { Text("Confirm Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = DivCyan) },
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle confirm password",
                                        tint = DivTextSecondary
                                    )
                                }
                            },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DivCyan,
                                unfocusedBorderColor = DivBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )
                    }

                    if (selectedTab == 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Forgot password?",
                            color = DivCyan,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .align(Alignment.End)
                                .clickable { showForgotPasswordDialog = true }
                                .testTag("forgot_password_button")
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Submit Button
                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                viewModel.showToast("Please enter email and password")
                                return@Button
                            }

                            if (selectedTab == 1) {
                                if (fullName.isBlank()) {
                                    viewModel.showToast("Please enter your full name")
                                    return@Button
                                }
                                if (username.isBlank()) {
                                    viewModel.showToast("Please choose a username")
                                    return@Button
                                }
                                if (password.length < 6) {
                                    viewModel.showToast("Password must be at least 6 characters")
                                    return@Button
                                }
                                if (password != confirmPassword) {
                                    viewModel.showToast("Passwords do not match")
                                    return@Button
                                }
                            }

                            isLoading = true
                            coroutineScope.launch {
                                if (selectedTab == 0) {
                                    val res = viewModel.authRepo.login(email, password)
                                    isLoading = false
                                    res.onSuccess {
                                        viewModel.showToast("Welcome back, ${it.fullName}!")
                                        onAuthSuccess()
                                    }.onFailure { err ->
                                        viewModel.showToast(err.message ?: "Login failed")
                                    }
                                } else {
                                    val res = viewModel.authRepo.signUp(
                                        fullName = fullName.trim(),
                                        username = username.trim(),
                                        email = email.trim(),
                                        password = password
                                    )
                                    isLoading = false
                                    res.onSuccess {
                                        showVerificationNoticeDialog = true
                                    }.onFailure { err ->
                                        viewModel.showToast(err.message ?: "Registration failed")
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("auth_submit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = DivPurple),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Text(if (selectedTab == 0) "Login to Studio" else "Create Account & Get 15 Credits", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
