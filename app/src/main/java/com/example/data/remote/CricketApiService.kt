package com.example.data.remote

import com.example.data.model.*
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CricketApiService {

    @GET("matches")
    suspend fun getMatches(
        @Query("apikey") apiKey: String? = null
    ): List<Match>

    @GET("match/{id}")
    suspend fun getMatchDetails(
        @Path("id") matchId: String,
        @Query("apikey") apiKey: String? = null
    ): Match

    @GET("match/{id}/scorecard")
    suspend fun getMatchScorecard(
        @Path("id") matchId: String,
        @Query("apikey") apiKey: String? = null
    ): Scorecard

    @GET("match/{id}/commentary")
    suspend fun getMatchCommentary(
        @Path("id") matchId: String,
        @Query("apikey") apiKey: String? = null
    ): List<Commentary>

    @GET("players")
    suspend fun getPlayers(): List<Player>

    @GET("player/{id}")
    suspend fun getPlayerProfile(
        @Path("id") playerId: String
    ): Player

    @GET("teams")
    suspend fun getTeams(): List<Team>

    @GET("points_table")
    suspend fun getPointsTable(
        @Query("tournament") tournament: String
    ): List<PointsTableEntry>
}
