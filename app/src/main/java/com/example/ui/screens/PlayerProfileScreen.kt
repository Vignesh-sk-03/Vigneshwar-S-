package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Player
import com.example.data.repository.Resource
import com.example.ui.components.CricPullToRefreshBox
import com.example.ui.components.LoadingSkeletonList
import com.example.ui.theme.CricGreen
import com.example.ui.theme.CricGold
import com.example.ui.theme.CricRed
import com.example.viewmodel.CricketViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerProfileScreen(
    playerId: String,
    viewModel: CricketViewModel,
    onBack: () -> Unit
) {
    val playerState by viewModel.selectedPlayer.collectAsState()
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(playerId) {
        viewModel.selectPlayer(playerId)
    }

    fun refreshPlayerData() {
        scope.launch {
            isRefreshing = true
            viewModel.selectPlayer(playerId)
            delay(1000)
            isRefreshing = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Player Profile", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        CricPullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = ::refreshPlayerData,
            modifier = Modifier.padding(innerPadding)
        ) {
            when (val state = playerState) {
                is Resource.Loading -> LoadingSkeletonList()
                is Resource.Error -> ErrorStateView(state.message, ::refreshPlayerData)
                is Resource.Success -> {
                    val player = state.data
                    PlayerProfileContent(player)
                }
            }
        }
    }
}

@Composable
fun PlayerProfileContent(player: Player) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Avatar Hero
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
        }

        // Name & Bio
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = player.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${player.role} • ${player.country}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Stats summary cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatsCompactCard(
                title = "MATCHES",
                value = "${player.matchesPlayed}",
                modifier = Modifier.weight(1f)
            )
            StatsCompactCard(
                title = "RUNS",
                value = "${player.runs}",
                modifier = Modifier.weight(1f)
            )
            StatsCompactCard(
                title = "WICKETS",
                value = "${player.wickets}",
                modifier = Modifier.weight(1f)
            )
        }

        // Detailed Stats Block
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "CAREER STATISTICS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        StatRow("Batting Avg", String.format("%.2f", player.batAvg))
                        StatRow("Strike Rate", String.format("%.2f", player.strikeRate))
                        StatRow("Batting Style", player.batStyle)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        StatRow("Bowling Avg", String.format("%.2f", player.bowlAvg))
                        StatRow("Economy Rate", String.format("%.2f", player.economy))
                        StatRow("Bowling Style", player.bowlStyle)
                    }
                }
            }
        }

        // Recent Form performance capsules
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "RECENT FORM (LAST 5)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    player.recentScores.split(",").forEach { score ->
                        val trimmed = score.trim()
                        val isHigh = trimmed.replace("*", "").toIntOrNull()?.let { it >= 50 } ?: false
                        val isWicketLog = trimmed.contains("W")

                        val pillBg = when {
                            isHigh -> CricGold.copy(alpha = 0.2f)
                            isWicketLog -> CricGreen.copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        val pillText = when {
                            isHigh -> CricGold
                            isWicketLog -> CricGreen
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(pillBg)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = trimmed,
                                fontWeight = FontWeight.Bold,
                                color = pillText,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatsCompactCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
