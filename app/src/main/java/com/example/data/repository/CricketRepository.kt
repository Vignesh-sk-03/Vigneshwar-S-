package com.example.data.repository

import com.example.data.local.CricketDao
import com.example.data.local.SettingsDataStore
import com.example.data.model.*
import com.example.data.remote.CricketApiService
import kotlinx.coroutines.flow.*
import java.io.IOException

class CricketRepository(
    private val cricketDao: CricketDao,
    private val apiService: CricketApiService,
    private val settingsDataStore: SettingsDataStore
) {

    val themeModeFlow: Flow<String> = settingsDataStore.themeModeFlow
    val notificationsEnabledFlow: Flow<Boolean> = settingsDataStore.notificationsEnabledFlow
    val notificationFrequencyFlow: Flow<String> = settingsDataStore.notificationFrequencyFlow
    val simulationActiveFlow: Flow<Boolean> = settingsDataStore.simulationActiveFlow
    val favoriteTeamsFlow: Flow<Set<String>> = settingsDataStore.favoriteTeamsFlow

    suspend fun setThemeMode(mode: String) = settingsDataStore.setThemeMode(mode)
    suspend fun setNotificationsEnabled(enabled: Boolean) = settingsDataStore.setNotificationsEnabled(enabled)
    suspend fun setNotificationFrequency(frequency: String) = settingsDataStore.setNotificationFrequency(frequency)
    suspend fun setSimulationActive(active: Boolean) = settingsDataStore.setSimulationActive(active)
    suspend fun toggleFavoriteTeam(teamName: String) = settingsDataStore.toggleFavoriteTeam(teamName)

    // Matches
    fun getMatches(forceRefresh: Boolean = false): Flow<Resource<List<Match>>> = flow {
        emit(Resource.Loading)
        
        // 1. Emitting cached values first
        val cached = cricketDao.getAllMatches().first()
        if (cached.isNotEmpty()) {
            emit(Resource.Success(cached))
        }

        // 2. Fetch from API if forced or empty cache
        if (forceRefresh || cached.isEmpty()) {
            try {
                val remoteMatches = apiService.getMatches()
                
                // Preserve favorite flags from local cache
                val updatedMatches = remoteMatches.map { remote ->
                    val isFav = cached.find { it.id == remote.id }?.isFavorite ?: false
                    remote.copy(isFavorite = isFav)
                }

                cricketDao.insertMatches(updatedMatches)
                emit(Resource.Success(updatedMatches))
            } catch (e: Exception) {
                if (cached.isNotEmpty()) {
                    emit(Resource.Success(cached)) // Still succeed with cache
                } else {
                    emit(Resource.Error("Failed to fetch matches. Please check your connection.", e))
                }
            }
        } else {
            // Observe live updates from database Flow
            emitAll(cricketDao.getAllMatches().map { Resource.Success(it) })
        }
    }

    fun getLiveMatches(): Flow<Resource<List<Match>>> = cricketDao.getLiveMatches().map {
        Resource.Success(it)
    }

    fun getMatchDetails(matchId: String, forceRefresh: Boolean = false): Flow<Resource<Match>> = flow {
        emit(Resource.Loading)
        
        val cached = cricketDao.getMatchById(matchId).first()
        if (cached != null) {
            emit(Resource.Success(cached))
        }

        if (forceRefresh || cached == null) {
            try {
                val remote = apiService.getMatchDetails(matchId)
                val isFav = cached?.isFavorite ?: false
                val updated = remote.copy(isFavorite = isFav)
                
                cricketDao.insertMatches(listOf(updated))
                emit(Resource.Success(updated))
            } catch (e: Exception) {
                if (cached != null) {
                    emit(Resource.Success(cached))
                } else {
                    emit(Resource.Error("Match details not found.", e))
                }
            }
        } else {
            emitAll(cricketDao.getMatchById(matchId).filterNotNull().map { Resource.Success(it) })
        }
    }

    fun getMatchScorecard(matchId: String): Flow<Resource<Scorecard>> = flow {
        emit(Resource.Loading)
        try {
            val scorecard = apiService.getMatchScorecard(matchId)
            emit(Resource.Success(scorecard))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to load scorecard.", e))
        }
    }

    fun getMatchCommentary(matchId: String): Flow<Resource<List<Commentary>>> = flow {
        emit(Resource.Loading)
        try {
            val commentary = apiService.getMatchCommentary(matchId)
            emit(Resource.Success(commentary))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to load live commentary.", e))
        }
    }

    // Players
    fun getPlayers(forceRefresh: Boolean = false): Flow<Resource<List<Player>>> = flow {
        emit(Resource.Loading)
        val cached = cricketDao.getAllPlayers().first()
        if (cached.isNotEmpty()) {
            emit(Resource.Success(cached))
        }

        if (forceRefresh || cached.isEmpty()) {
            try {
                val remote = apiService.getPlayers()
                cricketDao.insertPlayers(remote)
                emit(Resource.Success(remote))
            } catch (e: Exception) {
                if (cached.isNotEmpty()) {
                    emit(Resource.Success(cached))
                } else {
                    emit(Resource.Error("Could not retrieve players list.", e))
                }
            }
        } else {
            emitAll(cricketDao.getAllPlayers().map { Resource.Success(it) })
        }
    }

    fun getPlayerProfile(playerId: String, forceRefresh: Boolean = false): Flow<Resource<Player>> = flow {
        emit(Resource.Loading)
        val cached = cricketDao.getPlayerById(playerId).first()
        if (cached != null) {
            emit(Resource.Success(cached))
        }

        if (forceRefresh || cached == null) {
            try {
                val remote = apiService.getPlayerProfile(playerId)
                cricketDao.insertPlayers(listOf(remote))
                emit(Resource.Success(remote))
            } catch (e: Exception) {
                if (cached != null) {
                    emit(Resource.Success(cached))
                } else {
                    emit(Resource.Error("Player profile details offline.", e))
                }
            }
        } else {
            emitAll(cricketDao.getPlayerById(playerId).filterNotNull().map { Resource.Success(it) })
        }
    }

    // Teams
    fun getTeams(forceRefresh: Boolean = false): Flow<Resource<List<Team>>> = flow {
        emit(Resource.Loading)
        val cached = cricketDao.getAllTeams().first()
        if (cached.isNotEmpty()) {
            emit(Resource.Success(cached))
        }

        if (forceRefresh || cached.isEmpty()) {
            try {
                val remote = apiService.getTeams()
                cricketDao.insertTeams(remote)
                emit(Resource.Success(remote))
            } catch (e: Exception) {
                if (cached.isNotEmpty()) {
                    emit(Resource.Success(cached))
                } else {
                    emit(Resource.Error("Could not retrieve teams list.", e))
                }
            }
        } else {
            emitAll(cricketDao.getAllTeams().map { Resource.Success(it) })
        }
    }

    fun getTeamById(teamId: String, forceRefresh: Boolean = false): Flow<Resource<Team>> = flow {
        emit(Resource.Loading)
        val cached = cricketDao.getTeamById(teamId).first()
        if (cached != null) {
            emit(Resource.Success(cached))
        }

        if (forceRefresh || cached == null) {
            try {
                val remote = apiService.getTeams().find { it.id == teamId }
                if (remote != null) {
                    cricketDao.insertTeams(listOf(remote))
                    emit(Resource.Success(remote))
                } else {
                    emit(Resource.Error("Team not found."))
                }
            } catch (e: Exception) {
                if (cached != null) {
                    emit(Resource.Success(cached))
                } else {
                    emit(Resource.Error("Team profile offline.", e))
                }
            }
        } else {
            emitAll(cricketDao.getTeamById(teamId).filterNotNull().map { Resource.Success(it) })
        }
    }

    // Points Table
    fun getPointsTable(tournamentName: String, forceRefresh: Boolean = false): Flow<Resource<List<PointsTableEntry>>> = flow {
        emit(Resource.Loading)
        val cached = cricketDao.getPointsTable(tournamentName).first()
        if (cached.isNotEmpty()) {
            emit(Resource.Success(cached))
        }

        if (forceRefresh || cached.isEmpty()) {
            try {
                val remote = apiService.getPointsTable(tournamentName)
                cricketDao.insertPointsTable(remote)
                emit(Resource.Success(remote))
            } catch (e: Exception) {
                if (cached.isNotEmpty()) {
                    emit(Resource.Success(cached))
                } else {
                    emit(Resource.Error("Points table not available offline.", e))
                }
            }
        } else {
            emitAll(cricketDao.getPointsTable(tournamentName).map { Resource.Success(it) })
        }
    }

    // Search
    fun searchTeamsAndPlayers(query: String): Flow<Pair<List<Team>, List<Player>>> {
        if (query.isBlank()) {
            return flowOf(Pair(emptyList(), emptyList()))
        }
        val teamsFlow = cricketDao.searchTeams(query)
        val playersFlow = cricketDao.searchPlayers(query)
        return combine(teamsFlow, playersFlow) { teams, players ->
            Pair(teams, players)
        }
    }

    // Favorites
    suspend fun toggleMatchFavorite(matchId: String, isFavorite: Boolean) {
        cricketDao.updateMatchFavorite(matchId, isFavorite)
    }

    fun getFavoriteMatches(): Flow<List<Match>> = cricketDao.getFavoriteMatches()
}
