package com.android.shelfLife.ui.utils

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FilterBar(filters : List<String>) {
  // State to track the selection of each filter chip
  val selectedFilters = remember { mutableStateListOf<String>() }
  val scrollState = rememberScrollState()

    Row(
        modifier =
        Modifier.horizontalScroll(scrollState) // Enables horizontal scrolling
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        filters.forEach { filter ->
            val isSelected = selectedFilters.contains(filter)
            FilterChipItem(
                text = filter,
                isSelected = isSelected,
                onClick = {
                    if (isSelected) {
                        selectedFilters.remove(filter)
                    } else {
                        selectedFilters.add(filter)
                    }
                })
        }
    }
}

@Composable
fun FilterChipItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(text = text) },
        leadingIcon =
        if (isSelected) {
            { Icon(imageVector = Icons.Default.Check, contentDescription = "Selected") }
        } else null,
        colors =
        FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.secondary,
            selectedLabelColor = Color.White,
            selectedLeadingIconColor = Color.White,
            containerColor = Color.White,
            labelColor = Color.Black
        ),
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp) // Add padding between chips
    )
}