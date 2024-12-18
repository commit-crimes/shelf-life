package com.android.shelfLife.ui.recipe;

import com.android.shelfLife.HiltTestActivity;
import com.android.shelfLife.model.recipe.RecipeRepository;
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.recipe.RecipeGeneratorRepository
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.recipes.generateRecipe.RecipeInputStep
import com.android.shelfLife.viewmodel.overview.OverviewScreenViewModel
import com.android.shelfLife.viewmodel.recipes.RecipeGenerationViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import javax.inject.Inject
import org.junit.Rule
import dagger.hilt.android.testing.HiltAndroidTest;
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@HiltAndroidTest
class RecipeGenerationTest {

    @get:Rule(order = 0) val hiltAndroidTestRule = HiltAndroidRule(this)
    @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    private lateinit var navigationActions: NavigationActions

    @Inject lateinit var recipeRepository: RecipeRepository
    @Inject lateinit var recipeGeneratorRepository: RecipeGeneratorRepository
    @Inject lateinit var userRepository: UserRepository
    @Inject lateinit var foodItemRepository: FoodItemRepository

    private lateinit var recipeGenerationViewModel: RecipeGenerationViewModel

    private lateinit var instrumentationContext: android.content.Context


    @Before
    fun setUp() {
        hiltAndroidTestRule.inject()
        navigationActions = mock()
        instrumentationContext = InstrumentationRegistry.getInstrumentation().context
        whenever(navigationActions.currentRoute()).thenReturn(Route.RECIPES)

        recipeGenerationViewModel = RecipeGenerationViewModel(recipeRepository, recipeGeneratorRepository, foodItemRepository, userRepository)
    }

    @Test
    fun recipeInputStep_renderedCorrectly() {
        composeTestRule.setContent {
            RecipeInputStep(viewModel = recipeGenerationViewModel, onNext = {})
        }

        // Check for the presence of input fields and buttons
        composeTestRule.onNodeWithText("Recipe Name").assertExists()
        composeTestRule.onNodeWithText("Recipe Type").assertExists()
        composeTestRule.onNodeWithText("Servings").assertExists()
        composeTestRule.onNodeWithTag("recipeSubmitButton").assertExists()
        composeTestRule.onNodeWithTag("cancelButton").assertExists()
    }

}
