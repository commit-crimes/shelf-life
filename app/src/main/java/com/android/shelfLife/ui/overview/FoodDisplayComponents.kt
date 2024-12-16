package com.android.shelfLife.ui.overview

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
 * This function renders a list of food items, and allows the user to click or long press on any
 * item to trigger a specific action. If no food items are available, a message indicating so is displayed.
 *
 * @param foodItems The list of food items to display.
 * @param overviewScreenViewModel The ViewModel to manage the food item data and actions.
 * @param onFoodItemClick Lambda function to handle click actions on a food item.
 * @param onFoodItemLongHold Lambda function to handle long press actions on a food item.
 * @param isSelectedItemsList Boolean flag to indicate if the displayed list is a selection of items.
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
            contentAlignment = Alignment.Center
        ) {
            Text(text = text)
        }
    } else {
        // Display the full list of food items
        LazyColumn(modifier = Modifier.fillMaxSize().testTag("foodItemList")) {
            items(foodItems) { item ->
                // Render each individual food item card
                FoodItemCard(
                    foodItem = item,
                    overviewScreenViewModel = overviewScreenViewModel,
                    onClick = { onFoodItemClick(item) },
                    onLongPress = { onFoodItemLongHold(item) },
                    isSelectedItemsList = isSelectedItemsList
                )
            }
        }
    }
}

/**
 * Composable function to display each food item in a card.
 *
 * This function renders a card for each food item, showing its name, quantity, expiry date, and a
 * progress bar to indicate how close it is to expiry. The card can be clicked or long pressed to trigger actions.
 *
 * @param foodItem The food item to display.
 * @param overviewScreenViewModel The ViewModel to manage the food item data and actions.
 * @param onClick Lambda function to handle click actions on the food item.
 * @param onLongPress Lambda function to handle long press actions on the food item.
 * @param isSelectedItemsList Boolean flag to indicate if the card is part of a selected items list.
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

    // Get progress bar state based on expiry
    val (progress, progressBarColor) = getProgressBarState(timeRemainingInDays)

    // Get formatted expiry date and message
    val formattedExpiryDate =
        expiryDate?.toDate()?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) }
            ?: "No Expiry Date"
    val expiryDateMessage = getExpiryMessageBasedOnDays(timeRemainingInDays, formattedExpiryDate)

    // Check if the food item has expired and update the status
    if ((expiryDateMessage == "Expired") && (foodItem.status != FoodStatus.EXPIRED)) {
        val newFoodItem = FoodItem(
            uid = foodItem.uid,
            foodFacts = foodItem.foodFacts,
            location = foodItem.location,
            expiryDate = foodItem.expiryDate,
            openDate = foodItem.openDate,
            buyDate = foodItem.buyDate,
            status = FoodStatus.EXPIRED,
            owner = foodItem.owner
        )
        overviewScreenViewModel.editFoodItem(newFoodItem)
        overviewScreenViewModel.selectFoodItem(newFoodItem)
    }

    val cardHeight = if (isSelectedItemsList) 3.dp else 8.dp
    val unit = when (foodItem.foodFacts.quantity.unit) {
        FoodUnit.GRAM -> "g"
        FoodUnit.ML -> "ml"
        FoodUnit.COUNT -> "in stock"
    }

    // Render the food item card UI
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = cardColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = elevation),
        modifier =
        Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(MaterialTheme.colorScheme.background)
            .combinedClickable(onClick = { onClick() }, onLongClick = { onLongPress() })
            .testTag("foodItemCard")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Row for displaying food item details
            Row {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = foodItem.foodFacts.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    if (!isSelectedItemsList) {
                        Text(text = "${foodItem.foodFacts.quantity.amount.toInt()}$unit", fontSize = 12.sp)
                    }
                    Text(text = expiryDateMessage, fontSize = 12.sp)
                }
                if (isSelectedItemsList) {
                    Text(text = "${foodItem.foodFacts.quantity.amount.toInt()}$unit", fontSize = 12.sp)
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
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Progress bar for expiry status
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = progressBarColor,
                trackColor = LightGray
            )
        }
    }
}