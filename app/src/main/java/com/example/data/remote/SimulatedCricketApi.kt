package com.example.data.remote

import com.example.data.model.*
import kotlinx.coroutines.delay
import java.util.UUID
import kotlin.random.Random

class SimulatedCricketApi : CricketApiService {

    // Seed initial players
    private val playersDb = mutableListOf(
        // India
        Player("p_virat", "Virat Kohli", "Batsman", "India", "", "Right hand bat", "Right arm medium", 295, 13848, 58.67, 93.54, 4, 166.2, 6.12, "85, 117*, 34, 50, 4"),
        Player("p_rohit", "Rohit Sharma", "Batsman", "India", "", "Right hand bat", "Right arm offbreak", 265, 10709, 49.12, 91.97, 8, 64.3, 5.21, "131, 8, 46, 86, 47"),
        Player("p_bumrah", "Jasprit Bumrah", "Bowler", "India", "", "Right hand bat", "Right arm fast", 89, 79, 7.9, 54.3, 149, 23.55, 4.59, "W, 2W, 4W, 1W, W"),
        Player("p_pant", "Rishabh Pant", "Wicketkeeper", "India", "", "Left hand bat", "None", 34, 890, 31.78, 105.4, 0, 0.0, 0.0, "20, 64, 15, 88*, 3"),
        Player("p_hardik", "Hardik Pandya", "All-rounder", "India", "", "Right hand bat", "Right arm fast-medium", 85, 1769, 34.01, 110.2, 84, 35.6, 5.51, "40, 2W, 11, 3W, 51*"),
        
        // Australia
        Player("p_smith", "Steve Smith", "Batsman", "Australia", "", "Right hand bat", "Right arm legbreak", 155, 5420, 43.54, 87.41, 28, 34.5, 5.34, "44, 71, 0, 104, 32"),
        Player("p_cummins", "Pat Cummins", "Bowler", "Australia", "", "Right hand bat", "Right arm fast", 85, 395, 12.1, 78.4, 141, 28.1, 4.89, "2W, 3W, 1W, W, 2W"),
        Player("p_maxwell", "Glenn Maxwell", "All-rounder", "Australia", "", "Right hand bat", "Right arm offbreak", 138, 3895, 35.4, 126.8, 68, 38.2, 5.56, "201*, 41, 15, 12, 5"),
        Player("p_starc", "Mitchell Starc", "Bowler", "Australia", "", "Left hand bat", "Left arm fast", 121, 512, 10.4, 71.2, 236, 22.8, 5.12, "3W, W, 4W, 2W, W"),
        Player("p_head", "Travis Head", "Batsman", "Australia", "", "Left hand bat", "Right arm offbreak", 65, 2450, 41.5, 102.3, 15, 42.1, 5.8, "137, 62, 11, 0, 50")
    )

    // Seed teams
    private val teamsDb = mutableListOf(
        Team("t_ind", "India", "IND", "", 1, 1, 1, "p_virat,p_rohit,p_bumrah,p_pant,p_hardik"),
        Team("t_aus", "Australia", "AUS", "", 2, 2, 2, "p_smith,p_cummins,p_maxwell,p_starc,p_head")
    )

    // Match list
    private var matchesDb = mutableListOf(
        Match(
            id = "match_1",
            title = "ICC Men's World Cup Final",
            teamA = "India",
            teamAShort = "IND",
            teamALogo = "india_logo",
            teamB = "Australia",
            teamBShort = "AUS",
            teamBLogo = "australia_logo",
            status = "LIVE",
            date = "Today",
            time = "14:00",
            venue = "Narendra Modi Stadium, Ahmedabad",
            seriesName = "ICC Cricket World Cup 2026",
            teamAScore = "240/1",
            teamAOvers = "38.2",
            teamBScore = "Yet to bat",
            teamBOvers = "0.0",
            currentInnings = "IND",
            target = "241",
            result = "India chooses to bat first",
            lastComment = "Beautiful cover drive by Kohli for a boundary!",
            isFavorite = false
        ),
        Match(
            id = "match_2",
            title = "T20 Tri-Series Match 3",
            teamA = "England",
            teamAShort = "ENG",
            teamALogo = "england_logo",
            teamB = "Pakistan",
            teamBShort = "PAK",
            teamBLogo = "pakistan_logo",
            status = "UPCOMING",
            date = "Tomorrow",
            time = "18:30",
            venue = "Lord's, London",
            seriesName = "T20 Tri-Series 2026",
            teamAScore = "Yet to bat",
            teamAOvers = "0",
            teamBScore = "Yet to bat",
            teamBOvers = "0",
            currentInnings = "",
            target = "",
            result = "Match starts in 1 day",
            lastComment = "England and Pakistan squads are arriving tomorrow.",
            isFavorite = false
        ),
        Match(
            id = "match_3",
            title = "Test Match Series - Day 5",
            teamA = "South Africa",
            teamAShort = "RSA",
            teamALogo = "rsa_logo",
            teamB = "New Zealand",
            teamBShort = "NZ",
            teamBLogo = "nz_logo",
            status = "COMPLETED",
            date = "Yesterday",
            time = "10:00",
            venue = "SuperSport Park, Centurion",
            seriesName = "New Zealand Tour of South Africa 2026",
            teamAScore = "354 & 210",
            teamAOvers = "112.0 & 68.2",
            teamBScore = "289 & 260",
            teamBOvers = "94.5 & 80.1",
            currentInnings = "",
            target = "276",
            result = "South Africa won by 15 runs",
            lastComment = "Rabada claims the final wicket of Mitchell!",
            isFavorite = true
        )
    )

    // Points Table
    private var pointsTableDb = listOf(
        PointsTableEntry("India", "", 8, 7, 1, 1.45, 14, "ICC Cricket World Cup 2026"),
        PointsTableEntry("Australia", "", 8, 6, 2, 0.98, 12, "ICC Cricket World Cup 2026"),
        PointsTableEntry("South Africa", "", 8, 5, 3, 0.42, 10, "ICC Cricket World Cup 2026"),
        PointsTableEntry("New Zealand", "", 8, 4, 4, -0.12, 8, "ICC Cricket World Cup 2026"),
        PointsTableEntry("England", "", 8, 3, 5, -0.32, 6, "ICC Cricket World Cup 2026"),
        PointsTableEntry("Pakistan", "", 8, 3, 5, -0.65, 6, "ICC Cricket World Cup 2026")
    )

    // Commentary generators
    private var liveCommentaryList = mutableListOf(
        Commentary("38.2", "4", "Virat Kohli", "Mitchell Starc", "Starc bowls a full delivery outside off stump. Kohli leans in and hits an elegant cover drive for four!", false, true),
        Commentary("38.1", "1", "Rohit Sharma", "Mitchell Starc", "Starc back of a length delivery, tucked away to deep square leg for a single.", false, false),
        Commentary("37.6", "0", "Rohit Sharma", "Pat Cummins", "Good length ball outside off, Rohit lets it go through to the keeper.", false, false),
        Commentary("37.5", "6", "Rohit Sharma", "Pat Cummins", "SHORT BALL! Rohit Sharma rolls his wrists and nails his signature pull shot over mid-wicket for SIX!", false, true),
        Commentary("37.4", "W", "Shubman Gill", "Pat Cummins", "OUT! CAUGHT! Gill tries to clear mid-on but doesn't get the timing. Mitchell Starc runs back and takes a brilliant diving catch! Big breakthrough.", true, false),
        Commentary("37.3", "1", "Virat Kohli", "Pat Cummins", "Quick tap to extra cover, Kohli calls for a quick single." , false, false)
    )

    // Generate simulated score progression
    init {
        // Start a slow simulation ticker in background
        // Wait, since this is a class, we can progress score whenever API is polled
    }

    private fun progressSimulation() {
        val matchIndex = matchesDb.indexOfFirst { it.id == "match_1" }
        if (matchIndex != -1) {
            val oldMatch = matchesDb[matchIndex]
            val oversString = oldMatch.teamAOvers
            val currentOvers = oversString.toDoubleOrNull() ?: 38.2
            
            // Advance over by 0.1
            var nextOvers = currentOvers + 0.1
            // Check if over is completed (.6 is not valid, goes to next int)
            val decimals = (nextOvers * 10).toInt() % 10
            if (decimals >= 6) {
                nextOvers = nextOvers.toInt() + 1.0
            }
            
            // Extract score and wickets
            val scoreParts = oldMatch.teamAScore.split("/")
            var score = scoreParts[0].toIntOrNull() ?: 240
            var wickets = if (scoreParts.size > 1) scoreParts[1].toIntOrNull() ?: 1 else 1

            // Random ball result
            val r = Random.nextInt(100)
            val run: String
            var commentText = ""
            var isWicket = false
            var isBoundary = false

            when {
                r < 5 -> { // Wicket
                    wickets += 1
                    run = "W"
                    isWicket = true
                    commentText = "OUT! Timber! Cummins bowls a beautiful inswinger, beating the inside edge and crashing into the off stump! Excellent bowling."
                }
                r < 15 -> { // Six
                    score += 6
                    run = "6"
                    isBoundary = true
                    commentText = "SIX! High and handsome! Smashed straight down the ground, easily clearing the boundary rope!"
                }
                r < 35 -> { // Four
                    score += 4
                    run = "4"
                    isBoundary = true
                    commentText = "FOUR! Gorgeous shot! Short of a length delivery, slapped past point. Terrific timing."
                }
                r < 50 -> { // Dot ball
                    run = "0"
                    commentText = "Good length, defensive block back to the bowler."
                }
                r < 80 -> { // Single
                    score += 1
                    run = "1"
                    commentText = "Tucked into the leg-side gap for a quick single."
                }
                else -> { // Double
                    score += 2
                    run = "2"
                    commentText = "Clipped past deep mid-wicket. Hard running between wickets yields two runs."
                }
            }

            if (wickets >= 10) {
                // Innings ends
                matchesDb[matchIndex] = oldMatch.copy(
                    teamAScore = "$score/10",
                    teamAOvers = "50.0",
                    status = "LIVE",
                    result = "IND innings completed. Target: ${score + 1}",
                    lastComment = "Innings over! IND is all out."
                )
            } else {
                val nextScoreStr = "$score/$wickets"
                val nextOversStr = String.format("%.1f", nextOvers)
                val newMatch = oldMatch.copy(
                    teamAScore = nextScoreStr,
                    teamAOvers = nextOversStr,
                    lastComment = commentText
                )
                matchesDb[matchIndex] = newMatch

                // Prepend to commentary
                val bowler = if (nextOvers.toInt() % 2 == 0) "Mitchell Starc" else "Pat Cummins"
                val batsman = if (Random.nextBoolean()) "Virat Kohli" else "Rohit Sharma"
                
                liveCommentaryList.add(0, Commentary(
                    overBall = nextOversStr,
                    runText = run,
                    batsman = batsman,
                    bowler = bowler,
                    text = commentText,
                    isWicket = isWicket,
                    isBoundary = isBoundary
                ))
            }
        }
    }

    override suspend fun getMatches(apiKey: String?): List<Match> {
        delay(300) // Simulated delay
        progressSimulation()
        return matchesDb
    }

    override suspend fun getMatchDetails(matchId: String, apiKey: String?): Match {
        delay(300)
        progressSimulation()
        return matchesDb.find { it.id == matchId } ?: matchesDb[0]
    }

    override suspend fun getMatchScorecard(matchId: String, apiKey: String?): Scorecard {
        delay(400)
        // Simulated full scorecard for India vs Australia
        val scoreParts = matchesDb.find { it.id == matchId }?.teamAScore?.split("/") ?: listOf("245", "2")
        val score = scoreParts[0].toIntOrNull() ?: 245
        
        return Scorecard(
            matchId = matchId,
            inningsTitle = "India 1st Innings",
            batsmen = listOf(
                BatsmanScore("Rohit Sharma", "not out", 92, 58, 8, 5, 158.62),
                BatsmanScore("Shubman Gill", "c Starc b Cummins", 45, 34, 4, 1, 132.35),
                BatsmanScore("Virat Kohli", "not out", score - 92 - 45 - 12, 60, 9, 2, 115.00),
                BatsmanScore("Extras", "b 4, lb 3, w 5", 12, 0, 0, 0, 0.0)
            ),
            bowlers = listOf(
                BowlerScore("Mitchell Starc", 8.0, 0, 56, 0, 7.0),
                BowlerScore("Josh Hazlewood", 7.0, 1, 42, 0, 6.0),
                BowlerScore("Pat Cummins", 8.0, 0, 48, 1, 6.0),
                BowlerScore("Adam Zampa", 9.0, 0, 68, 0, 7.56),
                BowlerScore("Glenn Maxwell", 6.2, 0, 31, 0, 4.89)
            ),
            extras = "12 (wd 5, nb 0, lb 3, b 4)",
            totalScore = "$score (1 wkts, 38.2 Ov)",
            fallOfWickets = "1-112 (Shubman Gill, 18.2 Ov)"
        )
    }

    override suspend fun getMatchCommentary(matchId: String, apiKey: String?): List<Commentary> {
        delay(300)
        return liveCommentaryList.take(30)
    }

    override suspend fun getPlayers(): List<Player> {
        delay(300)
        return playersDb
    }

    override suspend fun getPlayerProfile(playerId: String): Player {
        delay(300)
        return playersDb.find { it.id == playerId } ?: playersDb[0]
    }

    override suspend fun getTeams(): List<Team> {
        delay(300)
        return teamsDb
    }

    override suspend fun getPointsTable(tournament: String): List<PointsTableEntry> {
        delay(300)
        return pointsTableDb
    }
}
