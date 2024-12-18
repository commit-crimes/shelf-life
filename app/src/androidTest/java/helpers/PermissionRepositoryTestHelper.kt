package helpers

import com.android.shelfLife.model.permission.PermissionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.mockito.kotlin.whenever

class PermissionRepositoryTestHelper(private val permissionRepository: PermissionRepository) {
  private val permissionGranted = MutableStateFlow(true)
  private val permissionRequested = MutableStateFlow(true)

  init {
    whenever(permissionRepository.permissionGranted).thenReturn(permissionGranted.asStateFlow())
    whenever(permissionRepository.permissionRequested).thenReturn(permissionRequested.asStateFlow())
  }

  fun setPermissionGranted(isGranted: Boolean) {
    permissionGranted.value = isGranted
  }

  fun setPermissionRequested(isRequested: Boolean) {
    permissionRequested.value = isRequested
  }
}
