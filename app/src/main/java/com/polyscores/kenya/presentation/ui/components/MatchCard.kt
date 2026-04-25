package com.polyscores.kenya.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.polyscores.kenya.data.model.Match
import com.polyscores.kenya.data.model.MatchStatus
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

@Composable
fun MatchCard(
    match: Match,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isLive = match.matchStatus == MatchStatus.LIVE || match.matchStatus == MatchStatus.SECOND_HALF || match.matchStatus == MatchStatus.EXTRA_TIME

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Match header (league, round, venue)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.venue.ifEmpty { "Main Field" },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isLive || match.matchStatus == MatchStatus.HALFTIME) {
                    LiveMatchTimer(match = match)
                } else if (match.matchStatus == MatchStatus.POSTPONED) {
                    Text(
                        text = "POSTPONED",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = formatMatchDate(match.scheduledTime.toDate()),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Teams and scores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Team
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TeamBadge(
                        teamName = match.homeTeamName,
                        teamLogo = match.homeTeamLogo,
                        isHome = true
                    )
                }

                // Score
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (match.matchStatus == MatchStatus.SCHEDULED || match.matchStatus == MatchStatus.POSTPONED) {
                        Text(
                            text = "VS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        ScoreDisplay(
                            homeScore = match.homeScore,
                            awayScore = match.awayScore,
                            lastUpdated = match.lastUpdated,
                            status = match.matchStatus
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = getMatchStatusText(match.matchStatus),
                        style = MaterialTheme.typography.labelSmall,
                        color = when (match.matchStatus) {
                            MatchStatus.LIVE -> MaterialTheme.colorScheme.error
                            MatchStatus.FULLTIME -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                // Away Team
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TeamBadge(
                        teamName = match.awayTeamName,
                        teamLogo = match.awayTeamLogo,
                        isHome = false
                    )
                }
            }

            // Match round
            if (match.round.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = match.round,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun LiveBadge() {
    Surface(
        color = MaterialTheme.colorScheme.error,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "LIVE",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ScoreDisplay(
    homeScore: Int,
    awayScore: Int,
    lastUpdated: com.google.firebase.Timestamp,
    status: MatchStatus
) {
    // If LIVE and updated within the last 3 minutes (180000 ms), it's tentative
    val now = System.currentTimeMillis()
    val isTentative = status == MatchStatus.LIVE && (now - lastUpdated.toDate().time) < 180000
    val scoreColor = if (isTentative) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = homeScore.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = scoreColor
        )
        Text(
            text = " - ",
            style = MaterialTheme.typography.headlineMedium,
            color = scoreColor
        )
        Text(
            text = awayScore.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = scoreColor
        )
    }
}

@Composable
private fun TeamBadge(
    teamName: String,
    teamLogo: String,
    isHome: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (teamLogo.isNotEmpty()) {
            AsyncImage(
                model = teamLogo,
                contentDescription = teamName,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                error = painterResource(id = android.R.drawable.ic_menu_gallery)
            )
        } else {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = teamName.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = teamName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun LiveMatchTimer(match: Match) {
    var currentTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(match.matchStatus) {
        while (match.matchStatus == MatchStatus.LIVE || match.matchStatus == MatchStatus.SECOND_HALF || match.matchStatus == MatchStatus.EXTRA_TIME) {
            delay(1000L * 30) // Update every 30 seconds
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    val displayTime = remember(currentTimeMillis, match) {
        when (match.matchStatus) {
            MatchStatus.LIVE -> {
                val start = match.startTime?.toDate()?.time
                if (start != null) {
                    val minutes = ((currentTimeMillis - start) / 60000).coerceAtLeast(1)
                    if (minutes > 45) "45+'" else "$minutes'"
                } else {
                    "Live"
                }
            }
            MatchStatus.SECOND_HALF -> {
                val start = match.secondHalfStartTime?.toDate()?.time
                if (start != null) {
                    val minutes = 45 + ((currentTimeMillis - start) / 60000).coerceAtLeast(1)
                    if (minutes > 90) "90+'" else "$minutes'"
                } else {
                    "2nd Half"
                }
            }
            MatchStatus.HALFTIME -> "HT"
            MatchStatus.EXTRA_TIME -> "ET"
            else -> ""
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        // Red flashing dot
        val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "timerDot")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.3f,
            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                animation = androidx.compose.animation.core.tween(1000),
                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
            ),
            label = "timerAlpha"
        )
        
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(
                    color = if (match.matchStatus == MatchStatus.HALFTIME) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error.copy(alpha = alpha),
                    shape = androidx.compose.foundation.shape.CircleShape
                )
        )
        
        Text(
            text = displayTime,
            style = MaterialTheme.typography.labelSmall,
            color = if (match.matchStatus == MatchStatus.HALFTIME) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onErrorContainer,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatMatchDate(date: Date): String {
    val now = Calendar.getInstance()
    val matchCal = Calendar.getInstance().apply { time = date }
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    return when {
        matchCal.timeInMillis == today.timeInMillis -> "Today"
        matchCal.timeInMillis == today.timeInMillis + 86400000 -> "Tomorrow"
        else -> SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(date)
    }
}

private fun getMatchStatusText(status: MatchStatus): String {
    return when (status) {
        MatchStatus.SCHEDULED -> "Scheduled"
        MatchStatus.LIVE -> "Live"
        MatchStatus.HALFTIME -> "Half Time"
        MatchStatus.SECOND_HALF -> "2nd Half"
        MatchStatus.FULLTIME -> "Full Time"
        MatchStatus.EXTRA_TIME -> "Extra Time"
        MatchStatus.PENALTY_SHOOTOUT -> "Penalties"
        MatchStatus.POSTPONED -> "Postponed"
        MatchStatus.CANCELLED -> "Cancelled"
        MatchStatus.AWARDED -> "Awarded"
    }
}
