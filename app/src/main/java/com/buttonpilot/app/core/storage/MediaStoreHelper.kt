package com.buttonpilot.app.core.storage

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.buttonpilot.app.core.common.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun generateFileName(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
        return "${Constants.RECORDING_FILE_PREFIX}${sdf.format(Date())}${Constants.RECORDING_FILE_EXTENSION}"
    }

    fun getRecordingDirectory(): File {
        val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        val dir = File(musicDir, "ButtonPilot")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun createRecordingFile(): File {
        return File(getRecordingDirectory(), generateFileName())
    }

    fun addToMediaStore(file: File): Uri? {
        return try {
            val values = ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, file.name)
                put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp4")
                put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/ButtonPilot")
                put(MediaStore.Audio.Media.IS_PENDING, 0)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Audio.Media.IS_MUSIC, 0)
                }
            }
            context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
        } catch (e: Exception) {
            null
        }
    }

    fun getFileSize(file: File): Long {
        return if (file.exists()) file.length() else 0L
    }
}
