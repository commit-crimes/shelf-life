import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.android.shelfLife.R

/**
 * A utility object to handle audio playback for specific audio resources in the Shelf Life app.
 *
 * This object provides methods to play looping audio tracks and ensures proper resource management
 * by stopping and releasing the media player when necessary.
 */
object AudioPlayer {
  private var mediaPlayer: MediaPlayer? = null

  /**
   * Starts playing the "rat" audio track in a loop.
   *
   * If there is already an audio track playing, it stops the current track before starting the new one.
   *
   * @param context The application context used to access the audio resource.
   */
  fun startRatAudio(context: Context) {
    stopAudio() // Ensure any playing audio is stopped first
    Log.d("AudioPlayer", "Playing rat audio")
    mediaPlayer = MediaPlayer.create(context, R.raw.rat_song).apply {
      isLooping = true // Set the audio to loop
      start() // Start playback
    }
  }

  /**
   * Starts playing the "stinky" audio track in a loop.
   *
   * If there is already an audio track playing, it stops the current track before starting the new one.
   *
   * @param context The application context used to access the audio resource.
   */
  fun startStinkyAudio(context: Context) {
    stopAudio() // Ensure any playing audio is stopped first
    Log.d("AudioPlayer", "Playing stinky audio")
    mediaPlayer = MediaPlayer.create(context, R.raw.stinky_song).apply {
      isLooping = true // Set the audio to loop
      start() // Start playback
    }
  }

  /**
   * Stops any currently playing audio and releases the media player resources.
   *
   * This method ensures proper resource cleanup to avoid memory leaks or playback issues.
   */
  fun stopAudio() {
    mediaPlayer?.stop() // Stop the current audio track
    mediaPlayer?.release() // Release the media player resources
    mediaPlayer = null // Set the media player reference to null
    Log.d("AudioPlayer", "Audio playback stopped")
  }
}