package com.android.shelfLife.ui.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.shelfLife.R
import com.android.shelfLife.model.foodItem.FoodItem

/**
 * Composable function to display the details of a food item.
 *
 * @param foodItem The food item whose details are to be displayed.
 */
@Composable
fun FoodItemDetails(foodItem: FoodItem) {
  val textStyle = TextStyle(fontSize = 14.sp)

  val formattedExpiryDate =
      foodItem.expiryDate?.let { formatTimestampToDisplayDate(it) }
          ?: stringResource(R.string.food_item_no_expiry_date)
  val formattedOpenDate =
      foodItem.openDate?.let { formatTimestampToDisplayDate(it) }
          ?: stringResource(R.string.food_item_not_opened)
  val formattedBuyDate = foodItem.buyDate?.let { formatTimestampToDisplayDate(it) }

  ElevatedCard(
      modifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp)
              .testTag("foodItemDetailsCard")) {
        Column(modifier = Modifier.padding(16.dp)) {
          FoodItemDetailText(
              text =
                  stringResource(
                      R.string.food_item_category_label, foodItem.foodFacts.category.name),
              tag = "categoryText",
              style = textStyle)
          FoodItemDetailText(
              text = stringResource(R.string.food_item_location_label, foodItem.location.name),
              tag = "locationText",
              style = textStyle)
          FoodItemDetailText(
              text = stringResource(R.string.food_item_status_label, foodItem.status.name),
              tag = "statusText",
              style = textStyle)
          FoodItemDetailText(
              text = stringResource(R.string.food_item_expiry_date_label, formattedExpiryDate),
              tag = "expiryDateText",
              style = textStyle)
          FoodItemDetailText(
              text = stringResource(R.string.food_item_open_date_label, formattedOpenDate),
              tag = "openDateText",
              style = textStyle)
          formattedBuyDate
              ?.let { stringResource(R.string.food_item_buy_date_label, it) }
              ?.let { FoodItemDetailText(text = it, tag = "buyDateText", style = textStyle) }
          FoodItemDetailText(
              text =
                  stringResource(
                      R.string.food_item_energy_label,
                      foodItem.foodFacts.nutritionFacts.energyKcal),
              tag = "energyText",
              style = textStyle)
          FoodItemDetailText(
              text =
                  stringResource(
                      R.string.food_item_proteins_label,
                      foodItem.foodFacts.nutritionFacts.proteins),
              tag = "proteinsText",
              style = textStyle)
          FoodItemDetailText(
              text =
                  stringResource(
                      R.string.food_item_quantity_label,
                      foodItem.foodFacts.quantity.amount,
                      foodItem.foodFacts.quantity.unit.name),
              tag = "quantityText",
              style = textStyle)
        }
      }
}
