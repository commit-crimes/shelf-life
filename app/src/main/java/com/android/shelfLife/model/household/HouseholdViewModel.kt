package com.android.shelfLife.model.household

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.util.Log
import com.android.shelfLife.model.foodItem.FoodItemRepositoryFirestore

class HouseholdViewModel(
    private val repository: HouseHoldRepository,
    private val listFoodItemsViewModel: ListFoodItemsViewModel
) : ViewModel() {
    private val _households = MutableStateFlow<List<HouseHold>>(emptyList())
    val households: StateFlow<List<HouseHold>> = _households.asStateFlow()

    private val _selectedHousehold = MutableStateFlow<HouseHold?>(null)
    val selectedHousehold: StateFlow<HouseHold?> = _selectedHousehold.asStateFlow()

    init {
        loadHouseholds()
    }

    private fun loadHouseholds() {
        repository.getHouseholds(
            onSuccess = { householdList ->
                _households.value = householdList
                selectHousehold(householdList.firstOrNull()) // Default to the first household
            },
            onFailure = { exception ->
                Log.e("HouseholdViewModel", "Error loading households: $exception")
            }
        )
    }

    fun selectHousehold(household: HouseHold?) {
        _selectedHousehold.value = household
        household?.let {
            listFoodItemsViewModel.setFoodItems(it.foodItems)
        }
    }

    /**
     * Adds a new household to the repository and updates the household list.
     *
     * @param householdName - The name of the household to be added.
     */
    fun addNewHousehold(householdName: String) {
        val household = HouseHold(repository.getNewUid(), householdName, emptyList(), emptyList())
        repository.addHousehold(
            household,
            onSuccess = {
                // Refresh the household list after successful addition
                loadHouseholds()
                Log.d("HouseholdViewModel", "Household added successfully")
            },
            onFailure = { exception ->
                Log.e("HouseholdViewModel", "Error adding household: $exception")
            }
        )
        loadHouseholds()
    }

    // Factory for creating HouseholdViewModel instances
    companion object {
        val Factory: ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val firebaseFirestore = FirebaseFirestore.getInstance()
                    val foodItemRepository = FoodItemRepositoryFirestore(firebaseFirestore)
                    val listFoodItemsViewModel = ListFoodItemsViewModel(foodItemRepository)
                    val repository = HouseholdRepositoryFirestore(firebaseFirestore)
                    return HouseholdViewModel(repository, listFoodItemsViewModel) as T
                }
            }
    }
}