package com.android.shelfLife.ui.recipes

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.shelfLife.R
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.recipe.Ingredient
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.theme.onSecondaryDark
import com.android.shelfLife.ui.theme.primaryContainerDark
import com.android.shelfLife.ui.theme.secondaryContainerLight
import com.android.shelfLife.ui.utils.CustomButtons
import com.android.shelfLife.ui.utils.CustomTopAppBar
import com.android.shelfLife.ui.utils.UnitDropdownField
import com.android.shelfLife.viewmodel.recipes.AddRecipeViewModel
import kotlinx.coroutines.launch

@Composable
fun AddRecipeScreen(
    navigationActions: NavigationActions,
    addRecipeViewModel: AddRecipeViewModel = hiltViewModel() // default in production
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  val ingredients by addRecipeViewModel.ingredients.collectAsState()
  val instructions by addRecipeViewModel.instructions.collectAsState()

  Scaffold(
      modifier = Modifier.testTag("addRecipeScreen"),
      topBar = {
        CustomTopAppBar(
            onClick = { navigationActions.goBack() },
            title = stringResource(R.string.title_of_AddRecipeScreen),
            titleTestTag = "addRecipeTitle")
      },
      content = { paddingValues -> // New content parameter
        LazyColumn(
            modifier = Modifier.padding(paddingValues).padding(horizontal = 20.dp).fillMaxSize()) {
              // Recipe title
              item {
                RecipeTitleOutlinedTextField(addRecipeViewModel)
              }

              // Error message for title
              item { ErrorTextBoxNEW(
                    addRecipeViewModel.titleError.collectAsState().value, "titleErrorMessage")
              }

              // Recipe servings
              item {
                RecipeServingsOutlinedTextField(addRecipeViewModel)
              }

              // Error message for servings
              item { ErrorTextBoxNEW(
                    addRecipeViewModel.servingsError.collectAsState().value, "servingsErrorMessage")
              }

              // Recipe time
              item {
                  RecipeTimeOutlinedTextField(addRecipeViewModel)
              }

              // Error message for time
              item { ErrorTextBoxNEW(
                    addRecipeViewModel.timeError.collectAsState().value, "timeErrorMessage")
              }

              // Recipe Ingredients Section
              item {
                  RecipeIngredientsText()
              }

              itemsIndexed(ingredients) { index, ingredient ->
                IngredientItem(
                    index = index,
                    ingredient = ingredient,
                    onRemoveClick = { addRecipeViewModel.removeIngredient(index) })
              }

              // Add Ingredient Button
              item { RecipeAddIngredientButton(addRecipeViewModel) }

              // Instructions Section
              item { RecipeInstructionText() }

              itemsIndexed(instructions) { index, instruction ->
                InstructionItem(index = index, addRecipeViewModel = addRecipeViewModel)
              }

              // Add Instruction Button
              item { RecipeAddIngredient(addRecipeViewModel) }

              // Footer Buttons
              item {
                CustomButtons(
                    button1OnClick = { navigationActions.goBack() },
                    button1TestTag = "cancelButton",
                    button1Text = stringResource(R.string.cancel_button),
                    button2OnClick = {
                      coroutineScope.launch {
                        addRecipeViewModel.addNewRecipe(
                            showToast = { messageId ->
                              val message =
                                  if (messageId == 0) {
                                    R.string.submission_error_message
                                  } else {
                                    R.string.error_uploading_recipe_message
                                  }

                              Toast.makeText(
                                      context, context.getString(message), Toast.LENGTH_SHORT)
                                  .show()
                            })
                      }
                      navigationActions.goBack()
                    },
                    button2TestTag = "addButton",
                    button2Text = stringResource(R.string.add_button))
              }
            }

        // Ingredient Dialog, inside the composable block
        if (addRecipeViewModel.showIngredientDialog.collectAsState().value) {
          IngredientDialog(addRecipeViewModel)
        }
      })
}
// @Preview(showBackground = true)
// @Composable
// fun AddRecipeScreenPreview() {
//    val navController = rememberNavController()
//    val navigationActions = NavigationActions(navController)
//
//    // Create mock or fake repositories for testing
//    val fakeRecipeRepository = FakeRecipeRepository() // Implement a simple fake
//    val fakeUserRepository = FakeUserRepository()     // Implement a simple fake
//
//    // Manually instantiate your ViewModel with fakes
//    val viewModel = AddRecipeViewModel(
//        recipeRepository = fakeRecipeRepository,
//        userRepository = fakeUserRepository
//    )
//
//    AddRecipeScreen(
//        navigationActions = navigationActions,
//        addRecipeViewModel = viewModel
//    )
// }
