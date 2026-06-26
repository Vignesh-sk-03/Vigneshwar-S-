package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PointsTableEntry
import com.example.data.model.Team
import com.example.data.repository.Resource
import com.example.ui.components.CricPullToRefreshBox
import com.example.ui.components.LoadingSkeletonList
import com.example.ui.components.TeamLogo
import com.example.ui.theme.CricGreen
import com.example.ui.theme.CricGold
import com.example.ui.theme.CricRed
import com.example.viewmodel.CricketViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamsScreen(
    viewModel: CricketViewModel,
    onNavigateToTeam: (String) -> Unit
) {
    val teamsState by viewModel.teamsState.collectAsState()
    val pointsTableState by viewModel.pointsTableState.collectAsState()
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Teams List, 1: Points Table

    fun refreshData() {
        scope.launch {
            isRefreshing = true
            viewModel.fetchTeams(force = true)
            viewModel.fetchPointsTable("ICC Cricket World Cup 2026", force = true)
            delay(1000)
            isRefreshing = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = "Teams"
                        )
                        Text(
                            text = "Teams & Rankings",
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
        CricPullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = ::refreshData,
            modifier = Modifier.padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary,
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("ICC RANKS", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Leaderboard, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("POINTS TABLE", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }

                if (selectedTab == 0) {
                    when (val state = teamsState) {
                        is Resource.Loading -> LoadingSkeletonList()
                        is Resource.Error -> ErrorStateView(state.message, ::refreshData)
                        is Resource.Success -> TeamsList(state.data, onNavigateToTeam)
                    }
                } else {
                    when (val state = pointsTableState) {
                        is Resource.Loading -> LoadingSkeletonList()
                        is Resource.Error -> ErrorStateView(state.message, ::refreshData)
                        is Resource.Success -> PointsTableList(state.data, onNavigateToTeam)
                    }
                }
            }
        }
    }
}

@Composable
fun TeamsList(
    teams: List<Team>,
    onNavigateToTeam: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(teams) { team ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTeam(team.id) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TeamLogo(teamShort = team.shortName)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = team.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RankingBadge("TEST", team.rankTest)
                            RankingBadge("ODI", team.rankOdi)
                            RankingBadge("T20", team.rankT20)
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = "Squad",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun RankingBadge(format: String, rank: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = format,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "#$rank",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = CricGreen
        )
    }
}

@Composable
fun PointsTableList(
    entries: List<PointsTableEntry>,
    onNavigateToTeam: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Points Table Header Row
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#  TEAM",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(2.5f)
                )
                Text(
                    text = "P",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "W",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "L",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "NRR",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1.2f)
                )
                Text(
                    text = "PTS",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    color = CricGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Table Rows
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            itemsIndexed(entries) { index, entry ->
                val teamShort = when (entry.teamName) {
                    "India" -> "IND"
                    "Australia" -> "AUS"
                    "South Africa" -> "RSA"
                    "New Zealand" -> "NZ"
                    "England" -> "ENG"
                    "Pakistan" -> "PAK"
                    else -> entry.teamName.take(3).uppercase()
                }

                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (index < 4) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f)
                        else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Position & Logo
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(2.5f)
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (index < 4) CricGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TeamLogo(teamShort, modifier = Modifier.size(24.dp))
                            Text(
                                text = entry.teamName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Played
                        Text(
                            text = "${entry.played}",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )

                        // Won
                        Text(
                            text = "${entry.won}",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )

                        // Lost
                        Text(
                            text = "${entry.lost}",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )

                        // NRR
                        Text(
                            text = (if (entry.nrr >= 0) "+" else "") + String.format("%.2f", entry.nrr),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = if (entry.nrr >= 0) CricGreen else CricRed,
                            modifier = Modifier.weight(1.2f)
                        )

                        // Points
                        Text(
                            text = "${entry.points}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            color = CricGold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "* Top 4 teams qualify for the semi-finals.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}
