package com.android.shelfLife.ui.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.android.shelfLife.model.household.HouseholdViewModel

@Composable
fun EditHouseHoldPopUp(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    householdViewModel: HouseholdViewModel
) {
  val houseHoldsToDeleteId: MutableList<String> = mutableListOf()
  val houseHolds = householdViewModel.households.collectAsState().value

  if (showDialog) {
    androidx.compose.material3.AlertDialog(
        modifier = Modifier.testTag("editHouseholdPopup"),
        onDismissRequest = { onDismiss() },
        title = { Text("Edit household") },
        text = {
          Column {
            houseHolds.forEach {
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically) {
                    var textColor by remember { mutableStateOf(Color.Black) }
                    var text by remember { mutableStateOf(it.name) }
                    val houseHold = it
                    BasicTextField(
                        value = text,
                        maxLines = 1,
                        textStyle = TextStyle(fontSize = 25.sp, color = textColor),
                        onValueChange = {
                          text = it
                          houseHolds[houseHolds.indexOf(houseHold)].name = it
                        },
                    )
                    IconButton(
                        onClick = {
                          if (houseHoldsToDeleteId.contains(it.uid)) {
                            houseHoldsToDeleteId.remove(it.uid)
                            textColor = Color.Black
                          } else {
                            houseHoldsToDeleteId.add(it.uid)
                            textColor = Color.Gray
                          }
                        },
                        modifier = Modifier.testTag("deleteIcon_${it.uid}")) {
                          Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Delete")
                        }
                  }
            }
          }
        },
        confirmButton = {
          Button(
              onClick = {
                houseHoldsToDeleteId.forEach { householdViewModel.deleteHouseholdById(it) }
                houseHolds.forEach { householdViewModel.updateHousehold(it) }
                onDismiss()
              }) {
                Text("Apply")
              }
        },
        dismissButton = { Button(onClick = { onDismiss() }) { Text("Cancel") } })
  }
}
