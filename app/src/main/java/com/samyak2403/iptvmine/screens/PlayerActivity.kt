/**
 * Created by Samyak Kamble on 8/14/24, 11:38 AM
 * Copyright (c) 2024. All rights reserved.
 * Last modified 8/14/24, 11:38 AM
 */

package com.samyak2403.iptvmine.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.util.Util
import com.samyak2403.iptvmine.R
import com.samyak2403.iptvmine.model.Channel  // Ensure this is the correct import

class PlayerActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private lateinit var channel: Channel
    private var isPlayerReady = false

    companion object {
        fun start(context: Context, channel: Channel) {  // Ensure this uses the correct Channel class
            val intent = Intent(context, PlayerActivity::class.java).apply {
                putExtra("channel", channel)  // Ensure Channel implements Parcelable
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        // Keep the screen on while playing video
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        channel = intent.getParcelableExtra("channel") ?: return

        playerView = findViewById(R.id.playerView)
        progressBar = findViewById(R.id.progressBar)
        errorTextView = findViewById(R.id.errorTextView)

        setupPlayer()
    }

    private fun setupPlayer() {
        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            playerView.player = exoPlayer

            val mediaItem = MediaItem.fromUri(Uri.parse(channel.streamUrl))
            exoPlayer.setMediaItem(mediaItem)
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
            player.playWhenReady = false
            player.release()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            player.playWhenReady = false
            player.release()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}

//
//package com.samyak2403.iptv.screens
//
//import android.content.Context
//import android.content.Intent
//import android.net.Uri
//import android.os.Bundle
//import android.view.View
//import android.view.WindowManager
//import android.widget.ProgressBar
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//import com.google.android.exoplayer2.ExoPlayer
//import com.google.android.exoplayer2.MediaItem
//import com.google.android.exoplayer2.PlaybackException
//import com.google.android.exoplayer2.Player
//import com.google.android.exoplayer2.ui.PlayerView
//import com.google.android.exoplayer2.util.Util
//import com.samyak2403.iptv.R
//import com.samyak2403.iptv.model.Channel
//
//class PlayerActivity : AppCompatActivity() {
//
//    private lateinit var player: ExoPlayer
//    private lateinit var playerView: PlayerView
//    private lateinit var progressBar: ProgressBar
//    private lateinit var errorTextView: TextView
//    private lateinit var channel: Channel
//    private var isPlayerReady = false
//
//    companion object {
//        fun start(context: Context, channel: com.samyak2403.iptv.provider.Channel) {
//            val intent = Intent(context, PlayerActivity::class.java).apply {
//                putExtra("channel", channel)
//            }
//            context.startActivity(intent)
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_player)
//
//        // Keep the screen on while playing video
//        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//
//        channel = intent.getParcelableExtra("channel") ?: return
//
//        playerView = findViewById(R.id.playerView)
//        progressBar = findViewById(R.id.progressBar)
//        errorTextView = findViewById(R.id.errorTextView)
//
//        setupPlayer()
//    }
//
//    private fun setupPlayer() {
//        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
//            playerView.player = exoPlayer
//
//            val mediaItem = MediaItem.fromUri(Uri.parse(channel.streamUrl))
//            exoPlayer.setMediaItem(mediaItem)
//            exoPlayer.prepare()
//
//            exoPlayer.addListener(object : Player.Listener {
//                override fun onIsLoadingChanged(isLoading: Boolean) {
//                    progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
//                }
//
//                override fun onPlayerError(error: PlaybackException) {
//                    errorTextView.visibility = View.VISIBLE
//                    playerView.visibility = View.GONE
//                }
//
//                override fun onPlaybackStateChanged(playbackState: Int) {
//                    if (playbackState == Player.STATE_READY && !isPlayerReady) {
//                        isPlayerReady = true
//                        progressBar.visibility = View.GONE
//                    }
//                }
//
//                override fun onIsPlayingChanged(isPlaying: Boolean) {
//                    // Optionally handle UI changes based on playback state
//                }
//            })
//        }
//    }
//
//    override fun onStart() {
//        super.onStart()
//        if (Util.SDK_INT > 23) {
//            player.playWhenReady = true
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        if (Util.SDK_INT <= 23 || !isPlayerReady) {
//            player.playWhenReady = true
//        }
//    }
//
//    override fun onPause() {
//        super.onPause()
//        if (Util.SDK_INT <= 23) {
//            player.playWhenReady = false
//            player.release()
//        }
//    }
//
//    override fun onStop() {
//        super.onStop()
//        if (Util.SDK_INT > 23) {
//            player.playWhenReady = false
//            player.release()
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        player.release()
//    }
//}
