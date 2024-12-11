package com.android.shelfLife.ui.newoverview

import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.android.shelfLife.R
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodFactsViewModel
import com.android.shelfLife.model.foodFacts.SearchStatus
import com.android.shelfLife.model.newFoodItem.FoodItemRepository
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.utils.CustomButtons
import com.android.shelfLife.ui.utils.CustomTopAppBar
import com.android.shelfLife.viewmodel.FoodItemViewModel

@Composable
fun ChooseFoodItem(
    navigationActions: NavigationActions,
    foodFactsViewModel: FoodFactsViewModel,
    foodItemRepository: FoodItemRepository,
    userRepository: UserRepository,
    paddingValues: PaddingValues = PaddingValues(16.dp)
) {
  val coroutineScope = rememberCoroutineScope()
  val foodItemViewModel = FoodItemViewModel(foodItemRepository, userRepository)


  val context = LocalContext.current
  // TODO These are temp while we work on the new FoodFactsModel
  val foodFacts by foodFactsViewModel.foodFactsSuggestions.collectAsState()
  val searchStatus by foodFactsViewModel.searchStatus.collectAsState()

    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    val captureImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            // Save the bitmap URI (you can upload it directly to Firebase here)
            val uri = MediaStore.Images.Media.insertImage(
                context.contentResolver,
                bitmap,
                "CapturedImage",
                "Captured by Camera"
            )?.toUri()
            capturedImageUri = uri
        }
    }

    val selectImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        capturedImageUri = uri
    }


  Scaffold(
      topBar = {
        CustomTopAppBar(
            onClick = { navigationActions.goBack() },
            title = stringResource(R.string.choose_food_item_title),
            titleTestTag = "chooseFoodItemTitle")
      },
      content = { paddingValues ->
        LazyColumn(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .scrollable(rememberScrollState(), Orientation.Horizontal),
            horizontalAlignment = Alignment.CenterHorizontally) {
              item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.select_image_label),
                    modifier = Modifier.testTag("selectImage"))
                Spacer(modifier = Modifier.height(16.dp))
              }

              item {
                when (searchStatus) {
                  is SearchStatus.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp).padding(16.dp).testTag("loadingIndicator"))
                  }
                  is SearchStatus.Success -> {
                    Box(modifier = Modifier.height(350.dp)) {
                      LazyVerticalGrid(
                          columns = GridCells.Fixed(3),
                          modifier = Modifier.fillMaxSize(),
                          contentPadding = PaddingValues(4.dp)) {
                            items(foodFacts.take(7) + listOf(null)) { foodFact ->
                              val borderColor =
                                  if (foodItemViewModel.selectedImage == foodFact) {
                                    MaterialTheme.colorScheme.primary
                                  } else {
                                    Color.Gray
                                  }

                              val alpha =
                                  if (foodItemViewModel.selectedImage == foodFact ||
                                      foodItemViewModel.selectedImage == null)
                                      1f
                                  else 0.5f

                              if (foodFact != null) {
                                Box(
                                    modifier =
                                        Modifier.padding(4.dp)
                                            .size(100.dp)
                                            .border(
                                                width =
                                                    if (foodItemViewModel.selectedImage == foodFact)
                                                        4.dp
                                                    else 1.dp,
                                                color = borderColor,
                                                shape = RoundedCornerShape(8.dp))
                                            .clickable {
                                              foodItemViewModel.selectedImage = foodFact
                                            }
                                            .testTag("foodImage")) {
                                      AsyncImage(
                                          model = foodFact.imageUrl,
                                          contentDescription = "Food Image",
                                          modifier =
                                              Modifier.fillMaxSize()
                                                  .clip(RoundedCornerShape(8.dp))
                                                  .graphicsLayer(alpha = alpha)
                                                  .testTag("IndividualFoodItemImage"),
                                          contentScale = ContentScale.Crop)
                                    }
                              } else {
                                Box(
                                    modifier =
                                        Modifier.padding(4.dp)
                                            .size(100.dp)
                                            .border(
                                                width =
                                                    if (foodItemViewModel.selectedImage == null)
                                                        4.dp
                                                    else 1.dp,
                                                color = borderColor,
                                                shape = RoundedCornerShape(8.dp))
                                            .clickable {
                                              foodItemViewModel.selectedImage =
                                                  null // Indicate no image selected
                                            }
                                            .testTag("noImage"),
                                    contentAlignment = Alignment.Center) {
                                      Text(
                                          stringResource(id = R.string.no_image_option),
                                          modifier = Modifier.testTag("noImageText"))
                                    }
                              }
                            }
                          }
                    }
                  }
                  is SearchStatus.Failure -> {
                    Text(
                        text = stringResource(id = R.string.image_not_loading_error),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp))
                  }
                  else -> Unit
                }
              }

              item { Spacer(modifier = Modifier.height(8.dp)) }



              item {
                foodItemViewModel.selectedImage?.let {
                  Text(
                      stringResource(id = R.string.selected_image_label),
                      modifier = Modifier.testTag("selectedImageText"))
                  Image(
                      painter = rememberAsyncImagePainter(it.imageUrl),
                      contentDescription = null,
                      modifier = Modifier.size(150.dp).padding(8.dp).testTag("selectedImage"))
                  Spacer(modifier = Modifier.height(16.dp))
                }
                    ?: run {
                      Text(
                          stringResource(id = R.string.default_image_label),
                          modifier = Modifier.testTag("defaultImageText"))
                      Image(
                          painter = rememberAsyncImagePainter(FoodFacts.DEFAULT_IMAGE_URL),
                          contentDescription = null,
                          modifier = Modifier.size(150.dp).padding(8.dp).testTag("defaultImage"))
                      Spacer(modifier = Modifier.height(16.dp))
                    }
              }

                item{
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { captureImageLauncher.launch() },
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(text = "Take Photo")
                        }
                        Button(
                            onClick = { selectImageLauncher.launch("image/*") },
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(text = "Upload from Gallery")
                        }
                    }

                }

            item {
                capturedImageUri?.let { uri ->
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = null,
                        modifier = Modifier
                            .size(150.dp)
                            .padding(8.dp)
                    )
                }
            }

              item {
                CustomButtons(
                    button1OnClick = {
                      foodFactsViewModel.resetSearchStatus()
                      foodItemViewModel.selectedImage = null
                      navigationActions.navigateTo(Route.OVERVIEW)
                    },
                    button1TestTag = "cancelButton",
                    button1Text = stringResource(id = R.string.cancel_button),
                    button2OnClick = {
                      foodFactsViewModel.resetSearchStatus()
                      navigationActions.navigateTo(Screen.EDIT_FOOD)
                    },
                    button2TestTag = "foodSave",
                    button2Text = stringResource(id = R.string.submit_button_text))
              }

              item {
                Text(
                    text = "OR",
                    modifier = Modifier.padding(bottom = 16.dp).testTag("foodItemDetailsTitle"))
                Button(
                    onClick = {
                      foodFactsViewModel.resetSearchStatus()
                      navigationActions.navigateTo(Screen.ADD_FOOD)
                    },
                    modifier = Modifier.testTag("manualEntryButton")) {
                      Text(stringResource(id = R.string.food_item_manual_alternative))
                    }
              }
            }
      })
}
