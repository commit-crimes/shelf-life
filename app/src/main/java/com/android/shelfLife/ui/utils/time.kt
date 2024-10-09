package com.android.shelfLife.ui.utils

import com.google.firebase.Timestamp

/**
 * converts a Timestamp into an Int representing the minutes
 *
 * @param timestamp: the timestamp we want to convert
 */
fun getTotalMinutes(timestamp: Timestamp): Int {
  return (timestamp.seconds / 60).toInt() // Convert seconds to minutes
}
