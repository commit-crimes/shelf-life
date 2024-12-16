package com.android.shelfLife.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * A composable utility function that listens for lifecycle events of a given `LifecycleOwner`
 * and triggers corresponding callbacks for `ON_RESUME` and `ON_PAUSE` events.
 *
 * This can be used to perform actions when the lifecycle state changes, such as starting or stopping
 * processes based on the app's lifecycle state.
 *
 * @param lifecycleOwner The `LifecycleOwner` whose lifecycle is being observed. Defaults to the
 *   current `LocalLifecycleOwner`.
 * @param onResume A lambda function triggered when the lifecycle enters the `ON_RESUME` state.
 * @param onPause A lambda function triggered when the lifecycle enters the `ON_PAUSE` state.
 */
@Composable
fun OnLifecycleEvent(
    lifecycleOwner: LifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current,
    onResume: () -> Unit = {}, // Callback for ON_RESUME event
    onPause: () -> Unit = {} // Callback for ON_PAUSE event
) {
    // Add a DisposableEffect to manage the lifecycle observer
    DisposableEffect(lifecycleOwner) {
        // Create a lifecycle observer to listen for lifecycle events
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> onResume() // Trigger the onResume callback
                Lifecycle.Event.ON_PAUSE -> onPause() // Trigger the onPause callback
                else -> {} // Ignore other lifecycle events
            }
        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // Clean up by removing the observer when the effect is disposed
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}