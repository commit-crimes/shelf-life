package helpers

import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.mockito.kotlin.whenever

class FoodItemRepositoryTestHelper(private val foodItemRepository: FoodItemRepository) {

  private val foodItems = MutableStateFlow<List<FoodItem>>(emptyList())
  private val selectedFoodItem = MutableStateFlow<FoodItem?>(null)
  private val errorMessage = MutableStateFlow<String?>(null)

  init {
    whenever(foodItemRepository.foodItems).thenReturn(foodItems.asStateFlow())
    whenever(foodItemRepository.selectedFoodItem).thenReturn(selectedFoodItem.asStateFlow())
    whenever(foodItemRepository.errorMessage).thenReturn(errorMessage.asStateFlow())
    whenever(foodItemRepository.getNewUid()).thenReturn("mockedUID")
  }

  fun setFoodItems(foodItemsList: List<FoodItem>) {
    foodItems.value = foodItemsList
  }

  fun setSelectedFoodItem(foodItem: FoodItem) {
    selectedFoodItem.value = foodItem
  }
}
