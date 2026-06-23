package com.example.playx.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.playx.data.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ConfigViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = PreferencesManager(application)

    private val _imageUri = MutableStateFlow<String?>(null)
    val imageUri: StateFlow<String?> = _imageUri

    private val _videoUri = MutableStateFlow<String?>(null)
    val videoUri: StateFlow<String?> = _videoUri

    private val _noCoverMode = MutableStateFlow(false)
    val noCoverMode: StateFlow<Boolean> = _noCoverMode

    private val _repeatMode = MutableStateFlow(2) // Player.REPEAT_MODE_ALL
    val repeatMode: StateFlow<Int> = _repeatMode

    private val _adminPassword = MutableStateFlow("2026")
    val adminPassword: StateFlow<String> = _adminPassword

    init {
        viewModelScope.launch {
            _imageUri.value = prefsManager.imageUri.first()
            _videoUri.value = prefsManager.videoUri.first()
            _noCoverMode.value = prefsManager.noCoverMode.first()
            _repeatMode.value = prefsManager.repeatMode.first()
            _adminPassword.value = prefsManager.adminPassword.first()
        }
    }

    fun updateImageUri(uri: Uri, context: Context) {
        context.contentResolver.takePersistableUriPermission(
            uri,
            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        val uriString = uri.toString()
        _imageUri.value = uriString
        viewModelScope.launch {
            prefsManager.setImageUri(uriString)
        }
    }

    fun updateVideoUri(uri: Uri, context: Context) {
        context.contentResolver.takePersistableUriPermission(
            uri,
            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        val uriString = uri.toString()
        _videoUri.value = uriString
        viewModelScope.launch {
            prefsManager.setVideoUri(uriString)
        }
    }

    fun clearImageUri() {
        _imageUri.value = null
        viewModelScope.launch {
            prefsManager.setImageUri(null)
        }
    }

    fun clearVideoUri() {
        _videoUri.value = null
        viewModelScope.launch {
            prefsManager.setVideoUri(null)
        }
    }

    fun setNoCoverMode(enabled: Boolean) {
        _noCoverMode.value = enabled
        viewModelScope.launch {
            prefsManager.setNoCoverMode(enabled)
            // If no cover mode is enabled and repeat mode is OFF, force it to ALL
            if (enabled && _repeatMode.value == 0) { // Player.REPEAT_MODE_OFF
                setRepeatMode(2) // Player.REPEAT_MODE_ALL
            }
        }
    }

    fun setRepeatMode(mode: Int) {
        _repeatMode.value = mode
        viewModelScope.launch {
            prefsManager.setRepeatMode(mode)
        }
    }

    fun updateAdminPassword(password: String) {
        _adminPassword.value = password
        viewModelScope.launch {
            prefsManager.setAdminPassword(password)
        }
    }
}
