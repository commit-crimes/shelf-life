package com.android.shelfLife.ui.overview

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.shelfLife.model.foodItem.FoodItem
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Composable function to display the list of food items
 *
 * @param foodItems The list of food items to display
 */
@Composable
fun ListFoodItems(foodItems: List<FoodItem>) {
  if (foodItems.isEmpty()) {
    // Display a prompt when there are no todos
    Box(
        modifier = Modifier.fillMaxSize().testTag("NoFoodItems"),
        contentAlignment = Alignment.Center) {
          Text(text = "No food available")
        }
  } else {
    // Display the full list
    LazyColumn(modifier = Modifier.fillMaxSize()) {
      items(foodItems) { item ->
        // Call a composable that renders each individual to-do item
        FoodItemCard(foodItem = item)
      }
    }
  }
}

/**
 * Composable function to display a single food item card
 *
 * @param foodItem The food item to display
 */
@Composable
fun FoodItemCard(foodItem: FoodItem) {
  val expiryDate = foodItem.expiryDate
  Log.d("FoodItemCard", "Expiry Date: $expiryDate")
  val formattedExpiryDate =
      expiryDate?.toDate()?.let { SimpleDateFormat("MM dd, yyyy", Locale.getDefault()).format(it) }
          ?: "No Expiry Date"
  Column(
      modifier =
          Modifier.fillMaxWidth()
              .padding(vertical = 8.dp, horizontal = 16.dp)
              .background(Color.White) // Add background color if needed
              .padding(16.dp)) {
        // First Row for Date and Status
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween) {
              Text(text = foodItem.foodFacts.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)

              Text(text = foodItem.foodFacts.quantity.toString() + "in stock")
              // Display the due date on the left
              Text(text = formattedExpiryDate, fontSize = 12.sp, color = Color.Black)
            }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.Center) {
              // Display the remaining days until expiry in the middle
              Text(text = "Expires on $formattedExpiryDate", fontSize = 12.sp, color = Color.Black)
            }
      }
}

/**
 * Composable function to display the search bar for filtering food items
 *
 * @param query The current query string
 * @param onQueryChange The callback to update the query string
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSearchBar(query: String, onQueryChange: (String) -> Unit) {
  Box(
      modifier = Modifier.fillMaxWidth().padding(16.dp), // Outer padding for spacing
      contentAlignment = Alignment.Center // Center the SearchBar within the Box
      ) {
        SearchBar(
            colors =
                SearchBarDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
            shadowElevation = 3.dp,
            query = query,
            onQueryChange = onQueryChange,
            placeholder = { Text("Search food item") },
            onSearch = { /* Optional: Handle search action if needed */},
            active = false,
            onActiveChange = {},
            leadingIcon = {},
            trailingIcon = {
              IconButton(onClick = {}) {
                Icon(Icons.Default.Search, contentDescription = "Search Icon")
              }
            },
            modifier =
                Modifier.widthIn(
                        max = 600.dp) // Restrict max width to prevent over-stretching on large
                    // screens
                    .fillMaxWidth(0.9f) // Make it responsive and occupy 90% of available width
                    .testTag("searchBar")) {}
      }
}
