package com.polyscores.kenya.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.polyscores.kenya.data.model.MatchResult
import com.polyscores.kenya.data.model.StandingsEntry

@Composable
fun StandingsTable(
    standings: List<StandingsEntry>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        // Header
        StandingsHeader()

        // Rows
        standings.forEach { entry ->
            StandingsRow(entry = entry)
        }
    }
}

@Composable
private fun StandingsHeader() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderCell("#", 40.dp)
            HeaderCell("Team", 120.dp, textAlign = TextAlign.Start)
            HeaderCell("P", 35.dp)
            HeaderCell("W", 35.dp)
            HeaderCell("D", 35.dp)
            HeaderCell("L", 35.dp)
            HeaderCell("GF", 35.dp)
            HeaderCell("GA", 35.dp)
            HeaderCell("GD", 40.dp)
            HeaderCell("Pts", 45.dp)
            HeaderCell("Form", 100.dp)
        }
    }
}

@Composable
private fun HeaderCell(
    text: String,
    width: Dp,
    textAlign: TextAlign = TextAlign.Center
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp),
        textAlign = textAlign
    )
}

@Composable
private fun StandingsRow(entry: StandingsEntry) {
    Surface(
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PositionCell(position = entry.position, width = 40.dp)
            TeamCell(name = entry.teamName, logo = entry.teamLogo, width = 120.dp)
            StatCell(value = entry.played.toString(), width = 35.dp)
            StatCell(value = entry.won.toString(), width = 35.dp)
            StatCell(value = entry.drawn.toString(), width = 35.dp)
            StatCell(value = entry.lost.toString(), width = 35.dp)
            StatCell(value = entry.goalsFor.toString(), width = 35.dp)
            StatCell(value = entry.goalsAgainst.toString(), width = 35.dp)
            GoalDifferenceCell(value = entry.goalDifference, width = 40.dp)
            PointsCell(points = entry.points, width = 45.dp)
            FormCell(form = entry.form, width = 100.dp)
        }
    }
}

@Composable
private fun PositionCell(position: Int, width: Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = position.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = when (position) {
                1 -> MaterialTheme.colorScheme.primary
                2, 3 -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TeamCell(name: String, logo: String, width: Dp) {
    Row(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCell(value: String, width: Dp) {
    Text(
        text = value,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun GoalDifferenceCell(value: Int, width: Dp) {
    Text(
        text = if (value > 0) "+$value" else value.toString(),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = if (value > 0) FontWeight.Bold else FontWeight.Normal,
        color = when {
            value > 0 -> MaterialTheme.colorScheme.primary
            value < 0 -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.onSurface
        },
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun PointsCell(points: Int, width: Dp) {
    Text(
        text = points.toString(),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun FormCell(form: List<MatchResult>, width: Dp) {
    Row(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        form.take(5).forEach { result ->
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        color = when (result) {
                            MatchResult.WIN -> MaterialTheme.colorScheme.primary
                            MatchResult.DRAW -> MaterialTheme.colorScheme.onSurfaceVariant
                            MatchResult.LOSS -> MaterialTheme.colorScheme.error
                        },
                        shape = MaterialTheme.shapes.small
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (result) {
                        MatchResult.WIN -> "W"
                        MatchResult.DRAW -> "D"
                        MatchResult.LOSS -> "L"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
