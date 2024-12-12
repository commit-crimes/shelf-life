package com.android.shelfLife.ui.recipes.generateRecipe

import android.widget.Toast
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.shelfLife.R
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.newoverview.ListFoodItems
import com.android.shelfLife.ui.recipes.IndividualRecipe.IndividualRecipeScreen
import com.android.shelfLife.ui.utils.CustomButtons
import com.android.shelfLife.ui.utils.CustomTopAppBar
import com.android.shelfLife.viewmodel.overview.OverviewScreenViewModel
import com.android.shelfLife.viewmodel.recipes.IndividualRecipeViewModel
import com.android.shelfLife.viewmodel.recipes.RecipeGenerationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateRecipeScreen(
  navigationActions: NavigationActions,
  viewModel: RecipeGenerationViewModel = hiltViewModel(),
) {
  val currentStep by viewModel.currentStep.collectAsState()
  val title = when (currentStep) {
    0 -> "Name your AI recipe"
    1 -> "Select your ingredients"
    2 -> "Chose your options"
    else -> "Recipe Generation Complete!"
  }

  Scaffold(
    topBar = {
      CustomTopAppBar(
        onClick = { if (currentStep == 0 || viewModel.isLastStep() ) navigationActions.goBack() else viewModel.previousStep() },
        title = title,
        titleTestTag = "addRecipeTitle")
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Step Content
      when (currentStep) {
        0 -> RecipeInputStep(viewModel = viewModel, navigationActions = navigationActions)
        1 -> FoodSelectionStep(viewModel = viewModel, navigationActions)
        2 -> ReviewStep(viewModel = viewModel)
        else -> CompletionStep(viewModel, navigationActions = navigationActions)
      }
    }
  }
}

@Composable
fun RecipeInputStep(
  viewModel: RecipeGenerationViewModel,
  navigationActions: NavigationActions
) {
  val recipePrompt by viewModel.recipePrompt.collectAsState()
  val context = LocalContext.current

  Scaffold(
    bottomBar = {
      // Fixed buttons at the bottom
      Row(verticalAlignment = Alignment.Bottom) {
        // Buttons Section
        CustomButtons(
          button1OnClick = { viewModel.previousStep() },// Cancel button
          button1TestTag = "cancelButton",
          button1Text = stringResource(id = R.string.cancel_button),

          button2OnClick = {
            // Validate the recipe name
            if (recipePrompt.name.isNotBlank()) {
              viewModel.nextStep() // Proceed to the next step
            } else {
              Toast.makeText(context, R.string.recipe_title_empty_error, Toast.LENGTH_SHORT).show()
            }
          },
          button2TestTag = "recipeSubmitButton",
          button2Text = stringResource(id = R.string.next_button_text)
        )
      }
    }
    ) { paddingValues ->
    Column(modifier = Modifier.fillMaxSize()
      .padding(paddingValues)
      .padding(16.dp)) {

      OutlinedTextField(
        value = recipePrompt.name,
        onValueChange = { viewModel.updateRecipePrompt(recipePrompt.copy(name = it)) },
        label = { Text("Recipe Name") },
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}


@Composable
fun FoodSelectionStep(viewModel: RecipeGenerationViewModel, navigationActions: NavigationActions, overviewViewModel: OverviewScreenViewModel = hiltViewModel()) {
  var newFoodItem by rememberSaveable { mutableStateOf("") }
  val recipePrompt by viewModel.recipePrompt.collectAsState()
  val context = LocalContext.current
  val availableFoodItems by viewModel.availableFoodItems.collectAsState()
  val selectedFoodItems by viewModel.selectedFoodItems.collectAsState()

  // Calculate the height dynamically based on whether the list is empty
  val targetHeight = if (selectedFoodItems.isEmpty()) 0.dp else 200.dp // Adjust height as needed

// Animate the height using a spring animation
  val animatedWeight by animateFloatAsState(
    targetValue = if (selectedFoodItems.isEmpty()) 0.1f else 0.55f,
    animationSpec = tween(durationMillis = 1500, easing = LinearOutSlowInEasing)) // Adjust the duration for smoothness



  Scaffold(
    bottomBar = {
      // Fixed buttons at the bottom
      Row(verticalAlignment = Alignment.Bottom) {
        // Buttons Section
        CustomButtons(
          button1OnClick = { viewModel.previousStep() },// Cancel button
          button1TestTag = "cancelButton",
          button1Text = stringResource(id = R.string.back_button),

          button2OnClick = {
            // Validate the recipe name
            if (recipePrompt.name.isNotBlank()) {
              viewModel.nextStep() // Proceed to the next step
            } else {
              Toast.makeText(context, R.string.recipe_title_empty_error, Toast.LENGTH_SHORT).show()
            }
          },
          button2TestTag = "recipeSubmitButton",
          button2Text = stringResource(id = R.string.next_button_text)
        )
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // "Household" Section
      Text(
        text = "Household",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
      )
      Box(modifier = Modifier.weight(1f)) {
        ListFoodItems(
          foodItems = availableFoodItems,
          overviewScreenViewModel = overviewViewModel,
          onFoodItemClick = { selectedFoodItem ->
            viewModel.selectFoodItem(selectedFoodItem)
          },
          onFoodItemLongHold = { selectedFoodItem -> viewModel.selectFoodItem(selectedFoodItem) }
        )
      }
      Spacer(modifier = Modifier.height(8.dp))

      // "Selected" Section
      Text(
        text = "Ingredients (${selectedFoodItems.size})",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp, top = 4.dp)
      )
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(animatedWeight)// Smooth height animation
      ) {
        ListFoodItems(
          foodItems = selectedFoodItems,
          overviewScreenViewModel = overviewViewModel,
          onFoodItemClick = { selectedFoodItem ->
            viewModel.deselectFoodItem(selectedFoodItem)
          },
          onFoodItemLongHold = { selectedFoodItem ->  viewModel.deselectFoodItem(selectedFoodItem)},
          isSelectedItemsList = true
        )
      }

    }
  }

}

@Composable
fun ReviewStep(viewModel: RecipeGenerationViewModel) {
  val recipePrompt by viewModel.recipePrompt.collectAsState()
  val context = LocalContext.current

  Scaffold(
    bottomBar = {
      // Fixed buttons at the bottom
      Row(verticalAlignment = Alignment.Bottom) {
        // Buttons Section
        CustomButtons(
          button1OnClick = { viewModel.previousStep() },// Cancel button
          button1TestTag = "cancelButton2",
          button1Text = stringResource(id = R.string.back_button),

          button2OnClick = {
            // Validate the recipe name
            if (recipePrompt.name.isNotBlank()) {
              viewModel.nextStep() // Proceed to the next step
            } else {
              Toast.makeText(context, R.string.recipe_title_empty_error, Toast.LENGTH_SHORT).show()
            }
          },
          button2TestTag = "generateButton",
          button2Text = stringResource(id = R.string.generate_button)
        )
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier.fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp)
    ) {
      Text(
        text = "Step 3: Review Recipe",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 16.dp)
      )

      Text("Recipe Name: ${recipePrompt.name}")
      Text("Food Items: ${recipePrompt.ingredients.joinToString()}")

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
fun CompletionStep(viewModel: RecipeGenerationViewModel, navigationActions: NavigationActions) {
  val context = LocalContext.current
  val recipePrompt by viewModel.recipePrompt.collectAsState()

  Scaffold(
    bottomBar = {
      // Fixed buttons at the bottom
      Row(verticalAlignment = Alignment.Bottom) {
        // Buttons Section
        CustomButtons(
          button1OnClick = {
            viewModel.generateRecipe(onSuccess = {
              Toast.makeText(context, "Recipe Generated!", Toast.LENGTH_SHORT).show()
              navigationActions.navigateTo(Screen.INDIVIDUAL_RECIPE)
            }, onFailure = {
              Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            })
          },// Cancel button
          button1TestTag = "regenerateButton",
          button1Text = stringResource(id = R.string.regenerate_button),

          button2OnClick = {

          },
          button2TestTag = "recipeSubmitButton",
          button2Text = stringResource(id = R.string.save_button)
        )
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier.fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp)
    ) {
      Text(
        text = "Recipe Generation Complete!",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 16.dp)
      )
//      IndividualRecipeScreen(
//        navigationActions = navigationActions,
//        individualRecipeViewModel = IndividualRecipeViewModel(recipeRepository)
//      )
    }
  }
}
