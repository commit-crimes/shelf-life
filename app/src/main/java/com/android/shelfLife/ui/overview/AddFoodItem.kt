package com.android.shelfLife.ui.overview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodStatus
import com.android.shelfLife.model.foodItem.FoodStorageLocation
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodItemScreen(
    navigationActions: NavigationActions,
    houseHoldViewModel: HouseholdViewModel,
    foodItemViewModel: ListFoodItemsViewModel
) {

  var foodName by remember { mutableStateOf("") }
  var amount by remember { mutableStateOf("") }
  var unit by remember { mutableStateOf(FoodUnit.GRAM) }
  var category by remember { mutableStateOf(FoodCategory.OTHER) }
  var location by remember { mutableStateOf(FoodStorageLocation.PANTRY) }
  var expireDate by remember { mutableStateOf("") }
  var openDate by remember { mutableStateOf("") }
  var buyDate by remember { mutableStateOf(formatTimestampToDate(Timestamp.now())) }

  var unitExpanded by remember { mutableStateOf(false) }
  var categoryExpanded by remember { mutableStateOf(false) }
  var locationExpanded by remember { mutableStateOf(false) }

  Scaffold(
      modifier = Modifier.fillMaxSize(),
      topBar = { TopAppBar(title = { Text("Add Food Item") }) },
  ) { padding ->
    Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top) {
          OutlinedTextField(
              value = foodName,
              onValueChange = { foodName = it },
              label = { Text("Name of food") },
              modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp))

          Row(
              modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
              horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).padding(end = 8.dp))

                ExposedDropdownMenuBox(
                    expanded = unitExpanded,
                    onExpandedChange = { unitExpanded = !unitExpanded },
                    modifier = Modifier.weight(1f)) {
                      OutlinedTextField(
                          value = unit.name.lowercase(),
                          onValueChange = {},
                          label = { Text("Unit") },
                          readOnly = true,
                          trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded)
                          },
                          modifier = Modifier.menuAnchor())
                      ExposedDropdownMenu(
                          expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                            FoodUnit.values().forEach { selectionOption ->
                              DropdownMenuItem(
                                  text = { Text(selectionOption.name.lowercase()) },
                                  onClick = {
                                    unit = selectionOption
                                    unitExpanded = false // Close dropdown
                                  })
                            }
                          }
                    }
              }

          // Category dropdown
          ExposedDropdownMenuBox(
              expanded = categoryExpanded,
              onExpandedChange = { categoryExpanded = !categoryExpanded }) {
                OutlinedTextField(
                    value = category.name.lowercase(),
                    onValueChange = {},
                    label = { Text("Category") },
                    readOnly = true,
                    trailingIcon = {
                      ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                    },
                    modifier = Modifier.fillMaxWidth().menuAnchor())
                ExposedDropdownMenu(
                    expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                      FoodCategory.values().forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption.name.lowercase()) },
                            onClick = {
                              category = selectionOption
                              categoryExpanded = false
                            })
                      }
                    }
              }

          Spacer(modifier = Modifier.height(16.dp))

          ExposedDropdownMenuBox(
              expanded = locationExpanded,
              onExpandedChange = { locationExpanded = !locationExpanded }) {
                OutlinedTextField(
                    value = location.name.lowercase(),
                    onValueChange = {},
                    label = { Text("Location") },
                    readOnly = true,
                    trailingIcon = {
                      ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationExpanded)
                    },
                    modifier = Modifier.fillMaxWidth().menuAnchor())
                ExposedDropdownMenu(
                    expanded = locationExpanded, onDismissRequest = { locationExpanded = false }) {
                      FoodStorageLocation.values().forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption.name) },
                            onClick = {
                              location = selectionOption
                              locationExpanded = false
                            })
                      }
                    }
              }

          // For dates a future improvement could be having a calendar interfare rather than manual
          // input
          OutlinedTextField(
              value = expireDate,
              onValueChange = { expireDate = it },
              label = { Text("Expire Date") },
              placeholder = { Text("dd/mm/yyyy") },
              modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
          )

          OutlinedTextField(
              value = openDate,
              onValueChange = { openDate = it },
              label = { Text("Open Date") },
              placeholder = { Text("dd/mm/yyyy") },
              modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
          )

          OutlinedTextField(
              value = buyDate,
              onValueChange = { buyDate = it },
              label = { Text("Buy Date") },
              placeholder = { Text("dd/mm/yyyy") },
              modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
          )

          Button(
              onClick = {
                val foodFacts =
                    FoodFacts(
                        name = foodName,
                        barcode = "",
                        quantity = Quantity(amount.toDouble(), unit),
                        category = category,
                        // nutritionFacts = NutritionFacts()
                    )
                val newFoodItem = FoodItem(
                    uid = foodItemViewModel.getUID(),
                    foodFacts = foodFacts,
                    location = location,
                    expiryDate = formatDateToTimestamp(expireDate),
                    openDate = formatDateToTimestamp(openDate),
                    buyDate = formatDateToTimestamp(buyDate),
                    // Have to add logic to determine status depending on the dates given here/
                    // should be calculated
                    status = FoodStatus.CLOSED)
                  houseHoldViewModel.addFoodItem(newFoodItem)
                navigationActions.goBack()
              },
              modifier = Modifier.fillMaxWidth().height(50.dp)) {
                Text(text = "Submit", fontSize = 18.sp)
              }
        }
  }
}

fun formatTimestampToDate(timestamp: Timestamp): String {
  val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
  return sdf.format(timestamp.toDate())
}

fun formatDateToTimestamp(dateString: String): Timestamp {
  val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
  val date = sdf.parse(dateString)
  return Timestamp(date)
}
