package com.android.shelfLife.viewmodel.recipe

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.model.newRecipe.RecipeRepository
import com.android.shelfLife.model.recipe.Recipe
import com.android.shelfLife.model.recipe.RecipeGeneratorRepository
import com.android.shelfLife.model.recipe.RecipePrompt
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
open class RecipeGenerationViewModel
@Inject
constructor(
    private val recipeRepository: RecipeRepository,
    private val recipeGeneratorRepository: RecipeGeneratorRepository
) : ViewModel() {

  private val _recipePrompt = MutableStateFlow<RecipePrompt?>(null)
  open val recipePrompt: StateFlow<RecipePrompt?> = _recipePrompt.asStateFlow()

  private val _currentGeneratedRecipe = MutableStateFlow<Recipe?>(null)
  open val currentGeneratedRecipe: StateFlow<Recipe?> = _currentGeneratedRecipe.asStateFlow()

  fun updateRecipePrompt(prompt: RecipePrompt) {
    _recipePrompt.value = prompt
  }

  /** Generates a recipe based on the current prompt. */
  fun generateRecipe(onSuccess: (Recipe) -> Unit, onFailure: (String) -> Unit) {
    val prompt = _recipePrompt.value
    if (prompt != null) {
      recipeGeneratorRepository.generateRecipe(
          prompt,
          onSuccess = { recipe ->
            // Update the state with the generated recipe
            viewModelScope.launch { _currentGeneratedRecipe.emit(recipe) }
            onSuccess(recipe)
          },
          onFailure = { onFailure("Failed to generate recipe") })
    } else {
      onFailure("Failed to generate recipe, no prompt provided")
    }
  }

  /** Accepts the current generated recipe and saves it to the repository. */
  suspend fun acceptGeneratedRecipe(onSuccess: () -> Unit) {
    val recipe = _currentGeneratedRecipe.value
    if (recipe != null) {
      recipeRepository.addRecipe(recipe.copy(uid = recipeRepository.getUid()))
    } else {
      Log.e("RecipeGenerationViewModel", "No generated recipe to accept")
    }
  }
}
