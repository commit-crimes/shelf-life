package com.android.shelflife.viewmodel

import com.android.shelfLife.model.newRecipe.RecipeRepository
import com.android.shelfLife.model.recipe.ListRecipesViewModel
import com.android.shelfLife.model.recipe.Recipe
import com.android.shelfLife.model.recipe.RecipeGeneratorRepository
import com.android.shelfLife.model.recipe.RecipePrompt
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any

class ListRecipesViewModelTest {

  private lateinit var recipeRepository: RecipeRepository
  private lateinit var recipeGeneratorRepository: RecipeGeneratorRepository
  private lateinit var viewModel: ListRecipesViewModel

  @Before
  fun setUp() {
    recipeRepository = mock(RecipeRepository::class.java)
    recipeGeneratorRepository = mock(RecipeGeneratorRepository::class.java)
    viewModel = ListRecipesViewModel(recipeRepository, recipeGeneratorRepository)
  }

  @Test
  fun returnsGeneratedUID() {
    `when`(recipeRepository.getUid()).thenReturn("generatedUID")
    val uid = viewModel.getUID()
    assertEquals("generatedUID", uid)
  }

  @Test
  fun fetchesRecipesSuccessfully() = runBlocking {
    val recipes =
        listOf(Recipe("1", "Recipe 1", listOf(), 1f, 1.toDuration(DurationUnit.SECONDS), listOf()))
    `when`(recipeRepository.getRecipes(any(), any())).thenAnswer {
      (it.arguments[0] as (List<Recipe>) -> Unit).invoke(recipes)
    }
    viewModel.getRecipes()
    assertEquals(recipes, listOf(viewModel.recipes.value[1]))
  }

  @Test
  fun handlesFailureToFetchRecipes() = runBlocking {
    val exception = Exception("Failed to fetch recipes")
    `when`(recipeRepository.getRecipes(any(), any())).thenAnswer {
      (it.arguments[1] as (Exception) -> Unit).invoke(exception)
    }
    viewModel.getRecipes()
    // Verify that the error handling method is called
    verify(recipeRepository).getRecipes(any(), any())
  }

  @Test
  fun generatesRecipeSuccessfully() = runBlocking {
    val recipePrompt = RecipePrompt("prompt")
    val generatedRecipe =
        Recipe("1", "Generated Recipe", listOf(), 1f, 1.toDuration(DurationUnit.SECONDS), listOf())
    `when`(recipeGeneratorRepository.generateRecipe(any(), any(), any())).thenAnswer {
      (it.arguments[1] as (Recipe) -> Unit).invoke(generatedRecipe)
    }
    var result: Recipe? = null
    viewModel.generateRecipe(recipePrompt, { result = it }, {})
    assertEquals(generatedRecipe, result)
  }

  @Test
  fun handlesFailureToGenerateRecipe() = runBlocking {
    val recipePrompt = RecipePrompt("prompt")
    val exception = Exception("Failed to generate recipe")
    `when`(recipeGeneratorRepository.generateRecipe(any(), any(), any())).thenAnswer {
      (it.arguments[2] as (Exception) -> Unit).invoke(exception)
    }
    var error: Exception? = null
    viewModel.generateRecipe(recipePrompt, {}, { error = it })
    assertEquals(exception, error)
  }

  @Test
  fun savesRecipeSuccessfully() = runBlocking {
    val recipe = Recipe("1", "Recipe 1", listOf(), 1f, 1.toDuration(DurationUnit.SECONDS), listOf())
    `when`(recipeRepository.getUid()).thenReturn("generatedUID")
    `when`(recipeRepository.addRecipe(any(), any(), any())).thenAnswer {
      (it.arguments[1] as () -> Unit).invoke()
    }
    viewModel.saveRecipe(recipe)
    verify(recipeRepository).addRecipe(any(), any(), any())
  }

  @Test
  fun handlesFailureToSaveRecipe() = runBlocking {
    val recipe = Recipe("1", "Recipe 1", listOf(), 1f, 1.toDuration(DurationUnit.SECONDS), listOf())
    val exception = Exception("Failed to save recipe")
    `when`(recipeRepository.getUid()).thenReturn("generatedUID")
    `when`(recipeRepository.addRecipe(any(), any(), any())).thenAnswer {
      (it.arguments[2] as (Exception) -> Unit).invoke(exception)
    }
    viewModel.saveRecipe(recipe)
    verify(recipeRepository).addRecipe(any(), any(), any())
  }

  @Test
  fun selectsRecipeSuccessfully() {
    val recipe = Recipe("1", "Recipe 1", listOf(), 1f, 1.toDuration(DurationUnit.SECONDS), listOf())
    viewModel.selectRecipe(recipe)
    assertEquals(recipe, viewModel.selectedRecipe.value)
  }

  @Test
  fun handlesNullRecipeSelection() {
    viewModel.selectRecipe(null)
    assertNull(viewModel.selectedRecipe.value)
  }
}
