package com.android.shelfLife.viewmodel.recipes

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.model.newFoodItem.FoodItem
import com.android.shelfLife.model.newFoodItem.FoodItemRepository
import com.android.shelfLife.model.newRecipe.RecipeRepository
import com.android.shelfLife.model.newhousehold.HouseHoldRepository
import com.android.shelfLife.model.recipe.Recipe
import com.android.shelfLife.model.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ExecuteRecipeViewModel @Inject constructor(
    private val houseHoldRepository: HouseHoldRepository,
    private val listFoodItemsRepository: FoodItemRepository,
    private val recipeRepositoryFirestore: RecipeRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "ExecuteRecipeViewModel"
    }

    val households = houseHoldRepository.households
    val selectedHousehold = houseHoldRepository.selectedHousehold

    private val originalSelectedRecipe = recipeRepositoryFirestore.selectedRecipe
    val foodItems = listFoodItemsRepository.foodItems

    private val _servings = MutableStateFlow(originalSelectedRecipe.value!!.servings)
    val servings = _servings.asStateFlow()

    private val _executingRecipe = MutableStateFlow(originalSelectedRecipe.value!!)
    val executingRecipe = _executingRecipe.asStateFlow()

    private val _currentIngredientIndex = MutableStateFlow(0)

    private val _selectedFoodItemsForIngredients = MutableStateFlow<Map<String, List<FoodItem>>>(emptyMap())
    val selectedFoodItemsForIngredients = _selectedFoodItemsForIngredients.asStateFlow()

    private val _currentIngredientName = MutableStateFlow<String?>(null)
    val currentIngredientName = _currentIngredientName.asStateFlow()

    init {
        // Update currentIngredientName whenever _currentIngredientIndex or executingRecipe changes
        combine(_currentIngredientIndex, executingRecipe) { index, recipe ->
            if (index in recipe.ingredients.indices) {
                recipe.ingredients[index].name
            } else null
        }.onEach { ingredientName ->
            Log.d(TAG, "Current ingredient name updated to: $ingredientName")
            _currentIngredientName.value = ingredientName
        }.launchIn(viewModelScope)
    }

    fun updateServings(newServings: Float) {
        Log.d(TAG, "Updating servings to $newServings")
        _servings.value = newServings
        val recipe = originalSelectedRecipe.value
        val updatedRecipe = recipe?.copy(servings = newServings)
        if (updatedRecipe != null) {
            _executingRecipe.value = updatedRecipe
        }
    }

    fun selectFoodItemForIngredient(ingredientName: String, foodItem: FoodItem, newAmount: Float) {
        Log.d(TAG, "Selecting food item: ${foodItem.foodFacts.name} with amount: $newAmount for ingredient: $ingredientName")

        // Create a mutable copy of the current selections
        val currentSelections = _selectedFoodItemsForIngredients.value.toMutableMap()

        // Get or initialize the list of selected items for this ingredient
        val selectedItemsForIngredient = currentSelections[ingredientName]?.toMutableList() ?: mutableListOf()

        // Check if the food item already exists in the list
        val existingItemIndex = selectedItemsForIngredient.indexOfFirst { it.uid == foodItem.uid }

        if (existingItemIndex != -1) {
            // If the item exists, update its quantity
            val existingItem = selectedItemsForIngredient[existingItemIndex]
            val updatedItem = existingItem.copy(
                foodFacts = existingItem.foodFacts.copy(
                    quantity = existingItem.foodFacts.quantity.copy(
                        amount = newAmount.toDouble() // Update the amount
                    )
                )
            )
            selectedItemsForIngredient[existingItemIndex] = updatedItem
            Log.d(TAG, "Updated item: ${updatedItem.foodFacts.name} with new amount: $newAmount")
        } else {
            // If the item does not exist, add it to the list with the specified amount
            val newItem = foodItem.copy(
                foodFacts = foodItem.foodFacts.copy(
                    quantity = foodItem.foodFacts.quantity.copy(
                        amount = newAmount.toDouble() // Set the initial amount
                    )
                )
            )
            selectedItemsForIngredient.add(newItem)
            Log.d(TAG, "Added new item: ${newItem.foodFacts.name} with amount: $newAmount")
        }

        // Update the selections map with the modified list
        currentSelections[ingredientName] = selectedItemsForIngredient
        _selectedFoodItemsForIngredients.value = currentSelections

        Log.d(TAG, "Updated selections for $ingredientName: ${currentSelections[ingredientName]}")
    }

    fun selectFoodItemsForIngredient(ingredientName: String, selectedItems: List<FoodItem>) {
        Log.d(TAG, "Selecting food items for ingredient: $ingredientName, Items: $selectedItems")
        val currentSelections = _selectedFoodItemsForIngredients.value.toMutableMap()
        currentSelections[ingredientName] = selectedItems
        _selectedFoodItemsForIngredients.value = currentSelections
    }

    suspend fun consumeSelectedItems() {
        val allSelectedItems = _selectedFoodItemsForIngredients.value.values.flatten()
        Log.d(TAG, "Consuming selected items: $allSelectedItems")
        if (allSelectedItems.isNotEmpty()) {
            consumeFoodItems(allSelectedItems)
        }
    }

    private suspend fun consumeFoodItems(foodItems: List<FoodItem>) {
        Log.d(TAG, "Consuming food items: $foodItems")
        TODO()
    }

    fun nextIngredient() {
        val currentIndex = _currentIngredientIndex.value
        val ingredientCount = executingRecipe.value.ingredients.size
        if (currentIndex < ingredientCount - 1) {
            _currentIngredientIndex.value = currentIndex + 1
            Log.d(TAG, "Moved to next ingredient, current index: ${_currentIngredientIndex.value}")
        } else {
            Log.d(TAG, "No more ingredients to process.")
        }
    }

    fun hasMoreIngredients(): Boolean {
        val currentIndex = _currentIngredientIndex.value
        val ingredientCount = executingRecipe.value.ingredients.size
        val hasMore = currentIndex < ingredientCount - 1
        Log.d(TAG, "Has more ingredients: $hasMore")
        return hasMore
    }



}