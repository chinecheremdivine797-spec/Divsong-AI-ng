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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.PlanEntity
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
fun PricingScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    val plans by viewModel.plans.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val transactions by viewModel.userTransactions.collectAsStateWithLifecycle()

    var selectedPlanForCheckout by remember { mutableStateOf<PlanEntity?>(null) }
    var selectedPaymentGateway by remember { mutableStateOf("Paystack") } // "Paystack", "Flutterwave", "Stripe"
    var isProcessingPayment by remember { mutableStateOf(false) }

    // Checkout Modal
    if (selectedPlanForCheckout != null) {
        val targetPlan = selectedPlanForCheckout!!
        AlertDialog(
            onDismissRequest = {
                if (!isProcessingPayment) selectedPlanForCheckout = null
            },
            title = {
                Text(
                    text = "Upgrade to ${targetPlan.name}",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Instant credit activation: +${targetPlan.generationCredits} generation credits",
                        color = DivCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Total Price: $${targetPlan.priceMonthlyUsd} / month",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Select Payment Gateway:", color = DivTextSecondary, fontSize = 12.sp)

                    listOf("Paystack (Card, Bank Transfer, USSD)", "Flutterwave (African & Global Cards)", "Stripe (International Credit Cards)").forEach { gatewayStr ->
                        val gatewayCode = gatewayStr.substringBefore(" ")
                        val isSelected = selectedPaymentGateway == gatewayCode
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) DivPurple.copy(alpha = 0.3f) else DivCard)
                                .border(1.dp, if (isSelected) DivCyan else DivBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedPaymentGateway = gatewayCode }
                                .padding(10.dp)
                        ) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, tint = if (isSelected) DivCyan else DivTextMuted, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(gatewayStr, color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isProcessingPayment = true
                        viewModel.subscribeToPlan(targetPlan, selectedPaymentGateway) {
                            isProcessingPayment = false
                            selectedPlanForCheckout = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DivPurple),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isProcessingPayment) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                    } else {
                        Text("Confirm & Activate ($${targetPlan.priceMonthlyUsd})")
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
        modifier = Modifier
            .fillMaxSize()
            .background(DivBackground)
            .padding(horizontal = 16.dp)
            .testTag("pricing_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Unlock Creative Power",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Choose the plan that fits your musical ambition. Upgrade or top up credits instantly.",
                    color = DivTextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Plan Cards
        items(plans) { plan ->
            val isCurrentPlan = currentUser?.planId == plan.planId
            val isPopular = plan.planId == "creator"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        1.5.dp,
                        if (isPopular) Brush.horizontalGradient(listOf(DivPurple, DivPink, DivCyan)) else Brush.linearGradient(listOf(DivBorder, DivBorder)),
                        RoundedCornerShape(20.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = DivSurfaceDark)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = plan.name,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Text(
                                text = plan.description,
                                color = DivTextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        if (isPopular) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DivGradientBrand)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("POPULAR", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$${plan.priceMonthlyUsd.toInt()}",
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp,
                            color = if (isPopular) DivCyan else Color.White
                        )
                        Text(
                            text = if (plan.priceMonthlyUsd > 0) " / month" else " free forever",
                            color = DivTextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "✨ Includes ${plan.generationCredits} AI Song Credits",
                        color = DivPink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Button
                    Button(
                        onClick = {
                            if (plan.priceMonthlyUsd > 0) {
                                selectedPlanForCheckout = plan
                            } else {
                                viewModel.showToast("You are on Free Tier")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("select_plan_${plan.planId}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCurrentPlan) DivCardElevated else DivPurple
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isCurrentPlan) "Active Plan" else if (plan.priceMonthlyUsd == 0.0) "Current Free Tier" else "Subscribe ($${plan.priceMonthlyUsd}/mo)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Transaction Receipts
        if (transactions.isNotEmpty()) {
            item {
                Text(
                    text = "Billing & Payment History",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            items(transactions) { tx ->
                Card(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = DivCard)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(tx.planName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Ref: ${tx.reference} • ${tx.provider}", color = DivTextSecondary, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("$${tx.amount}", color = DivCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("+${tx.creditsAdded} Credits", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
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
