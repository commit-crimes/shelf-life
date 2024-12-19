package helpers

import com.android.shelfLife.model.recipe.Recipe
import com.android.shelfLife.model.recipe.RecipeRepository
import io.mockk.every
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class RecipeRepositoryTestHelper(private val recipeRepository: RecipeRepository) {
  private val recipes = MutableStateFlow<List<Recipe>>(emptyList())

  private val selectedRecipe = MutableStateFlow<Recipe?>(null)

  init {
    every { recipeRepository.recipes } returns recipes.asStateFlow()
    every { recipeRepository.selectedRecipe } returns selectedRecipe.asStateFlow()
    every { recipeRepository.getUid() } returns "uid"
  }

  fun setRecipes(newRecipes: List<Recipe>) {
    recipes.value = newRecipes
  }

  fun setSelectedRecipe(recipe: Recipe?) {
    selectedRecipe.value = recipe
  }
}
