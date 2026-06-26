package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CricketDao {

    // Matches
    @Query("SELECT * FROM matches ORDER BY date DESC, time DESC")
    fun getAllMatches(): Flow<List<Match>>

    @Query("SELECT * FROM matches WHERE status = 'LIVE'")
    fun getLiveMatches(): Flow<List<Match>>

    @Query("SELECT * FROM matches WHERE id = :matchId")
    fun getMatchById(matchId: String): Flow<Match?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<Match>)

    @Query("UPDATE matches SET isFavorite = :isFavorite WHERE id = :matchId")
    suspend fun updateMatchFavorite(matchId: String, isFavorite: Boolean)

    @Query("SELECT * FROM matches WHERE isFavorite = 1")
    fun getFavoriteMatches(): Flow<List<Match>>

    // Players
    @Query("SELECT * FROM players")
    fun getAllPlayers(): Flow<List<Player>>

    @Query("SELECT * FROM players WHERE id = :playerId")
    fun getPlayerById(playerId: String): Flow<Player?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayers(players: List<Player>)

    @Query("SELECT * FROM players WHERE name LIKE '%' || :query || '%' OR country LIKE '%' || :query || '%'")
    fun searchPlayers(query: String): Flow<List<Player>>

    // Teams
    @Query("SELECT * FROM teams")
    fun getAllTeams(): Flow<List<Team>>

    @Query("SELECT * FROM teams WHERE id = :teamId")
    fun getTeamById(teamId: String): Flow<Team?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeams(teams: List<Team>)

    @Query("SELECT * FROM teams WHERE name LIKE '%' || :query || '%' OR shortName LIKE '%' || :query || '%'")
    fun searchTeams(query: String): Flow<List<Team>>

    // Points Table
    @Query("SELECT * FROM points_table WHERE tournamentName = :tournamentName ORDER BY points DESC, nrr DESC")
    fun getPointsTable(tournamentName: String): Flow<List<PointsTableEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPointsTable(entries: List<PointsTableEntry>)
}
