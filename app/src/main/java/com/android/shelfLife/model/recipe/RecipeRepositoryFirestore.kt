package com.android.shelfLife.model.recipe

import android.util.Log
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.NutritionFacts
import com.android.shelfLife.model.foodFacts.Quantity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.coroutines.flow.asStateFlow

class RecipeRepositoryFirestore(private val db: FirebaseFirestore) : RecipeRepository {

  companion object {
    private const val COLLECTION_PATH = "recipes"
  }

  private val auth = FirebaseAuth.getInstance()
  private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
  override val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()


  private val _selectedRecipe = MutableStateFlow<Recipe?>(null)
  override val selectedRecipe: StateFlow<Recipe?> = _selectedRecipe.asStateFlow()

  /**
   * Generates a new unique ID for a recipe.
   *
   * @return A new unique ID.
   */
  override fun getUid(): String {
    Log.d("RecipeRepository", "getUid called")
    return db.collection(COLLECTION_PATH).document().id
  }

  override fun init(onSuccess: () -> Unit) {
    auth.addAuthStateListener { authVal ->
      val currentUser = authVal.currentUser
      if (currentUser != null) {
        db.collection(COLLECTION_PATH).get().addOnCompleteListener { task ->
          if (task.isSuccessful) {
            onSuccess()
          } else {
            Log.e(
                "RecipeRepositoryFirestore",
                "init failed: could not get collection : ${task.exception}")
          }
        }
      } else {
        Log.e("RecipeRepositoryFirestore", "init failed: user not logged in")
      }
    }
  }

  /**
   * Fetches all recipes from the repository.
   *
   * @param onSuccess - Called when the list of recipes is successfully retrieved.
   * @param onFailure - Called when there is an error retrieving the recipes.
   */
  override fun getRecipes(onSuccess: (List<Recipe>) -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(COLLECTION_PATH)
        .get()
        .addOnSuccessListener { result ->
          val recipeList = mutableListOf<Recipe>()
          for (document in result) {
            val recipe = convertToRecipe(document)
            if (recipe != null) recipeList.add(recipe)
          }
          _recipes.value = recipeList
          onSuccess(recipeList)
        }
        .addOnFailureListener { exception ->
          Log.e("RecipeRepository", "Error fetching recipes", exception)
          onFailure(exception)
        }
  }

  /**
   * Fetches a single recipe by its ID.
   *
   * @param recipeId - The ID of the recipe to retrieve.
   * @param onSuccess - Called when the recipe is successfully retrieved.
   * @param onFailure - Called when there is an error retrieving the recipe.
   */
  override fun getRecipe(
      recipeId: String,
      onSuccess: (Recipe) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(COLLECTION_PATH)
        .document(recipeId)
        .get()
        .addOnSuccessListener { document ->
          val recipe = convertToRecipe(document)
          if (recipe != null) {
            onSuccess(recipe)
          } else {
            onFailure(Exception("Recipe not found"))
          }
        }
        .addOnFailureListener { exception ->
          Log.e("RecipeRepository", "Error fetching recipe", exception)
          onFailure(exception)
        }
  }

  /**
   * Adds a new recipe to the repository.
   *
   * @param recipe - The recipe to be added.
   * @param onSuccess - Called when the recipe is successfully added.
   * @param onFailure - Called when there is an error adding the recipe.
   */
  override fun addRecipe(recipe: Recipe, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(COLLECTION_PATH)
        .document(recipe.uid)
        .set(recipe)
        .addOnSuccessListener {
          _recipes.update { currentRecipes ->
            currentRecipes + recipe
          }
          onSuccess() }
        .addOnFailureListener { exception ->
          Log.e("RecipeRepository", "Error adding recipe", exception)
          onFailure(exception)
        }
  }

  /**
   * Updates an existing recipe in the repository.
   *
   * @param recipe - The recipe with updated data.
   * @param onSuccess - Called when the recipe is successfully updated.
   * @param onFailure - Called when there is an error updating the recipe.
   */
  override fun updateRecipe(recipe: Recipe, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(COLLECTION_PATH)
        .document(recipe.uid)
        .set(recipe)
        .addOnSuccessListener {
          _recipes.update { currentRecipes ->
            currentRecipes.map { existingRecipe ->
              if (existingRecipe.uid == recipe.uid) recipe else existingRecipe
            }
          }
          onSuccess()

        }
        .addOnFailureListener { exception ->
          Log.e("RecipeRepository", "Error updating recipe", exception)
          onFailure(exception)
        }
  }

  /**
   * Deletes a recipe by its unique ID.
   *
   * @param recipeId - The unique ID of the recipe to delete.
   * @param onSuccess - Called when the recipe is successfully deleted.
   * @param onFailure - Called when there is an error deleting the recipe.
   */
  override fun deleteRecipe(
      recipeId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(COLLECTION_PATH)
        .document(recipeId)
        .delete()
        .addOnSuccessListener {
          _recipes.update { currentRecipes ->
            currentRecipes.filter { it.uid != recipeId }
          }
          onSuccess()
        }
        .addOnFailureListener { exception ->
          Log.e("RecipeRepository", "Error deleting recipe", exception)
          onFailure(exception)
        }
  }

  override fun selectRecipe(recipe: Recipe) {
    _selectedRecipe.value = recipe
  }

  // Helper function to convert Firestore DocumentSnapshot into a Recipe object
  private fun convertToRecipe(doc: DocumentSnapshot): Recipe? {
    return try {
      val uid = doc.getString("uid") ?: return null
      val name = doc.getString("name") ?: return null
      val instructions = doc["instructions"] as? List<String> ?: emptyList()
      val servings = (doc.getDouble("servings") ?: 0.0).toFloat()
      val timeMillis = doc.getLong("time") ?: 0L
      val time = timeMillis.toDuration(DurationUnit.SECONDS)
      val ingredients = doc["ingredients"] as? List<Map<String, Any>> ?: return null

      Recipe(
          uid = uid,
          name = name,
          instructions = instructions,
          servings = servings,
          time = time,
          ingredients = ingredients.map { convertToIngredient(it) })
    } catch (e: Exception) {
      Log.e("RecipeRepository", "Error converting document to Recipe", e)
      null
    }
  }

  // Helper function to convert a Map to an Ingredient object
  private fun convertToIngredient(map: Map<String, Any>): Ingredient {
    return try {
      val name = map["name"] as? String ?: ""
      val quantityMap = map["quantity"] as? Map<String, Any> ?: emptyMap()
      val amount = quantityMap["amount"] as? Double ?: 0.0
      val unit = quantityMap["unit"] as? String ?: "GRAM"
      val macrosMap = map["macros"] as? Map<String, Any> ?: emptyMap()

      val quantity = Quantity(amount = amount, unit = FoodUnit.valueOf(unit))
      val macros =
          NutritionFacts(
              energyKcal = (macrosMap["energyKcal"] as? Long)?.toInt() ?: 0,
              fat = (macrosMap["fat"] as? Double) ?: 0.0,
              saturatedFat = (macrosMap["saturatedFat"] as? Double) ?: 0.0,
              carbohydrates = (macrosMap["carbohydrates"] as? Double) ?: 0.0,
              sugars = (macrosMap["sugars"] as? Double) ?: 0.0,
              proteins = (macrosMap["proteins"] as? Double) ?: 0.0,
              salt = (macrosMap["salt"] as? Double) ?: 0.0)

      Ingredient(name = name, quantity = quantity, macros = macros)
    } catch (e: Exception) {
      Log.e("RecipeRepository", "Error converting map to Ingredient", e)
      Ingredient(name = "", quantity = Quantity(0.0, FoodUnit.GRAM), macros = NutritionFacts())
    }
  }

  /**
   * Converts a Recipe object to a Firestore document.
   *
   * @param recipe The Recipe object to convert.
   * @return A Firestore document.
   */
  //  fun convertRecipeToMap(recipe: Recipe): Map<String, Any?> {
  //    return try {
  //      mapOf(
  //          "uid" to recipe.uid,
  //          "name" to recipe.name,
  //          "instructions" to recipe.instructions,
  //          "servings" to recipe.servings,
  //          "time" to recipe.time.toLong(DurationUnit.SECONDS),
  //          "ingredients" to
  //              recipe.ingredients.map {
  //                mapOf(
  //                    "name" to it.name,
  //                    "quantity" to
  //                        mapOf("amount" to it.quantity.amount, "unit" to it.quantity.unit.name),
  //                    "macros" to
  //                        mapOf(
  //                            "energyKcal" to it.macros.energyKcal,
  //                            "fat" to it.macros.fat,
  //                            "saturatedFat" to it.macros.saturatedFat,
  //                            "carbohydrates" to it.macros.carbohydrates,
  //                            "sugars" to it.macros.sugars,
  //                            "proteins" to it.macros.proteins,
  //                            "salt" to it.macros.salt))
  //              })
  //    } catch (e: Exception) {
  //      Log.e("RecipeRepository", "Error converting Recipe to map", e)
  //      emptyMap()
  //    }
  //  }
}
