package com.android.shelflife.model.recipe

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.android.shelfLife.model.foodItem.FoodItemRepositoryFirestore
import com.android.shelfLife.model.recipe.RecipeRepositoryFirestore
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule

import com.android.shelfLife.model.recipe.Recipe
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.capture
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@RunWith(RobolectricTestRunner::class)
class RecipeRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot

  private lateinit var recipeRepository: RecipeRepositoryFirestore

  private val testRecipe = Recipe(
    uid = "recipe1",
    name = "Test Recipe",
    instructions = listOf("Step 1", "Step 2"),
    servings = 4.0f,
    time = 3600.toDuration(DurationUnit.SECONDS),
    ingredients = emptyList()
  )

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    recipeRepository = RecipeRepositoryFirestore(mockFirestore)

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
  }

  @Test
  fun `getUid generates a new ID`() {
    `when`(mockDocumentReference.id).thenReturn("newRecipeId")

    val generatedId = recipeRepository.getUid()

    assertEquals("newRecipeId", generatedId)
  }

  @Test
  fun `getRecipes retrieves all recipes`() {
    val onSuccess: (List<Recipe>) -> Unit = mock()
    val onFailure: (Exception) -> Unit = mock()

    val mockQuerySnapshot: QuerySnapshot = mock()
    `when`(mockCollectionReference.get()).thenReturn(mock())
    `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))
    `when`(mockDocumentSnapshot.getString("uid")).thenReturn("recipe1")
    `when`(mockDocumentSnapshot.getString("name")).thenReturn("Test Recipe")
    `when`(mockDocumentSnapshot.get("instructions")).thenReturn(listOf("Step 1", "Step 2"))
    `when`(mockDocumentSnapshot.getDouble("servings")).thenReturn(4.0)
    `when`(mockDocumentSnapshot.getLong("time")).thenReturn(3600L)
    `when`(mockDocumentSnapshot.get("ingredients")).thenReturn(emptyList<Any>())

    val taskCaptor = argumentCaptor<OnSuccessListener<QuerySnapshot>>()
    verify(mockCollectionReference.get()).addOnSuccessListener(taskCaptor.capture())
    taskCaptor.firstValue.onSuccess(mockQuerySnapshot)

    verify(onSuccess).invoke(any())
    verify(onFailure, never()).invoke(any())
  }

  @Test
  fun `getRecipe retrieves a recipe by ID`() {
    val onSuccess: (Recipe) -> Unit = mock()
    val onFailure: (Exception) -> Unit = mock()

    `when`(mockDocumentReference.get()).thenReturn(mock())
    `when`(mockDocumentSnapshot.getString("uid")).thenReturn("recipe1")
    `when`(mockDocumentSnapshot.getString("name")).thenReturn("Test Recipe")
    `when`(mockDocumentSnapshot.get("instructions")).thenReturn(listOf("Step 1", "Step 2"))
    `when`(mockDocumentSnapshot.getDouble("servings")).thenReturn(4.0)
    `when`(mockDocumentSnapshot.getLong("time")).thenReturn(3600L)
    `when`(mockDocumentSnapshot.get("ingredients")).thenReturn(emptyList<Any>())

    val taskCaptor = argumentCaptor<OnSuccessListener<DocumentSnapshot>>()
    verify(mockDocumentReference.get()).addOnSuccessListener(taskCaptor.capture())
    taskCaptor.firstValue.onSuccess(mockDocumentSnapshot)

    verify(onSuccess).invoke(any())
    verify(onFailure, never()).invoke(any())
  }

  @Test
  fun `addRecipe successfully adds a recipe`() {
    val onSuccess: () -> Unit = mock()
    val onFailure: (Exception) -> Unit = mock()

    val task = mock<Task<Void>>()
    `when`(mockDocumentReference.set(any())).thenReturn(task)

    recipeRepository.addRecipe(testRecipe, onSuccess, onFailure)

    val successCaptor = argumentCaptor<OnSuccessListener<Void>>()
    verify(task).addOnSuccessListener(successCaptor.capture())
    successCaptor.firstValue.onSuccess(null)

    verify(onSuccess).invoke()
    verify(onFailure, never()).invoke(any())
  }

  @Test
  fun `updateRecipe successfully updates a recipe`() {
    val onSuccess: () -> Unit = mock()
    val onFailure: (Exception) -> Unit = mock()

    val task = mock<Task<Void>>()
    `when`(mockDocumentReference.set(any())).thenReturn(task)

    recipeRepository.updateRecipe(testRecipe, onSuccess, onFailure)

    val successCaptor = argumentCaptor<OnSuccessListener<Void>>()
    verify(task).addOnSuccessListener(successCaptor.capture())
    successCaptor.firstValue.onSuccess(null)

    verify(onSuccess).invoke()
    verify(onFailure, never()).invoke(any())
  }

  @Test
  fun `deleteRecipe successfully deletes a recipe`() {
    val onSuccess: () -> Unit = mock()
    val onFailure: (Exception) -> Unit = mock()

    val task: Task<Void> = mock()

    `when`(mockDocumentReference.delete()).thenReturn(task)

    recipeRepository.deleteRecipe("recipe1", onSuccess, onFailure)

    val successCaptor = argumentCaptor<OnSuccessListener<Void>>()
    verify(task).addOnSuccessListener(successCaptor.capture())
    successCaptor.firstValue.onSuccess(null)

    verify(onSuccess).invoke()
    verify(onFailure, never()).invoke(any())
  }

/*  @Test
  fun `convertToRecipe correctly parses document to Recipe`() {
    `when`(mockDocumentSnapshot.getString("uid")).thenReturn("recipe1")
    `when`(mockDocumentSnapshot.getString("name")).thenReturn("Test Recipe")
    `when`(mockDocumentSnapshot.get("instructions")).thenReturn(listOf("Step 1", "Step 2"))
    `when`(mockDocumentSnapshot.getDouble("servings")).thenReturn(4.0)
    `when`(mockDocumentSnapshot.getLong("time")).thenReturn(3600L)
    `when`(mockDocumentSnapshot.get("ingredients")).thenReturn(emptyList<Any>())

    val recipe = recipeRepository.convertToRecipe(mockDocumentSnapshot)

    assertNotNull(recipe)
    assertEquals("recipe1", recipe?.uid)
    assertEquals("Test Recipe", recipe?.name)
    assertEquals(listOf("Step 1", "Step 2"), recipe?.instructions)
    assertEquals(4.0f, recipe?.servings)
  }*/
}
