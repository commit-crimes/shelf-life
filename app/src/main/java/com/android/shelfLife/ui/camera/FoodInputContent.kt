// FoodInputContent.kt

package com.android.shelfLife.ui.camera

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.android.shelfLife.R
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.ui.utils.CustomButtons
import com.android.shelfLife.ui.utils.DateField
import com.android.shelfLife.ui.utils.LocationDropdownField
import com.android.shelfLife.viewmodel.overview.FoodItemViewModel
import kotlinx.coroutines.launch

/**
 * Composable function to display the food input content.
 *
 * @param foodFacts The food facts to be displayed.
 * @param onSubmit Callback function to handle the submission of the food item.
 * @param onCancel Callback function to handle the cancellation of the input.
 * @param foodItemViewModel The ViewModel for the food items.
 */
@Composable
fun FoodInputContent(
    foodItemViewModel: FoodItemViewModel,
    foodFacts: FoodFacts,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    onExpandRequested: () -> Unit
) {
  foodItemViewModel.isScanned()

  val coroutineScope = rememberCoroutineScope()
  val context = LocalContext.current

  Column(
      modifier = Modifier.fillMaxWidth().padding(16.dp).clickable { onExpandRequested() },
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Top) {
        // Food information
        Row(verticalAlignment = Alignment.CenterVertically) {
          Column(modifier = Modifier.weight(1f)) {
            Text(text = foodFacts.name, style = TextStyle(fontSize = 20.sp))

            Text(text = foodFacts.category.name, style = TextStyle(fontSize = 13.sp))
          }

          AsyncImage(
              model = foodFacts.imageUrl,
              contentDescription = "Food Image",
              modifier =
                  Modifier.size(80.dp)
                      .clip(RoundedCornerShape(8.dp))
                      .align(Alignment.CenterVertically),
              contentScale = ContentScale.Crop)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Location Dropdown
        LocationDropdownField(
            location = foodItemViewModel.location,
            onLocationChange = { foodItemViewModel.location = it },
            locationExpanded = foodItemViewModel.locationExpanded,
            onExpandedChange = { foodItemViewModel.locationExpanded = it },
            modifier = Modifier,
            testTag = "locationDropdown")

        Spacer(modifier = Modifier.height(16.dp))

        // Expire Date Field with Error Handling
        DateField(
            date = foodItemViewModel.expireDate,
            onDateChange = { newValue -> foodItemViewModel.changeExpiryDate(newValue) },
            dateErrorResId = foodItemViewModel.expireDateErrorResId,
            labelResId = R.string.expire_date_hint,
            testTag = "expireDateTextField")

        Spacer(modifier = Modifier.height(8.dp))

        // Open Date Field with Error Handling
        DateField(
            date = foodItemViewModel.openDate,
            onDateChange = { newValue -> foodItemViewModel.changeOpenDate(newValue) },
            dateErrorResId = foodItemViewModel.openDateErrorResId,
            labelResId = R.string.open_date_hint,
            testTag = "openDateTextField")

        Spacer(modifier = Modifier.height(8.dp))

        // Buy Date Field with Error Handling
        DateField(
            date = foodItemViewModel.buyDate,
            onDateChange = { newValue -> foodItemViewModel.changeBuyDate(newValue) },
            dateErrorResId = foodItemViewModel.buyDateErrorResId,
            labelResId = R.string.buy_date_hint,
            testTag = "buyDateTextField")

        Spacer(modifier = Modifier.height(16.dp))

        CustomButtons(
            button1OnClick = { onCancel() },
            button1TestTag = "cancelButton",
            button1Text = stringResource(R.string.cancel_button),
            button2OnClick = {
              coroutineScope.launch {
                val success = foodItemViewModel.submitFoodItem(foodFacts)
                if (success) {
                  // foodFactsViewModel.clearFoodFactsSuggestions()
                  Log.d("FoodInputContent", "Food item submitted successfully")
                  onSubmit()
                } else {
                  Toast.makeText(context, R.string.submission_error_message, Toast.LENGTH_SHORT)
                      .show()
                }
              }
            },
            button2TestTag = "submitButton",
            button2Text = stringResource(R.string.submit_button_text),
        )
      }
}
