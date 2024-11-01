package com.android.shelfLife.ui.recipes

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.model.recipe.ListRecipesViewModel
import com.android.shelfLife.ui.navigation.HouseHoldSelectionDrawer
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.overview.FirstTimeWelcomeScreen

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(
    navigationActions: NavigationActions,
    listRecipesViewModel: ListRecipesViewModel,
    householdViewModel: HouseholdViewModel
) {

  var title by remember { mutableStateOf("") }
  var servings by remember { mutableStateOf("0.0") }
  var time by remember { mutableStateOf("0.0") }

  Scaffold(
      modifier = Modifier.testTag("addRecipeScreen"),
      topBar = {
          TopAppBar(
              colors =
              TopAppBarDefaults.topAppBarColors(
                  containerColor = MaterialTheme.colorScheme.secondaryContainer,
                  titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                  navigationIconContentColor =
                  MaterialTheme.colorScheme.onSecondaryContainer,
                  actionIconContentColor =
                  MaterialTheme.colorScheme.onSecondaryContainer
              ),
              modifier = Modifier.testTag("topBar"),
              navigationIcon = {
                  // Back button to return to the previous screen
                  IconButton(
                      onClick = { navigationActions.goBack() },
                      modifier = Modifier.testTag("goBackArrow")
                  ) {
                      Icon(
                          imageVector = Icons.Default.ArrowBack,
                          contentDescription = "Go back Icon"
                      )
                  }
              },
              // Title of the screen: Recipe name
              title = {
                  Text(
                      text = "Add your own recipe",
                      style =
                      MaterialTheme.typography.bodyLarge.copy(
                          fontSize = 24.sp, fontWeight = FontWeight.Bold
                      )
                  )
              })
      },
  ) {
      Column(modifier = Modifier.fillMaxSize()) {
          OutlinedTextField(
              value = title,
              onValueChange = { title = it },
              label = { Text("Recipe title") },
              modifier =
              Modifier.fillMaxWidth()
                  .padding(horizontal = 20.dp)
                  .padding(vertical = 20.dp)
                  .testTag("inputRecipeTitle")
          )

          OutlinedTextField(
              value = servings,
              onValueChange = { servings = it },
              label = { Text("Servings") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              modifier =
              Modifier.fillMaxWidth()
                  .padding(horizontal = 20.dp)
                  .padding(vertical = 20.dp)
                  .testTag("inputRecipeServings")
          )

          OutlinedTextField(
              value = time,
              onValueChange = { time = it },
              label = { Text("Time in minutes") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              modifier =
              Modifier.fillMaxWidth()
                  .padding(horizontal = 20.dp)
                  .padding(vertical = 20.dp)
                  .testTag("inputRecipeTime")
          )


      }
  }
}

