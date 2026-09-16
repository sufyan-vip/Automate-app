package com.buttonpilot.app.service.recording

sealed class RecordingState {
    data object Idle : RecordingState()
    data object Preparing : RecordingState()
    data class Recording(val startTime: Long, val elapsedMs: Long = 0) : RecordingState()
    data class Paused(val elapsedMs: Long) : RecordingState()
    data object Stopping : RecordingState()
    data class Saved(val filePath: String, val durationMs: Long) : RecordingState()
    data class Error(val message: String) : RecordingState()

    fun isRecording(): Boolean = this is Recording
    fun isIdle(): Boolean = this is Idle || this is Saved || this is Error
}
