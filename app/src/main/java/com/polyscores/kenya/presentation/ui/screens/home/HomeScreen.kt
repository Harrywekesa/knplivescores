package com.polyscores.kenya.presentation.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.polyscores.kenya.data.model.Match
import com.polyscores.kenya.data.model.MatchStatus
import com.polyscores.kenya.presentation.ui.components.MatchCard
import com.polyscores.kenya.presentation.ui.components.PolyScoresTopBar
import com.polyscores.kenya.presentation.ui.components.SectionHeader
import com.polyscores.kenya.presentation.ui.components.EmptyState
import com.polyscores.kenya.presentation.ui.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    matches: List<Match>,
    isLoading: Boolean,
    onMatchClick: (String) -> Unit,
    onNavigateToMatches: () -> Unit,
    onNavigateToStandings: () -> Unit,
    onNavigateToTeams: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    val liveMatches = matches.filter { it.matchStatus == MatchStatus.LIVE || it.matchStatus == MatchStatus.SECOND_HALF || it.matchStatus == MatchStatus.EXTRA_TIME }
    val recentMatches = matches.filter {
        it.matchStatus == MatchStatus.FULLTIME ||
        it.matchStatus == MatchStatus.HALFTIME
    }.take(5)
    val upcomingMatches = matches.filter {
        it.matchStatus == MatchStatus.SCHEDULED
    }.take(5)

    Scaffold(
        topBar = {
            PolyScoresTopBar(
                title = "KNP Live Scores",
                actions = {
                    IconButton(onClick = { /* Show notifications */ }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Live Matches Section
            if (liveMatches.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Live Now",
                        actionLabel = "See All",
                        onActionClick = onNavigateToMatches
                    )
                }
                items(liveMatches) { match ->
                    MatchCard(
                        match = match,
                        onClick = { onMatchClick(match.id) }
                    )
                }
            }

            // Upcoming Matches Section
            if (upcomingMatches.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Upcoming",
                        actionLabel = "See All",
                        onActionClick = onNavigateToMatches
                    )
                }
                items(upcomingMatches) { match ->
                    MatchCard(
                        match = match,
                        onClick = { onMatchClick(match.id) }
                    )
                }
            }

            // Recent Results Section
            if (recentMatches.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Recent Results",
                        actionLabel = "See All",
                        onActionClick = onNavigateToMatches
                    )
                }
                items(recentMatches) { match ->
                    MatchCard(
                        match = match,
                        onClick = { onMatchClick(match.id) }
                    )
                }
            }

            // Empty State
            if (matches.isEmpty() && !isLoading) {
                item {
                    EmptyState(
                        message = "No matches scheduled. Check back later!"
                    )
                }
            }

            // Loading State
            if (isLoading) {
                item {
                    LoadingIndicator()
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
