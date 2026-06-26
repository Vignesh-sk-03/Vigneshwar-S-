package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Match
import com.example.ui.theme.CricGreen
import com.example.ui.theme.CricGold
import com.example.ui.theme.CricRed
import com.example.viewmodel.CricketViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: CricketViewModel,
    onNavigateToMatch: (String) -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val notificationFrequency by viewModel.notificationFrequency.collectAsState()
    val simulationActive by viewModel.simulationActive.collectAsState()
    val favoriteTeams by viewModel.favoriteTeams.collectAsState()
    val favoriteMatches by viewModel.favoriteMatches.collectAsState()
    
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile"
                        )
                        Text(
                            text = "Settings & Favorites",
                            fontWeight = FontWeight.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Hero Profile/Avatar Section
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_cric_taught_logo),
                            contentDescription = "Cric.taught Logo",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                        )
                        Column {
                            Text(
                                text = "Cricket Enthusiast",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "vigneshwarsk2003@gmail.com",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Ticker / Live Simulation Settings
            item {
                Column {
                    Text(
                        text = "LIVE TICKER ENGINE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CricGreen,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FlashOn,
                                        contentDescription = "Simulation",
                                        tint = CricGold
                                    )
                                    Column {
                                        Text(
                                            text = "Simulated Live Ticker",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Auto-update matches with live actions",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Switch(
                                    checked = simulationActive,
                                    onCheckedChange = { viewModel.setSimulationActiveState(it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = CricGreen)
                                )
                            }
                        }
                    }
                }
            }

            // Theme Preferences
            item {
                Column {
                    Text(
                        text = "VISUAL PREFERENCES",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CricGreen,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "App Theme Select",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("SYSTEM", "LIGHT", "DARK").forEach { mode ->
                                    val isSelected = themeMode == mode
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .clickable { viewModel.setThemeModeState(mode) }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = mode,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Push Notifications Configuration
            item {
                Column {
                    Text(
                        text = "ALERT CHANNEL NOTIFICATIONS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CricGreen,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Push alerts for key moments",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Alert on wickets, innings break, boundaries",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = notificationsEnabled,
                                    onCheckedChange = { viewModel.setNotificationsEnabledState(it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = CricGreen)
                                )
                            }
                        }
                    }
                }
            }

            // Persistence: Favorite Matches
            item {
                Column {
                    Text(
                        text = "PERSISTED FAVORITE MATCHES",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CricRed,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (favoriteMatches.isEmpty()) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No favorited matches yet. Heart any match card in Home to pin it here offline!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        // Display list of favorited matches
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            favoriteMatches.forEach { match ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToMatch(match.id) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Favorite,
                                                tint = CricRed,
                                                contentDescription = "Pinned"
                                            )
                                            Column {
                                                Text(
                                                    text = "${match.teamAShort} vs ${match.teamBShort}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = match.seriesName,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = "Details",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
