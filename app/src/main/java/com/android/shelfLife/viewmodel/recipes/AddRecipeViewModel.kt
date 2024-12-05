package com.android.shelfLife.viewmodel.recipes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.android.shelfLife.R
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.recipe.Ingredient
import com.android.shelfLife.model.recipe.Recipe
import com.android.shelfLife.model.recipe.RecipeRepository
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.utils.validateNumber
import com.android.shelfLife.ui.utils.validateString
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddRecipeViewModel(
    private val recipeRepository: RecipeRepository,
    private val userRepository: UserRepository
) : ViewModel() {

  var title by mutableStateOf("")
  var servings by mutableStateOf("")
  var time by mutableStateOf("")
  val ingredients = mutableStateListOf<Ingredient>()
  var newIngredient by
      mutableStateOf<Ingredient>(Ingredient(name = "", quantity = Quantity(amount = 0.0)))
  val instructions = mutableStateListOf<String>()
  var ingredientName by mutableStateOf("")
  var ingredientQuantityAmount by mutableStateOf("")
  var ingredientQuantityUnit by mutableStateOf<FoodUnit>(FoodUnit.GRAM)

  var showIngredientDialog by mutableStateOf(false)

  var error by
      mutableStateOf<Boolean>(
          false) // represent a general error (if there is a single error on the screen then this is
  // true)
  var errorIngredient by
      mutableStateOf<Boolean>(false) // represent a general error in the ingredient popUp
  var titleError by mutableStateOf<Int?>(null)
  var servingsError by mutableStateOf<Int?>(null)
  var timeError by mutableStateOf<Int?>(null)
  var instructionError =
      mutableStateListOf<
          Int?>() // a list that the error of each instruction. Their indexes are the same. ex: if
  // there is an error on the instruction at index 1, the value in this list will be
  // non-null at index 1
  var instructionsError by
      mutableStateOf(
          false) // a general look at the whole error list, i.e. if there is a single error on the
  // list, this will be true
  var ingredientsError by mutableStateOf(false) // a general error for the list of ingredients
  var ingredientNameError by mutableStateOf<Int?>(null)
  var ingredientQuantityAmountError by mutableStateOf<Int?>(null)

  init {
    // we make sure we set the values to their initial values
    title = ""
    servings = ""
    time = ""
    ingredients.clear()
    instructions.clear()

    showIngredientDialog = false

    error = false
    titleError = null
    servingsError = null
    timeError = null
    instructionsError = false
  }

  // Helper function to validate if any instruction is empty
  fun validateInstructions() {
    instructionsError =
        instructions.any {
          validateString(
              it, R.string.instruction_empty_error, R.string.instructions_invalid_error) != null
        }
  }

    // checks that the no ingredient inside the list of ingredients is empty
  fun validateIngredients() {
    instructionsError = instructions.any { it.isBlank() }
  }

    //function that allows us to change the title
  fun changeTitle(newRecipeTitle: String) {
    title = newRecipeTitle
    titleError =
        validateString(
            newRecipeTitle, R.string.recipe_title_empty_error, R.string.recipe_title_invalid_error)
  }

    //function that allows us to change the number of servings
  fun changeServings(newServings: String) {
    servings = newServings
    servingsError =
        validateNumber(
            newServings,
            R.string.servings_empty_error,
            R.string.servings_not_number_error,
            R.string.amount_negative_error)
  }

    //function that allows us to change the time
  fun changeTime(newTime: String) {
    time = newTime
    timeError =
        validateNumber(
            newTime,
            R.string.time_empty_error,
            R.string.time_not_number_error,
            R.string.time_negative_error)
  }

    // function that allows us to change the ingredient name
  fun changeIngredientName(newIngredientName: String) {
    ingredientName = newIngredientName
    ingredientNameError =
        validateString(
            newIngredientName,
            R.string.ingredient_name_empty_error,
            R.string.ingredient_name_invalid_error)
  }

    //function that allows us to change the amount of an ingredient
  fun changeIngredientQuantityAmount(newIngredientQuantityAmount: String) {
    ingredientQuantityAmount = newIngredientQuantityAmount
    ingredientQuantityAmountError =
        validateNumber(
            newIngredientQuantityAmount,
            R.string.ingredient_quantity_empty_error,
            R.string.ingredient_quantity_not_number_error,
            R.string.ingredient_quantity_negative_error)
  }

    // function that allows us to change the unit of the ingredient
  fun changeIngredientQuantityUnit(newUnit: FoodUnit) {
    ingredientQuantityUnit = newUnit
  }

    // this function is called when we click the add Ingredient button. This function checks that there are no errors with the ingredient information
  fun validateAllIngredientFieldsWhenAddButton() {
    ingredientNameError =
        validateString(
            ingredientName,
            R.string.ingredient_name_empty_error,
            R.string.ingredient_name_invalid_error)
    ingredientQuantityAmountError =
        validateNumber(
            ingredientQuantityAmount,
            R.string.ingredient_quantity_empty_error,
            R.string.ingredient_quantity_not_number_error,
            R.string.ingredient_quantity_negative_error)
    errorIngredient = (ingredientNameError != null) || (ingredientQuantityAmountError != null)
  }

    //this function is called when the submit the recipe. It check that there are no error in the whole screen
  fun validateAllFieldsWhenSubmitButton() {
    titleError =
        validateString(
            title, R.string.recipe_title_empty_error, R.string.recipe_title_invalid_error)
    servingsError =
        validateNumber(
            servings,
            R.string.servings_empty_error,
            R.string.servings_not_number_error,
            R.string.amount_negative_error)
    timeError =
        validateNumber(
            time,
            R.string.time_empty_error,
            R.string.time_not_number_error,
            R.string.time_negative_error)
    validateInstructions()
    validateIngredients()

    error =
        (timeError != null) ||
            (servingsError != null) ||
            (timeError != null) ||
            instructionsError ||
            ingredientsError
  }

    //this function is used to create a new ingredient
    //we need to show the popUp and make sure the ingredient info is back at to the initials values
  fun createNewIngredient() {
    showIngredientDialog = true
    ingredientName = ""
    ingredientQuantityAmount = ""
    ingredientQuantityUnit = FoodUnit.GRAM
  }

    //function to close the popUp
  fun closeIngredientDialog() {
    showIngredientDialog = false
  }

    //This function is to add an ingredient into our list
  fun addNewIngredient(): Boolean {
    validateAllIngredientFieldsWhenAddButton()
    if (!errorIngredient) {
      newIngredient =
          Ingredient(
              name = ingredientName,
              quantity = Quantity(ingredientQuantityAmount.toDouble(), ingredientQuantityUnit))
      ingredients.add(newIngredient)
      return true
    }
    return false
  }

    //This function is to remove an ingredient from the list
  fun removeIngredient(index: Int) {
    if (ingredients.size > 0 && index < ingredients.size) {
      ingredients.removeAt(index)
    }
  }

    //This function is to create a new instruction
  fun createNewInstruction() {
    instructions.add("")
    instructionError.add(null)
  }

    //This function is used to modify an instruction
  fun changeInstruction(index: Int, newInstruction: String) {
    instructions[index] = newInstruction
    instructionError[index] =
        validateString(
            newInstruction, R.string.instruction_empty_error, R.string.instructions_invalid_error)
    validateInstructions()
  }

    //This function is used to remove an instruction
  fun removeInstruction(index: Int) {
    if (instructions.size > 0 && instructions.size > index) {
      instructions.removeAt(index)
      instructionError.removeAt(index)
    }
  }

    // this function adds the new recipe into our database. It will check there are no errors by calling the validateAllFieldsWhenSubmitButton()
  @OptIn(DelicateCoroutinesApi::class)
  suspend fun addNewRecipe(onSuccess: () -> Unit, showToast: (Int) -> Unit) {
    validateAllFieldsWhenSubmitButton()
    if (error) return showToast(0)
    val newRecipeUid = recipeRepository.getUid()

    val newRecipe =
        Recipe(
            uid = newRecipeUid,
            name = title,
            instructions = instructions.toList(),
            servings = servings.toFloat(),
            time = time.toDouble().minutes,
            ingredients = ingredients.toList())

    recipeRepository.addRecipe(
        recipe = newRecipe.copy(uid = recipeRepository.getUid()), // Assign a UID during save
        onSuccess = {
          GlobalScope.launch { // uses a delicate coroutine api
            userRepository.addRecipeUID(newRecipeUid)
            onSuccess()
          }
        },
        onFailure = { showToast(1) })
  }
}
