package com.example.playx.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.playx.data.PreferencesManager
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.playx.data.ConfigLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class ScreenMode { COVER, VIDEO }

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = PreferencesManager(application)
    private val configLoader = ConfigLoader(application, prefsManager)

    var screenMode: ScreenMode by mutableStateOf(ScreenMode.COVER)
        private set

    private var exoPlayer: ExoPlayer? = null
    private var hasAutoStarted = false

    val uiState: StateFlow<MainUiState> = combine(
        prefsManager.imageUri,
        prefsManager.videoUri,
        prefsManager.noCoverMode,
        prefsManager.repeatMode
    ) { imageUri, videoUri, noCoverMode, repeatMode ->
        withContext(Dispatchers.IO) {
            val imageOk = imageUri != null && prefsManager.isUriAccessible(imageUri)
            val videoOk = videoUri != null && prefsManager.isUriAccessible(videoUri)
            MainUiState(
                imageUri = if (imageOk) imageUri else null,
                videoUri = if (videoOk) videoUri else null,
                noCoverMode = noCoverMode,
                repeatMode = repeatMode,
                isConfigured = (imageOk || noCoverMode) && videoOk
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainUiState())

    val adminPassword = prefsManager.adminPassword

    init {
        // Load external config file if it exists
        viewModelScope.launch {
            configLoader.loadConfigFromFile()
        }

        // Auto-start logic handled in UI layer (MainScreen) for better experience
    }

    fun onAutoStartTriggered() {
        if (screenMode == ScreenMode.COVER && uiState.value.isConfigured && !hasAutoStarted) {
            screenMode = ScreenMode.VIDEO
            hasAutoStarted = true
        }
    }

    fun onCoverTapped() {
        if (screenMode == ScreenMode.COVER && uiState.value.isConfigured) {
            screenMode = ScreenMode.VIDEO
        }
    }

    fun onBackPressed(): Boolean {
        return if (screenMode == ScreenMode.VIDEO) {
            releasePlayer()
            screenMode = ScreenMode.COVER
            hasAutoStarted = false // Reset so it can auto-start again after 4s
            true
        } else {
            false
        }
    }

    fun getOrCreatePlayer(): ExoPlayer? {
        val uri = uiState.value.videoUri ?: return null
        return exoPlayer ?: ExoPlayer.Builder(getApplication()).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(uri)))
            repeatMode = uiState.value.repeatMode
            playWhenReady = true
            prepare()
            exoPlayer = this
        }
    }

    val player: ExoPlayer? get() = exoPlayer

    fun togglePlayPause() {
        exoPlayer?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
    }

    fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
    }

    fun updateAdminPassword(newPassword: String) {
        viewModelScope.launch {
            prefsManager.setAdminPassword(newPassword)
        }
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }
}

data class MainUiState(
    val imageUri: String? = null,
    val videoUri: String? = null,
    val noCoverMode: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_ALL,
    val isConfigured: Boolean = false
)