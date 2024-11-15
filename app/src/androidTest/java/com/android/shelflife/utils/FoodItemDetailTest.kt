import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.shelfLife.model.foodFacts.*
import com.android.shelfLife.model.foodItem.*
import com.android.shelfLife.ui.utils.FoodItemDetails
import com.android.shelfLife.ui.utils.formatTimestampToDisplayDate
import com.google.firebase.Timestamp
import java.util.Date
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FoodItemDetailsTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun mockFoodItem(): FoodItem {
    val foodFacts =
        FoodFacts(
            name = "Apple",
            barcode = "123456789",
            quantity = Quantity(5.0, FoodUnit.COUNT),
            category = FoodCategory.FRUIT)
    return FoodItem(
        uid = "foodItem1",
        foodFacts = foodFacts,
        expiryDate = Timestamp(Date(System.currentTimeMillis() + 86400000)), // 1 day from now
        openDate = Timestamp(Date(System.currentTimeMillis() - 86400000)), // 1 day ago
        buyDate = Timestamp(Date(System.currentTimeMillis() - 172800000)), // 2 days ago
        location = FoodStorageLocation.FREEZER,
        status = FoodStatus.OPEN)
  }

  @Test
  fun foodItemDetails_DisplaysCorrectly() {
    val foodItem = mockFoodItem()

    composeTestRule.setContent { FoodItemDetails(foodItem = foodItem) }

    // Validate the displayed content
    composeTestRule.onNodeWithTag("foodItemDetailsCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("categoryText").assertTextEquals("Category: FRUIT")
    composeTestRule.onNodeWithTag("locationText").assertTextEquals("Location: FREEZER")
    composeTestRule.onNodeWithTag("statusText").assertTextEquals("Status: OPEN")

    val expectedExpiryDate =
        "Expiry Date: ${foodItem.expiryDate?.let { formatTimestampToDisplayDate(it) }}"
    composeTestRule.onNodeWithTag("expiryDateText").assertTextEquals(expectedExpiryDate)

    val expectedOpenDate = "Open Date: ${foodItem.openDate?.let { formatTimestampToDisplayDate(it) }}"
    composeTestRule.onNodeWithTag("openDateText").assertTextEquals(expectedOpenDate)

    val expectedBuyDate = "Buy Date: ${formatTimestampToDisplayDate(foodItem.buyDate)}"
    composeTestRule.onNodeWithTag("buyDateText").assertTextEquals(expectedBuyDate)

    composeTestRule.onNodeWithTag("energyText").assertTextEquals("Energy: 0 Kcal")
    composeTestRule.onNodeWithTag("proteinsText").assertTextEquals("Proteins: 0.0 g")
    composeTestRule.onNodeWithTag("quantityText").assertTextEquals("Quantity: 5.0 COUNT")
  }
}
