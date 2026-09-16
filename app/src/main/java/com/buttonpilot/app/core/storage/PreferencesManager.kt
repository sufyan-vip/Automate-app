package com.buttonpilot.app.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.buttonpilot.app.core.common.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.PREFS_DATASTORE_NAME)

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val DOUBLE_PRESS_INTERVAL = longPreferencesKey("double_press_interval")
        val TRIPLE_PRESS_INTERVAL = longPreferencesKey("triple_press_interval")
        val SEQUENCE_TIMEOUT = longPreferencesKey("sequence_timeout")
        val LONG_PRESS_DURATION = longPreferencesKey("long_press_duration")
        val COOLDOWN = longPreferencesKey("cooldown")
        val AUDIO_QUALITY = stringPreferencesKey("audio_quality")
        val STORAGE_LOCATION = stringPreferencesKey("storage_location")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val SOUND_FEEDBACK = booleanPreferencesKey("sound_feedback")
        val ROOT_ENHANCED_ENABLED = booleanPreferencesKey("root_enhanced_enabled")
        val SHORTCUTS_ENABLED = booleanPreferencesKey("shortcuts_enabled")
        val HISTORY_ENABLED = booleanPreferencesKey("history_enabled")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
    }

    val doublePressInterval: Flow<Long> = context.dataStore.data.map { it[Keys.DOUBLE_PRESS_INTERVAL] ?: Constants.DEFAULT_DOUBLE_PRESS_INTERVAL_MS }
    val triplePressInterval: Flow<Long> = context.dataStore.data.map { it[Keys.TRIPLE_PRESS_INTERVAL] ?: Constants.DEFAULT_TRIPLE_PRESS_INTERVAL_MS }
    val sequenceTimeout: Flow<Long> = context.dataStore.data.map { it[Keys.SEQUENCE_TIMEOUT] ?: Constants.DEFAULT_SEQUENCE_TIMEOUT_MS }
    val longPressDuration: Flow<Long> = context.dataStore.data.map { it[Keys.LONG_PRESS_DURATION] ?: Constants.DEFAULT_LONG_PRESS_DURATION_MS }
    val cooldown: Flow<Long> = context.dataStore.data.map { it[Keys.COOLDOWN] ?: Constants.DEFAULT_COOLDOWN_MS }
    val audioQuality: Flow<String> = context.dataStore.data.map { it[Keys.AUDIO_QUALITY] ?: "HIGH" }
    val vibrationEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.VIBRATION_ENABLED] ?: true }
    val hapticEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.HAPTIC_ENABLED] ?: true }
    val rootEnhancedEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.ROOT_ENHANCED_ENABLED] ?: false }
    val shortcutsEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.SHORTCUTS_ENABLED] ?: true }
    val historyEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.HISTORY_ENABLED] ?: true }
    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { it[Keys.ONBOARDING_COMPLETED] ?: false }

    suspend fun setDoublePressInterval(value: Long) = context.dataStore.edit { it[Keys.DOUBLE_PRESS_INTERVAL] = value }
    suspend fun setTriplePressInterval(value: Long) = context.dataStore.edit { it[Keys.TRIPLE_PRESS_INTERVAL] = value }
    suspend fun setSequenceTimeout(value: Long) = context.dataStore.edit { it[Keys.SEQUENCE_TIMEOUT] = value }
    suspend fun setLongPressDuration(value: Long) = context.dataStore.edit { it[Keys.LONG_PRESS_DURATION] = value }
    suspend fun setCooldown(value: Long) = context.dataStore.edit { it[Keys.COOLDOWN] = value }
    suspend fun setAudioQuality(value: String) = context.dataStore.edit { it[Keys.AUDIO_QUALITY] = value }
    suspend fun setVibrationEnabled(value: Boolean) = context.dataStore.edit { it[Keys.VIBRATION_ENABLED] = value }
    suspend fun setHapticEnabled(value: Boolean) = context.dataStore.edit { it[Keys.HAPTIC_ENABLED] = value }
    suspend fun setRootEnhancedEnabled(value: Boolean) = context.dataStore.edit { it[Keys.ROOT_ENHANCED_ENABLED] = value }
    suspend fun setShortcutsEnabled(value: Boolean) = context.dataStore.edit { it[Keys.SHORTCUTS_ENABLED] = value }
    suspend fun setHistoryEnabled(value: Boolean) = context.dataStore.edit { it[Keys.HISTORY_ENABLED] = value }
    suspend fun setOnboardingCompleted(value: Boolean) = context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = value }
}
