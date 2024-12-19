package com.android.shelfLife.ui.overview

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Upload
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.android.shelfLife.R
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodFacts.SearchStatus
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.utils.CustomButtons
import com.android.shelfLife.ui.utils.CustomTopAppBar
import com.android.shelfLife.viewmodel.overview.FoodItemViewModel

/**
 * Composable function to display the Choose Food Item screen.
 *
 * @param navigationActions The navigation actions to be used in the screen.
 * @param foodItemViewModel The ViewModel for managing the state of the food item.
 */
@Composable
fun ChooseFoodItem(
    navigationActions: NavigationActions,
    foodItemViewModel: FoodItemViewModel = hiltViewModel(),
) {
  val context = LocalContext.current

  val selectImageLauncher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri?
        ->
        foodItemViewModel.capturedImageUri = uri
      }

  LaunchedEffect(foodItemViewModel.capturedImageUri) {
    foodItemViewModel.capturedImageUri?.let { uri ->
      foodItemViewModel.isLoading = true
      val uploadedUrl = foodItemViewModel.uploadImageToFirebaseStorage(uri, context)
      if (uploadedUrl != null) {
        foodItemViewModel.selectedImage =
            FoodFacts(name = "", quantity = Quantity(1.0, FoodUnit.GRAM), imageUrl = uploadedUrl)
        Toast.makeText(context, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
      } else {
        Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
      }
      foodItemViewModel.isLoading = false
      foodItemViewModel.capturedImageUri = null // Reset the URI
    }
  }

  Scaffold(
      topBar = {
        CustomTopAppBar(
            onClick = {
              foodItemViewModel.selectedImage = null
              foodItemViewModel.setFoodItem(null)
              foodItemViewModel.resetSearchStatus()
              navigationActions.goBack()
            },
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
                val searchStatus by foodItemViewModel.searchStatus.collectAsState()
                when (searchStatus) {
                  is SearchStatus.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp).padding(16.dp).testTag("loadingIndicator"))
                  }
                  is SearchStatus.Success -> {
                    val foodFacts by foodItemViewModel.foodFactsSuggestions.collectAsState()
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
                                            .clickable { foodItemViewModel.selectedImage = null }
                                            .testTag("noImage"),
                                    contentAlignment = Alignment.Center) {
                                      Text(
                                          stringResource(id = R.string.no_image_option),
                                          modifier = Modifier.testTag("noImageText"))
                                    }
                              }
                            }

                            // Add grid item for uploading an image
                            item {
                              Box(
                                  modifier =
                                      Modifier.padding(4.dp)
                                          .size(100.dp)
                                          .border(
                                              width = 1.dp,
                                              color = Color.Gray,
                                              shape = RoundedCornerShape(8.dp))
                                          .clickable {
                                            if (!foodItemViewModel.isLoading)
                                                selectImageLauncher.launch("image/*")
                                          }
                                          .testTag("uploadImage"),
                                  contentAlignment = Alignment.Center) {
                                    if (foodItemViewModel.isLoading) {
                                      CircularProgressIndicator() // Show loading during upload
                                    } else {
                                      Icon(
                                          imageVector = Icons.Default.Upload,
                                          contentDescription = "Upload Image",
                                          modifier = Modifier.size(48.dp))
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

              item {
                CustomButtons(
                    button1OnClick = {
                      foodItemViewModel.selectedImage = null
                      foodItemViewModel.setFoodItem(null)
                      foodItemViewModel.resetSearchStatus()
                      navigationActions.navigateTo(Route.OVERVIEW)
                    },
                    button1TestTag = "cancelButton",
                    button1Text = stringResource(id = R.string.cancel_button),
                    button2OnClick = {
                      if (!foodItemViewModel.isLoading) {
                        foodItemViewModel.selectedImage?.let { selectedImage ->
                          selectedImage.name = foodItemViewModel.selectedFood?.foodFacts?.name ?: ""
                          foodItemViewModel.setFoodItem(
                              // This is a temporary food item to remember the state of the Food
                              // Item at this screen
                              FoodItem(foodFacts = selectedImage, owner = "", uid = ""))
                          navigationActions.navigateTo(Screen.EDIT_FOOD)
                        }
                      }
                    },
                    button2TestTag = "foodSave",
                    button2Text = stringResource(id = R.string.submit_button_text))
              }
            }
      })
}
