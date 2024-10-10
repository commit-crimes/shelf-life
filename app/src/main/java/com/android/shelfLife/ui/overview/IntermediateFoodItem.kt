package com.android.shelfLife.ui.overview

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.shelfLife.R

@Preview
@Composable
fun IntermediateScanScreen() {
  val context = LocalContext.current

  Column(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center) {

        // Scan Barcode Text
        Text(
            text = "Scan Barcode",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp))

        // Barcode Image
        Image(
            painter = painterResource(id = R.drawable.scan_logo),
            contentDescription = "scan logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(250.dp).padding(bottom = 24.dp))

        // Scan Button
        Button(
            onClick = { Toast.makeText(context, "Scan Button Clicked", Toast.LENGTH_SHORT).show() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), // Green color
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 32.dp)) {
              Text(text = "Scan", color = Color.White, fontSize = 16.sp)
            }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
              Toast.makeText(context, "Manual Entry Clicked", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 32.dp)) {
              Text(text = "Or manually enter", color = Color.White, fontSize = 16.sp)
            }
      }
}
