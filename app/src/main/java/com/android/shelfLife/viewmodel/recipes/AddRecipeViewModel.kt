package com.android.shelfLife.viewmodel.recipes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.android.shelfLife.R
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.newRecipe.RecipeRepository
import com.android.shelfLife.model.recipe.Ingredient
import com.android.shelfLife.model.recipe.Recipe
import com.android.shelfLife.model.recipe.RecipeType
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.utils.validateNumber
import com.android.shelfLife.ui.utils.validateString
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class AddRecipeViewModel
@Inject
constructor(
    private val recipeRepository: RecipeRepository,
    private val userRepository: UserRepository
) : ViewModel() {

  private val _title = MutableStateFlow("")
  val title: StateFlow<String> = _title.asStateFlow()

  private val _servings = MutableStateFlow("")
  val servings: StateFlow<String> = _servings.asStateFlow()

  private val _time = MutableStateFlow("")
  val time: StateFlow<String> = _time.asStateFlow()

  private val _ingredients = MutableStateFlow<List<Ingredient>>(emptyList())
  val ingredients: StateFlow<List<Ingredient>> = _ingredients.asStateFlow()

  private val _newIngredient =
      MutableStateFlow(Ingredient(name = "", quantity = Quantity(amount = 0.0)))

  private val _instructions = MutableStateFlow<List<String>>(emptyList())
  val instructions: StateFlow<List<String>> = _instructions.asStateFlow()

  private val _ingredientName = MutableStateFlow("")
  val ingredientName: StateFlow<String> = _ingredientName.asStateFlow()

  private val _ingredientQuantityAmount = MutableStateFlow("")
  val ingredientQuantityAmount: StateFlow<String> = _ingredientQuantityAmount.asStateFlow()

  private val _ingredientQuantityUnit = MutableStateFlow(FoodUnit.GRAM)
  val ingredientQuantityUnit: StateFlow<FoodUnit> = _ingredientQuantityUnit.asStateFlow()

  private val _showIngredientDialog = MutableStateFlow(false)
  val showIngredientDialog: StateFlow<Boolean> = _showIngredientDialog.asStateFlow()

    var unitExpanded by mutableStateOf(false)

  // Error Properties
  private val _error = MutableStateFlow(false)
  val error: StateFlow<Boolean> = _error.asStateFlow()

  private val _errorIngredient = MutableStateFlow(false)
  val errorIngredient: StateFlow<Boolean> = _errorIngredient.asStateFlow()

  private val _titleError = MutableStateFlow<Int?>(null)
  val titleError: StateFlow<Int?> = _titleError.asStateFlow()

  private val _servingsError = MutableStateFlow<Int?>(null)
  val servingsError: StateFlow<Int?> = _servingsError.asStateFlow()

  private val _timeError = MutableStateFlow<Int?>(null)
  val timeError: StateFlow<Int?> = _timeError.asStateFlow()

  private val _instructionError = MutableStateFlow<List<Int?>>(emptyList())
  val instructionError: StateFlow<List<Int?>> = _instructionError.asStateFlow()

  private val _instructionsError = MutableStateFlow(false)
  private val _ingredientsError = MutableStateFlow(false)

  private val _ingredientNameError = MutableStateFlow<Int?>(null)
  val ingredientNameError: StateFlow<Int?> = _ingredientNameError.asStateFlow()

  private val _ingredientQuantityAmountError = MutableStateFlow<Int?>(null)
  val ingredientQuantityAmountError: StateFlow<Int?> = _ingredientQuantityAmountError.asStateFlow()

  // Helper function to validate if any instruction is empty
  fun validateInstructions() {
    _instructionsError.value =
        instructions.value.any {
          validateString(
              it, R.string.instruction_empty_error, R.string.instructions_invalid_error) != null
        }
  }

  // Checks that no ingredient inside the list of ingredients is empty
  fun validateIngredients() {
    _ingredientsError.value = ingredients.value.any { it.name.isBlank() }
  }

  // Function that allows us to change the title
  fun changeTitle(newRecipeTitle: String) {
    _title.value = newRecipeTitle
    _titleError.value =
        validateString(
            newRecipeTitle, R.string.recipe_title_empty_error, R.string.recipe_title_invalid_error)
  }

  // Function that allows us to change the number of servings
  fun changeServings(newServings: String) {
    _servings.value = newServings
    _servingsError.value =
        validateNumber(
            newServings,
            R.string.servings_empty_error,
            R.string.servings_not_number_error,
            R.string.amount_negative_error)
  }

  // Function that allows us to change the time
  fun changeTime(newTime: String) {
    _time.value = newTime
    _timeError.value =
        validateNumber(
            newTime,
            R.string.time_empty_error,
            R.string.time_not_number_error,
            R.string.time_negative_error)
  }

  // Function that allows us to change the ingredient name
  fun changeIngredientName(newIngredientName: String) {
    _ingredientName.value = newIngredientName
    _ingredientNameError.value =
        validateString(
            newIngredientName,
            R.string.ingredient_name_empty_error,
            R.string.ingredient_name_invalid_error)
  }

  // Function that allows us to change the amount of an ingredient
  fun changeIngredientQuantityAmount(newIngredientQuantityAmount: String) {
    _ingredientQuantityAmount.value = newIngredientQuantityAmount
    _ingredientQuantityAmountError.value =
        validateNumber(
            newIngredientQuantityAmount,
            R.string.ingredient_quantity_empty_error,
            R.string.ingredient_quantity_not_number_error,
            R.string.ingredient_quantity_negative_error)
  }

  // Function that allows us to change the unit of the ingredient
  fun changeIngredientQuantityUnit(newUnit: FoodUnit) {
    _ingredientQuantityUnit.value = newUnit
  }

  // Function to validate all ingredient fields when the Add button is clicked
  fun validateAllIngredientFieldsWhenAddButton() {
    _ingredientNameError.value =
        validateString(
            ingredientName.value,
            R.string.ingredient_name_empty_error,
            R.string.ingredient_name_invalid_error)
    _ingredientQuantityAmountError.value =
        validateNumber(
            ingredientQuantityAmount.value,
            R.string.ingredient_quantity_empty_error,
            R.string.ingredient_quantity_not_number_error,
            R.string.ingredient_quantity_negative_error)
    _errorIngredient.value =
        (_ingredientNameError.value != null) || (_ingredientQuantityAmountError.value != null)
  }

  // Function to validate all fields when the Submit button is clicked
  fun validateAllFieldsWhenSubmitButton() {
    _titleError.value =
        validateString(
            title.value, R.string.recipe_title_empty_error, R.string.recipe_title_invalid_error)
    _servingsError.value =
        validateNumber(
            servings.value,
            R.string.servings_empty_error,
            R.string.servings_not_number_error,
            R.string.amount_negative_error)
    _timeError.value =
        validateNumber(
            time.value,
            R.string.time_empty_error,
            R.string.time_not_number_error,
            R.string.time_negative_error)
    validateInstructions()
    validateIngredients()

    _error.value =
        (_titleError.value != null) ||
            (_servingsError.value != null) ||
            (_timeError.value != null) ||
            _instructionsError.value ||
            _ingredientsError.value
  }

  // Function to create a new ingredient
  fun createNewIngredient() {
    _showIngredientDialog.value = true
    _ingredientName.value = ""
    _ingredientQuantityAmount.value = ""
    _ingredientQuantityUnit.value = FoodUnit.GRAM
  }

  // Function to close the pop-up dialog
  fun closeIngredientDialog() {
    _showIngredientDialog.value = false
  }

  // Function to add a new ingredient to the list
  fun addNewIngredient(): Boolean {
    validateAllIngredientFieldsWhenAddButton()
    if (!_errorIngredient.value) {
      _newIngredient.value =
          Ingredient(
              name = ingredientName.value,
              quantity =
                  Quantity(ingredientQuantityAmount.value.toDouble(), ingredientQuantityUnit.value))
      _ingredients.value = ingredients.value + _newIngredient.value
      return true
    }
    return false
  }

  // Function to remove an ingredient from the list
  fun removeIngredient(index: Int) {
    if (ingredients.value.isNotEmpty() && index < ingredients.value.size) {
      _ingredients.value = ingredients.value.toMutableList().apply { removeAt(index) }
    }
  }

  // Function to create a new instruction
  fun createNewInstruction() {
    _instructions.value = instructions.value + ""
    _instructionError.value = instructionError.value + null
  }

  // Function to modify an instruction
  fun changeInstruction(index: Int, newInstruction: String) {
    val updatedInstructions =
        instructions.value.toMutableList().apply { this[index] = newInstruction }
    _instructions.value = updatedInstructions
    val updatedErrors =
        instructionError.value.toMutableList().apply {
          this[index] =
              validateString(
                  newInstruction,
                  R.string.instruction_empty_error,
                  R.string.instructions_invalid_error)
        }
    _instructionError.value = updatedErrors
    validateInstructions()
  }

  // Function to remove an instruction
  fun removeInstruction(index: Int) {
    if (instructions.value.size > index) {
      _instructions.value = instructions.value.toMutableList().apply { removeAt(index) }
      _instructionError.value = instructionError.value.toMutableList().apply { removeAt(index) }
    }
  }

  suspend fun addNewRecipe(showToast: (Int) -> Unit) {
    validateAllFieldsWhenSubmitButton()
    if (_error.value) {
      return showToast(0)
    }
    val newRecipeUid = recipeRepository.getUid()
    val newRecipe =
        Recipe(
            uid = newRecipeUid,
            name = title.value,
            instructions = instructions.value,
            servings = servings.value.toFloat(),
            time = time.value.toDouble().minutes,
            ingredients = ingredients.value,
            recipeType = RecipeType.PERSONAL)
    recipeRepository.addRecipe(recipe = newRecipe.copy(uid = newRecipeUid))
    userRepository.addRecipeUID(newRecipeUid)
  }

    fun changeUnitExpanded(){
        unitExpanded = if(unitExpanded) false else true
    }
}
