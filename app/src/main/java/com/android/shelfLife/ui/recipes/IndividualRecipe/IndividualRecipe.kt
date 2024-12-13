package com.android.shelfLife.ui.recipes.IndividualRecipe

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.android.shelfLife.R
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.newRecipe.RecipeRepositoryFirestore
import com.android.shelfLife.model.recipe.Ingredient
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.newnavigation.BottomNavigationMenu
import com.android.shelfLife.ui.utils.CustomTopAppBar
import com.android.shelfLife.viewmodel.recipes.IndividualRecipeViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.floor

@Composable
/**
 * Composable function to display the screen for an individual recipe.
 *
 * The screen provides detailed information about a selected recipe, including its image, servings,
 * cooking time, ingredients, and instructions. If no recipe is selected, an error message is
 * displayed.
 *
 * @param navigationActions The navigation actions for handling navigation events, such as going
 *   back.
 *
 * Structure:
 * - Displays a top bar with the recipe name and a back button.
 * - Shows a bottom navigation bar for switching between main destinations in the app.
 * - If a recipe is selected:
 *     - Displays the recipe image, servings, cooking time, ingredients, and instructions in a
 *       scrollable layout.
 * - If no recipe is selected:
 *     - Shows an error message and an easter egg image.
 */
fun IndividualRecipeScreen(
    navigationActions: NavigationActions,
    individualRecipeViewModel: IndividualRecipeViewModel = hiltViewModel()
) {

  if (individualRecipeViewModel.selectedRecipeIsNonEmpty) {
    // Scaffold that provides the structure for the screen, including top and bottom bars.
    Scaffold(
        modifier = Modifier.testTag("individualRecipesScreen"),
        topBar = {
          CustomTopAppBar(
              onClick = { navigationActions.goBack() },
              title = individualRecipeViewModel.getRecipeName(),
              titleTestTag = "individualRecipeTitle")
        },
        bottomBar = {
          // Bottom navigation bar for switching between main app destinations.
          BottomNavigationMenu(
              onTabSelect = { destination -> navigationActions.navigateTo(destination) },
              tabList = LIST_TOP_LEVEL_DESTINATION,
              selectedItem = Route.RECIPES)
        },
        content = { paddingValues ->
          Column(modifier = Modifier.padding(paddingValues).fillMaxSize().testTag("recipe")) {

            // Recipe content: image, servings, time, and instructions
            Column(
                modifier =
                    Modifier.padding(8.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()) // Enable vertical scrolling
                ) {
                  // Display the recipe image (placeholder for now)
                  Image(
                      painter = painterResource(R.drawable.google_logo),
                      contentDescription = "Recipe Image",
                      modifier = Modifier.width(537.dp).height(159.dp).testTag("recipeImage"),
                      contentScale = ContentScale.FillWidth)

                  // Row displaying servings and time information
                  Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Servings: ${individualRecipeViewModel.getRecipeServing()}",
                        modifier = Modifier.testTag("recipeServings"))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Time: ${individualRecipeViewModel.getRecipeTime()} min",
                        modifier = Modifier.testTag("recipeTime"))
                  }

                  Spacer(modifier = Modifier.height(16.dp))

                  Column(modifier = Modifier.testTag("recipeIngredients")) {
                    individualRecipeViewModel.getRecipeIngredients().forEach { ingredient ->
                      DisplayIngredientNew(ingredient)
                    }
                  }

                  Column(modifier = Modifier.testTag("recipeInstructions")) {
                    individualRecipeViewModel.getRecipeInstruction().forEach { instruction ->
                      DisplayInstructionNew(instruction)
                    }
                  }
                }
          }
        })
  } else {
    // If no recipe is selected, go to the easteregg screen
    navigationActions.navigateTo(Screen.EASTER_EGG)
  }
}

@Composable
fun DisplayIngredientNew(ingredient: Ingredient) {
  val unit =
      when (ingredient.quantity.unit) {
        FoodUnit.GRAM -> "gr"
        FoodUnit.ML -> "ml"
        FoodUnit.COUNT -> ""
      }

  val amount = ingredient.quantity.amount
  val quantity = if (floor(amount) == amount) amount.toInt().toString() else amount.toString()

  Text(
      text = " - ${quantity}${unit} of ${ingredient.name}",
      modifier = Modifier.testTag("recipeIngredient"))
}

@Composable
fun DisplayInstructionNew(instruction: String) {
  // Display recipe instructions, scrollable if long
  Text(
      text = instruction, modifier = Modifier.padding(vertical = 8.dp).testTag("recipeInstruction"))
}

// this preview function allows us to see the easter egg screen
@Preview()
@Composable
private fun IndividualRecipeScreenPreviewEasterEgg() {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  val firebaseFirestore = FirebaseFirestore.getInstance()
  val recipeRepository = RecipeRepositoryFirestore(firebaseFirestore)
  val individualRecipeViewModel = viewModel { IndividualRecipeViewModel(recipeRepository) }

  // Render the IndividualRecipeScreen with a null selectedRecipe
  IndividualRecipeScreen(navigationActions = navigationActions)
}
// this preview shows the example where we do have a selected recipe
// @Preview()
// @Composable
// private fun IndividualRecipeScreenPreview() {
//  val navController = rememberNavController()
//  val navigationActions = NavigationActions(navController)
//  val firebaseFirestore = FirebaseFirestore.getInstance()
//  val recipeRepository = RecipeRepositoryFirestore(firebaseFirestore)
//  Log.i("AAAAAAAAA", "1")
//  val recipe =
//      Recipe(
//          uid = "21",
//          name = "Roast chicken",
//          instructions =
//              listOf(
//                  "Preheat your oven to 425°F (220°C). Position a rack in the center.",
//                  "Remove the chicken giblets (if present) and pat the chicken dry with paper
// towels. Dry skin crisps better during roasting.",
//                  "In a small bowl, mix the salt, pepper, garlic powder, onion powder, paprika,
// and dried thyme.",
//                  "Rub the olive oil or melted butter all over the chicken, including under the
// skin if possible.",
//                  "Generously sprinkle the seasoning mixture over the chicken, rubbing it into the
// skin and inside the cavity.",
//                  "Stuff the cavity with the lemon halves, smashed garlic cloves, and optional
// fresh herb sprigs.",
//                  "Tie the chicken legs together with kitchen twine to ensure even cooking.",
//                  "Place the chicken breast-side up in a roasting pan or oven-safe skillet.",
//                  "Roast for 75–90 minutes (approximately 40 minutes per kg), or until a meat
// thermometer inserted into the thickest part of the thigh (without touching the bone) reads
// 75°C.",
//                  "For extra crispy skin, baste the chicken with pan drippings every 30 minutes.",
//                  "Remove the chicken from the oven and let it rest for 10–15 minutes to allow the
// juices to redistribute.",
//                  "Carve the chicken and serve with your favorite sides, such as roasted
// vegetables, mashed potatoes, or a fresh salad."),
//          servings = 5.0F,
//          time = 120.minutes,
//          ingredients =
//              listOf(
//                  Ingredient("whole chicken", Quantity(1.0, FoodUnit.COUNT)),
//                  Ingredient("olive oil", Quantity(30.0, FoodUnit.ML)),
//                  Ingredient("salt", Quantity(5.0)),
//                  Ingredient("balck peppet", Quantity(2.0)),
//                  Ingredient("garlic powder", Quantity(3.0)),
//                  Ingredient("onion powder", Quantity(3.0)),
//                  Ingredient("paprika", Quantity(3.0)),
//                  Ingredient("dried thyme", Quantity(3.0)),
//                  Ingredient("lemon", Quantity(1.0, FoodUnit.COUNT)),
//                  Ingredient("garlic cloves", Quantity(4.0, FoodUnit.COUNT)),
//              ))
//  Log.i("AAAAAAAAA", "2")
//
//  recipeRepository.addRecipe(recipe, {}, {})
//  recipeRepository.selectRecipe(recipe)
//  Log.i("AAAAAAAAA", "3")
//
//  val individualRecipeViewModel = viewModel { IndividualRecipeViewModel(recipeRepository) }
//  Log.i("AAAAAAAAA", "4")
//
//  // Render the IndividualRecipeScreen with a null selectedRecipe
//  IndividualRecipeScreen(navigationActions = navigationActions)
// }
