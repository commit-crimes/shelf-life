package com.android.shelfLife.ui.recipes.IndividualRecipe

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.shelfLife.R
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.recipe.Ingredient
import com.android.shelfLife.ui.navigation.BottomNavigationMenu
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.utils.CustomTopAppBar
import com.android.shelfLife.viewmodel.recipes.IndividualRecipeViewModel
import kotlin.math.floor
import kotlinx.coroutines.launch

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
    individualRecipeViewModel: IndividualRecipeViewModel =
        hiltViewModel<IndividualRecipeViewModel>()
) {

  val coroutineScope = rememberCoroutineScope()

  if (individualRecipeViewModel.selectedRecipeIsNonEmpty) {
    // Scaffold that provides the structure for the screen, including top and bottom bars.
    Scaffold(
        modifier = Modifier.testTag("individualRecipesScreen"),
        topBar = {
          CustomTopAppBar(
              onClick = {
                navigationActions.goBack()
                individualRecipeViewModel.deselectRecipe()
              },
              title = individualRecipeViewModel.getRecipeName(),
              titleTestTag = "individualRecipeTitle",
              actions = {
                IconButton(
                    onClick = {
                      coroutineScope.launch {
                        individualRecipeViewModel.deleteSelectedRecipe()
                        navigationActions.goBack()
                      }
                    },
                    modifier = Modifier.testTag("deleteFoodItem")) {
                      Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Icon")
                    }
              })
        },
        bottomBar = {
          BottomNavigationMenu(
              onTabSelect = { destination -> navigationActions.navigateTo(destination) },
              tabList = LIST_TOP_LEVEL_DESTINATION,
              selectedItem = Route.RECIPES)
        },
        floatingActionButton = {
          FloatingActionButton(
              onClick = { navigationActions.navigateTo(Route.RECIPE_EXECUTION) },
              containerColor = MaterialTheme.colorScheme.secondaryContainer,
              modifier = Modifier.testTag("startButton")) {
                Icon(
                    imageVector = Icons.Default.PlayArrow, // Replace with a suitable icon
                    contentDescription = "Start Recipe")
              }
        },
        content = { paddingValues ->
          Column(modifier = Modifier.padding(paddingValues).fillMaxSize().testTag("recipe")) {
            // Use the extracted RecipeContent composable
            RecipeContent(individualRecipeViewModel)
          }
        })
  } else {
    // If no recipe is selected, navigate to the easter egg screen
    navigationActions.navigateTo(Screen.EASTER_EGG)
  }
}

@Composable
fun RecipeContent(viewModel: IndividualRecipeViewModel) {
  Column(
      modifier =
          Modifier.padding(8.dp)
              .fillMaxSize()
              .verticalScroll(rememberScrollState()) // Enable vertical scrolling
              .testTag("recipeContent")) {
        // Display the recipe image
        Image(
            painter = painterResource(R.drawable.individual_recipe_pot),
            contentDescription = "Recipe Image",
            modifier = Modifier.width(537.dp).height(164.dp).testTag("recipeImage"),
            contentScale = ContentScale.FillWidth)

        // Row displaying servings and time information
        Row(modifier = Modifier.fillMaxWidth()) {
          Text(
              text = "Servings: ${viewModel.getRecipeServing()}",
              modifier = Modifier.testTag("recipeServings"))
          Spacer(modifier = Modifier.width(16.dp))
          Text(
              text = "Time: ${viewModel.getRecipeTime()} min",
              modifier = Modifier.testTag("recipeTime"))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ingredients section
        Column(modifier = Modifier.testTag("recipeIngredients")) {
          viewModel.getRecipeIngredients().forEach { ingredient ->
            DisplayIngredientNew(ingredient)
          }
        }

        // Instructions section
        Column(modifier = Modifier.testTag("recipeInstructions")) {
          viewModel.getRecipeInstruction().forEach { instruction ->
            DisplayInstructionNew(instruction)
          }
        }
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
// @Preview()
// @Composable
// private fun IndividualRecipeScreenPreviewEasterEgg() {
//  val navController = rememberNavController()
//  val navigationActions = NavigationActions(navController)
//  val firebaseFirestore = FirebaseFirestore.getInstance()
//  val recipeRepository = RecipeRepositoryFirestore(firebaseFirestore)
//  val individualRecipeViewModel = viewModel { IndividualRecipeViewModel(recipeRepository) }
//
//  // Render the IndividualRecipeScreen with a null selectedRecipe
//  IndividualRecipeScreen(navigationActions = navigationActions)
// }
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
