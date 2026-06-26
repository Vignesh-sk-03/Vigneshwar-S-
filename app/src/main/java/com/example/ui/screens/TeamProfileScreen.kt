package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Player
import com.example.data.model.Team
import com.example.data.repository.Resource
import com.example.ui.components.CricPullToRefreshBox
import com.example.ui.components.LoadingSkeletonList
import com.example.ui.components.TeamLogo
import com.example.ui.theme.CricGreen
import com.example.viewmodel.CricketViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamProfileScreen(
    teamId: String,
    viewModel: CricketViewModel,
    onNavigateToPlayer: (String) -> Unit,
    onBack: () -> Unit
) {
    val teamState by viewModel.selectedTeam.collectAsState()
    val playersState by viewModel.playersState.collectAsState()
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(teamId) {
        viewModel.selectTeam(teamId)
        viewModel.fetchPlayers(force = false)
    }

    fun refreshTeamData() {
        scope.launch {
            isRefreshing = true
            viewModel.selectTeam(teamId)
            viewModel.fetchPlayers(force = true)
            delay(1000)
            isRefreshing = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Team Squad Profile", fontWeight = FontWeight.Black) },
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
            onRefresh = ::refreshTeamData,
            modifier = Modifier.padding(innerPadding)
        ) {
            when (val state = teamState) {
                is Resource.Loading -> LoadingSkeletonList()
                is Resource.Error -> ErrorStateView(state.message, ::refreshTeamData)
                is Resource.Success -> {
                    val team = state.data
                    val allPlayers = if (playersState is Resource.Success) {
                        (playersState as Resource.Success<List<Player>>).data
                    } else {
                        emptyList()
                    }

                    // Filter players belonging to this squad
                    val squadPlayerIds = team.squadPlayerIds.split(",").map { it.trim() }
                    val squadPlayers = allPlayers.filter { it.id in squadPlayerIds }

                    TeamProfileContent(team, squadPlayers, onNavigateToPlayer)
                }
            }
        }
    }
}

@Composable
fun TeamProfileContent(
    team: Team,
    squadPlayers: List<Player>,
    onPlayerClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Banner Header Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TeamLogo(teamShort = team.shortName, modifier = Modifier.size(64.dp))
                    Column {
                        Text(
                            text = team.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "ICC Ranking Leaderboards",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Rankings Panel
        item {
            Text(
                text = "TEAM ICC RANKINGS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = CricGreen,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    RankingItem("TEST MATCHES", "#${team.rankTest}")
                    RankingItem("ODI MATCHES", "#${team.rankOdi}")
                    RankingItem("T20 FIXTURES", "#${team.rankT20}")
                }
            }
        }

        // Squad list
        item {
            Text(
                text = "OFFICIAL TEAM ROSTER SQUAD",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = CricGreen,
                letterSpacing = 1.sp
            )
        }

        if (squadPlayers.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No players registered in roster database.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(squadPlayers) { player ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPlayerClick(player.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = player.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = player.role,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "View Profile",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RankingItem(format: String, rank: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = format,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = rank,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = CricGreen
        )
    }
}
