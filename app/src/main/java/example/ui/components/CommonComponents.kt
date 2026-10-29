package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PrayerStatus
import com.example.data.model.PrayerType
import com.example.ui.theme.*

@Composable
fun SalahGuardTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    onQuickSwitchClick: (() -> Unit)? = null,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SalahDarkBg)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.CenterStart) {
                if (onBackClick != null) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .testTag("top_bar_back_button")
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SalahTextWhite
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (title == "SalahGuard") {
                        Icon(
                            imageVector = Icons.Default.Mosque,
                            contentDescription = null,
                            tint = SalahGold,
                            modifier = Modifier
                                .size(22.dp)
                                .padding(end = 6.dp)
                        )
                        Text(
                            text = "Salah",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = SalahTextWhite
                            )
                        )
                        Text(
                            text = "Guard",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = SalahGold
                            )
                        )
                    } else {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = SalahTextWhite
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = SalahTextSecondary
                        )
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.widthIn(min = 44.dp)
            ) {
                if (onQuickSwitchClick != null) {
                    IconButton(
                        onClick = onQuickSwitchClick,
                        modifier = Modifier
                            .testTag("quick_switch_button")
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = "All Screens",
                            tint = SalahGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                if (onSettingsClick != null) {
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier
                            .testTag("top_bar_settings_button")
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = SalahTextWhite
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SalahGuardBottomBar(
    currentTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SalahDarkSurface,
        tonalElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = SalahCardBorder.copy(alpha = 0.5f),
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
            )
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                label = "Home",
                icon = Icons.Default.Home,
                isSelected = currentTab == "home",
                onClick = { onTabSelected("home") },
                testTag = "nav_tab_home"
            )
            BottomNavItem(
                label = "Times",
                icon = Icons.Outlined.AccessTime,
                isSelected = currentTab == "times",
                onClick = { onTabSelected("times") },
                testTag = "nav_tab_times"
            )
            BottomNavItem(
                label = "History",
                icon = Icons.Outlined.AssignmentTurnedIn,
                isSelected = currentTab == "history" || currentTab == "reports",
                onClick = { onTabSelected("history") },
                testTag = "nav_tab_history"
            )
            BottomNavItem(
                label = "Settings",
                icon = Icons.Outlined.Settings,
                isSelected = currentTab == "settings",
                onClick = { onTabSelected("settings") },
                testTag = "nav_tab_settings"
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) SalahGold else SalahTextSecondary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 11.sp,
                color = if (isSelected) SalahGold else SalahTextSecondary
            )
        )
    }
}

@Composable
fun RoundedCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = SalahCardBg,
    borderColor: Color = SalahCardBorder,
    borderWidth: Dp = 1.dp,
    cornerRadius: Dp = 16.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius)),
        shape = RoundedCornerShape(cornerRadius),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun SalahPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    testTag: String = "primary_button"
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SalahEmerald,
            contentColor = SalahTextWhite,
            disabledContainerColor = SalahCardBorder,
            disabledContentColor = SalahTextSecondary
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag(testTag)
            .shadow(
                elevation = if (enabled) 6.dp else 0.dp,
                shape = RoundedCornerShape(14.dp),
                spotColor = SalahEmeraldLight
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = SalahTextWhite
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )
        }
    }
}

@Composable
fun SalahDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    testTag: String = "danger_button"
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SalahRed,
            contentColor = SalahTextWhite
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = SalahTextWhite
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )
        }
    }
}

@Composable
fun SalahOrangeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    testTag: String = "orange_button"
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SalahOrange,
            contentColor = SalahTextWhite
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = SalahTextWhite
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )
        }
    }
}

@Composable
fun SalahSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    testTag: String = "secondary_button"
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = SalahCardBg,
            contentColor = SalahTextWhite
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.horizontalGradient(listOf(SalahCardBorder, SalahCardBorder))
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = SalahTextSecondary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = SalahTextWhite
                )
            )
        }
    }
}

@Composable
fun StatusBadge(
    status: PrayerStatus,
    actualTime: String? = null,
    modifier: Modifier = Modifier
) {
    when (status) {
        PrayerStatus.PRAYED -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
                    .background(SalahEmerald.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .border(1.dp, SalahEmerald.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SalahEmerald,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Prayed",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SalahEmerald,
                            fontSize = 12.sp
                        )
                    )
                    if (actualTime != null) {
                        Text(
                            text = actualTime,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SalahTextSecondary,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }
        PrayerStatus.PENDING -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
                    .background(SalahGold.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .border(1.dp, SalahGold.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.HourglassBottom,
                    contentDescription = null,
                    tint = SalahGold,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Pending",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SalahGold,
                        fontSize = 12.sp
                    )
                )
            }
        }
        PrayerStatus.NOT_PRAYED -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
                    .background(Color(0xFF1E2D33), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Brightness1,
                    contentDescription = null,
                    tint = SalahTextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Not Prayed",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SalahTextSecondary,
                        fontSize = 12.sp
                    )
                )
            }
        }
        PrayerStatus.SNOOZED -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
                    .background(SalahOrange.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .border(1.dp, SalahOrange.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = null,
                    tint = SalahOrange,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Snoozed",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SalahOrange,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

@Composable
fun PrayerIcon(
    type: PrayerType,
    modifier: Modifier = Modifier,
    tint: Color = SalahGold
) {
    val icon = when (type) {
        PrayerType.FAJR -> Icons.Default.WbTwilight
        PrayerType.ZUHR -> Icons.Default.WbSunny
        PrayerType.ASR -> Icons.Default.CloudQueue
        PrayerType.MAGHRIB -> Icons.Default.Brightness5
        PrayerType.ISHA -> Icons.Default.NightsStay
    }
    Icon(
        imageVector = icon,
        contentDescription = type.displayName,
        tint = tint,
        modifier = modifier
    )
}
