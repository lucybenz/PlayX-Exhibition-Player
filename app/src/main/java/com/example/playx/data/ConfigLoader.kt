package com.example.playx.data

import android.content.Context
import android.net.Uri
import android.util.Log
import org.json.JSONObject
import java.io.File

class ConfigLoader(private val context: Context, private val prefsManager: PreferencesManager) {

    suspend fun loadConfigFromFile() {
        // Path: /sdcard/Android/data/com.example.playx/files/config.json
        val configFile = File(context.getExternalFilesDir(null), "config.json")
        
        if (!configFile.exists()) {
            Log.d("ConfigLoader", "Config file not found at ${configFile.absolutePath}")
            return
        }

        try {
            val jsonString = configFile.readText()
            val json = JSONObject(jsonString)

            if (json.has("no_cover_mode")) {
                prefsManager.setNoCoverMode(json.getBoolean("no_cover_mode"))
            }

            if (json.has("repeat_mode")) {
                prefsManager.setRepeatMode(json.getInt("repeat_mode"))
            }

            // Note: For video/image paths, we try to convert them to URIs
            // If the user provides a full path like "/sdcard/Download/v.mp4", 
            // it might still fail due to permissions, but we'll try.
            if (json.has("video_path")) {
                val path = json.getString("video_path")
                val uri = Uri.fromFile(File(path)).toString()
                prefsManager.setVideoUri(uri)
            }

            if (json.has("image_path")) {
                val path = json.getString("image_path")
                val uri = Uri.fromFile(File(path)).toString()
                prefsManager.setImageUri(uri)
            }

            Log.d("ConfigLoader", "Config loaded successfully from ${configFile.absolutePath}")
        } catch (e: Exception) {
            Log.e("ConfigLoader", "Error reading config file", e)
        }
    }
}
