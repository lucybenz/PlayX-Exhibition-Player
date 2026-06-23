package com.example.playx.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.FileNotFoundException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "playx_prefs")

class PreferencesManager(private val context: Context) {

    val imageUri: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_IMAGE_URI]
    }

    val videoUri: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_VIDEO_URI]
    }

    val noCoverMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_NO_COVER_MODE] ?: false
    }

    val repeatMode: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_REPEAT_MODE] ?: 2 // Default to Player.REPEAT_MODE_ALL
    }

    val adminPassword: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_ADMIN_PASSWORD] ?: "2026"
    }

    suspend fun setAdminPassword(password: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ADMIN_PASSWORD] = password
        }
    }

    suspend fun setImageUri(uri: String?) {
        context.dataStore.edit { prefs ->
            if (uri != null) {
                prefs[KEY_IMAGE_URI] = uri
            } else {
                prefs.remove(KEY_IMAGE_URI)
            }
        }
    }

    suspend fun setVideoUri(uri: String?) {
        context.dataStore.edit { prefs ->
            if (uri != null) {
                prefs[KEY_VIDEO_URI] = uri
            } else {
                prefs.remove(KEY_VIDEO_URI)
            }
        }
    }

    suspend fun setNoCoverMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_NO_COVER_MODE] = enabled
        }
    }

    suspend fun setRepeatMode(mode: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_REPEAT_MODE] = mode
        }
    }

    fun isUriAccessible(uri: String): Boolean {
        return try {
            context.contentResolver.openInputStream(android.net.Uri.parse(uri))?.close()
            true
        } catch (e: FileNotFoundException) {
            false
        } catch (e: SecurityException) {
            false
        }
    }

    companion object {
        private val KEY_IMAGE_URI = stringPreferencesKey("image_uri")
        private val KEY_VIDEO_URI = stringPreferencesKey("video_uri")
        private val KEY_NO_COVER_MODE = booleanPreferencesKey("no_cover_mode")
        private val KEY_REPEAT_MODE = intPreferencesKey("repeat_mode")
        private val KEY_ADMIN_PASSWORD = stringPreferencesKey("admin_password")
    }
}