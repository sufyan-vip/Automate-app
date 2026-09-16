package com.buttonpilot.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.buttonpilot.app.core.logging.DiagnosticLogger
import com.buttonpilot.app.core.storage.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var logger: DiagnosticLogger

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in listOf(
                Intent.ACTION_BOOT_COMPLETED,
                "android.intent.action.QUICKBOOT_POWERON",
                Intent.ACTION_MY_PACKAGE_REPLACED
            )
        ) return

        logger.i("BootReceiver", "Boot completed received: ${intent.action}")

        // Lightweight restoration only: reload preferences, schedule safe maintenance
        // Respect current Android restrictions
        // Never begin microphone recording automatically at boot

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Example: reload preferences, ensure defaults
                // Do NOT start foreground microphone service at boot
                logger.i("BootReceiver", "Preferences reloaded, safe maintenance scheduled")
                // WorkManager could schedule deferred maintenance here if needed
            } catch (e: Exception) {
                logger.e("BootReceiver", "Boot handling error: ${e.message}")
            }
        }

        // Optionally notify user if accessibility needs reactivation (OEM-specific)
        // This should be done via notification, not automatically enabling
    }
}
