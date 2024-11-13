package com.android.shelfLife.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.ui.utils.getProgressBarState
import com.android.shelfLife.ui.utils.getThresholdsForCategory
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Composable function to display the list of food items
 *
 * @param foodItems The list of food items to display
 */
@Composable
fun ListFoodItems(foodItems: List<FoodItem>, onFoodItemClick: (FoodItem) -> Unit) {
  if (foodItems.isEmpty()) {
    // Display a prompt when there are no todos
    Box(
        modifier = Modifier.fillMaxSize().testTag("NoFoodItems"),
        contentAlignment = Alignment.Center) {
          Text(text = "No food available")
        }
  } else {
    // Display the full list
    LazyColumn(modifier = Modifier.fillMaxSize().testTag("foodItemList")) {
      items(foodItems) { item ->
        // Call a composable that renders each individual to-do item
        FoodItemCard(foodItem = item, onClick = { onFoodItemClick(item) })
      }
    }
  }
}

@Composable
fun FoodItemCard(foodItem: FoodItem, onClick: () -> Unit) {
  val expiryDate = foodItem.expiryDate
  val currentDate = Timestamp.now()

  val timeRemaining = expiryDate?.seconds?.minus(currentDate.seconds) ?: 0L
  val thresholds = getThresholdsForCategory(foodItem.foodFacts.category)

  val (progress, progressBarColor) = getProgressBarState(timeRemaining, thresholds)

  val formattedExpiryDate =
      expiryDate?.toDate()?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) }
          ?: "No Expiry Date"

  ElevatedCard(
      elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
      modifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp)
              .background(Color.White)
              .clickable { onClick() }
              .testTag("foodItemCard")) {
        Row(modifier = Modifier.padding(16.dp)) {
          Column(modifier = Modifier.weight(1f)) {
            Text(text = foodItem.foodFacts.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)

            when (foodItem.foodFacts.quantity.unit) {
              FoodUnit.GRAM ->
                  Text(text = "${foodItem.foodFacts.quantity.amount.toInt()}g", fontSize = 12.sp)
              FoodUnit.ML ->
                  Text(text = "${foodItem.foodFacts.quantity.amount.toInt()}ml", fontSize = 12.sp)
              FoodUnit.COUNT ->
                  Text(
                      text = "${foodItem.foodFacts.quantity.amount.toInt()} in stock",
                      fontSize = 12.sp)
            }

            Text(text = "Expires on $formattedExpiryDate", fontSize = 12.sp)
          }

          Spacer(modifier = Modifier.width(8.dp))

          AsyncImage(
              model = foodItem.foodFacts.imageUrl,
              contentDescription = "Food Image",
              modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
              contentScale = ContentScale.Crop)
        }
        Row {
          LinearProgressIndicator(
              progress = progress,
              color = progressBarColor,
              trackColor = LightGray,
              modifier = Modifier.fillMaxWidth().height(8.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
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
                    .testTag("foodSearchBar")) {}
      }
}
