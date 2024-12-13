package com.android.shelfLife.ui.recipes.generateRecipe

import android.widget.Toast
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.shelfLife.R
import com.android.shelfLife.model.newFoodItem.FoodItem
import com.android.shelfLife.model.recipe.Recipe.Companion.MAX_SERVINGS
import com.android.shelfLife.model.recipe.RecipeType
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.newoverview.ListFoodItems
import com.android.shelfLife.ui.recipes.IndividualRecipe.IndividualRecipeScreen
import com.android.shelfLife.ui.utils.CustomButtons
import com.android.shelfLife.ui.utils.CustomTopAppBar
import com.android.shelfLife.ui.utils.DropdownFields
import com.android.shelfLife.viewmodel.overview.OverviewScreenViewModel
import com.android.shelfLife.viewmodel.recipes.IndividualRecipeViewModel
import com.android.shelfLife.viewmodel.recipes.RecipeGenerationViewModel
import io.ktor.util.toLowerCasePreservingASCIIRules
import io.ktor.util.valuesOf
import kotlin.math.floor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateRecipeScreen(
  navigationActions: NavigationActions,
  viewModel: RecipeGenerationViewModel = hiltViewModel(),
) {
  val currentStep by viewModel.currentStep.collectAsState()
  val title = when (currentStep) {
    0 -> "Generate your AI recipe"
    1 -> "Select your ingredients"
    2 -> "Review your recipe"
    else -> "Generated recipe"
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
  val expanded = rememberSaveable { mutableStateOf(false) }
  val options = RecipeType.values()
    .filter { it != RecipeType.PERSONAL }
    .toList().toTypedArray()

  // Local state for form inputs
  var localName by rememberSaveable { mutableStateOf(recipePrompt.name) }
  var localRecipeType by rememberSaveable { mutableStateOf(recipePrompt.recipeType) }
  var localServings by rememberSaveable { mutableStateOf(floor(recipePrompt.servings).toInt()) }
  var localShortDuration by rememberSaveable { mutableStateOf(recipePrompt.shortDuration) }
  var localOnlyHouseHoldItems by rememberSaveable { mutableStateOf(recipePrompt.onlyHouseHoldItems) }
  var localPrioritiseSoonToExpire by rememberSaveable { mutableStateOf(recipePrompt.prioritiseSoonToExpire) }

  Scaffold(
    bottomBar = {
      Row(verticalAlignment = Alignment.Bottom) {
        CustomButtons(
          button1OnClick = { viewModel.previousStep() },
          button1TestTag = "cancelButton",
          button1Text = stringResource(id = R.string.cancel_button),

          button2OnClick = {
            if (localName.isBlank()) {
              Toast.makeText(context, R.string.recipe_title_empty_error, Toast.LENGTH_SHORT
              ).show()
              return@CustomButtons
            }

            if(localServings > MAX_SERVINGS || localServings <= 0) {
              Toast.makeText(context, "Servings must be between 1 and $MAX_SERVINGS", Toast.LENGTH_SHORT)
                .show()
              return@CustomButtons
            }
            // Update the recipePrompt in the ViewModel with local values
            viewModel.updateRecipePrompt(
              recipePrompt.copy(
                name = localName,
                recipeType = localRecipeType,
                servings = localServings.toFloat(),
                shortDuration = localShortDuration,
                onlyHouseHoldItems = localOnlyHouseHoldItems,
                prioritiseSoonToExpire = localPrioritiseSoonToExpire
              )
            )
            viewModel.nextStep()

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
        .padding(16.dp)
    ) {
      // Recipe Name Input
      OutlinedTextField(
        value = localName,
        onValueChange = { localName = it },
        label = { Text("Recipe Name") },
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(16.dp))

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        // Dropdown for Recipe Type
        Box(
          modifier = Modifier
            .weight(1f)
            .padding(top = 8.dp)
        ) {
          DropdownFields(
            label = "Recipe Type",
            options = options,
            selectedOption = localRecipeType,
            onOptionSelected = { localRecipeType = it },
            expanded = expanded.value,
            onExpandedChange = { expanded.value = it },
            optionLabel = { it.toString() }
          )
        }

        // Number of Servings Input
        OutlinedTextField(
          value = if (localServings == 0) "" else localServings.toString(),
          onValueChange = { newValue ->
            if (newValue.all { it.isDigit() }) {
              val inValue = newValue.toIntOrNull() ?: 0
              localServings = inValue
            }
          },
          label = { Text("Servings") },
          keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Toggle for Short/Long Recipe
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp)
      ) {
        Text("Recipe duration", modifier = Modifier.weight(1f))
        Text(if (localShortDuration) "Short" else "Long", modifier = Modifier.padding(end = 8.dp))
        Switch(
          checked = !localShortDuration,
          onCheckedChange = { localShortDuration = !it }
        )
      }

      // Toggle for only household items
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp)
      ) {
        Text("Only household ingredients", modifier = Modifier.weight(1f))
        Switch(
          checked = localOnlyHouseHoldItems,
          onCheckedChange = { localOnlyHouseHoldItems = it }
        )
      }

      // Toggle for prioritise soon to expire
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp)
      ) {
        Text("Use expiring ingredients first", modifier = Modifier.weight(1f))
        Switch(
          checked = localPrioritiseSoonToExpire,
          onCheckedChange = { localPrioritiseSoonToExpire = it }
        )
      }
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
  var customInstructions by remember { mutableStateOf(recipePrompt.specialInstruction) }
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
            viewModel.generateRecipe(onSuccess = {
              Toast.makeText(context, "Recipe Generated!", Toast.LENGTH_SHORT).show()
            }, onFailure = {
              Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            })
            viewModel.nextStep() // Proceed to the next step

          },
          button2TestTag = "generateButton",
          button2Text = stringResource(id = R.string.generate_button)
        )
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp)
    ) {
      // Recipe Title
      Text(
        text = recipePrompt.name,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 16.dp)
      )


      // Subtitle for Ingredients
      Text(
        text = if(recipePrompt.ingredients.isNotEmpty()) "Specified ingredients:" else "No ingredients specified",
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 8.dp)
      )

      // List of Ingredients
      Column {
        recipePrompt.ingredients.forEach { ingredient ->
          Text(
            text = "- ${ingredient.foodFacts.name}, ${ingredient.foodFacts.quantity}",
            fontSize = 16.sp,
            modifier = Modifier.padding(vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Options:",
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 8.dp)
      )

      Column {
        Text(
          text = "- ${recipePrompt.servings.toInt()} ${if (recipePrompt.servings > 1) "servings" else "serving"}",
          fontSize = 16.sp,
          fontWeight = FontWeight.Medium,
          modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
          text = "- ${recipePrompt.recipeType} recipe",
          fontSize = 16.sp,
          modifier = Modifier.padding(bottom = 8.dp)
        )

        if (recipePrompt.onlyHouseHoldItems) {
          Text(
            text = "- Only household ingredients",
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
          )
        }

        if (recipePrompt.prioritiseSoonToExpire) {
          Text(
            text = "- Prioritising soon to expire ingredients",
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
          )
        }
      }

      // Spacer for separation
      Spacer(modifier = Modifier.padding(24.dp))

      // Custom Instructions or Comments Input Field
      OutlinedTextField(
        value = customInstructions,
        onValueChange = { customInstructions = it; viewModel.updateRecipePrompt(recipePrompt.copy(specialInstruction = it)) },
        label = { Text("Custom instructions or comments") },
        modifier = Modifier
          .fillMaxWidth()
          .height(150.dp) // Adjust the height to make it taller
          .padding(top = 8.dp),
        singleLine = false,
        maxLines = 6
      )

    }

  }
}

@Composable
fun CompletionStep(viewModel: RecipeGenerationViewModel, navigationActions: NavigationActions) {
  val context = LocalContext.current
  val recipePrompt by viewModel.recipePrompt.collectAsState()
  val isGeneratingRecipe by viewModel.isGeneratingRecipe.collectAsState()
  Scaffold(
    bottomBar = {
      // Fixed buttons at the bottom
      Row(verticalAlignment = Alignment.Bottom) {
        // Buttons Section
        CustomButtons(
          button1OnClick = {
            viewModel.generateRecipe(onSuccess = {
              Toast.makeText(context, "Recipe Generated!", Toast.LENGTH_SHORT).show()
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
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp)
    ) {
      Text(
        text = "Recipe Generation Complete!",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 16.dp)
      )
      if (!isGeneratingRecipe) {
        IndividualRecipeScreen(
          navigationActions = navigationActions
        )
      }
    }
  }
}
