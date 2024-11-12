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
          FoodItemDetailText(
              "Category: ${foodItem.foodFacts.category.name}", "categoryText", textStyle)
          FoodItemDetailText("Location: ${foodItem.location.name}", "locationText", textStyle)
          FoodItemDetailText("Status: ${foodItem.status.name}", "statusText", textStyle)
          FoodItemDetailText("Expiry Date: $formattedExpiryDate", "expiryDateText", textStyle)
          FoodItemDetailText("Open Date: $formattedOpenDate", "openDateText", textStyle)
          FoodItemDetailText("Buy Date: $formattedBuyDate", "buyDateText", textStyle)
          FoodItemDetailText(
              "Energy: ${foodItem.foodFacts.nutritionFacts.energyKcal} Kcal",
              "energyText",
              textStyle)
          FoodItemDetailText(
              "Proteins: ${foodItem.foodFacts.nutritionFacts.proteins} g",
              "proteinsText",
              textStyle)
          FoodItemDetailText(
              "Quantity: ${foodItem.foodFacts.quantity.amount} ${foodItem.foodFacts.quantity.unit}",
              "quantityText",
              textStyle)
        }
      }
}

@Composable
fun FoodItemDetailText(text: String, tag: String, style: TextStyle) {
  Text(text = text, style = style, modifier = Modifier.testTag(tag))
}
