package com.android.shelfLife.viewmodel.recipes

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.model.newFoodItem.FoodItem
import com.android.shelfLife.model.newFoodItem.FoodItemRepository
import com.android.shelfLife.model.newRecipe.RecipeRepository
import com.android.shelfLife.model.newhousehold.HouseHoldRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ExecuteRecipeViewModel @Inject constructor(
    private val houseHoldRepository: HouseHoldRepository,
    private val foodItemsRepository: FoodItemRepository,
    private val recipeRepositoryFirestore: RecipeRepository,
) : ViewModel() {
    companion object {
        private const val TAG = "ExecuteRecipeViewModel"
    }

    // Current state of the recipe execution
    private val _state = MutableStateFlow<RecipeExecutionState>(RecipeExecutionState.SelectServings)
    val state: StateFlow<RecipeExecutionState> = _state

    private val originalSelectedRecipe = recipeRepositoryFirestore.selectedRecipe

    private var _executingRecipe = MutableStateFlow(originalSelectedRecipe.value!!)
    val executingRecipe = _executingRecipe.asStateFlow()

    private val _servings = MutableStateFlow(_executingRecipe.value.servings)
    val servings = _servings.asStateFlow()

    private val _selectedFoodItemsForIngredients = MutableStateFlow<Map<String, List<FoodItem>>>(emptyMap())
    val selectedFoodItemsForIngredients = _selectedFoodItemsForIngredients.asStateFlow()

    private val _availableFoodItems = MutableStateFlow(foodItemsRepository.foodItems.value)
    val foodItems = _availableFoodItems.asStateFlow()

    private val householdUid = houseHoldRepository.selectedHousehold.value?.uid

    private val _currentIngredientIndex = MutableStateFlow(0)
    private val _currentIngredientName = MutableStateFlow<String?>(null)
    val currentIngredientName: StateFlow<String?> = _currentIngredientName.asStateFlow()

    private val _currentInstructionIndex = MutableStateFlow(0)
    val currentInstructionIndex: StateFlow<Int> = _currentInstructionIndex.asStateFlow()

    val currentInstruction: StateFlow<String?> = combine(
        _currentInstructionIndex,
        executingRecipe
    ) { index, recipe ->
        if (index in recipe.instructions.indices) {
            Log.d(TAG, "Current instruction updated: ${recipe.instructions[index]}")
            recipe.instructions[index]
        } else {
            Log.e(TAG, "Instruction index $index is out of bounds.")
            null
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        updateCurrentIngredientName()
    }

    // Function to transition to the next state
    fun nextState() {
        _state.value = when (_state.value) {
            is RecipeExecutionState.SelectServings -> RecipeExecutionState.SelectFood
            is RecipeExecutionState.SelectFood -> RecipeExecutionState.Instructions
            else -> _state.value // Stay in the same state
        }
        Log.d(TAG, "Transitioned to state: ${_state.value}")
    }

    // Function to go back to the previous state
    fun previousState() {
        _state.value = when (_state.value) {
            is RecipeExecutionState.SelectFood -> RecipeExecutionState.SelectServings
            is RecipeExecutionState.Instructions -> RecipeExecutionState.SelectFood
            else -> _state.value // Stay in the same state
        }
        Log.d(TAG, "Transitioned to state: ${_state.value}")
    }

    fun updateServings(servings: Float) {
        _servings.value = servings
        Log.d(TAG, "Updated servings to: $servings")
    }

    fun temporarilyConsumeItems(listOfFoodItems: List<FoodItem>, listOfAmounts: List<Float>) {
        for (i in listOfFoodItems.indices) {
            temporarilyConsumeItem(listOfFoodItems[i], listOfAmounts[i])
        }
    }

    private fun temporarilyConsumeItem(foodItem: FoodItem, amount: Float) {
        _availableFoodItems.value = _availableFoodItems.value.mapNotNull { currentItem ->
            if (currentItem.uid == foodItem.uid) {
                val remainingAmount = currentItem.foodFacts.quantity.amount - amount

                if (remainingAmount > 0) {
                    Log.d(TAG, "Temporarily consumed $amount of ${currentItem.foodFacts.name}. Remaining: $remainingAmount")
                    currentItem.copy(
                        foodFacts = currentItem.foodFacts.copy(
                            quantity = currentItem.foodFacts.quantity.copy(amount = remainingAmount)
                        )
                    )
                } else {
                    Log.d(TAG, "Completely consumed ${currentItem.foodFacts.name}.")
                    null
                }
            } else {
                currentItem
            }
        }
    }

    fun consumeSelectedItems() {
        if (householdUid != null) {
            foodItemsRepository.setFoodItems(householdId = householdUid, _availableFoodItems.value)
            Log.d(TAG, "Consumed selected items and updated Firestore for household: $householdUid")
        } else {
            Log.e(TAG, "Household ID is null. Cannot consume selected items.")
        }
    }

    private fun updateCurrentIngredientName() {
        val ingredients = _executingRecipe.value.ingredients
        _currentIngredientName.value = if (_currentIngredientIndex.value in ingredients.indices) {
            ingredients[_currentIngredientIndex.value].name
        } else {
            null
        }
        Log.d(TAG, "Current ingredient name updated: ${_currentIngredientName.value}")
    }

    fun nextIngredient() {
        val recipe = _executingRecipe.value
        if (_currentIngredientIndex.value < recipe.ingredients.size - 1) {
            _currentIngredientIndex.value += 1
            updateCurrentIngredientName()
        }
        Log.d(TAG, "Moved to next ingredient. Current index: ${_currentIngredientIndex.value}")
    }

    fun hasMoreIngredients(): Boolean {
        return _currentIngredientIndex.value < _executingRecipe.value.ingredients.size - 1
    }

    fun selectFoodItemForIngredient(ingredientName: String, foodItem: FoodItem, newAmount: Float) {
        val currentSelections = _selectedFoodItemsForIngredients.value.toMutableMap()
        val selectedItemsForIngredient = currentSelections[ingredientName]?.toMutableList() ?: mutableListOf()

        val existingItemIndex = selectedItemsForIngredient.indexOfFirst { it.uid == foodItem.uid }
        if (existingItemIndex != -1) {
            val updatedItem = selectedItemsForIngredient[existingItemIndex].copy(
                foodFacts = selectedItemsForIngredient[existingItemIndex].foodFacts.copy(
                    quantity = selectedItemsForIngredient[existingItemIndex].foodFacts.quantity.copy(amount = newAmount.toDouble())
                )
            )
            selectedItemsForIngredient[existingItemIndex] = updatedItem
        } else {
            val newItem = foodItem.copy(
                foodFacts = foodItem.foodFacts.copy(
                    quantity = foodItem.foodFacts.quantity.copy(amount = newAmount.toDouble())
                )
            )
            selectedItemsForIngredient.add(newItem)
        }

        currentSelections[ingredientName] = selectedItemsForIngredient
        _selectedFoodItemsForIngredients.value = currentSelections
        Log.d(TAG, "Selected food items updated for ingredient: $ingredientName")
    }

    fun nextInstruction() {
        val instructions = _executingRecipe.value.instructions
        if (_currentInstructionIndex.value < instructions.size - 1) {
            _currentInstructionIndex.value += 1
            Log.d(TAG, "Moved to next instruction. Current index: ${_currentInstructionIndex.value}")
        } else {
            Log.d(TAG, "No more instructions to display.")
        }
    }

    fun previousInstruction() {
        if (_currentInstructionIndex.value > 0) {
            _currentInstructionIndex.value -= 1
            Log.d(TAG, "Moved to previous instruction. Current index: ${_currentInstructionIndex.value}")
        } else {
            Log.d(TAG, "No previous instructions to display.")
        }
    }

    fun hasMoreInstructions(): Boolean {
        return _currentInstructionIndex.value < _executingRecipe.value.instructions.size - 1
    }

    fun hasPreviousInstructions(): Boolean {
        return _currentInstructionIndex.value > 0
    }
}

sealed class RecipeExecutionState {
    object SelectServings : RecipeExecutionState()
    object SelectFood : RecipeExecutionState()
    object Instructions : RecipeExecutionState()
}