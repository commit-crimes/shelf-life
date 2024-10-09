package com.android.shelfLife.model.recipe

import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

open class ListRecipesViewModel() : ViewModel() {
  // list of the recipes, for the moment I have filled up manually
  private val recipes_ =
      MutableStateFlow<List<Recipe>>(
          listOf(
              Recipe(
                  name = "Paella", instructions = "cook", servings = 4, time = Timestamp(5400, 0)),
              Recipe(
                  name = "Fideua", instructions = "cry", servings = 3, time = Timestamp(3600, 0)),
              Recipe(
                  name = "Tortilla de patata",
                  instructions = "cook",
                  servings = 4,
                  time = Timestamp(5400, 0)),
              Recipe(
                  name = "Costillas a la brasa",
                  instructions = "cry",
                  servings = 3,
                  time = Timestamp(3600, 0)),
              Recipe(
                  name = "Curry rojo",
                  instructions = "cook",
                  servings = 4,
                  time = Timestamp(5400, 0)),
              Recipe(
                  name = "Butifarra con boniato",
                  instructions = "cry",
                  servings = 3,
                  time = Timestamp(3600, 0))))
  val recipes: StateFlow<List<Recipe>> = recipes_.asStateFlow()

  // Selected recipe, i.e the recipe for the detail view
  private val selectedRecipe_ = MutableStateFlow<Recipe?>(null)
  open val selectedRecipe: StateFlow<Recipe?> = selectedRecipe_.asStateFlow()

  /**
   * Selects a recipe.
   *
   * @param recipe The Recipe to be selected.
   */
  fun selectRecipe(recipe: Recipe) {
    selectedRecipe_.value = recipe
  }
}
