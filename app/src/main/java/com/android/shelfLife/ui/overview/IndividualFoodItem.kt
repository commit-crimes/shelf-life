import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.ui.navigation.NavigationActions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndividualFoodItemScreen(foodItem: FoodItem, navigationActions: NavigationActions) {
  Scaffold(
      modifier = Modifier.fillMaxSize(),
      topBar = {
        TopAppBar(
            title = {},
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("individualFoodItemBack")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
                  }
            })
      }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
          // Add your content here
        }
      }
}

// @OptIn(ExperimentalFoundationApi::class)
// @Composable
// fun CarouselWithPager() {
//    val pagerState = rememberPagerState()
//
//    Column(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        HorizontalPager(
//            count = 5,
//            state = pagerState,
//            contentPadding = PaddingValues(horizontal = 32.dp),
//            modifier = Modifier.height(200.dp)
//        ) { page ->
//            CarouselItem(index = page)
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        HorizontalPagerIndicator(
//            pagerState = pagerState,
//            activeColor = MaterialTheme.colorScheme.primary,
//            inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
//        )
//    }
// }
