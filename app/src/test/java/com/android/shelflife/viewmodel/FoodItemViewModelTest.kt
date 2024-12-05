// FoodItemViewModelTest.kt

package com.android.shelfLife.viewmodel

import com.android.shelfLife.model.foodFacts.*
import com.android.shelfLife.model.newFoodItem.*
import com.android.shelfLife.model.user.User
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.utils.*
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
import org.mockito.ArgumentMatchers.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.anyOrNull

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
        // Given that selectedFoodItem is null
        `when`(mockFoodItemRepository.selectedFoodItem).thenReturn(MutableStateFlow(null))

        // Re-initialize the ViewModel to trigger init block
        foodItemViewModel = FoodItemViewModel(mockFoodItemRepository, mockUserRepository)

        // Then default values are set
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
        // Given a selected food item
        val selectedFoodItem = FoodItem(
            uid = "item1",
            foodFacts = FoodFacts(
                name = "Apple",
                barcode = "123456789",
                quantity = Quantity(100.0, FoodUnit.GRAM),
                category = FoodCategory.FRUIT,
                nutritionFacts = NutritionFacts(),
                imageUrl = ""
            ),
            location = FoodStorageLocation.FRIDGE,
            expiryDate = Timestamp.now(),
            openDate = null,
            buyDate = Timestamp.now(),
            status = FoodStatus.UNOPENED,
            owner = "user1"
        )
        `when`(mockFoodItemRepository.selectedFoodItem).thenReturn(MutableStateFlow(selectedFoodItem))

        // Re-initialize the ViewModel to trigger init block
        foodItemViewModel = FoodItemViewModel(mockFoodItemRepository, mockUserRepository)

        // Then fields are initialized with selectedFoodItem values
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
        // Given invalid inputs
        foodItemViewModel.foodName = ""
        foodItemViewModel.amount = "abc"
        foodItemViewModel.buyDate = "invalid date"
        foodItemViewModel.expireDate = "invalid date"
        foodItemViewModel.openDate = "invalid date"

        // When validation is triggered
        foodItemViewModel.validateAllFieldsWhenSubmitButton()

        // Then error messages are set
        assertNotNull(foodItemViewModel.foodNameErrorResId)
        assertNotNull(foodItemViewModel.amountErrorResId)
        assertNotNull(foodItemViewModel.buyDateErrorResId)
        assertNotNull(foodItemViewModel.expireDateErrorResId)
        assertNotNull(foodItemViewModel.openDateErrorResId)
    }

    @Test
    fun `validateAllFieldsWhenSubmitButton does not set error messages for valid fields`() {
        // Given valid inputs
        foodItemViewModel.foodName = "Apple"
        foodItemViewModel.amount = "100"
        foodItemViewModel.buyDate = "01012023"
        foodItemViewModel.expireDate = "02012023"
        foodItemViewModel.openDate = "01012023"

        // When validation is triggered
        foodItemViewModel.validateAllFieldsWhenSubmitButton()

        // Then error messages are null
        assertNull(foodItemViewModel.foodNameErrorResId)
        assertNull(foodItemViewModel.amountErrorResId)
        assertNull(foodItemViewModel.buyDateErrorResId)
        assertNull(foodItemViewModel.expireDateErrorResId)
        assertNull(foodItemViewModel.openDateErrorResId)
    }

    @Test
    fun `changeFoodName updates foodName and validates it`() {
        // When changing the food name to an invalid value
        foodItemViewModel.changeFoodName("")

        // Then foodName is updated and error message is set
        assertEquals("", foodItemViewModel.foodName)
        assertNotNull(foodItemViewModel.foodNameErrorResId)

        // When changing to a valid name
        foodItemViewModel.changeFoodName("Banana")

        // Then error message is cleared
        assertEquals("Banana", foodItemViewModel.foodName)
        assertNull(foodItemViewModel.foodNameErrorResId)
    }

    @Test
    fun `changeAmount updates amount and validates it`() {
        // When changing the amount to an invalid value
        foodItemViewModel.changeAmount("abc")

        // Then amount is updated and error message is set
        assertEquals("abc", foodItemViewModel.amount)
        assertNotNull(foodItemViewModel.amountErrorResId)

        // When changing to a valid amount
        foodItemViewModel.changeAmount("200")

        // Then error message is cleared
        assertEquals("200", foodItemViewModel.amount)
        assertNull(foodItemViewModel.amountErrorResId)
    }

    @Test
    fun `changeExpiryDate updates expireDate and validates it`() {
        // Given buyDate is valid
        foodItemViewModel.buyDate = "01012023"
        foodItemViewModel.buyDateErrorResId = null

        // When changing expireDate to an invalid value
        foodItemViewModel.changeExpiryDate("invalid date")

        // Then expireDate is updated and error message is set
        assertEquals("invaliddate", foodItemViewModel.expireDate)
        assertNotNull(foodItemViewModel.expireDateErrorResId)

        // When changing to a valid date
        foodItemViewModel.changeExpiryDate("02012023")

        // Then error message is cleared
        assertEquals("02012023", foodItemViewModel.expireDate)
        assertNull(foodItemViewModel.expireDateErrorResId)
    }

    @Test
    fun `changeOpenDate updates openDate and validates it`() {
        // Given buyDate and expireDate are valid
        foodItemViewModel.buyDate = "01012023"
        foodItemViewModel.buyDateErrorResId = null
        foodItemViewModel.expireDate = "03012023"
        foodItemViewModel.expireDateErrorResId = null

        // When changing openDate to an invalid value
        foodItemViewModel.changeOpenDate("invalid date")

        // Then openDate is updated and error message is set
        assertEquals("invaliddate", foodItemViewModel.openDate)
        assertNotNull(foodItemViewModel.openDateErrorResId)

        // When changing to a valid date
        foodItemViewModel.changeOpenDate("02012023")

        // Then error message is cleared
        assertEquals("02012023", foodItemViewModel.openDate)
        assertNull(foodItemViewModel.openDateErrorResId)
    }

    @Test
    fun `changeBuyDate updates buyDate and re-validates dependent fields`() {
        // Given expireDate and openDate depend on buyDate
        foodItemViewModel.expireDate = "03012023"
        foodItemViewModel.openDate = "02012023"

        // When changing buyDate to an invalid value
        foodItemViewModel.changeBuyDate("invalid date")

        // Then buyDateErrorResId is set and dependent fields are re-validated
        assertEquals("invaliddate", foodItemViewModel.buyDate)
        assertNotNull(foodItemViewModel.buyDateErrorResId)
        assertNotNull(foodItemViewModel.expireDateErrorResId)
        assertNotNull(foodItemViewModel.openDateErrorResId)

        // When changing buyDate to a valid value
        foodItemViewModel.changeBuyDate("01012023")

        // Then error messages are cleared
        assertEquals("01012023", foodItemViewModel.buyDate)
        assertNull(foodItemViewModel.buyDateErrorResId)
        assertNull(foodItemViewModel.expireDateErrorResId)
        assertNull(foodItemViewModel.openDateErrorResId)
    }

    @Test
    fun `submitFoodItem returns true and adds item when fields are valid and isSelected is false`() = runTest {
        // Given valid inputs and isSelected is false
        foodItemViewModel.isSelected = false
        foodItemViewModel.foodName = "Apple"
        foodItemViewModel.amount = "100"
        foodItemViewModel.unit = FoodUnit.GRAM
        foodItemViewModel.category = FoodCategory.FRUIT
        foodItemViewModel.location = FoodStorageLocation.FRIDGE
        foodItemViewModel.buyDate = "01012023"
        foodItemViewModel.expireDate = "02012023"
        foodItemViewModel.openDate = ""

        // Mock user repository
        val user = User(uid = "user1", username = "User1", email = "user1@example.com", selectedHouseholdUID = "household1")
        `when`(mockUserRepository.user).thenReturn(MutableStateFlow(user))

        // Mock new UID generation
        `when`(mockFoodItemRepository.getNewUid()).thenReturn("newItemId")

        // When submitFoodItem is called
        val result = foodItemViewModel.submitFoodItem()

        // Then result is true and addFoodItem is called
        assertTrue(result)
        verify(mockFoodItemRepository).addFoodItem(eq("household1"), any())
        // Optionally, you can capture the FoodItem and assert its properties
    }

    @Test
    fun `submitFoodItem returns false when fields are invalid`() = runTest {
        // Given invalid inputs
        foodItemViewModel.isSelected = false
        foodItemViewModel.foodName = ""
        foodItemViewModel.amount = "abc"
        foodItemViewModel.buyDate = "invalid date"
        foodItemViewModel.expireDate = "invalid date"
        foodItemViewModel.openDate = "invalid date"

        // When submitFoodItem is called
        val result = foodItemViewModel.submitFoodItem()

        // Then result is false and addFoodItem is not called
        assertFalse(result)
        verify(mockFoodItemRepository, never()).addFoodItem(any(), any())
    }

    @Test
    fun `submitFoodItem edits item when isSelected is true`() = runTest {
        // Given valid inputs and isSelected is true
        foodItemViewModel.isSelected = true
        val selectedFoodItem = FoodItem(
            uid = "item1",
            foodFacts = FoodFacts(
                name = "OldName",
                barcode = "123456789",
                quantity = Quantity(100.0, FoodUnit.GRAM),
                category = FoodCategory.OTHER,
                nutritionFacts = NutritionFacts(),
                imageUrl = ""
            ),
            location = FoodStorageLocation.PANTRY,
            expiryDate = null,
            openDate = null,
            buyDate = null,
            status = FoodStatus.UNOPENED,
            owner = "user1"
        )
        foodItemViewModel.selectedFood = selectedFoodItem
        foodItemViewModel.foodName = "NewName"
        foodItemViewModel.amount = "200"
        foodItemViewModel.unit = FoodUnit.GRAM
        foodItemViewModel.category = FoodCategory.FRUIT
        foodItemViewModel.location = FoodStorageLocation.FRIDGE
        foodItemViewModel.buyDate = "01012023"
        foodItemViewModel.expireDate = "02012023"
        foodItemViewModel.openDate = "01012023"

        // Mock user repository
        val user = User(uid = "user1", username = "User1", email = "user1@example.com", selectedHouseholdUID = "household1")
        `when`(mockUserRepository.user).thenReturn(MutableStateFlow(user))

        // When submitFoodItem is called
        val result = foodItemViewModel.submitFoodItem()

        // Then result is true and editFoodItem is called
        assertTrue(result)
        verify(mockFoodItemRepository).updateFoodItem(eq("household1"), any())
        // Optionally, capture the FoodItem and assert changes
    }

    @Test
    fun `addFoodItem does nothing when householdId is null`() = runTest {
        // Given user has no selected household
        val user = User(uid = "user1", username = "User1", email = "user1@example.com")
        `when`(mockUserRepository.user).thenReturn(MutableStateFlow(user))

        // When addFoodItem is called
        val foodItem = mock(FoodItem::class.java)
        foodItemViewModel.addFoodItem(foodItem)

        // Then repository method is not called
        verify(mockFoodItemRepository, never()).addFoodItem(any(), any())
    }

    @Test
    fun `editFoodItem does nothing when householdId is null`() = runTest {
        // Given user has no selected household
        val user = User(uid = "user1", username = "User1", email = "user1@example.com")
        `when`(mockUserRepository.user).thenReturn(MutableStateFlow(user))

        // When editFoodItem is called
        val foodItem = mock(FoodItem::class.java)
        foodItemViewModel.editFoodItem(foodItem)

        // Then repository method is not called
        verify(mockFoodItemRepository, never()).updateFoodItem(any(), any())
    }

    @Test
    fun `deleteFoodItem deletes selected food item when householdId is not null`() = runTest {
        // Given a selected food item and user with household
        val selectedFoodItem = FoodItem(
            uid = "item1",
            foodFacts = FoodFacts("apple", quantity =  Quantity(100.0, FoodUnit.GRAM)),
            location = FoodStorageLocation.PANTRY,
            expiryDate = null,
            openDate = null,
            buyDate = null,
            status = FoodStatus.UNOPENED,
            owner = "user1"
        )
        foodItemViewModel.selectedFood = selectedFoodItem

        val user = User(uid = "user1", username = "User1", email = "user1@example.com", selectedHouseholdUID = "household1")
        `when`(mockUserRepository.user).thenReturn(MutableStateFlow(user))

        // When deleteFoodItem is called
        foodItemViewModel.deleteFoodItem()

        // Then deleteFoodItem is called on repository
        verify(mockFoodItemRepository).deleteFoodItem("household1", "item1")
        verify(mockFoodItemRepository).selectFoodItem(null)
    }

    @Test
    fun `deleteFoodItem does nothing when selectedFood is null`() = runTest {
        // Given no selected food item
        foodItemViewModel.selectedFood = null

        // When deleteFoodItem is called
        foodItemViewModel.deleteFoodItem()

        // Then repository methods are not called
        verify(mockFoodItemRepository, never()).deleteFoodItem(any(), any())
        verify(mockFoodItemRepository, never()).selectFoodItem(anyOrNull())
    }

    @Test
    fun `reset clears all fields and resets state`() {
        // Given fields have values
        foodItemViewModel.foodName = "Apple"
        foodItemViewModel.amount = "100"
        foodItemViewModel.unit = FoodUnit.GRAM
        foodItemViewModel.category = FoodCategory.FRUIT
        foodItemViewModel.location = FoodStorageLocation.FRIDGE
        foodItemViewModel.expireDate = "02012023"
        foodItemViewModel.openDate = "01012023"
        foodItemViewModel.buyDate = "01012023"
        foodItemViewModel.isSelected = true

        // When reset is called
        foodItemViewModel.reset()

        // Then fields are cleared and state is reset
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
