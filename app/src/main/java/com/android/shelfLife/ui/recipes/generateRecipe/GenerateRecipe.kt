package com.android.shelfLife.ui.recipes.generateRecipe

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.shelfLife.R
import com.android.shelfLife.model.recipe.RecipeGeneratorRepository
import com.android.shelfLife.model.recipe.RecipeRepository
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.utils.CustomButtons
import com.android.shelfLife.ui.utils.CustomTopAppBar
import com.android.shelfLife.viewmodel.recipe.RecipeGenerationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateRecipeScreen(
  navigationActions: NavigationActions,
  recipeRepository: RecipeRepository,
  recipeGeneratorRepository: RecipeGeneratorRepository
) {
  val viewModel: RecipeGenerationViewModel = viewModel {
    RecipeGenerationViewModel(recipeRepository, recipeGeneratorRepository)
  }
  val currentStep by viewModel.currentStep.collectAsState()

  Scaffold(
    topBar = {
      CustomTopAppBar(
        onClick = { if (currentStep == 0) navigationActions.goBack() else viewModel.previousStep() },
        title = "Generate your AI recipe",
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
        1 -> FoodSelectionStep(viewModel = viewModel)
        2 -> ReviewStep(viewModel = viewModel)
        else -> CompletionStep(onRestart = { viewModel.resetSteps() })
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
          button2Text = stringResource(id = R.string.submit_button_text)
        )
      }
    }
    ) { paddingValues ->
    Column(modifier = Modifier.fillMaxSize()
      .padding(paddingValues)
      .padding(16.dp)) {
      Text(
        text = "Step 1: Recipe Details",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 16.dp)
      )

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
fun FoodSelectionStep(viewModel: RecipeGenerationViewModel) {
  var newFoodItem by rememberSaveable { mutableStateOf("") }
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
          button2Text = stringResource(id = R.string.submit_button_text)
        )
      }
    }
  ) { paddingValues ->
    Column(modifier = Modifier.fillMaxSize()
      .padding(paddingValues)
      .padding(16.dp)) {
      Text(
        text = "Step 2: Select Food Items",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 16.dp)
      )

      Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
          value = newFoodItem,
          onValueChange = { newFoodItem = it },
          label = { Text("Add Food Item") },
          modifier = Modifier.weight(1f)
        )

        Button(
          onClick = {
            if (newFoodItem.isNotBlank()) {
              viewModel.updateRecipePrompt(recipePrompt.copy(ingredients = recipePrompt.ingredients /*+ newFoodItem.trim()*/))
              newFoodItem = ""
            }
          },
          modifier = Modifier.padding(start = 8.dp)
        ) {
          Text("Add")
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      recipePrompt.ingredients.forEachIndexed { index, foodItem ->
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(text = "${index + 1}. $foodItem", modifier = Modifier.weight(1f))
          Button(onClick = {
            viewModel.updateRecipePrompt(
              recipePrompt.copy(
                ingredients = recipePrompt.ingredients.minus(
                  foodItem
                )
              )
            )
          }) {
            Text("Remove")
          }
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
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
          button2Text = stringResource(id = R.string.submit_button_text)
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

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Button(onClick = { viewModel.previousStep() }) {
          Text("Back")
        }
        Button(onClick = { viewModel.nextStep() }) {
          Text("Finish")
        }
      }
    }
  }
}

@Composable
fun CompletionStep(onRestart: () -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = "Recipe Generation Complete!",
      fontSize = 24.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(bottom = 16.dp)
    )

    Button(onClick = onRestart) {
      Text("Start Over")
    }
  }
}
