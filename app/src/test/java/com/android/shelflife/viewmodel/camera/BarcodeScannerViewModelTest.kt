package com.android.shelflife.viewmodel.camera

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodFactsRepository
import com.android.shelfLife.model.foodFacts.SearchStatus
import com.android.shelfLife.model.permission.PermissionRepository
import com.android.shelfLife.viewmodel.camera.BarcodeScannerViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = dagger.hilt.android.testing.HiltTestApplication::class)
class BarcodeScannerViewModelTest {
  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)
  @Mock private lateinit var application: Application
  @Mock private lateinit var foodFactsRepository: FoodFactsRepository
  @Mock private lateinit var permissionRepository: PermissionRepository
  @Mock private lateinit var sharedPreferences: SharedPreferences
  @Mock private lateinit var sharedPreferencesEditor: SharedPreferences.Editor
  val appContext = RuntimeEnvironment.getApplication()
  private lateinit var viewModel: BarcodeScannerViewModel
  private val searchStatusFlow = MutableStateFlow<SearchStatus>(SearchStatus.Idle)
  private val foodFactsSuggestionsFlow = MutableStateFlow<List<FoodFacts>>(emptyList())

  @Before
  fun setUp() {
    hiltRule.inject()
    MockitoAnnotations.openMocks(this)

    whenever(application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE))
        .thenReturn(sharedPreferences) // Complete the stubbing here

    whenever(sharedPreferences.getBoolean("permissionRequested", false)).thenReturn(false)
    whenever(sharedPreferences.edit()).thenReturn(sharedPreferencesEditor)
    whenever(sharedPreferencesEditor.putBoolean(anyString(), anyBoolean()))
        .thenReturn(sharedPreferencesEditor)

    whenever(foodFactsRepository.searchStatus).thenReturn(searchStatusFlow)
    whenever(foodFactsRepository.foodFactsSuggestions).thenReturn(foodFactsSuggestionsFlow)
    whenever(permissionRepository.permissionRequested).thenReturn(MutableStateFlow(false))
    // Now create the viewModel after all stubbings are done
    viewModel = BarcodeScannerViewModel(foodFactsRepository, permissionRepository)
  }

  @Test
  fun `init invokes repository check`() = runTest {
    verify(permissionRepository).checkCameraPermission()
  }

  @Test
  fun `onPermissionResult invokes repository onPermissionResult`() = runTest {
    val isGranted = true
    viewModel.onPermissionResult(isGranted)

    verify(permissionRepository).onPermissionResult(isGranted)
  }

  @Test
  fun `searchByBarcode invokes repository search`() = runTest {
    val barcode = 123456789L
    viewModel.searchByBarcode(barcode)

    verify(foodFactsRepository).searchByBarcode(barcode)
  }

  @Test
  fun `resetSearchStatus invokes repository reset`() = runTest {
    viewModel.resetSearchStatus()

    verify(foodFactsRepository).resetSearchStatus()
  }
}
