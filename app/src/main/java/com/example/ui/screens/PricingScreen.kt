package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ai.MusicProviderManager
import com.example.data.local.entities.PlanEntity
import com.example.data.payment.PaystackPayment
import com.example.ui.theme.DivBackground
import com.example.ui.theme.DivBorder
import com.example.ui.theme.DivCard
import com.example.ui.theme.DivCardElevated
import com.example.ui.theme.DivCyan
import com.example.ui.theme.DivGradientBrand
import com.example.ui.theme.DivPink
import com.example.ui.theme.DivPurple
import com.example.ui.theme.DivSurfaceDark
import com.example.ui.theme.DivTextMuted
import com.example.ui.theme.DivTextSecondary
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun PricingScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    val plans by viewModel.plans.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val transactions by viewModel.userTransactions.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedPlanForCheckout by remember { mutableStateOf<PlanEntity?>(null) }
    var isProcessingPayment by remember { mutableStateOf(false) }

    if (selectedPlanForCheckout != null) {
        val targetPlan = selectedPlanForCheckout!!
        AlertDialog(
            onDismissRequest = { if (!isProcessingPayment) selectedPlanForCheckout = null },
            title = { Text("Upgrade to Premium", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("DIV SONG AI Premium", color = DivCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Full Premium access with monthly AI song credits and studio features.", color = DivTextSecondary, fontSize = 13.sp)
                    Text("₦5,000 / month", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Text("Payment: Paystack", color = DivTextSecondary, fontSize = 12.sp)
                    Text("Card • Bank Transfer • USSD", color = DivTextMuted, fontSize = 11.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val email = currentUser?.email?.trim().orEmpty()
                        if (email.isBlank()) {
                            viewModel.showToast("Please log in with an email before paying")
                            return@Button
                        }
                        isProcessingPayment = true
                        scope.launch {
                            val result = PaystackPayment.startPremiumCheckout(email)
                            result.onSuccess { checkoutUrl ->
                                isProcessingPayment = false
                                selectedPlanForCheckout = null
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUrl)))
                                viewModel.showToast("Paystack checkout opened. Complete the ₦5,000 payment.")
                            }.onFailure { error ->
                                isProcessingPayment = false
                                viewModel.showToast("Payment error: ${error.message ?: "Unable to start checkout"}")
                            }
                        }
                    },
                    enabled = !isProcessingPayment,
                    colors = ButtonDefaults.buttonColors(containerColor = DivPurple),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isProcessingPayment) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                    } else {
                        Text("Pay ₦5,000 / month")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPlanForCheckout = null }, enabled = !isProcessingPayment) {
                    Text("Cancel", color = DivTextSecondary)
                }
            },
            containerColor = DivSurfaceDark,
            modifier = Modifier.border(1.dp, DivBorder, RoundedCornerShape(16.dp))
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(DivBackground).padding(horizontal = 16.dp).testTag("pricing_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Unlock Creative Power", color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Upgrade to DIV SONG AI Premium and pay securely with Paystack.", color = DivTextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).border(1.5.dp, Brush.horizontalGradient(listOf(DivPurple, DivPink, DivCyan)), RoundedCornerShape(20.dp)), colors = CardDefaults.cardColors(containerColor = DivSurfaceDark)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("DIV SONG AI Premium", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Create more music with Premium access.", color = DivTextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("₦5,000", color = DivCyan, fontWeight = FontWeight.Black, fontSize = 30.sp)
                        Text(" / month", color = DivTextSecondary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp, start = 4.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("✓ Paystack secure checkout", color = Color.White, fontSize = 13.sp)
                    Text("✓ Card, bank transfer and USSD", color = Color.White, fontSize = 13.sp)
                    Text("✓ Premium activates after Paystack verification", color = Color.White, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { selectedPlanForCheckout = plans.firstOrNull { it.planId != "free" } },
                        modifier = Modifier.fillMaxWidth().testTag("premium_paystack_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = if (currentUser?.planId != "free") DivCardElevated else DivPurple),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (currentUser?.planId != "free") "Premium Active" else "Upgrade — ₦5,000/month", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (transactions.isNotEmpty()) {
            item { Text("Billing & Payment History", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White) }
            items(transactions) { tx ->
                Card(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)), colors = CardDefaults.cardColors(containerColor = DivCard)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(tx.planName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Ref: ${tx.reference} • ${tx.provider}", color = DivTextSecondary, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(if (tx.currency == "NGN") "₦${tx.amount}" else "$${tx.amount}", color = DivCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(tx.status, color = DivTextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
