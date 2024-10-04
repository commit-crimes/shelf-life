package com.android.shelfLife.ui.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.android.shelfLife.ui.navigation.NavigationActions

@Composable
fun BarcodeScannerScreen(navigationActions: NavigationActions) {
  val context = LocalContext.current

  CameraPreviewView(modifier = Modifier.fillMaxSize()) { previewView ->
    startCamera(context, previewView) // This will initialize CameraX
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
