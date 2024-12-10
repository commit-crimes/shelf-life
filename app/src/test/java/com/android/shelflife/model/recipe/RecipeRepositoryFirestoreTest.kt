import androidx.test.core.app.ApplicationProvider
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.recipe.Recipe
import com.android.shelfLife.model.newRecipe.RecipeRepositoryFirestore
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.reflect.Field
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class RecipeRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore

  @Mock private lateinit var mockAuth: FirebaseAuth

  @Mock private lateinit var mockUserCollection: CollectionReference

  @Mock private lateinit var mockUserDocument: DocumentReference

  @Mock private lateinit var mockFirebaseUser: FirebaseUser

  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot

  private lateinit var recipeRepository: RecipeRepositoryFirestore

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }
    // Mock FirebaseAuth and FirebaseUser
    `when`(mockAuth.currentUser).thenReturn(mockFirebaseUser)
    `when`(mockFirebaseUser.uid).thenReturn("testUserId")

    // Mock FirebaseFirestore
    `when`(mockFirestore.collection("recipes")).thenReturn(mockUserCollection)
    `when`(mockUserCollection.document("testUserId")).thenReturn(mockUserDocument)

    // Initialize the recipeRepository with mocks
    recipeRepository = RecipeRepositoryFirestore(mockFirestore)
    val authField: Field = recipeRepository.javaClass.getDeclaredField("auth")
    authField.isAccessible = true
    authField.set(recipeRepository, mockAuth)

    // Set the Dispatchers to use the TestCoroutineDispatcher
    Dispatchers.setMain(StandardTestDispatcher())
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `getNewUid returns new document ID`() {
    `when`(mockUserCollection.document()).thenReturn(mockUserDocument)
    `when`(mockUserDocument.id).thenReturn("newUserId")

    val uid = recipeRepository.getUid()

    assertEquals("newUserId", uid)
  }

  @Test
  fun `addRecipe invokes onSuccess when recipe is added successfully`() {
    // Arrange
    val testRecipe =
        Recipe(
            uid = "testRecipeId",
            name = "Test Recipe",
            instructions = listOf("Step 1", "Step 2"),
            servings = 4.0f,
            time = 3600.toDuration(DurationUnit.SECONDS),
            ingredients = emptyList())

    val mockTask = mock<com.google.android.gms.tasks.Task<Void>>()
    `when`(mockFirestore.collection("recipes").document(testRecipe.uid))
        .thenReturn(mockUserDocument)
    `when`(mockUserDocument.set(testRecipe)).thenReturn(mockTask)
    `when`(mockTask.addOnSuccessListener(any())).thenAnswer {
      val successListener = it.arguments[0] as com.google.android.gms.tasks.OnSuccessListener<Void>
      successListener.onSuccess(null)
      mockTask
    }
    `when`(mockTask.addOnFailureListener(any())).thenReturn(mockTask)

    var successCalled = false

    // Act
    recipeRepository.addRecipe(
        testRecipe,
        onSuccess = { successCalled = true },
        onFailure = {
          // Failure should not be called in this test
          throw AssertionError("onFailure should not be called")
        })

    // Assert
    assertEquals(true, successCalled)
  }

  @Test
  fun `addRecipe invokes onFailure when adding recipe fails`() {
    // Arrange
    val testRecipe =
        Recipe(
            uid = "testRecipeId",
            name = "Test Recipe",
            instructions = listOf("Step 1", "Step 2"),
            servings = 4.0f,
            time = 3600.toDuration(DurationUnit.SECONDS),
            ingredients = emptyList())

    val mockTask = mock<com.google.android.gms.tasks.Task<Void>>()
    `when`(mockFirestore.collection("recipes").document(testRecipe.uid))
        .thenReturn(mockUserDocument)
    `when`(mockUserDocument.set(testRecipe)).thenReturn(mockTask)
    `when`(mockTask.addOnFailureListener(any())).thenAnswer {
      val failureListener = it.arguments[0] as com.google.android.gms.tasks.OnFailureListener
      failureListener.onFailure(Exception("Firestore error"))
      mockTask
    }

    `when`(mockTask.addOnSuccessListener(any())).thenReturn(mockTask)
    var failureCalled = false
    var failureMessage: String? = null

    // Act
    recipeRepository.addRecipe(
        testRecipe,
        onSuccess = {
          // Success should not be called in this test
          throw AssertionError("onSuccess should not be called")
        },
        onFailure = { exception ->
          failureCalled = true
          failureMessage = exception.message
        })

    // Assert
    assertEquals(true, failureCalled)
    assertEquals("Firestore error", failureMessage)
  }

  @Test
  fun `updateRecipe invokes onSuccess when recipe is updated successfully`() {
    // Arrange
    val testRecipe =
        Recipe(
            uid = "testRecipeId",
            name = "Updated Recipe",
            instructions = listOf("Step 1", "Step 2"),
            servings = 2.0f,
            time = 1800.toDuration(DurationUnit.SECONDS),
            ingredients = emptyList())

    val mockTask = mock<com.google.android.gms.tasks.Task<Void>>()
    `when`(mockFirestore.collection("recipes").document(testRecipe.uid))
        .thenReturn(mockUserDocument)
    `when`(mockUserDocument.set(testRecipe)).thenReturn(mockTask)
    `when`(mockTask.addOnSuccessListener(any())).thenAnswer {
      val successListener = it.arguments[0] as com.google.android.gms.tasks.OnSuccessListener<Void>
      successListener.onSuccess(null)
      mockTask
    }
    `when`(mockTask.addOnFailureListener(any())).thenReturn(mockTask)

    var successCalled = false

    // Act
    recipeRepository.updateRecipe(
        testRecipe,
        onSuccess = { successCalled = true },
        onFailure = {
          // Failure should not be called in this test
          throw AssertionError("onFailure should not be called")
        })

    // Assert
    assertEquals(true, successCalled)
  }

  @Test
  fun `updateRecipe invokes onFailure when updating recipe fails`() {
    // Arrange
    val testRecipe =
        Recipe(
            uid = "testRecipeId",
            name = "Updated Recipe",
            instructions = listOf("Step 1", "Step 2"),
            servings = 2.0f,
            time = 1800.toDuration(DurationUnit.SECONDS),
            ingredients = emptyList())

    val mockTask = mock<com.google.android.gms.tasks.Task<Void>>()
    `when`(mockFirestore.collection("recipes").document(testRecipe.uid))
        .thenReturn(mockUserDocument)
    `when`(mockUserDocument.set(testRecipe)).thenReturn(mockTask)
    `when`(mockTask.addOnFailureListener(any())).thenAnswer {
      val failureListener = it.arguments[0] as com.google.android.gms.tasks.OnFailureListener
      failureListener.onFailure(Exception("Firestore update error"))
      mockTask
    }
    `when`(mockTask.addOnSuccessListener(any())).thenReturn(mockTask)

    var failureCalled = false
    var failureMessage: String? = null

    // Act
    recipeRepository.updateRecipe(
        testRecipe,
        onSuccess = {
          // Success should not be called in this test
          throw AssertionError("onSuccess should not be called")
        },
        onFailure = { exception ->
          failureCalled = true
          failureMessage = exception.message
        })

    // Assert
    assertEquals(true, failureCalled)
    assertEquals("Firestore update error", failureMessage)
  }

  @Test
  fun `deleteRecipe invokes onSuccess when recipe is deleted successfully`() {
    // Arrange
    val testRecipeId = "testRecipeId"

    val mockTask = mock<com.google.android.gms.tasks.Task<Void>>()
    `when`(mockFirestore.collection("recipes").document(testRecipeId)).thenReturn(mockUserDocument)
    `when`(mockUserDocument.delete()).thenReturn(mockTask)
    `when`(mockTask.addOnSuccessListener(any())).thenAnswer {
      val successListener = it.arguments[0] as com.google.android.gms.tasks.OnSuccessListener<Void>
      successListener.onSuccess(null)
      mockTask
    }
    `when`(mockTask.addOnFailureListener(any())).thenReturn(mockTask)

    var successCalled = false

    // Act
    recipeRepository.deleteRecipe(
        testRecipeId,
        onSuccess = { successCalled = true },
        onFailure = {
          // Failure should not be called in this test
          throw AssertionError("onFailure should not be called")
        })

    // Assert
    assertEquals(true, successCalled)
  }

  @Test
  fun `deleteRecipe invokes onFailure when deleting recipe fails`() {
    // Arrange
    val testRecipeId = "testRecipeId"

    val mockTask = mock<com.google.android.gms.tasks.Task<Void>>()
    `when`(mockFirestore.collection("recipes").document(testRecipeId)).thenReturn(mockUserDocument)
    `when`(mockUserDocument.delete()).thenReturn(mockTask)
    `when`(mockTask.addOnFailureListener(any())).thenAnswer {
      val failureListener = it.arguments[0] as com.google.android.gms.tasks.OnFailureListener
      failureListener.onFailure(Exception("Firestore delete error"))
      mockTask
    }
    `when`(mockTask.addOnSuccessListener(any())).thenReturn(mockTask)

    var failureCalled = false
    var failureMessage: String? = null

    // Act
    recipeRepository.deleteRecipe(
        testRecipeId,
        onSuccess = {
          // Success should not be called in this test
          throw AssertionError("onSuccess should not be called")
        },
        onFailure = { exception ->
          failureCalled = true
          failureMessage = exception.message
        })

    // Assert
    assertEquals(true, failureCalled)
    assertEquals("Firestore delete error", failureMessage)
  }

  @Test
  fun `getRecipe invokes onSuccess when recipe is successfully retrieved`() {
    // Arrange
    val testRecipeId = "testRecipeId"
    val mockDocument = mock<DocumentSnapshot>()
    val mockTask = mock<com.google.android.gms.tasks.Task<DocumentSnapshot>>()

    `when`(mockFirestore.collection("recipes").document(testRecipeId)).thenReturn(mockUserDocument)
    `when`(mockUserDocument.get()).thenReturn(mockTask)
    `when`(mockTask.addOnSuccessListener(any())).thenAnswer {
      val successListener =
          it.arguments[0] as com.google.android.gms.tasks.OnSuccessListener<DocumentSnapshot>
      successListener.onSuccess(mockDocument)
      mockTask
    }
    `when`(mockTask.addOnFailureListener(any())).thenReturn(mockTask)

    // Mock the document data
    `when`(mockDocument.getString("uid")).thenReturn(testRecipeId)
    `when`(mockDocument.getString("name")).thenReturn("Test Recipe")
    `when`(mockDocument["instructions"]).thenReturn(listOf("Step 1", "Step 2"))
    `when`(mockDocument.getDouble("servings")).thenReturn(4.0)
    `when`(mockDocument.getLong("time")).thenReturn(3600L)
    `when`(mockDocument["ingredients"]).thenReturn(emptyList<Map<String, Any>>())

    // Use reflection to access the private method
    val method =
        RecipeRepositoryFirestore::class
            .java
            .getDeclaredMethod("convertToRecipe", DocumentSnapshot::class.java)
    method.isAccessible = true

    var retrievedRecipe: Recipe? = null

    // Act
    recipeRepository.getRecipe(
        testRecipeId,
        onSuccess = { recipe -> retrievedRecipe = recipe },
        onFailure = {
          // Failure should not be called in this test
          throw AssertionError("onFailure should not be called")
        })

    // Assert
    assertNotNull(retrievedRecipe)

    // Use reflection to verify the recipe is correctly converted
    val expectedRecipe = method.invoke(recipeRepository, mockDocument) as? Recipe
    assertEquals(expectedRecipe, retrievedRecipe)
  }

  @Test
  fun `getRecipe invokes onFailure when recipe document does not exist`() {
    // Arrange
    val testRecipeId = "testRecipeId"
    val mockDocument = mock<DocumentSnapshot>()
    val mockTask = mock<com.google.android.gms.tasks.Task<DocumentSnapshot>>()
    `when`(mockFirestore.collection("recipes").document(testRecipeId)).thenReturn(mockUserDocument)
    `when`(mockUserDocument.get()).thenReturn(mockTask)
    `when`(mockTask.addOnSuccessListener(any())).thenAnswer {
      val successListener =
          it.arguments[0] as com.google.android.gms.tasks.OnSuccessListener<DocumentSnapshot>
      successListener.onSuccess(mockDocument) // Simulate document retrieval
      mockTask
    }
    `when`(mockTask.addOnFailureListener(any())).thenReturn(mockTask)

    // Use reflection to access the private method
    val method =
        RecipeRepositoryFirestore::class
            .java
            .getDeclaredMethod("convertToRecipe", DocumentSnapshot::class.java)
    method.isAccessible = true

    var failureCalled = false
    var failureMessage: String? = null

    // Act
    recipeRepository.getRecipe(
        testRecipeId,
        onSuccess = {
          // Success should not be called in this test
          throw AssertionError("onSuccess should not be called")
        },
        onFailure = { exception ->
          failureCalled = true
          failureMessage = exception.message
        })

    // Assert
    assertTrue(failureCalled)
    assertEquals("Recipe not found", failureMessage)
  }

  @Test
  fun `getRecipe invokes onFailure when fetching recipe fails`() {
    // Arrange
    val testRecipeId = "testRecipeId"
    val mockTask = mock<com.google.android.gms.tasks.Task<DocumentSnapshot>>()
    `when`(mockFirestore.collection("recipes").document(testRecipeId)).thenReturn(mockUserDocument)
    `when`(mockUserDocument.get()).thenReturn(mockTask)
    `when`(mockTask.addOnSuccessListener(any())).thenReturn(mockTask)

    `when`(mockTask.addOnFailureListener(any())).thenAnswer {
      val failureListener = it.arguments[0] as com.google.android.gms.tasks.OnFailureListener
      failureListener.onFailure(Exception("Firestore error"))
      mockTask
    }

    var failureCalled = false
    var failureMessage: String? = null

    // Act
    recipeRepository.getRecipe(
        testRecipeId,
        onSuccess = {
          // Success should not be called in this test
          throw AssertionError("onSuccess should not be called")
        },
        onFailure = { exception ->
          failureCalled = true
          failureMessage = exception.message
        })

    // Assert
    assertTrue(failureCalled)
    assertEquals("Firestore error", failureMessage)
  }

  @Test
  fun `convertToRecipe successfully converts valid DocumentSnapshot`() {
    // Arrange
    val mockDocument = mock<DocumentSnapshot>()
    `when`(mockDocument.getString("uid")).thenReturn("testRecipeId")
    `when`(mockDocument.getString("name")).thenReturn("Test Recipe")
    `when`(mockDocument["instructions"]).thenReturn(listOf("Step 1", "Step 2"))
    `when`(mockDocument.getDouble("servings")).thenReturn(4.0)
    `when`(mockDocument.getLong("time")).thenReturn(3600L)
    `when`(mockDocument["ingredients"])
        .thenReturn(
            listOf(
                mapOf(
                    "name" to "Ingredient 1",
                    "quantity" to mapOf("amount" to 200.0, "unit" to "GRAM"),
                    "macros" to
                        mapOf(
                            "energyKcal" to 100L,
                            "fat" to 1.5,
                            "saturatedFat" to 0.5,
                            "carbohydrates" to 20.0,
                            "sugars" to 5.0,
                            "proteins" to 2.0,
                            "salt" to 0.1))))

    val method =
        RecipeRepositoryFirestore::class
            .java
            .getDeclaredMethod("convertToRecipe", DocumentSnapshot::class.java)
    method.isAccessible = true

    // Act
    val recipe = method.invoke(recipeRepository, mockDocument) as? Recipe

    // Assert
    assertNotNull(recipe)
    assertEquals("testRecipeId", recipe?.uid)
    assertEquals("Test Recipe", recipe?.name)
    assertEquals(listOf("Step 1", "Step 2"), recipe?.instructions)
    assertEquals(4.0f, recipe?.servings)
    assertEquals(3600L.toDuration(DurationUnit.SECONDS), recipe?.time)
    assertEquals(1, recipe?.ingredients?.size)
    val ingredient = recipe?.ingredients?.first()
    assertEquals("Ingredient 1", ingredient?.name)
    assertEquals(200.0, ingredient?.quantity?.amount)
    assertEquals(FoodUnit.GRAM, ingredient?.quantity?.unit)
    assertEquals(100, ingredient?.macros?.energyKcal)
  }

  @Test
  fun `convertToRecipe returns null when DocumentSnapshot is missing required fields`() {
    // Arrange
    val mockDocument = mock<DocumentSnapshot>()
    `when`(mockDocument.getString("uid")).thenReturn(null) // Missing UID

    // Use reflection to access private method
    val method =
        RecipeRepositoryFirestore::class
            .java
            .getDeclaredMethod("convertToRecipe", DocumentSnapshot::class.java)
    method.isAccessible = true

    // Act
    val recipe = method.invoke(recipeRepository, mockDocument) as? Recipe

    // Assert
    assertNull(recipe)
  }

  @Test
  fun `convertToRecipe handles invalid ingredients gracefully`() {
    // Arrange
    val mockDocument = mock<DocumentSnapshot>()
    `when`(mockDocument.getString("uid")).thenReturn("testRecipeId")
    `when`(mockDocument.getString("name")).thenReturn("Test Recipe")
    `when`(mockDocument["instructions"]).thenReturn(listOf("Step 1", "Step 2"))
    `when`(mockDocument.getDouble("servings")).thenReturn(4.0)
    `when`(mockDocument.getLong("time")).thenReturn(3600L)
    `when`(mockDocument["ingredients"])
        .thenReturn(
            listOf(
                mapOf(
                    "name" to "Ingredient 1",
                    "quantity" to "Invalid Quantity", // Invalid quantity data
                    "macros" to
                        mapOf(
                            "energyKcal" to 100L,
                            "fat" to 1.5,
                            "saturatedFat" to 0.5,
                            "carbohydrates" to 20.0,
                            "sugars" to 5.0,
                            "proteins" to 2.0,
                            "salt" to 0.1))))

    // Use reflection to access private method
    val method =
        RecipeRepositoryFirestore::class
            .java
            .getDeclaredMethod("convertToRecipe", DocumentSnapshot::class.java)
    method.isAccessible = true

    // Act
    val recipe = method.invoke(recipeRepository, mockDocument) as? Recipe

    // Assert
    assertEquals(Quantity(0.0, FoodUnit.GRAM), recipe?.ingredients?.first()?.quantity)
  }
}
