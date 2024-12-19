package helpers

import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodFactsRepository
import com.android.shelfLife.model.foodFacts.SearchStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.mockito.kotlin.whenever

class FoodFactsRepositoryTestHelper(private val foodFactsRepository: FoodFactsRepository) {

  private val searchStatus = MutableStateFlow<SearchStatus>(SearchStatus.Idle)

  private val foodFactsSuggestions = MutableStateFlow<List<FoodFacts>>(emptyList())

  init {
    whenever(foodFactsRepository.searchStatus).thenReturn(searchStatus.asStateFlow())
    whenever(foodFactsRepository.foodFactsSuggestions)
        .thenReturn(foodFactsSuggestions.asStateFlow())
  }

  fun setFoodFactsSuggestions(foodFactsSuggestions: List<FoodFacts>) {
    this.foodFactsSuggestions.value = foodFactsSuggestions
  }

  fun setSearchStatus(searchStatus: SearchStatus) {
    this.searchStatus.value = searchStatus
  }
}
