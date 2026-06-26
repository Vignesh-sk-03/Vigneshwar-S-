package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.CricketRepository
import com.example.data.repository.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CricketViewModel(private val repository: CricketRepository) : ViewModel() {

    // Theme & Settings
    val themeMode = repository.themeModeFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")
    val notificationsEnabled = repository.notificationsEnabledFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val notificationFrequency = repository.notificationFrequencyFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "ALL")
    val simulationActive = repository.simulationActiveFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val favoriteTeams = repository.favoriteTeamsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // UI States
    private val _matchesState = MutableStateFlow<Resource<List<Match>>>(Resource.Loading)
    val matchesState: StateFlow<Resource<List<Match>>> = _matchesState.asStateFlow()

    private val _selectedMatchDetails = MutableStateFlow<Resource<Match>>(Resource.Loading)
    val selectedMatchDetails: StateFlow<Resource<Match>> = _selectedMatchDetails.asStateFlow()

    private val _selectedScorecard = MutableStateFlow<Resource<Scorecard>>(Resource.Loading)
    val selectedScorecard: StateFlow<Resource<Scorecard>> = _selectedScorecard.asStateFlow()

    private val _selectedCommentary = MutableStateFlow<Resource<List<Commentary>>>(Resource.Loading)
    val selectedCommentary: StateFlow<Resource<List<Commentary>>> = _selectedCommentary.asStateFlow()

    private val _playersState = MutableStateFlow<Resource<List<Player>>>(Resource.Loading)
    val playersState: StateFlow<Resource<List<Player>>> = _playersState.asStateFlow()

    private val _selectedPlayer = MutableStateFlow<Resource<Player>>(Resource.Loading)
    val selectedPlayer: StateFlow<Resource<Player>> = _selectedPlayer.asStateFlow()

    private val _teamsState = MutableStateFlow<Resource<List<Team>>>(Resource.Loading)
    val teamsState: StateFlow<Resource<List<Team>>> = _teamsState.asStateFlow()

    private val _selectedTeam = MutableStateFlow<Resource<Team>>(Resource.Loading)
    val selectedTeam: StateFlow<Resource<Team>> = _selectedTeam.asStateFlow()

    private val _pointsTableState = MutableStateFlow<Resource<List<PointsTableEntry>>>(Resource.Loading)
    val pointsTableState: StateFlow<Resource<List<PointsTableEntry>>> = _pointsTableState.asStateFlow()

    // Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            repository.searchTeamsAndPlayers(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(emptyList(), emptyList()))

    // Favorites
    val favoriteMatches = repository.getFavoriteMatches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Simulated Push Notifications List
    private val _pushNotifications = MutableStateFlow<List<String>>(emptyList())
    val pushNotifications: StateFlow<List<String>> = _pushNotifications.asStateFlow()

    private var pollingJob: Job? = null
    private var lastObservedMatchState: Match? = null

    init {
        fetchMatches(force = true)
        fetchPlayers(force = true)
        fetchTeams(force = true)
        fetchPointsTable("ICC Cricket World Cup 2026", force = true)
        startSimulationPolling()
    }

    fun fetchMatches(force: Boolean = false) {
        viewModelScope.launch {
            repository.getMatches(force).collect { resource ->
                _matchesState.value = resource
                
                // Track state for notifications
                if (resource is Resource.Success) {
                    val liveMatch = resource.data.find { it.id == "match_1" }
                    if (liveMatch != null) {
                        checkForWicketsAndEvents(liveMatch)
                    }
                }
            }
        }
    }

    fun selectMatch(matchId: String) {
        viewModelScope.launch {
            repository.getMatchDetails(matchId, forceRefresh = true).collect {
                _selectedMatchDetails.value = it
            }
        }
        viewModelScope.launch {
            repository.getMatchScorecard(matchId).collect {
                _selectedScorecard.value = it
            }
        }
        viewModelScope.launch {
            repository.getMatchCommentary(matchId).collect {
                _selectedCommentary.value = it
            }
        }
    }

    fun fetchPlayers(force: Boolean = false) {
        viewModelScope.launch {
            repository.getPlayers(force).collect {
                _playersState.value = it
            }
        }
    }

    fun selectPlayer(playerId: String) {
        viewModelScope.launch {
            repository.getPlayerProfile(playerId, forceRefresh = true).collect {
                _selectedPlayer.value = it
            }
        }
    }

    fun fetchTeams(force: Boolean = false) {
        viewModelScope.launch {
            repository.getTeams(force).collect {
                _teamsState.value = it
            }
        }
    }

    fun selectTeam(teamId: String) {
        viewModelScope.launch {
            repository.getTeamById(teamId, forceRefresh = true).collect {
                _selectedTeam.value = it
            }
        }
    }

    fun fetchPointsTable(tournamentName: String, force: Boolean = false) {
        viewModelScope.launch {
            repository.getPointsTable(tournamentName, force).collect {
                _pointsTableState.value = it
            }
        }
    }

    fun toggleMatchFavorite(matchId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleMatchFavorite(matchId, isFavorite)
            // Re-fetch match details if currently viewing
            val currentDetail = _selectedMatchDetails.value
            if (currentDetail is Resource.Success && currentDetail.data.id == matchId) {
                _selectedMatchDetails.value = Resource.Success(currentDetail.data.copy(isFavorite = isFavorite))
            }
            // Trigger quick match update
            fetchMatches(force = false)
        }
    }

    fun toggleFavoriteTeamState(teamName: String) {
        viewModelScope.launch {
            repository.toggleFavoriteTeam(teamName)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setThemeModeState(mode: String) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun setNotificationsEnabledState(enabled: Boolean) {
        viewModelScope.launch {
            repository.setNotificationsEnabled(enabled)
        }
    }

    fun setNotificationFrequencyState(frequency: String) {
        viewModelScope.launch {
            repository.setNotificationFrequency(frequency)
        }
    }

    fun setSimulationActiveState(active: Boolean) {
        viewModelScope.launch {
            repository.setSimulationActive(active)
            if (active) {
                startSimulationPolling()
            } else {
                stopSimulationPolling()
            }
        }
    }

    // Polling setup for simulation & live score updates
    private fun startSimulationPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(5000) // Poll matches every 5 seconds for simulation updates
                if (simulationActive.value) {
                    fetchMatches(force = true)
                    
                    // If viewing match detail, refresh commentary and details
                    val currentDetail = _selectedMatchDetails.value
                    if (currentDetail is Resource.Success) {
                        val viewId = currentDetail.data.id
                        repository.getMatchDetails(viewId, forceRefresh = true).collect {
                            _selectedMatchDetails.value = it
                        }
                        repository.getMatchCommentary(viewId).collect {
                            _selectedCommentary.value = it
                        }
                        repository.getMatchScorecard(viewId).collect {
                            _selectedScorecard.value = it
                        }
                    }
                }
            }
        }
    }

    private fun stopSimulationPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private fun checkForWicketsAndEvents(currentMatch: Match) {
        val lastMatch = lastObservedMatchState
        if (lastMatch == null) {
            lastObservedMatchState = currentMatch
            return
        }

        if (notificationsEnabled.value) {
            // Check wicket changes
            val lastWickets = lastMatch.teamAScore.split("/").getOrNull(1)?.toIntOrNull() ?: 1
            val currentWickets = currentMatch.teamAScore.split("/").getOrNull(1)?.toIntOrNull() ?: 1

            if (currentWickets > lastWickets) {
                triggerSimulatedNotification("🏏 WICKET! Wicket falls in ${currentMatch.title}! ${currentMatch.teamAShort} is now ${currentMatch.teamAScore} (${currentMatch.teamAOvers} Ov).")
            }

            // Check if innings break (overs = 50.0 or 10 wickets)
            if (currentWickets >= 10 && lastWickets < 10) {
                triggerSimulatedNotification("☕ INNINGS BREAK! ${currentMatch.teamAShort} finishes their innings at ${currentMatch.teamAScore}. Target: ${currentMatch.target} runs.")
            }

            // Check if score changed to boundary
            val lastScore = lastMatch.teamAScore.split("/").getOrNull(0)?.toIntOrNull() ?: 240
            val currentScore = currentMatch.teamAScore.split("/").getOrNull(0)?.toIntOrNull() ?: 240
            val runsDiff = currentScore - lastScore
            if (runsDiff == 6) {
                triggerSimulatedNotification("💥 MASSIVE SIX! Ball flies over the fence! ${currentMatch.teamAShort} ${currentMatch.teamAScore}")
            } else if (runsDiff == 4) {
                triggerSimulatedNotification("✨ FOUR! Exquisite boundary to the fence! ${currentMatch.teamAShort} ${currentMatch.teamAScore}")
            }
        }

        lastObservedMatchState = currentMatch
    }

    private fun triggerSimulatedNotification(message: String) {
        val updated = _pushNotifications.value.toMutableList()
        updated.add(0, message)
        _pushNotifications.value = updated.take(15) // Keep last 15 notifications
    }

    fun clearNotifications() {
        _pushNotifications.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        stopSimulationPolling()
    }

    // ViewModel Factory
    companion object {
        fun provideFactory(repository: CricketRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CricketViewModel(repository) as T
            }
        }
    }
}
