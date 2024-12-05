package com.android.shelfLife.viewmodel

import com.android.shelfLife.model.foodFacts.*
import com.android.shelfLife.model.newFoodItem.*
import com.android.shelfLife.model.user.User
import com.android.shelfLife.model.user.UserRepository
import com.google.firebase.Timestamp
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class FoodItemViewModelTest {

  @Mock private lateinit var mockFoodItemRepository: FoodItemRepository

  @Mock private lateinit var mockUserRepository: UserRepository

  private lateinit var foodItemViewModel: FoodItemViewModel

  private val testDispatcher = UnconfinedTestDispatcher()

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Mock default values
    `when`(mockFoodItemRepository.selectedFoodItem).thenReturn(MutableStateFlow(null))
    `when`(mockUserRepository.user).thenReturn(MutableStateFlow(null))

    // Initialize the ViewModel with mocks
    foodItemViewModel = FoodItemViewModel(mockFoodItemRepository, mockUserRepository)

    // Set the Dispatchers to use the TestDispatcher
    Dispatchers.setMain(testDispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `init initializes fields when selectedFood is null`() {
    `when`(mockFoodItemRepository.selectedFoodItem).thenReturn(MutableStateFlow(null))

    foodItemViewModel = FoodItemViewModel(mockFoodItemRepository, mockUserRepository)

    assertFalse(foodItemViewModel.isSelected)
    assertEquals("", foodItemViewModel.foodName)
    assertEquals("", foodItemViewModel.amount)
    assertEquals(FoodUnit.GRAM, foodItemViewModel.unit)
    assertEquals(FoodCategory.OTHER, foodItemViewModel.category)
    assertEquals(FoodStorageLocation.PANTRY, foodItemViewModel.location)
    assertEquals("", foodItemViewModel.expireDate)
    assertEquals("", foodItemViewModel.openDate)
    assertNotNull(foodItemViewModel.buyDate)
    assertNull(foodItemViewModel.selectedImage)
  }

  @Test
  fun `init initializes fields when selectedFood is not null`() {
    val selectedFoodItem =
        FoodItem(
            uid = "item1",
            foodFacts =
                FoodFacts(
                    name = "Apple",
                    barcode = "123456789",
                    quantity = Quantity(100.0, FoodUnit.GRAM),
                    category = FoodCategory.FRUIT,
                    nutritionFacts = NutritionFacts(),
                    imageUrl = ""),
            location = FoodStorageLocation.FRIDGE,
            expiryDate = Timestamp.now(),
            openDate = null,
            buyDate = Timestamp.now(),
            status = FoodStatus.UNOPENED,
            owner = "user1")
    `when`(mockFoodItemRepository.selectedFoodItem).thenReturn(MutableStateFlow(selectedFoodItem))

    foodItemViewModel = FoodItemViewModel(mockFoodItemRepository, mockUserRepository)

    assertTrue(foodItemViewModel.isSelected)
    assertEquals("Apple", foodItemViewModel.foodName)
    assertEquals("100.0", foodItemViewModel.amount)
    assertEquals(FoodUnit.GRAM, foodItemViewModel.unit)
    assertEquals(FoodCategory.FRUIT, foodItemViewModel.category)
    assertEquals(FoodStorageLocation.FRIDGE, foodItemViewModel.location)
    assertNotNull(foodItemViewModel.expireDate)
    assertEquals("", foodItemViewModel.openDate)
    assertNotNull(foodItemViewModel.buyDate)
  }

  @Test
  fun `validateAllFieldsWhenSubmitButton sets error messages for invalid fields`() {
    foodItemViewModel.foodName = ""
    foodItemViewModel.amount = "abc"
    foodItemViewModel.buyDate = "99999999" // Invalid numeric date
    foodItemViewModel.expireDate = "99999999"
    foodItemViewModel.openDate = "99999999"

    foodItemViewModel.validateAllFieldsWhenSubmitButton()

    assertNotNull(foodItemViewModel.foodNameErrorResId)
    assertNotNull(foodItemViewModel.amountErrorResId)
    assertNotNull(foodItemViewModel.buyDateErrorResId)
    assertNotNull(foodItemViewModel.expireDateErrorResId)
    assertNotNull(foodItemViewModel.openDateErrorResId)
  }

  @Test
  fun `validateAllFieldsWhenSubmitButton does not set error messages for valid fields`() {
    foodItemViewModel.foodName = "Apple"
    foodItemViewModel.amount = "100"
    // Valid dates in ddMMyyyy format
    foodItemViewModel.buyDate = "01012023"
    foodItemViewModel.expireDate = "02012025"
    foodItemViewModel.openDate = "01012023"

    foodItemViewModel.validateAllFieldsWhenSubmitButton()

    assertNull(foodItemViewModel.foodNameErrorResId)
    assertNull(foodItemViewModel.amountErrorResId)
    assertNull(foodItemViewModel.buyDateErrorResId)
    assertNull(foodItemViewModel.expireDateErrorResId)
    assertNull(foodItemViewModel.openDateErrorResId)
  }

  @Test
  fun `changeFoodName updates foodName and validates it`() {
    foodItemViewModel.changeFoodName("")
    assertEquals("", foodItemViewModel.foodName)
    assertNotNull(foodItemViewModel.foodNameErrorResId)

    foodItemViewModel.changeFoodName("Banana")
    assertEquals("Banana", foodItemViewModel.foodName)
    assertNull(foodItemViewModel.foodNameErrorResId)
  }

  @Test
  fun `changeAmount updates amount and validates it`() {
    foodItemViewModel.changeAmount("abc")
    assertEquals("abc", foodItemViewModel.amount)
    assertNotNull(foodItemViewModel.amountErrorResId)

    foodItemViewModel.changeAmount("200")
    assertEquals("200", foodItemViewModel.amount)
    assertNull(foodItemViewModel.amountErrorResId)
  }

  @Test
  fun `changeExpiryDate updates expireDate and validates it`() {
    foodItemViewModel.buyDate = "01012023"
    foodItemViewModel.buyDateErrorResId = null

    // Input non-digit characters: "invalid date" => filtered to ""
    foodItemViewModel.changeExpiryDate("invalid date")
    // After filtering, no digits remain, so expireDate = ""
    assertEquals("", foodItemViewModel.expireDate)
    assertNotNull(foodItemViewModel.expireDateErrorResId)

    // Valid date
    foodItemViewModel.changeExpiryDate("02012025")
    assertEquals("02012025", foodItemViewModel.expireDate)
    assertNull(foodItemViewModel.expireDateErrorResId)
  }

  @Test
  fun `changeOpenDate updates openDate and validates it`() {
    foodItemViewModel.buyDate = "01012023"
    foodItemViewModel.buyDateErrorResId = null
    foodItemViewModel.expireDate = "03012023"
    foodItemViewModel.expireDateErrorResId = null

    // "11432900" is all digits but likely invalid as a date
    foodItemViewModel.changeOpenDate("11432900")
    // It remains "11432900" after filtering, but invalid format triggers error
    assertEquals("11432900", foodItemViewModel.openDate)
    assertNotNull(foodItemViewModel.openDateErrorResId)

    // Valid open date
    foodItemViewModel.changeOpenDate("02012023")
    assertEquals("02012023", foodItemViewModel.openDate)
    assertNull(foodItemViewModel.openDateErrorResId)
  }

  @Test
  fun `changeBuyDate updates buyDate and re-validates dependent fields`() {
    foodItemViewModel.expireDate = "03012025"
    foodItemViewModel.openDate = "02012023"

    // Invalid numeric date
    foodItemViewModel.changeBuyDate("99999999")
    assertEquals("99999999", foodItemViewModel.buyDate)
    assertNotNull(foodItemViewModel.buyDateErrorResId)

    // Valid buy date
    foodItemViewModel.changeBuyDate("01012023")
    assertEquals("01012023", foodItemViewModel.buyDate)
    assertNull(foodItemViewModel.buyDateErrorResId)
    assertNull(foodItemViewModel.expireDateErrorResId)
    assertNull(foodItemViewModel.openDateErrorResId)
  }

  @Test
  fun `submitFoodItem returns true and adds item when fields are valid and isSelected is false`() =
      runTest {
        foodItemViewModel.isSelected = false
        foodItemViewModel.foodName = "Apple"
        foodItemViewModel.amount = "100"
        foodItemViewModel.unit = FoodUnit.GRAM
        foodItemViewModel.category = FoodCategory.FRUIT
        foodItemViewModel.location = FoodStorageLocation.FRIDGE
        foodItemViewModel.buyDate = "01012023"
        foodItemViewModel.expireDate = "02012025"
        foodItemViewModel.openDate = ""

        val user =
            User(
                uid = "user1",
                username = "User1",
                email = "user1@example.com",
                selectedHouseholdUID = "household1")
        `when`(mockUserRepository.user).thenReturn(MutableStateFlow(user))
        `when`(mockFoodItemRepository.getNewUid()).thenReturn("newItemId")

        val result = foodItemViewModel.submitFoodItem()
        assertTrue(result)
        verify(mockFoodItemRepository).addFoodItem(eq("household1"), any())
      }

  @Test
  fun `submitFoodItem returns false when fields are invalid`() = runTest {
    foodItemViewModel.isSelected = false
    foodItemViewModel.foodName = ""
    foodItemViewModel.amount = "abc"
    foodItemViewModel.buyDate = "99999999"
    foodItemViewModel.expireDate = "99999999"
    foodItemViewModel.openDate = "99999999"

    val result = foodItemViewModel.submitFoodItem()
    assertFalse(result)
    verify(mockFoodItemRepository, never()).addFoodItem(any(), any())
  }

  @Test
  fun `submitFoodItem edits item when isSelected is true`() = runTest {
    foodItemViewModel.isSelected = true
    val selectedFoodItem =
        FoodItem(
            uid = "item1",
            foodFacts =
                FoodFacts(
                    name = "OldName",
                    barcode = "123456789",
                    quantity = Quantity(100.0, FoodUnit.GRAM),
                    category = FoodCategory.OTHER,
                    nutritionFacts = NutritionFacts(),
                    imageUrl = ""),
            location = FoodStorageLocation.PANTRY,
            expiryDate = null,
            openDate = null,
            buyDate = null,
            status = FoodStatus.UNOPENED,
            owner = "user1")
    foodItemViewModel.selectedFood = selectedFoodItem
    foodItemViewModel.foodName = "NewName"
    foodItemViewModel.amount = "200"
    foodItemViewModel.unit = FoodUnit.GRAM
    foodItemViewModel.category = FoodCategory.FRUIT
    foodItemViewModel.location = FoodStorageLocation.FRIDGE
    foodItemViewModel.buyDate = "01012023"
    foodItemViewModel.expireDate = "02012025"
    foodItemViewModel.openDate = "01012023"

    val user =
        User(
            uid = "user1",
            username = "User1",
            email = "user1@example.com",
            selectedHouseholdUID = "household1")
    `when`(mockUserRepository.user).thenReturn(MutableStateFlow(user))

    val result = foodItemViewModel.submitFoodItem()
    assertTrue(result)
    verify(mockFoodItemRepository).updateFoodItem(eq("household1"), any())
  }

  @Test
  fun `addFoodItem does nothing when householdId is null`() = runTest {
    val user = User(uid = "user1", username = "User1", email = "user1@example.com")
    `when`(mockUserRepository.user).thenReturn(MutableStateFlow(user))

    val foodItem = mock(FoodItem::class.java)
    foodItemViewModel.addFoodItem(foodItem)
    verify(mockFoodItemRepository, never()).addFoodItem(any(), any())
  }

  @Test
  fun `editFoodItem does nothing when householdId is null`() = runTest {
    val user = User(uid = "user1", username = "User1", email = "user1@example.com")
    `when`(mockUserRepository.user).thenReturn(MutableStateFlow(user))

    val foodItem = mock(FoodItem::class.java)
    foodItemViewModel.editFoodItem(foodItem)
    verify(mockFoodItemRepository, never()).updateFoodItem(any(), any())
  }

  @Test
  fun `deleteFoodItem deletes selected food item when householdId is not null`() = runTest {
    val selectedFoodItem =
        FoodItem(
            uid = "item1",
            foodFacts = FoodFacts("apple", quantity = Quantity(100.0, FoodUnit.GRAM)),
            location = FoodStorageLocation.PANTRY,
            expiryDate = null,
            openDate = null,
            buyDate = null,
            status = FoodStatus.UNOPENED,
            owner = "user1")
    foodItemViewModel.selectedFood = selectedFoodItem

    val user =
        User(
            uid = "user1",
            username = "User1",
            email = "user1@example.com",
            selectedHouseholdUID = "household1")
    `when`(mockUserRepository.user).thenReturn(MutableStateFlow(user))

    foodItemViewModel.deleteFoodItem()

    verify(mockFoodItemRepository).deleteFoodItem("household1", "item1")
    verify(mockFoodItemRepository).selectFoodItem(null)
  }

  @Test
  fun `deleteFoodItem does nothing when selectedFood is null`() = runTest {
    foodItemViewModel.selectedFood = null

    foodItemViewModel.deleteFoodItem()

    verify(mockFoodItemRepository, never()).deleteFoodItem(any(), any())
    verify(mockFoodItemRepository, never()).selectFoodItem(any())
  }

  @Test
  fun `reset clears all fields and resets state`() {
    foodItemViewModel.foodName = "Apple"
    foodItemViewModel.amount = "100"
    foodItemViewModel.unit = FoodUnit.GRAM
    foodItemViewModel.category = FoodCategory.FRUIT
    foodItemViewModel.location = FoodStorageLocation.FRIDGE
    foodItemViewModel.expireDate = "02012023"
    foodItemViewModel.openDate = "01012023"
    foodItemViewModel.buyDate = "01012023"
    foodItemViewModel.isSelected = true

    foodItemViewModel.reset()

    assertEquals("", foodItemViewModel.foodName)
    assertEquals("", foodItemViewModel.amount)
    assertEquals(FoodUnit.GRAM, foodItemViewModel.unit)
    assertEquals(FoodCategory.OTHER, foodItemViewModel.category)
    assertEquals(FoodStorageLocation.PANTRY, foodItemViewModel.location)
    assertEquals("", foodItemViewModel.expireDate)
    assertEquals("", foodItemViewModel.openDate)
    assertNotNull(foodItemViewModel.buyDate)
    assertFalse(foodItemViewModel.isSelected)
  }
}
