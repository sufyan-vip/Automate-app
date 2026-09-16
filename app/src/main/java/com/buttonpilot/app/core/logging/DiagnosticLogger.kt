package com.buttonpilot.app.core.logging

import com.buttonpilot.app.core.common.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class LogEntry(
    val timestamp: Long,
    val level: String,
    val tag: String,
    val message: String
) {
    fun formatted(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
        return "${sdf.format(Date(timestamp))} [$level] $tag: $message"
    }
}

@Singleton
class DiagnosticLogger @Inject constructor() {
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private val lock = Any()

    fun log(level: String, tag: String, message: String) {
        val entry = LogEntry(System.currentTimeMillis(), level, tag, message)
        synchronized(lock) {
            val current = _logs.value.toMutableList()
            current.add(0, entry)
            if (current.size > Constants.DIAGNOSTIC_LOG_MAX_SIZE) {
                current.removeAt(current.lastIndex)
            }
            _logs.value = current
        }
    }

    fun d(tag: String, message: String) = log("DEBUG", tag, message)
    fun i(tag: String, message: String) = log("INFO", tag, message)
    fun w(tag: String, message: String) = log("WARN", tag, message)
    fun e(tag: String, message: String) = log("ERROR", tag, message)

    fun getExportText(): String {
        return _logs.value.reversed().joinToString("\n") { it.formatted() }
    }

    fun clear() {
        _logs.value = emptyList()
    }
}
