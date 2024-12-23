package com.android.shelfLife.ui.overview

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodStatus
import com.android.shelfLife.ui.utils.getExpiryMessageBasedOnDays
import com.android.shelfLife.ui.utils.getProgressBarState
import com.android.shelfLife.viewmodel.overview.OverviewScreenViewModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Composable function to display the list of food items.
 *
 * @param foodItems The list of food items to display.
 * @param overviewScreenViewModel The ViewModel for managing the state of the overview screen.
 * @param onFoodItemClick Callback function to handle food item click events.
 * @param onFoodItemLongHold Callback function to handle food item long press events.
 * @param isSelectedItemsList Flag to indicate if the list is for selected items.
 */
@Composable
fun ListFoodItems(
    foodItems: List<FoodItem>,
    overviewScreenViewModel: OverviewScreenViewModel,
    onFoodItemClick: (FoodItem) -> Unit,
    onFoodItemLongHold: (FoodItem) -> Unit,
    isSelectedItemsList: Boolean = false
) {
  if (foodItems.isEmpty()) {
    val text = if (isSelectedItemsList) "None selected" else "No food available"
    Box(
        modifier = Modifier.fillMaxSize().testTag("NoFoodItems"),
        contentAlignment = Alignment.Center) {
          Text(text = text)
        }
  } else {
    // Display the full list
    LazyColumn(modifier = Modifier.fillMaxSize().testTag("foodItemList")) {
      items(foodItems) { item ->
        // Call a composable that renders each individual food item
        FoodItemCard(
            foodItem = item,
            overviewScreenViewModel = overviewScreenViewModel,
            onClick = { onFoodItemClick(item) },
            onLongPress = { onFoodItemLongHold(item) },
            isSelectedItemsList = isSelectedItemsList)
      }
    }
  }
}

/**
 * Composable function to display a card for a single food item.
 *
 * @param foodItem The food item to display.
 * @param overviewScreenViewModel The ViewModel for managing the state of the overview screen.
 * @param onClick Callback function to handle click events.
 * @param onLongPress Callback function to handle long press events.
 * @param isSelectedItemsList Flag to indicate if the card is for a selected item.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FoodItemCard(
    foodItem: FoodItem,
    overviewScreenViewModel: OverviewScreenViewModel,
    onClick: () -> Unit = {},
    onLongPress: () -> Unit = {},
    isSelectedItemsList: Boolean = false
) {
  val selectedItems by overviewScreenViewModel.multipleSelectedFoodItems.collectAsState()
  val isSelected = selectedItems.contains(foodItem)
  val cardColor =
      if (isSelected) MaterialTheme.colorScheme.primaryContainer
      else if (isSelectedItemsList) MaterialTheme.colorScheme.surfaceVariant
      else MaterialTheme.colorScheme.background
  val elevation = if (isSelected) 16.dp else 8.dp
  val expiryDate = foodItem.expiryDate
  val currentDate = Timestamp.now()

  // Calculate time remaining in days
  val timeRemainingInDays =
      expiryDate?.let { ((it.seconds - currentDate.seconds) / (60 * 60 * 24)).toInt() } ?: -1

  // Get progress bar state
  val (progress, progressBarColor) = getProgressBarState(timeRemainingInDays)

  // Get formatted expiry date and message
  val formattedExpiryDate =
      expiryDate?.toDate()?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) }
          ?: "No Expiry Date"
  val expiryDateMessage = getExpiryMessageBasedOnDays(timeRemainingInDays, formattedExpiryDate)

  // Check if the food item has expired and its status has not been updated
  if ((expiryDateMessage == "Expired") && (foodItem.status != FoodStatus.EXPIRED)) {
    // Create a new food item to update the one that's wrong
    val newFoodItem =
        FoodItem(
            uid = foodItem.uid,
            foodFacts = foodItem.foodFacts,
            location = foodItem.location,
            expiryDate = foodItem.expiryDate,
            openDate = foodItem.openDate,
            buyDate = foodItem.buyDate,
            status = FoodStatus.EXPIRED,
            owner = foodItem.owner)
    overviewScreenViewModel.editFoodItem(newFoodItem)
    overviewScreenViewModel.selectFoodItem(newFoodItem)
  }

  val cardHeight = if (isSelectedItemsList) 3.dp else 8.dp
  val unit =
      when (foodItem.foodFacts.quantity.unit) {
        FoodUnit.GRAM -> "g"
        FoodUnit.ML -> "ml"
        FoodUnit.COUNT -> " in stock"
      }
  // Composable UI
  ElevatedCard(
      colors = CardDefaults.elevatedCardColors(containerColor = cardColor),
      elevation = CardDefaults.elevatedCardElevation(defaultElevation = elevation),
      modifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp)
              .background(MaterialTheme.colorScheme.background)
              .combinedClickable(onClick = { onClick() }, onLongClick = { onLongPress() })
              .testTag("foodItemCard")) {
        Column(modifier = Modifier.padding(16.dp)) {
          // Row for details
          Row {
            Column(modifier = Modifier.weight(1f)) {
              Text(text = foodItem.foodFacts.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
              if (!isSelectedItemsList) {
                Text(text = foodItem.foodFacts.quantity.toString(), fontSize = 12.sp)
              }
              Text(text = expiryDateMessage, fontSize = 12.sp)
            }
            if (isSelectedItemsList) {
              Text(text = foodItem.foodFacts.quantity.toString(), fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (!isSelectedItemsList) {
              AsyncImage(
                  model = foodItem.foodFacts.imageUrl,
                  contentDescription = "Food Image",
                  modifier =
                      Modifier.size(80.dp)
                          .clip(RoundedCornerShape(8.dp))
                          .align(Alignment.CenterVertically),
                  contentScale = ContentScale.Crop)
            }
          }

          // Progress bar
          Spacer(modifier = Modifier.height(8.dp))
          LinearProgressIndicator(
              progress = { progress },
              modifier = Modifier.fillMaxWidth().height(8.dp),
              color = progressBarColor,
              trackColor = LightGray,
          )
        }
      }
}
