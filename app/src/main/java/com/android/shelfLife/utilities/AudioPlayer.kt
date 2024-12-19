import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.android.shelfLife.R

/** Object responsible for playing audio files. */
object AudioPlayer {
  private var mediaPlayer: MediaPlayer? = null

  /**
   * Starts playing the rat audio file in a loop.
   *
   * @param context The context used to access the audio resource.
   */
  fun startRatAudio(context: Context) {
    stopAudio() // ensure any playing audio is stopped first
    Log.d("AudioPlayer", "Playing rat audio")
    mediaPlayer = MediaPlayer.create(context, R.raw.rat_song)
    mediaPlayer?.isLooping = true
    mediaPlayer?.start()
  }

  /**
   * Starts playing the stinky audio file in a loop.
   *
   * @param context The context used to access the audio resource.
   */
  fun startStinkyAudio(context: Context) {
    stopAudio()
    mediaPlayer = MediaPlayer.create(context, R.raw.stinky_song)
    mediaPlayer?.isLooping = true
    mediaPlayer?.start()
  }

  /** Stops any currently playing audio and releases the media player resources. */
  fun stopAudio() {
    mediaPlayer?.stop()
    mediaPlayer?.release()
    mediaPlayer = null
  }
}
