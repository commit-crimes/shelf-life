package com.android.shelfLife.ui.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.android.shelfLife.model.foodItem.FoodItem
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun FoodItemDetails(foodItem: FoodItem) {
    val expiryDate = foodItem.expiryDate
    val formattedExpiryDate = expiryDate?.toDate()?.let { SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(it) } ?: "No Expiry Date"
    val formattedOpenDate = foodItem.openDate?.toDate()?.let { SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(it) } ?: "Not Opened"
    val formattedBuyDate = SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(foodItem.buyDate.toDate())

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Category: ${foodItem.foodFacts.category.name}", fontSize = 12.sp)
        Text(text = "Location: ${foodItem.location.name}", fontSize = 12.sp)
        Text(text = "Status: ${foodItem.status.name}", fontSize = 12.sp)
        Text(text = "Expiry Date: $formattedExpiryDate", fontSize = 12.sp)
        Text(text = "Open Date: $formattedOpenDate", fontSize = 12.sp)
        Text(text = "Buy Date: $formattedBuyDate", fontSize = 12.sp)
        Text(text = "Energy: ${foodItem.foodFacts.nutritionFacts.energyKcal} Kcal", fontSize = 12.sp)
        Text(text = "Proteins: ${foodItem.foodFacts.nutritionFacts.proteins} g", fontSize = 12.sp)
        Text(text = "Quantity: ${foodItem.foodFacts.quantity.amount} ${foodItem.foodFacts.quantity.unit}", fontSize = 12.sp)
    }
}