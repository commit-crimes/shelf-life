package com.android.shelfLife.model.recipe

import android.util.Log
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.NutritionFacts
import com.android.shelfLife.model.foodFacts.Quantity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

  private var recipesListenerRegistration: ListenerRegistration? = null

  /**
   * Generates a new unique ID for a recipe.
   *
   * @return A new unique ID.
   */
  override fun getUid(): String {
    return db.collection(COLLECTION_PATH).document().id
  }

  /**
   * Initializes the local cache of recipes with the given list of recipe IDs. If the list is empty,
   * clears the local cache.
   *
   * @param recipeIds A list of recipe IDs to load from Firestore.
   * @param selectedRecipeId An optional ID to pre-select a recipe.
   */
  override suspend fun initializeRecipes(recipeIds: List<String>, selectedRecipeId: String?) {
    Log.d("RecipeRepository", "Initializing recipes with IDs: $recipeIds")
    if (recipeIds.isEmpty()) {
      _recipes.value = emptyList()
      _selectedRecipe.value = null
      return
    }
    try {
      val querySnapshot =
          db.collection(COLLECTION_PATH).whereIn(FieldPath.documentId(), recipeIds).get().await()

      val fetchedRecipes = querySnapshot.documents.mapNotNull { convertToRecipe(it) }
      _recipes.value = fetchedRecipes
      selectRecipe(_recipes.value.find { it.uid == selectedRecipeId })
    } catch (e: Exception) {
      Log.e("RecipeRepository", "Error initializing recipes", e)
      _recipes.value = emptyList()
      _selectedRecipe.value = null
    }
    Log.i("RecipeRepository", "_recipes : ${_recipes.value}")
  }

  /**
   * Fetches recipes by their IDs directly, returning the result.
   *
   * @param listUserRecipeUid A list of recipe UIDs.
   * @return A list of fetched recipes.
   */
  override suspend fun getRecipes(listUserRecipeUid: List<String>): List<Recipe> {
    if (listUserRecipeUid.isEmpty()) {
      return emptyList()
    }
    return try {
      val querySnapshot =
          db.collection(COLLECTION_PATH)
              .whereIn(FieldPath.documentId(), listUserRecipeUid)
              .get()
              .await()
      querySnapshot.documents.mapNotNull { convertToRecipe(it) }
    } catch (e: Exception) {
      Log.e("RecipeRepository", "Error fetching recipes", e)
      emptyList()
    }
  }

  /**
   * Adds a new recipe to Firestore and updates the local cache. Rolls back local changes if
   * Firestore operation fails.
   *
   * @param recipe The recipe to add.
   */
  override suspend fun addRecipe(recipe: Recipe) {
    Log.i("RecipeRepository", "recipe  ${recipe}")
    var addedLocally = false
    try {
      // Update local cache first
      val currentRecipes = _recipes.value.toMutableList()
      currentRecipes.add(recipe)
      _recipes.value = currentRecipes
      addedLocally = true

      db.collection(COLLECTION_PATH).document(recipe.uid).set(recipe)
    } catch (e: Exception) {
      Log.e("RecipeRepository", "Error adding recipe", e)
      // Rollback
      if (addedLocally) {
        _recipes.value = _recipes.value.filterNot { it.uid == recipe.uid }
      }
    }
  }

  /**
   * Updates an existing recipe in Firestore and the local cache. Rolls back local changes if
   * Firestore operation fails.
   *
   * @param recipe The recipe with updated data.
   */
  override suspend fun updateRecipe(recipe: Recipe) {
    var originalRecipe: Recipe? = null
    try {
      // Update local cache
      val currentRecipes = _recipes.value.toMutableList()
      val index = currentRecipes.indexOfFirst { it.uid == recipe.uid }
      if (index != -1) {
        originalRecipe = currentRecipes[index]
        currentRecipes[index] = recipe
      } else {
        // If not found, just add it
        currentRecipes.add(recipe)
      }
      _recipes.value = currentRecipes

      db.collection(COLLECTION_PATH).document(recipe.uid).set(recipe)
    } catch (e: Exception) {
      Log.e("RecipeRepository", "Error updating recipe", e)
      // Rollback changes
      originalRecipe?.let {
        val currentRecipes = _recipes.value.toMutableList()
        val index = currentRecipes.indexOfFirst { it.uid == recipe.uid }
        if (index != -1) {
          currentRecipes[index] = it
          _recipes.value = currentRecipes
        } else {
          // If we had added it because it didn't exist, remove it now
          _recipes.value = currentRecipes.filterNot { r -> r.uid == recipe.uid }
        }
      }
    }
  }

  /**
   * Deletes a recipe by its unique ID. Rolls back local changes if Firestore operation fails.
   *
   * @param recipeId The unique ID of the recipe to delete.
   */
  override suspend fun deleteRecipe(recipeId: String): Boolean {
    var deletedRecipe: Recipe? = null
    try {
      // Update local cache
      deletedRecipe = _recipes.value.find { it.uid == recipeId }
      _recipes.value = _recipes.value.filterNot { it.uid == recipeId }

      db.collection(COLLECTION_PATH).document(recipeId).delete()
    } catch (e: Exception) {
      Log.e("RecipeRepository", "Error deleting recipe", e)
      // Rollback
      deletedRecipe?.let {
        val current = _recipes.value.toMutableList()
        current.add(it)
        _recipes.value = current
      }
      return false
    }
    return true
  }

  /**
   * Selects a recipe locally. This does not affect Firestore.
   *
   * @param recipe The recipe to select.
   */
  override fun selectRecipe(recipe: Recipe?) {
    _selectedRecipe.value = recipe
  }

  /**
   * (Optional) Start listening for real-time updates to a given set of recipes. If your app
   * requires real-time updates, this can be useful.
   *
   * @param recipeIds List of recipe IDs to listen to.
   */
  fun startListeningForRecipes(recipeIds: List<String>) {
    // Remove any existing listener
    recipesListenerRegistration?.remove()

    if (recipeIds.isEmpty()) {
      _recipes.value = emptyList()
      return
    }

    recipesListenerRegistration =
        db.collection(COLLECTION_PATH)
            .whereIn(FieldPath.documentId(), recipeIds)
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                Log.e("RecipeRepository", "Error fetching recipes", error)
                _recipes.value = emptyList()
                return@addSnapshotListener
              }
              if (snapshot != null) {
                val updatedRecipes = snapshot.documents.mapNotNull { convertToRecipe(it) }
                _recipes.value = updatedRecipes
              }
            }
  }

  /** (Optional) Stops listening for real-time updates to recipes. */
  fun stopListeningForRecipes() {
    recipesListenerRegistration?.remove()
    recipesListenerRegistration = null
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
      val recipeType = doc.getString("recipeType") ?: return null

      Log.d("RecipeRepository", "Name of recipe: $name")
      Recipe(
          uid = uid,
          name = name,
          instructions = instructions,
          servings = servings,
          time = time,
          ingredients = ingredients.map { convertToIngredient(it) },
          recipeType = convertToSearchType(recipeType))
    } catch (e: Exception) {
      Log.e("RecipeRepository", "Error converting document to Recipe", e)
      null
    }
  }

  private fun convertToSearchType(recipeType: String): RecipeType {
    return when (recipeType) {
      "HIGH_PROTEIN" -> RecipeType.HIGH_PROTEIN
      "LOW_CALORIE" -> RecipeType.LOW_CALORIE
      "PERSONAL" -> RecipeType.PERSONAL
      else -> RecipeType.BASIC // its the default value
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
}
