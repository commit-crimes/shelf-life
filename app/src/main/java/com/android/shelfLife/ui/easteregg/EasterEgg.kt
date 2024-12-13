package com.android.shelfLife.ui.easteregg

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.shelfLife.R
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.utils.CustomTopAppBar

@Composable
fun EasterEggScreen(navigationActions: NavigationActions){

    Scaffold(
        modifier = Modifier.fillMaxSize(), // Ensure Scaffold takes up the full size
        topBar = {CustomTopAppBar(
            onClick = {navigationActions.goBack()},
            title = stringResource(id =R.string.easteregg_title),
            titleTestTag = "eastereggTitle",
        )},
        content = { paddingValues ->
            // Column for the content inside the Scaffold
            Column(
                modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues), // Apply padding values from Scaffold
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center // Center content vertically
            ) {
                // Easter egg image
                Image(
                    painter = painterResource(id = R.drawable.how_did_we_get_here),
                    contentDescription = "How did we get here?",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit)

                // Spacer for some space between the image and the text
                Spacer(modifier = Modifier.size(16.dp))

                // Error message text
                Text(
                    text = "No recipe selected. Should not happen",
                    modifier = Modifier.testTag("noRecipeSelectedMessage"),
                    color = Color.Red)
            }
        })
}