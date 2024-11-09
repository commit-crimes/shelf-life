package com.android.shelfLife.ui.overview

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.foodItem.FoodStatus
import com.android.shelfLife.model.foodItem.FoodStorageLocation
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.*
import java.util.*

@RunWith(AndroidJUnit4::class)
class IndividualFoodItemScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navigationActions: NavigationActions
    private lateinit var houseHoldRepository: HouseHoldRepository
    private lateinit var listFoodItemsViewModel: ListFoodItemsViewModel
    private lateinit var foodItem: FoodItem
    private lateinit var houseHold: HouseHold

    @Before
    fun setUp() {
        navigationActions = mock()
        houseHoldRepository = mock()
        val foodItemRepository = mock<FoodItemRepository>()
        listFoodItemsViewModel = ListFoodItemsViewModel(foodItemRepository)

        // Create a sample FoodItem
        val foodFacts = FoodFacts(
            name = "Apple",
            barcode = "123456789",
            quantity = Quantity(5.0, FoodUnit.COUNT),
            category = FoodCategory.FRUIT
        )
        foodItem = FoodItem(
            uid = "foodItem1",
            foodFacts = foodFacts,
            expiryDate = Timestamp(Date(System.currentTimeMillis() + 86400000)),
            openDate = Timestamp(Date(System.currentTimeMillis() - 86400000)),
            buyDate = Timestamp(Date(System.currentTimeMillis() - 172800000)),
            location = FoodStorageLocation.FREEZER,
            status = FoodStatus.OPEN
        )

        // Create a sample HouseHold with the food item
        houseHold = HouseHold(
            uid = "1",
            name = "Test Household",
            members = listOf("John", "Doe"),
            foodItems = listOf(foodItem)
        )
    }

    @Test
    fun individualFoodItemScreenDisplaysCorrectly() = runTest {
        val householdViewModel = spy(HouseholdViewModel(houseHoldRepository, listFoodItemsViewModel))

        householdViewModel.selectHousehold(houseHold)
        householdViewModel.finishedLoading.value = true

        val foodItemFlow: StateFlow<FoodItem?> = MutableStateFlow(foodItem)
        doReturn(foodItemFlow).whenever(householdViewModel).getFoodItemById("foodItem1")

        composeTestRule.setContent {
            IndividualFoodItemScreen(
                foodItemId = "foodItem1",
                navigationActions = navigationActions,
                householdViewModel = householdViewModel
            )
        }

        composeTestRule.onNodeWithTag("IndividualFoodItemScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("IndividualFoodItemName").assertTextEquals("Apple")
        composeTestRule.onNodeWithTag("IndividualFoodItemImage").assertIsDisplayed()
    }

    @Test
    fun individualFoodItemScreenShowsLoadingIndicatorWhenFoodItemIsNull() = runTest {
        val householdViewModel = spy(HouseholdViewModel(houseHoldRepository, listFoodItemsViewModel))

        householdViewModel.selectHousehold(houseHold)
        householdViewModel.finishedLoading.value = true

        val foodItemFlow: StateFlow<FoodItem?> = MutableStateFlow(null)
        doReturn(foodItemFlow).whenever(householdViewModel).getFoodItemById("foodItem2")

        composeTestRule.setContent {
            IndividualFoodItemScreen(
                foodItemId = "foodItem2",
                navigationActions = navigationActions,
                householdViewModel = householdViewModel
            )
        }

        composeTestRule.onNodeWithTag("CircularProgressIndicator").assertIsDisplayed()
    }




}

