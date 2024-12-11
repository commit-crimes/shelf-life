package com.android.shelfLife.viewmodel.recipes

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.model.recipe.Recipe
import com.android.shelfLife.model.recipe.RecipeGeneratorRepository
import com.android.shelfLife.model.recipe.RecipePrompt
import com.android.shelfLife.model.recipe.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

open class RecipeGenerationViewModel(
    private val recipeRepository: RecipeRepository,
    private val recipeGeneratorRepository: RecipeGeneratorRepository
) : ViewModel() {

  companion object {
    private const val CREATION_STEP_COUNT = 3
  }

  private val _recipePrompt = MutableStateFlow<RecipePrompt>(RecipePrompt(name = ""))
  open val recipePrompt: StateFlow<RecipePrompt> = _recipePrompt.asStateFlow()

  private val _currentGeneratedRecipe = MutableStateFlow<Recipe?>(null)
  open val currentGeneratedRecipe: StateFlow<Recipe?> = _currentGeneratedRecipe.asStateFlow()

  private val _currentStep = MutableStateFlow(0)
  open val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

  fun updateRecipePrompt(prompt: RecipePrompt) {
    _recipePrompt.value = prompt
  }

  fun nextStep() {
    _currentStep.value += 1
  }

  fun previousStep() {
    _currentStep.value -= 1
  }

  fun resetSteps() {
    _currentStep.value = 0
  }

  fun isLastStep(): Boolean {
    return _currentStep.value == (CREATION_STEP_COUNT-1)
  }

  /** Generates a recipe based on the current prompt. */
  fun generateRecipe(onSuccess: (Recipe) -> Unit, onFailure: (String) -> Unit) {
    val prompt = _recipePrompt.value
    recipeGeneratorRepository.generateRecipe(
        prompt,
        onSuccess = { recipe ->
          // Update the state with the generated recipe
          recipeRepository.selectRecipe(recipe) //select the recipe so individual recipe view can show it
          viewModelScope.launch { _currentGeneratedRecipe.emit(recipe) }
          onSuccess(recipe)
        },
        onFailure = { onFailure("Failed to generate recipe") })
  }

  /** Accepts the current generated recipe and saves it to the repository. */
  fun acceptGeneratedRecipe(onSuccess: () -> Unit) {
    val recipe = _currentGeneratedRecipe.value
    if (recipe != null) {
      recipeRepository.addRecipe(recipe.copy(uid = recipeRepository.getUid(), workInProgress = false),
        onSuccess) { Log.e("RecipeGenerationViewModel", "Failed to save the recipe")
      }
    } else {
      Log.e("RecipeGenerationViewModel", "No generated recipe to accept")
    }
  }
}
