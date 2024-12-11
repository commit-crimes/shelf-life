package com.android.shelfLife.model.newRecipe

import android.util.Log
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.NutritionFacts
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.recipe.Ingredient
import com.android.shelfLife.model.recipe.Recipe
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await

@Singleton
class RecipeRepositoryFirestore @Inject constructor(private val db: FirebaseFirestore) :
    RecipeRepository {

  companion object {
    private const val COLLECTION_PATH = "recipes"
  }

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
     * Fetches recipes from the database whose document IDs match the provided list of user recipe UIDs.
     *
     * @param listUserRecipeUid A list of recipe UIDs belonging to the user. If the list is empty, the function sets `_recipes` to an empty list.
     *
     * This function performs the following steps:
     * 1. Queries the database collection specified by `COLLECTION_PATH` for documents with IDs in `listUserRecipeUid`.
     * 2. Converts the resulting documents into `Recipe` objects using the `convertToRecipe` function.
     * 3. Updates the `_recipes` state with the fetched recipes or an empty list if no matches are found.
     *
     * If an exception occurs during the database operation, it logs the error and sets `_recipes` to an empty list.
     *
     * This is a `suspend` function and should be called within a coroutine.
     */
  override suspend fun getRecipes(listUserRecipeUid : List<String>) {
      if(listUserRecipeUid.isEmpty()){
          _recipes.value = emptyList()
      }
      try{
          val querySnapshot = db.collection(COLLECTION_PATH)
              .whereIn(FieldPath.documentId(), listUserRecipeUid)
              .get()
              .await()
          _recipes.value = querySnapshot.documents.mapNotNull { convertToRecipe(it) }
      }catch(e : Exception){
          Log.e("RecipesRepository", "Error fetching recipes", e)
          _recipes.value = emptyList()
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
          _recipes.update { currentRecipes -> currentRecipes + recipe }
          onSuccess()
        }
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
          _recipes.update { currentRecipes -> currentRecipes.filter { it.uid != recipeId } }
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
