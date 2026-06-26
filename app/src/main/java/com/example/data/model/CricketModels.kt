package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class Match(
    @PrimaryKey val id: String,
    val title: String,
    val teamA: String,
    val teamAShort: String,
    val teamALogo: String,
    val teamB: String,
    val teamBShort: String,
    val teamBLogo: String,
    val status: String, // LIVE, UPCOMING, COMPLETED
    val date: String,
    val time: String,
    val venue: String,
    val seriesName: String,
    val teamAScore: String = "",
    val teamAOvers: String = "",
    val teamBScore: String = "",
    val teamBOvers: String = "",
    val currentInnings: String = "",
    val target: String = "",
    val result: String = "",
    val lastComment: String = "",
    val isFavorite: Boolean = false
)

@Entity(tableName = "players")
data class Player(
    @PrimaryKey val id: String,
    val name: String,
    val role: String, // Batsman, Bowler, All-rounder, Wicketkeeper
    val country: String,
    val image: String,
    val batStyle: String,
    val bowlStyle: String,
    val matchesPlayed: Int,
    val runs: Int,
    val batAvg: Double,
    val strikeRate: Double,
    val wickets: Int,
    val bowlAvg: Double,
    val economy: Double,
    val recentScores: String // Comma separated list like "45, 12, 102*, 33, 8"
)

@Entity(tableName = "teams")
data class Team(
    @PrimaryKey val id: String,
    val name: String,
    val shortName: String,
    val logo: String,
    val rankTest: Int,
    val rankOdi: Int,
    val rankT20: Int,
    val squadPlayerIds: String // Comma separated player IDs
)

@Entity(tableName = "points_table")
data class PointsTableEntry(
    @PrimaryKey val teamName: String,
    val teamLogo: String,
    val played: Int,
    val won: Int,
    val lost: Int,
    val nrr: Double,
    val points: Int,
    val tournamentName: String
)

data class Commentary(
    val overBall: String, // e.g. "44.2"
    val runText: String, // e.g. "W", "4", "6", "1"
    val batsman: String,
    val bowler: String,
    val text: String,
    val isWicket: Boolean = false,
    val isBoundary: Boolean = false
)

data class Scorecard(
    val matchId: String,
    val inningsTitle: String,
    val batsmen: List<BatsmanScore>,
    val bowlers: List<BowlerScore>,
    val extras: String,
    val totalScore: String,
    val fallOfWickets: String
)

data class BatsmanScore(
    val name: String,
    val howOut: String, // "c Dhoni b Ashwin" or "not out"
    val runs: Int,
    val balls: Int,
    val fours: Int,
    val sixes: Int,
    val strikeRate: Double
)

data class BowlerScore(
    val name: String,
    val overs: Double,
    val maidens: Int,
    val runs: Int,
    val wickets: Int,
    val economy: Double
)
