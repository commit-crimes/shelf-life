package com.android.shelfLife.ui.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/**
 * A composable function that displays an error pop-up dialog.
 *
 * @param showDialog A boolean value that indicates whether the dialog should be shown.
 * @param onDismiss A lambda function that is called when the dialog is dismissed.
 * @param errorMessages A list of error messages to be displayed in the dialog.
 */
@Composable
fun ErrorPopUp(showDialog: Boolean, onDismiss: () -> Unit, errorMessages: List<String>) {
  if (showDialog) {
    Dialog(onDismissRequest = onDismiss) {
      Surface(
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.padding(16.dp).widthIn(min = 280.dp, max = 400.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(
                      text = "Error", modifier = Modifier.padding(bottom = 16.dp), fontSize = 24.sp)

                  Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    errorMessages.forEach { message ->
                      Text(text = message, modifier = Modifier.padding(bottom = 8.dp))
                    }
                  }

                  Button(onClick = onDismiss) { Text("OK") }
                }
          }
    }
  }
}
