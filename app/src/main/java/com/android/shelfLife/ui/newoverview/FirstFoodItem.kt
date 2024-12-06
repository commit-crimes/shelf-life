package com.android.shelfLife.ui.newoverview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.shelfLife.R
import com.android.shelfLife.model.foodFacts.FoodFactsViewModel
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.utils.CustomButtons
import com.android.shelfLife.ui.utils.CustomTopAppBar
import com.android.shelfLife.ui.utils.FoodNameField
import com.android.shelfLife.ui.utils.validateFoodName

@Composable
fun FirstFoodItem(
    navigationActions: NavigationActions,
    foodItemViewModel: ListFoodItemsViewModel,
    foodFactsViewModel: FoodFactsViewModel,
    paddingValues: PaddingValues = PaddingValues(16.dp)
) {

    var foodName by remember { mutableStateOf("") }
    var foodNameErrorResId by remember { mutableStateOf<Int?>(null) }
    Scaffold(
        topBar = {
            CustomTopAppBar(
                onClick = { navigationActions.goBack() },
                title = "First Food Item",
                titleTestTag = "firstFoodItemTitle"
            )
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        text = stringResource(R.string.food_item_details_title),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp).testTag("firstFoodItemTitle")
                    )
                }
                item {
                    FoodNameField(
                        foodName,
                        onFoodNameChange = { newValue ->
                            foodName = newValue
                            foodNameErrorResId = validateFoodName(foodName)
                        },
                        foodNameErrorResId = foodNameErrorResId,
                        modifier = Modifier.padding(bottom = 16.dp).testTag("inputFoodName")
                    )
                }
                item {
                    CustomButtons(
                        button1OnClick = { navigationActions.goBack() },
                        button1TestTag = "cancelButton",
                        button1Text = stringResource(R.string.cancel_button),
                        button2OnClick = {
                            val isNameValid = validateFoodName(foodName) == null
                            if (isNameValid) {
                                foodItemViewModel.setNewFoodItemName(foodName)
                                foodFactsViewModel.searchByQuery(foodName)
                                navigationActions.navigateTo(Screen.CHOOSE_FOOD_ITEM)
                            }
                        },
                        button2TestTag = "submitButton",
                        button2Text = stringResource(R.string.submit_button_text)
                    )
                }
            }
        }
    )
}