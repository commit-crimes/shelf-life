package helpers

import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.mockito.kotlin.whenever

class HouseholdRepositoryTestHelper(private val houseHoldRepository: HouseHoldRepository) {
  private val selectedHousehold = MutableStateFlow<HouseHold?>(null)
  private val householdToEdit = MutableStateFlow<HouseHold?>(null)
  private val households = MutableStateFlow<List<HouseHold>>(emptyList())

  init {
    whenever(houseHoldRepository.selectedHousehold).thenReturn(selectedHousehold.asStateFlow())
    whenever(houseHoldRepository.households).thenReturn(households.asStateFlow())
    whenever(houseHoldRepository.householdToEdit).thenReturn(householdToEdit.asStateFlow())
  }

  fun selectHousehold(houseHold: HouseHold?) {
    households.value = houseHold?.let { listOf(it) } ?: emptyList()
    selectedHousehold.value = houseHold
  }
}
