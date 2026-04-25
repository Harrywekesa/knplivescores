package com.polyscores.kenya.presentation.ui.screens.matches

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.polyscores.kenya.data.model.Match
import com.polyscores.kenya.presentation.ui.components.MatchCard
import com.polyscores.kenya.presentation.ui.components.PolyScoresTopBar
import com.polyscores.kenya.presentation.ui.components.SearchBar
import com.polyscores.kenya.presentation.ui.components.EmptyState
import com.polyscores.kenya.presentation.ui.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchesScreen(
    matches: List<Match>,
    isLoading: Boolean,
    onMatchClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0=All, 1=Live, 2=Results

    val filteredMatches = remember(matches, searchQuery) {
        matches.filter { match ->
            (match.homeTeamName.contains(searchQuery, ignoreCase = true) ||
             match.awayTeamName.contains(searchQuery, ignoreCase = true) ||
             match.venue.contains(searchQuery, ignoreCase = true))
        }
    }

    Scaffold(
        topBar = {
            PolyScoresTopBar(
                title = "All Matches",
                showBackButton = true,
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search by team or venue..."
            )

            // Filter Chips
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 0.dp
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                ) {
                    Text("All", modifier = Modifier.padding(16.dp))
                }
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                ) {
                    Text("Live", modifier = Modifier.padding(16.dp))
                }
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                ) {
                    Text("Results", modifier = Modifier.padding(16.dp))
                }
            }

            // Matches List
            val displayMatches = when (selectedTab) {
                0 -> filteredMatches
                1 -> filteredMatches.filter { it.matchStatus.name == "LIVE" }
                else -> filteredMatches.filter { it.matchStatus.name == "FULLTIME" }
            }

            val pullRefreshState = androidx.compose.material3.pulltorefresh.rememberPullToRefreshState()
            var isRefreshing by remember { mutableStateOf(false) }

            LaunchedEffect(isRefreshing) {
                if (isRefreshing) {
                    kotlinx.coroutines.delay(1000) // Simulate refresh delay for Firestore
                    isRefreshing = false
                }
            }

            if (displayMatches.isEmpty() && !isLoading) {
                EmptyState(
                    message = if (searchQuery.isEmpty()) {
                        "No matches found"
                    } else {
                        "No matches match your search"
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(displayMatches) { match ->
                        MatchCard(
                            match = match,
                            onClick = { onMatchClick(match.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
