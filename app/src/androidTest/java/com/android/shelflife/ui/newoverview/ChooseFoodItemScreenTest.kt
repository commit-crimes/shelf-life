package com.android.shelfLife.ui.newoverview

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.shelfLife.R
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodFactsRepository
import com.android.shelfLife.model.foodFacts.FoodFactsViewModel
import com.android.shelfLife.model.foodFacts.FoodSearchInput
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.NutritionFacts
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodFacts.SearchStatus
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Screen
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChooseFoodItemTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navigationActions: NavigationActions
    private lateinit var foodFactsViewModel: FoodFactsViewModel
    private lateinit var fakeRepository: FakeFoodFactsRepository

    private val sampleFoodFacts = listOf(
        FoodFacts(
            "Sample Food 1",
            "1234567890",
            Quantity(1.0, FoodUnit.COUNT),
            FoodCategory.OTHER,
            NutritionFacts(),
            DEFAULT_IMAGE_URL
        ),
        FoodFacts(
            "Sample Food 2",
            "1234567891",
            Quantity(2.0, FoodUnit.COUNT),
            FoodCategory.OTHER,
            NutritionFacts(),
            DEFAULT_IMAGE_URL
        ),
        FoodFacts(
            "Sample Food 3",
            "1234567892",
            Quantity(3.0, FoodUnit.COUNT),
            FoodCategory.OTHER,
            NutritionFacts(),
            DEFAULT_IMAGE_URL
        ),
        FoodFacts(
            "Sample Food 4",
            "1234567893",
            Quantity(4.0, FoodUnit.COUNT),
            FoodCategory.OTHER,
            NutritionFacts(),
            DEFAULT_IMAGE_URL
        ),
        FoodFacts(
            "Sample Food 5",
            "1234567894",
            Quantity(5.0, FoodUnit.COUNT),
            FoodCategory.OTHER,
            NutritionFacts(),
            DEFAULT_IMAGE_URL
        ),
        FoodFacts(
            "Sample Food 6",
            "1234567894",
            Quantity(5.0, FoodUnit.COUNT),
            FoodCategory.OTHER,
            NutritionFacts(),
            DEFAULT_IMAGE_URL
        ),
        FoodFacts(
            "Sample Food 7",
            "1234567894",
            Quantity(5.0, FoodUnit.COUNT),
            FoodCategory.OTHER,
            NutritionFacts(),
            DEFAULT_IMAGE_URL
        )

    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        // Initialize mocks
        navigationActions = mockk()
        fakeRepository = FakeFoodFactsRepository().apply {
            foodFactsList = sampleFoodFacts
            searchStatus = SearchStatus.Success // Ensure it's always success
        }
        foodFactsViewModel = FoodFactsViewModel(fakeRepository)

        // Mock navigation and ViewModel behaviors
        every { navigationActions.goBack() } just runs
        every { navigationActions.navigateTo(Screen.ADD_FOOD) } just runs
        every { navigationActions.navigateTo(Screen.EDIT_FOOD) } just runs
    }

    @Test
    fun testInitialUIComponentsDisplayed() {
        composeTestRule.setContent {
            ChooseFoodItem(
                navigationActions = navigationActions,
                foodFactsViewModel = foodFactsViewModel
            )
        }

        // Verify that the title, labels, and buttons are displayed
        composeTestRule.onNodeWithTag("chooseFoodItemTitle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("selectImage").assertIsDisplayed()
        composeTestRule.onNodeWithTag("cancelButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("foodSave").assertIsDisplayed()
        composeTestRule.onNodeWithTag("manualEntryButton").assertIsDisplayed()
    }



    @Test
    fun testSelectNoImageOption() {
        composeTestRule.setContent {
            ChooseFoodItem(
                navigationActions = navigationActions,
                foodFactsViewModel = foodFactsViewModel
            )
        }

        // Select the "No Image" option

        // Verify the default image is displayed
        composeTestRule.onNodeWithTag("defaultImage").assertIsDisplayed()
        composeTestRule.onNodeWithTag("defaultImageText").assertIsDisplayed()
    }

    @Test
    fun testCancelButtonNavigatesBack() {
        composeTestRule.setContent {
            ChooseFoodItem(
                navigationActions = navigationActions,
                foodFactsViewModel = foodFactsViewModel
            )
        }

        // Click the cancel button
        composeTestRule.onNodeWithTag("cancelButton").performClick()

        // Verify navigation back action
        verify { navigationActions.goBack() }
    }

    @Test
    fun testSubmitButtonNavigatesToEditFood() {
        composeTestRule.setContent {
            ChooseFoodItem(
                navigationActions = navigationActions,
                foodFactsViewModel = foodFactsViewModel
            )
        }

        // Select a food image and click the submit button
        composeTestRule.onNodeWithTag("foodSave").performClick()

        // Verify navigation to the edit food screen
        verify { navigationActions.navigateTo(Screen.EDIT_FOOD) }
    }

    @Test
    fun testManualEntryButtonNavigatesToAddFood() {
        composeTestRule.setContent {
            ChooseFoodItem(
                navigationActions = navigationActions,
                foodFactsViewModel = foodFactsViewModel
            )
        }

        // Click the manual entry button
        composeTestRule.onNodeWithTag("manualEntryButton").performClick()

        // Verify navigation to the add food screen
        verify { navigationActions.navigateTo(Screen.ADD_FOOD) }
    }


    inner class FakeFoodFactsRepository : FoodFactsRepository {
        var shouldReturnError = false
        var foodFactsList = listOf<FoodFacts>()
        var searchStatus =  SearchStatus.Success

        override fun searchFoodFacts(
            searchInput: FoodSearchInput,
            onSuccess: (List<FoodFacts>) -> Unit,
            onFailure: (Exception) -> Unit
        ) {
            if (shouldReturnError) {
                onFailure(Exception("Test exception"))
            } else {
                onSuccess(foodFactsList)
            }
        }
    }


    companion object {
        const val DEFAULT_IMAGE_URL =
            "https://media.istockphoto.com/id/1354776457/vector/default-image-icon-vector-missing-picture-page-for-website-design-or-mobile-app-no-photo.jpg?s=612x612&w=0&k=20&c=w3OW0wX3LyiFRuDHo9A32Q0IUMtD4yjXEvQlqyYk9O4="
    }
}
