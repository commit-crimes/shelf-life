package com.android.shelfLife.model.recipe

import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

open class ListRecipesViewModel() : ViewModel() {

  private val instructionsTortillaDePatata = // this is an example to shown the scroll-ability of
                                             // the IndividualRecipeScreen
      "Peel and slice the potatoes:\n" +
          "\n" +
          "Peel the potatoes and cut them into thin, even slices (around 3-4 mm thick).\n" +
          "Rinse and dry them thoroughly with a clean cloth or paper towel.\n" +
          "Slice the onion (if using):\n" +
          "\n" +
          "Peel and cut the onion into thin slices. The onion will add sweetness and extra flavor to the tortilla.\n" +
          "Fry the potatoes (and onion):\n" +
          "\n" +
          "Heat the olive oil in a deep frying pan over medium heat.\n" +
          "Add the potatoes and onions to the oil, making sure they are submerged. Cook them slowly, turning occasionally, until they are soft but not browned (about 15-20 minutes). You want them tender but not crispy.\n" +
          "Use a slotted spoon to remove the potatoes and onions from the oil, draining off excess oil. Set them aside in a large bowl.\n" +
          "Tip: Save the leftover oil for future use. You can strain and reuse it.\n" +
          "Beat the eggs:\n" +
          "\n" +
          "While the potatoes are cooling, crack the eggs into a large bowl, add a pinch of salt, and beat them until well mixed.\n" +
          "Combine potatoes and eggs:\n" +
          "\n" +
          "Once the potatoes have cooled slightly, add them to the beaten eggs. Stir gently to coat the potatoes with the egg mixture. Let it sit for about 10 minutes so the potatoes absorb the egg.\n" +
          "Cook the tortilla:\n" +
          "\n" +
          "Heat 1-2 tablespoons of olive oil in a non-stick frying pan (around 20-22 cm in diameter) over medium heat.\n" +
          "Pour the potato-egg mixture into the pan and cook on medium-low heat for about 5-7 minutes, until the bottom is set but the top is still slightly runny. Gently shake the pan to prevent sticking.\n" +
          "Flip the tortilla:\n" +
          "\n" +
          "Once the tortilla is mostly set, place a large plate or lid over the pan, then carefully flip the pan so the tortilla ends up on the plate.\n" +
          "Slide the tortilla back into the pan to cook the other side for about 3-5 minutes, until fully set but still moist in the middle.\n" +
          "Serve:\n" +
          "\n" +
          "Remove the tortilla from the pan and let it cool for a few minutes before slicing.\n" +
          "It can be served warm or at room temperature, and is typically enjoyed with crusty bread or a simple salad."

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
                  instructions = instructionsTortillaDePatata,
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
                  name = "Butifarra con boniato al horno",
                  instructions = "cry",
                  servings = 3,
                  time =
                      Timestamp(3600, 0)))) // it has an extra long name to show the ... in the card
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
