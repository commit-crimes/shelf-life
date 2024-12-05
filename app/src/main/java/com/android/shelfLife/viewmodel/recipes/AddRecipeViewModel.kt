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

  var error by mutableStateOf<Boolean>(false)
  var errorIngredient by mutableStateOf<Boolean>(false)
  var titleError by mutableStateOf<Int?>(null)
  var servingsError by mutableStateOf<Int?>(null)
  var timeError by mutableStateOf<Int?>(null)
  var instructionError by mutableStateOf<Int?>(null)
  var instructionsError by mutableStateOf(false)
  var ingredientsError by mutableStateOf(false)
  var ingredientNameError by mutableStateOf<Int?>(null)
  var ingredientQuantityAmountError by mutableStateOf<Int?>(null)

  init {
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

  fun validateIngredients() {
    instructionsError = instructions.any { it.isBlank() }
  }

  fun changeTitle(newRecipeTitle: String) {
    title = newRecipeTitle
    titleError =
        validateString(
            newRecipeTitle, R.string.recipe_title_empty_error, R.string.recipe_title_invalid_error)
  }

  fun changeServings(newServings: String) {
    servings = newServings
    servingsError =
        validateNumber(
            newServings,
            R.string.servings_empty_error,
            R.string.servings_not_number_error,
            R.string.amount_negative_error)
  }

  fun changeTime(newTime: String) {
    time = newTime
    timeError =
        validateNumber(
            newTime,
            R.string.time_empty_error,
            R.string.time_not_number_error,
            R.string.time_negative_error)
  }

  fun changeIngredientName(newIngredientName: String) {
    ingredientName = newIngredientName
    ingredientNameError =
        validateString(
            newIngredientName,
            R.string.ingredient_name_empty_error,
            R.string.ingredient_name_invalid_error)
  }

  fun changeIngredientQuantityAmount(newIngredientQuantityAmount: String) {
    ingredientQuantityAmount = newIngredientQuantityAmount
    ingredientQuantityAmountError =
        validateNumber(
            newIngredientQuantityAmount,
            R.string.ingredient_quantity_empty_error,
            R.string.ingredient_quantity_not_number_error,
            R.string.ingredient_quantity_negative_error)
  }

  fun changeIngredientQuantityUnit(newUnit: FoodUnit) {
    ingredientQuantityUnit = newUnit
  }

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

  fun createNewIngredient() {
    showIngredientDialog = true
    newIngredient = Ingredient(name = "", quantity = Quantity(0.0))
  }

  fun closeIngredientDialog() {
    showIngredientDialog = false
  }

  fun addNewIngredient(): Boolean {
    validateAllIngredientFieldsWhenAddButton()
    if (errorIngredient) {
      newIngredient =
          Ingredient(
              name = ingredientName,
              quantity = Quantity(ingredientQuantityAmount.toDouble(), ingredientQuantityUnit))
      return true
    }
    return false
  }

  fun removeIngredient(index: Int) {
    if (ingredients.size > 0 && index < ingredients.size) {
      ingredients.removeAt(index)
    }
  }

  fun createNewInstruction() {
    instructions.add("")
  }

  fun changeInstruction(index: Int, newInstruction: String) {
    instructions[index] = newInstruction
    instructionError =
        validateString(
            newInstruction, R.string.instruction_empty_error, R.string.instructions_invalid_error)
    validateInstructions()
  }

  fun removeInstruction(index: Int) {
    if (instructions.size > 0 && instructions.size > index) {
      instructions.removeAt(index)
    }
  }

  suspend fun addNewRecipe(): Pair<Boolean, Boolean> {
    validateAllFieldsWhenSubmitButton()
    if (error) {
      val recipeAdded = false
      val newRecipeUid = recipeRepository.getUid()
      val newRecipe =
          Recipe(
              uid = recipeRepository.getUid(),
              name = title,
              instructions = instructions.toList(),
              servings = servings.toFloat(),
              time = time.toDouble().minutes,
              ingredients = ingredients.toList())
      recipeRepository.addRecipe(
          newRecipe, onSuccess = { recipeAdded == true }, onFailure = { recipeAdded == false })
      userRepository.addRecipeUID(newRecipeUid)
      return Pair(true, recipeAdded)
    }
    return Pair(false, false)
  }
}
