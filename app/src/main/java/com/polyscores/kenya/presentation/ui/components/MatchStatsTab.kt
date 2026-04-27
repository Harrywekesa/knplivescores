package com.polyscores.kenya.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.polyscores.kenya.data.model.Match

@Composable
fun MatchStatsTab(match: Match) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        val hasStats = match.homePossession > 0 || match.awayPossession > 0 || match.homeShots > 0 || match.awayShots > 0
        
        if (!hasStats) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Match statistics will be available soon.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return
        }

        StatComparisonRow(
            label = "Possession",
            homeValue = match.homePossession,
            awayValue = match.awayPossession,
            isPercentage = true
        )
        StatComparisonRow(
            label = "Total Shots",
            homeValue = match.homeShots,
            awayValue = match.awayShots
        )
        StatComparisonRow(
            label = "Shots on Target",
            homeValue = match.homeShotsOnTarget,
            awayValue = match.awayShotsOnTarget
        )
        StatComparisonRow(
            label = "Corners",
            homeValue = match.homeCorners,
            awayValue = match.awayCorners
        )
        StatComparisonRow(
            label = "Fouls",
            homeValue = match.homeFouls,
            awayValue = match.awayFouls
        )
    }
}

@Composable
fun StatComparisonRow(
    label: String,
    homeValue: Int,
    awayValue: Int,
    isPercentage: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isPercentage) "$homeValue%" else homeValue.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (isPercentage) "$awayValue%" else awayValue.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        val total = (homeValue + awayValue).coerceAtLeast(1)
        val homeWeight = homeValue.toFloat() / total
        val awayWeight = awayValue.toFloat() / total
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        ) {
            if (homeWeight > 0) {
                Box(
                    modifier = Modifier
                        .weight(homeWeight)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            if (homeWeight > 0 && awayWeight > 0) {
                Spacer(modifier = Modifier.width(2.dp))
            }
            if (awayWeight > 0) {
                Box(
                    modifier = Modifier
                        .weight(awayWeight)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.tertiary)
                )
            }
        }
    }
}
