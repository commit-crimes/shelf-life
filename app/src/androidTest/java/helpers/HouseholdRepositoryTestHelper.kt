package helpers

import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepository
import io.mockk.coEvery
import io.mockk.every
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HouseholdRepositoryTestHelper(private val houseHoldRepository: HouseHoldRepository) {
  private val selectedHousehold = MutableStateFlow<HouseHold?>(null)
  private val householdToEdit = MutableStateFlow<HouseHold?>(null)
  private val households = MutableStateFlow<List<HouseHold>>(emptyList())
  private var householdMembers = emptyList<String>()

  init {
    every { houseHoldRepository.selectedHousehold } returns selectedHousehold.asStateFlow()
    every { houseHoldRepository.householdToEdit } returns householdToEdit.asStateFlow()
    every { houseHoldRepository.households } returns households.asStateFlow()
    every { houseHoldRepository.checkIfHouseholdNameExists(any()) } answers   {households.value.any { it.name == args[0] }}
    coEvery { houseHoldRepository.getHouseholdMembers(any()) } returns householdMembers
  }

  fun selectHousehold(houseHold: HouseHold?) {
    households.value = houseHold?.let { listOf(it) } ?: emptyList()
    selectedHousehold.value = houseHold
  }

    fun setHouseholds(houseHolds: List<HouseHold>) {
        households.value = houseHolds
    }

  fun setHouseholdToEdit(houseHold: HouseHold?) {
    householdToEdit.value = houseHold
  }

  fun setHouseholdMembers(members: List<String>) {
    householdMembers = members
  }
}
