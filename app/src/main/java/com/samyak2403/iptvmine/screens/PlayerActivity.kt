package com.samyak2403.iptvmine.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Util
import com.samyak2403.iptvmine.R
import com.samyak2403.iptvmine.model.Channel

class PlayerActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private lateinit var imageViewFullScreen: ImageView
    private lateinit var imageViewLock: ImageView
    private lateinit var linearLayoutControlUp: LinearLayout
    private lateinit var linearLayoutControlBottom: LinearLayout
    private lateinit var channel: Channel
    private var playbackPosition = 0L
    private var isPlayerReady = false
    private var isFullScreen = false
    private var isLock = false

    companion object {
        private const val INCREMENT_MILLIS = 5000L

        fun start(context: Context, channel: Channel) {
            val intent = Intent(context, PlayerActivity::class.java).apply {
                putExtra("channel", channel)
            }
            context.startActivity(intent)
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        // Keep the screen on while playing video
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Retrieve channel from savedInstanceState or intent
        channel = savedInstanceState?.getParcelable("channel") ?: intent.getParcelableExtra("channel") ?: return

        setFindViewById()
        setupPlayer()
        setLockScreen()
        setFullScreen()
    }

    private fun setFindViewById() {
        playerView = findViewById(R.id.playerView)
        progressBar = findViewById(R.id.progressBar)
        errorTextView = findViewById(R.id.errorTextView)
        imageViewFullScreen = findViewById(R.id.imageViewFullScreen)
        imageViewLock = findViewById(R.id.imageViewLock)
        linearLayoutControlUp = findViewById(R.id.linearLayoutControlUp)
        linearLayoutControlBottom = findViewById(R.id.linearLayoutControlBottom)
    }

    private fun setupPlayer() {
        player = ExoPlayer.Builder(this).setSeekBackIncrementMs(INCREMENT_MILLIS)
            .setSeekForwardIncrementMs(INCREMENT_MILLIS)
            .build().also { exoPlayer ->
                playerView.player = exoPlayer

                val mediaItem = MediaItem.fromUri(Uri.parse(channel.streamUrl))
                val mediaSource = HlsMediaSource.Factory(DefaultHttpDataSource.Factory()).createMediaSource(mediaItem)
                exoPlayer.setMediaSource(mediaSource)
                exoPlayer.seekTo(playbackPosition)
                exoPlayer.playWhenReady = true
                exoPlayer.prepare()

                exoPlayer.addListener(object : Player.Listener {
                    override fun onIsLoadingChanged(isLoading: Boolean) {
                        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        errorTextView.visibility = View.VISIBLE
                        playerView.visibility = View.GONE
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_READY && !isPlayerReady) {
                            isPlayerReady = true
                            progressBar.visibility = View.GONE
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        // Optionally handle UI changes based on playback state
                    }
                })
            }
    }

    private fun lockScreen(lock: Boolean) {
        linearLayoutControlUp.visibility = if (lock) View.INVISIBLE else View.VISIBLE
        linearLayoutControlBottom.visibility = if (lock) View.INVISIBLE else View.VISIBLE
    }

    private fun setLockScreen() {
        imageViewLock.setOnClickListener {
            isLock = !isLock
            imageViewLock.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    if (isLock) R.drawable.ic_baseline_lock else R.drawable.ic_baseline_lock_open
                )
            )
            lockScreen(isLock)
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun setFullScreen() {
        imageViewFullScreen.setOnClickListener {
            isFullScreen = !isFullScreen
            imageViewFullScreen.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    if (isFullScreen) R.drawable.ic_baseline_fullscreen_exit else R.drawable.ic_baseline_fullscreen
                )
            )
            requestedOrientation = if (isFullScreen) {
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
            supportActionBar?.hide()
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            supportActionBar?.show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("channel", channel)
        playbackPosition = player.currentPosition
        outState.putLong("playbackPosition", playbackPosition)
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            player.playWhenReady = true
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 || !isPlayerReady) {
            player.playWhenReady = true
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            playbackPosition = player.currentPosition
            player.playWhenReady = false
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            playbackPosition = player.currentPosition
            player.playWhenReady = false
        }
        player.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (isLock) return
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            imageViewFullScreen.performClick()
        } else super.onBackPressed()
    }
}
