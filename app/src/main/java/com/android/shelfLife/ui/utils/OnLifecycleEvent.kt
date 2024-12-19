package com.android.shelfLife.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Composable function to handle lifecycle events.
 *
 * This function observes the lifecycle of a given `LifecycleOwner` and triggers the provided
 * callbacks when the lifecycle events `ON_RESUME` and `ON_PAUSE` occur.
 *
 * @param lifecycleOwner The `LifecycleOwner` whose lifecycle is being observed. Defaults to the
 *   current `LocalLifecycleOwner`.
 * @param onResume A lambda function to be called when the `ON_RESUME` event occurs.
 * @param onPause A lambda function to be called when the `ON_PAUSE` event occurs.
 */
@Composable
fun OnLifecycleEvent(
    lifecycleOwner: LifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current,
    onResume: () -> Unit = {},
    onPause: () -> Unit = {}
) {
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_RESUME -> onResume()
        Lifecycle.Event.ON_PAUSE -> onPause()
        else -> {}
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }
}
