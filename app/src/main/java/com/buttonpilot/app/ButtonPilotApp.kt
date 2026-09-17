package com.buttonpilot.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.buttonpilot.app.R
import com.buttonpilot.app.core.common.Constants
import com.buttonpilot.app.data.local.AppDatabase
import com.buttonpilot.app.data.local.ShortcutRuleEntity
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class ButtonPilotApp : Application() {

    @Inject
    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        seedDefaultShortcuts()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val recordingChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_RECORDING_ID,
                getString(R.string.notification_channel_recording),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_recording_desc)
            }
            val generalChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_GENERAL_ID,
                getString(R.string.notification_channel_general),
                NotificationManager.IMPORTANCE_HIGH
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(recordingChannel)
            nm.createNotificationChannel(generalChannel)
        }
    }

    private fun seedDefaultShortcuts() {
        CoroutineScope(Dispatchers.IO).launch {
            val dao = database.shortcutRuleDao()
            val existing = dao.getEnabled()
            if (existing.isNotEmpty()) return@launch

            val defaults = listOf(
                ShortcutRuleEntity(
                    name = "Volume Down ×3 → Toggle Voice Recorder",
                    triggerKey = "VOLUME_DOWN",
                    triggerPattern = "TRIPLE",
                    actionType = "ToggleRecording",
                    actionData = null,
                    enabled = true,
                    timeWindowMs = 1100,
                    cooldownMs = 1500
                ),
                ShortcutRuleEntity(
                    name = "Volume Up ×2 → Flashlight Toggle",
                    triggerKey = "VOLUME_UP",
                    triggerPattern = "DOUBLE",
                    actionType = "ToggleFlashlight",
                    actionData = null,
                    enabled = false,
                    timeWindowMs = 1100,
                    cooldownMs = 1500
                ),
                ShortcutRuleEntity(
                    name = "Volume Up ×3 → Open Camera",
                    triggerKey = "VOLUME_UP",
                    triggerPattern = "TRIPLE",
                    actionType = "OpenCamera",
                    actionData = null,
                    enabled = false,
                    timeWindowMs = 1100,
                    cooldownMs = 1500
                ),
                ShortcutRuleEntity(
                    name = "Volume Down ×2 → Media Play/Pause",
                    triggerKey = "VOLUME_DOWN",
                    triggerPattern = "DOUBLE",
                    actionType = "MediaPlayPause",
                    actionData = null,
                    enabled = false,
                    timeWindowMs = 1100,
                    cooldownMs = 1500
                ),
                ShortcutRuleEntity(
                    name = "Volume Up then Volume Down → Launch App",
                    triggerKey = "VOLUME_BOTH",
                    triggerPattern = "UP_THEN_DOWN",
                    actionType = "LaunchApp",
                    actionData = null,
                    enabled = false,
                    timeWindowMs = 1500,
                    cooldownMs = 1500
                )
            )
            defaults.forEach { dao.insert(it) }
        }
    }
}
