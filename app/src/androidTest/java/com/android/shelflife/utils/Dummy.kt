package com.android.shelflife.utils

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Dummy {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun verifyTextIsDisplayed() {
        // Set up the content to be tested
        composeTestRule.setContent {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Hello, ShelfLife!")
            }
        }

        // Find the Text composable and verify it is displayed
        composeTestRule.onNodeWithText("Hello, ShelfLife!").assertIsDisplayed()
    }
}
