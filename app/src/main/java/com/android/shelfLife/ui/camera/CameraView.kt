package com.android.shelfLife.ui.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.android.shelfLife.ui.navigation.NavigationActions

@Composable
fun BarcodeScannerScreen(navigationActions: NavigationActions) {
  val context = LocalContext.current
  val permissionGranted =
      ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
          PackageManager.PERMISSION_GRANTED

  if (permissionGranted) {
    CameraPreviewView(modifier = Modifier.fillMaxSize()) { previewView ->
      startCamera(context, previewView)
    }
  } else {
    PermissionDeniedScreen(
        onRequestPermissionAgain = {
          ActivityCompat.requestPermissions(
              context as ComponentActivity, arrayOf(Manifest.permission.CAMERA), 101)
        })
  }
}

@Composable
fun CameraPreviewView(modifier: Modifier = Modifier, startCamera: (PreviewView) -> Unit) {
  AndroidView(
      factory = { context ->
        val previewView = PreviewView(context)
        startCamera(previewView)
        previewView
      },
      modifier = modifier.fillMaxSize())
}

fun startCamera(context: Context, previewView: PreviewView) {
  val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

  cameraProviderFuture.addListener(
      {
        val cameraProvider = cameraProviderFuture.get()

        val preview =
            Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
          cameraProvider.unbindAll()
          cameraProvider.bindToLifecycle(context as LifecycleOwner, cameraSelector, preview)
        } catch (exc: Exception) {
          Log.e("CameraX", "Use case binding failed", exc)
        }
      },
      ContextCompat.getMainExecutor(context))
}

@Composable
fun PermissionDeniedScreen(onRequestPermissionAgain: () -> Unit) {
  Column(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Camera permission is required to scan barcodes.")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRequestPermissionAgain) { Text(text = "Grant Permission") }
      }
}
