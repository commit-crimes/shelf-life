package com.android.shelflife.viewmodel.camera

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import androidx.core.content.ContextCompat
import androidx.lifecycle.testing.TestLifecycleOwner
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodFactsRepository
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodFacts.SearchStatus
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
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = dagger.hilt.android.testing.HiltTestApplication::class)
class BarcodeScannerViewModelTest {
    @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)
    @Mock private lateinit var application: Application
    @Mock private lateinit var foodFactsRepository: FoodFactsRepository
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
        whenever(sharedPreferencesEditor.putBoolean(anyString(), anyBoolean())).thenReturn(sharedPreferencesEditor)

        whenever(foodFactsRepository.searchStatus).thenReturn(searchStatusFlow)
        whenever(foodFactsRepository.foodFactsSuggestions).thenReturn(foodFactsSuggestionsFlow)

        // Now create the viewModel after all stubbings are done
        viewModel = BarcodeScannerViewModel(application, foodFactsRepository)
    }

    @Test
    fun `initial state - permission not granted and not requested`() = runTest{
        val permissionInfo = PermissionInfo();
        permissionInfo.name = Manifest.permission.CAMERA
        permissionInfo.protectionLevel = PermissionInfo.PROTECTION_DANGEROUS
        shadowOf(appContext.packageManager).addPermissionInfo(permissionInfo);

        viewModel = BarcodeScannerViewModel(appContext, foodFactsRepository)

        assertFalse(viewModel.permissionGranted)

    }

    @Test
    fun `initial state - permission granted`() = runTest{
        `when`(ContextCompat.checkSelfPermission(appContext, Manifest.permission.CAMERA))
            .thenReturn(PackageManager.PERMISSION_GRANTED)

        assertTrue(viewModel.permissionGranted)

    }

    @Test
    fun `checkCameraPermission updates permissionGranted`() = runTest{
        // Initially permission is denied
        `when`(ContextCompat.checkSelfPermission(appContext, Manifest.permission.CAMERA))
            .thenReturn(PackageManager.PERMISSION_DENIED)

        viewModel.checkCameraPermission()
        assertFalse(viewModel.permissionGranted)

        // Grant permission
        `when`(ContextCompat.checkSelfPermission(appContext, Manifest.permission.CAMERA))
            .thenReturn(PackageManager.PERMISSION_GRANTED)

        viewModel.checkCameraPermission()
        assertTrue(viewModel.permissionGranted)
    }

    @Test
    fun `onPermissionResult updates state and stores preference`() = runTest{
        viewModel.onPermissionResult(true)

        assertTrue(viewModel.permissionGranted)
        verify(sharedPreferencesEditor).putBoolean("permissionRequested", true)
        verify(sharedPreferencesEditor).apply()
    }

    @Test
    fun `searchByBarcode invokes repository search`() = runTest{
        val barcode = 123456789L
        viewModel.searchByBarcode(barcode)

        verify(foodFactsRepository).searchByBarcode(barcode)
    }

    @Test
    fun `resetSearchStatus invokes repository reset`() = runTest{
        viewModel.resetSearchStatus()

        verify(foodFactsRepository).resetSearchStatus()
    }

    @Test
    fun `searchStatus reflects repository flow`() = runTest {
        val expectedStatus = SearchStatus.Loading
        searchStatusFlow.value = expectedStatus

        val lifecycleOwner = TestLifecycleOwner()
        lifecycleOwner.lifecycle.addObserver(viewModel)

        val actualStatus = viewModel.searchStatus.value
        assertEquals(expectedStatus, actualStatus)
    }

    @Test
    fun `foodFactsSuggestions reflects repository flow`() = runTest {
        val expectedSuggestions = listOf(FoodFacts("Apple", "12345", Quantity(1.0), ))
        foodFactsSuggestionsFlow.value = expectedSuggestions

        val lifecycleOwner = TestLifecycleOwner()
        lifecycleOwner.lifecycle.addObserver(viewModel)

        val actualSuggestions = viewModel.foodFactsSuggestions.value
        assertEquals(expectedSuggestions, actualSuggestions)
    }
}