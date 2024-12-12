package com.android.shelfLife.viewmodel.recipes;


import android.content.Context;
import androidx.lifecycle.ViewModel
import com.android.shelfLife.model.newFoodItem.FoodItem

import com.android.shelfLife.model.newFoodItem.FoodItemRepository;
import com.android.shelfLife.model.newRecipe.RecipeRepository
import com.android.shelfLife.model.newhousehold.HouseHoldRepository;
import com.android.shelfLife.model.recipe.Recipe
import com.android.shelfLife.model.user.UserRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
@HiltViewModel
class ExecuteRecipeViewModel @Inject constructor(
    private val houseHoldRepository: HouseHoldRepository,
    private val listFoodItemsRepository: FoodItemRepository,
    private val recipeRepositoryFirestore: RecipeRepository,
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val households = houseHoldRepository.households
    val selectedHousehold = houseHoldRepository.selectedHousehold

    private val originalSelectedRecipe = recipeRepositoryFirestore.selectedRecipe
    val foodItems = listFoodItemsRepository.foodItems

    private val _servings = MutableStateFlow(originalSelectedRecipe.value!!.servings)
    val servings = _servings.asStateFlow()

    private val _executingRecipe = MutableStateFlow(originalSelectedRecipe.value!!)
    val executingRecipe = _executingRecipe.asStateFlow()

    // Tracks the current ingredient index we are selecting items for
    private val _currentIngredientIndex = MutableStateFlow(0)
    val currentIngredientIndex = _currentIngredientIndex.asStateFlow()

    // The user's selections: {"Tomatoes" -> [FoodItem1, FoodItem2]}
    private val _selectedFoodItemsForIngredients = MutableStateFlow<Map<String, List<FoodItem>>>(emptyMap())
    val selectedFoodItemsForIngredients = _selectedFoodItemsForIngredients.asStateFlow()

    fun updateServings(newServings: Float) {
        _servings.value = newServings
        val recipe = originalSelectedRecipe.value
        val updatedRecipe = recipe?.copy(servings = newServings)
        if (updatedRecipe != null) {
            _executingRecipe.value = updatedRecipe
        }
    }

    fun selectFoodItemsForIngredient(ingredientName: String, selectedItems: List<FoodItem>) {
        val currentSelections = _selectedFoodItemsForIngredients.value.toMutableMap()
        currentSelections[ingredientName] = selectedItems
        _selectedFoodItemsForIngredients.value = currentSelections
    }

    fun setFoodItemAmountForIngredient(ingredientName: String, foodItem: FoodItem, newAmount: Float) {
        // TODO: Implement if needed. This would store amounts if you decide to represent them differently.
    }

    suspend fun consumeSelectedItems() {
        val allSelectedItems = _selectedFoodItemsForIngredients.value.values.flatten()
        if (allSelectedItems.isNotEmpty()) {
            consumeFoodItems(allSelectedItems)
        }
    }

    private suspend fun consumeFoodItems(foodItems: List<FoodItem>) {
        TODO()
    }

    fun nextIngredient() {
        val currentIndex = _currentIngredientIndex.value
        val ingredientCount = executingRecipe.value.ingredients.size
        if (currentIndex < ingredientCount - 1) {
            _currentIngredientIndex.value = currentIndex + 1
        }
    }

    fun hasMoreIngredients(): Boolean {
        val currentIndex = _currentIngredientIndex.value
        val ingredientCount = executingRecipe.value.ingredients.size
        return currentIndex < ingredientCount - 1
    }

    fun currentIngredientName(): String? {
        val index = _currentIngredientIndex.value
        val ingredients = executingRecipe.value.ingredients
        return if (index in ingredients.indices) {
            ingredients[index].name
        } else null
    }
}