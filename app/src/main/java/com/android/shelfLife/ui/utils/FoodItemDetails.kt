package com.android.shelfLife.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.shelfLife.model.foodItem.FoodItem

@Composable
fun FoodItemDetails(foodItem: FoodItem) {
  val textStyle = TextStyle(fontSize = 14.sp)

  val formattedExpiryDate =
      foodItem.expiryDate?.let { formatTimestampToDate(it) } ?: "No Expiry Date"
  val formattedOpenDate = foodItem.openDate?.let { formatTimestampToDate(it) } ?: "Not Opened"
  val formattedBuyDate = formatTimestampToDate(foodItem.buyDate)

  ElevatedCard(
      elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
      colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
      modifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp)
              .background(Color.White)
              .testTag("foodItemDetailsCard")) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
              text = "Category: ${foodItem.foodFacts.category.name}",
              style = textStyle,
              modifier = Modifier.testTag("categoryText"))
          Text(
              text = "Location: ${foodItem.location.name}",
              style = textStyle,
              modifier = Modifier.testTag("locationText"))
          Text(
              text = "Status: ${foodItem.status.name}",
              style = textStyle,
              modifier = Modifier.testTag("statusText"))
          Text(
              text = "Expiry Date: $formattedExpiryDate",
              style = textStyle,
              modifier = Modifier.testTag("expiryDateText"))
          Text(
              text = "Open Date: $formattedOpenDate",
              style = textStyle,
              modifier = Modifier.testTag("openDateText"))
          Text(
              text = "Buy Date: $formattedBuyDate",
              style = textStyle,
              modifier = Modifier.testTag("buyDateText"))
          Text(
              text = "Energy: ${foodItem.foodFacts.nutritionFacts.energyKcal} Kcal",
              style = textStyle,
              modifier = Modifier.testTag("energyText"))
          Text(
              text = "Proteins: ${foodItem.foodFacts.nutritionFacts.proteins} g",
              style = textStyle,
              modifier = Modifier.testTag("proteinsText"))
          Text(
              text =
                  "Quantity: ${foodItem.foodFacts.quantity.amount} ${foodItem.foodFacts.quantity.unit}",
              style = textStyle,
              modifier = Modifier.testTag("quantityText"))
        }
      }
}
