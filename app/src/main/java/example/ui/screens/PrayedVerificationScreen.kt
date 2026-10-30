package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun PrayedVerificationScreen(
    onNavigateBack: () -> Unit,
    onConfirmPrayed: (String) -> Unit,
    onQuickSwitchClick: () -> Unit,
    prayerName: String = "Zuhr",
    modifier: Modifier = Modifier
) {
    var verificationText by remember { mutableStateOf("") }
    var selectedDetail by remember { mutableStateOf("") }
    val isVerified = verificationText.trim() == "I PRAYED"
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = SalahDarkBg,
        topBar = {
            SalahGuardTopBar(
                title = "Confirm Prayer",
                onBackClick = onNavigateBack,
                onQuickSwitchClick = onQuickSwitchClick
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SalahDarkBg)
                    .padding(16.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cancel Button
                Button(
                    onClick = onNavigateBack,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E2F35),
                        contentColor = SalahTextWhite
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("verify_cancel_button")
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    )
                }

                // Confirm Button
                Button(
                    onClick = { if (isVerified && selectedDetail.isNotBlank()) onConfirmPrayed(selectedDetail) },
                    enabled = isVerified && selectedDetail.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SalahEmerald,
                        contentColor = SalahTextWhite
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("verify_confirm_button")
                ) {
                    Text(
                        text = "Confirm",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                }
            }
        },
        modifier = modifier.testTag("prayed_verification_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // White Card (matches mockup exactly)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                color = Color.White,
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Did you actually perform $prayerName prayer?",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            fontSize = 19.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Allah knows our actions. Please be honest with yourself.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF64748B),
                            fontSize = 14.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Text input box "Type: I PRAYED"
                    OutlinedTextField(
                        value = verificationText,
                        onValueChange = { verificationText = it },
                        placeholder = {
                            Text(
                                text = "Type: I PRAYED",
                                color = Color(0xFF94A3B8),
                                fontSize = 14.sp
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            focusedBorderColor = SalahEmerald,
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("verify_input_field")
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("How did you pray? Select honestly:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF334155))
                    listOf("On time", "Late, but within prayer time", "Made up a missed prayer (Qada)").forEach { option ->
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                                .clickable { selectedDetail = option }.padding(vertical = 3.dp)) {
                            RadioButton(selected = selectedDetail == option, onClick = { selectedDetail = option })
                            Text(option, color = Color(0xFF334155), fontSize = 13.sp)
                        }
                    }
                    Text("Prayer is a trust with Allah. Choose only what truly happened.",
                        color = Color(0xFF64748B), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Reflection Checklist
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Before confirming, take a moment:",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF334155),
                                fontSize = 14.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        VerificationCheckItem("Did you pray on time?")
                        Spacer(modifier = Modifier.height(10.dp))
                        VerificationCheckItem("Did you complete it properly?")
                        Spacer(modifier = Modifier.height(10.dp))
                        VerificationCheckItem("Were you focused in your prayer?")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun VerificationCheckItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = SalahEmerald,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF475569),
                fontSize = 14.sp
            )
        )
    }
}
