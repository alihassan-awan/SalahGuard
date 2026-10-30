package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun NotPrayedReasonScreen(
    onNavigateBack: () -> Unit,
    onSubmitMissedReason: (String, String) -> Unit,
    onQuickSwitchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reasons = listOf(
        "I kept delaying it",
        "I chose my phone / entertainment",
        "Work took priority",
        "I overslept",
        "I forgot",
        "Illness or genuine hardship",
        "Other"
    )

    var selectedReason by remember { mutableStateOf("") }
    var explanationText by remember { mutableStateOf("") }
    val maxChars = 200
    var showValidation by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = SalahDarkBg,
        topBar = {
            SalahGuardTopBar(
                title = "",
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
                        .testTag("missed_cancel_button")
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    )
                }

                // Submit Button (Red)
                Button(
                    onClick = { if (selectedReason.isNotBlank() && explanationText.trim().length >= 15) onSubmitMissedReason(selectedReason, explanationText.trim()) else showValidation = true },

                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SalahRed,
                        contentColor = SalahTextWhite
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("missed_submit_button")
                ) {
                    Text(
                        text = "Submit",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                }
            }
        },
        modifier = modifier.testTag("not_prayed_reason_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            if (showValidation) Text("وجہ منتخب کریں اور کم از کم 15 حروف میں اپنی بات لکھیں۔", color = SalahGold, fontSize = 13.sp)
            // Header with amber diamond icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(SalahGold, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Why was the prayer missed?",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SalahTextWhite,
                        fontSize = 20.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Please select the reason and write a short explanation.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = SalahTextSecondary,
                    fontSize = 14.sp
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Radio options list (matches mockup)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SalahCardBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = SalahCardBg
            ) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    reasons.forEach { reason ->
                        val isSelected = selectedReason == reason
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedReason = reason }
                                .padding(horizontal = 16.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(SalahEmerald),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(SalahTextWhite)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, SalahTextMuted, CircleShape)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Text(
                                text = reason,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) SalahTextWhite else SalahTextSecondary,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Surface(color = SalahCardBg, shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("A moment of honest reflection", color = SalahGold,
                        fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        text = when (selectedReason) {
                            "I kept delaying it" -> "What felt more urgent than meeting Allah in salah? What will you change at the next prayer?"
                            "I chose my phone / entertainment" -> "The screen can wait. Your time with Allah deserves a place in your day."
                            "Work took priority" -> "Your responsibilities matter. Can you plan a short prayer break before the next prayer?"
                            "I overslept" -> "Rest matters too. Consider an earlier bedtime and a reliable alarm."
                            "I forgot" -> "A reminder can help. Set one before the next prayer and return with intention."
                            "Illness or genuine hardship" -> "Allah knows your hardship. Seek reliable guidance about prayer accommodations."
                            "Other" -> "What prevented you, and what practical step will help next time?"
                            else -> "Choose what truly happened. This is for self-accountability, not judgment."
                        },
                        color = SalahTextSecondary, fontSize = 13.sp, lineHeight = 19.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("اللہ کی رحمت سے مایوس نہ ہو۔ (الزمر، 39:53، مفہوم)",
                        color = SalahEmeraldLight, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(18.dp))

            // Explanation Text Box
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SalahCardBorder, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                color = SalahCardBg
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    TextField(
                        value = explanationText,
                        onValueChange = {
                            if (it.length <= maxChars) {
                                explanationText = it
                            }
                        },
                        placeholder = {
                            Text(
                                text = "Write a short explanation\n(minimum 15 characters)...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = SalahTextMuted,
                                    fontSize = 14.sp
                                )
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = SalahTextWhite,
                            unfocusedTextColor = SalahTextWhite,
                            cursorColor = SalahEmerald,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 90.dp)
                            .testTag("missed_explanation_field")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "${explanationText.length}/$maxChars",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SalahTextSecondary,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
