package com.buttonpilot.app.core.common

object Constants {
    const val DEFAULT_DOUBLE_PRESS_INTERVAL_MS = 450L
    const val DEFAULT_TRIPLE_PRESS_INTERVAL_MS = 450L
    const val DEFAULT_SEQUENCE_TIMEOUT_MS = 1100L
    const val DEFAULT_LONG_PRESS_DURATION_MS = 600L
    const val DEFAULT_COOLDOWN_MS = 1500L

    const val RECORDING_DIR = "Music/ButtonPilot"
    const val RECORDING_FILE_PREFIX = "BP_REC_"
    const val RECORDING_FILE_EXTENSION = ".m4a"

    const val NOTIFICATION_CHANNEL_RECORDING_ID = "recording_channel"
    const val NOTIFICATION_CHANNEL_GENERAL_ID = "general_channel"
    const val NOTIFICATION_ID_RECORDING = 1001
    const val NOTIFICATION_ID_GENERAL = 1002

    const val ACTION_STOP_RECORDING = "com.buttonpilot.app.action.STOP_RECORDING"
    const val ACTION_PAUSE_RECORDING = "com.buttonpilot.app.action.PAUSE_RECORDING"
    const val ACTION_RESUME_RECORDING = "com.buttonpilot.app.action.RESUME_RECORDING"
    const val ACTION_START_RECORDING = "com.buttonpilot.app.action.START_RECORDING"

    const val PREFS_DATASTORE_NAME = "button_pilot_prefs"
    const val DB_NAME = "button_pilot_db"

    const val DIAGNOSTIC_LOG_MAX_SIZE = 200
    const val SHORTCUT_HISTORY_MAX = 100
}
