import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.android.shelfLife.R

object AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null

    fun startRatAudio(context: Context) {
        stopAudio() // ensure any playing audio is stopped first
        Log.d("AudioPlayer", "Playing rat audio")
        mediaPlayer = MediaPlayer.create(context, R.raw.rat_song)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }

    fun startStinkyAudio(context: Context) {
        stopAudio()
        mediaPlayer = MediaPlayer.create(context, R.raw.stinky_song)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }

    fun stopAudio() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
