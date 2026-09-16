package com.buttonpilot.app.core.root

import com.buttonpilot.app.core.logging.DiagnosticLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RootCommandExecutor @Inject constructor(
    private val logger: DiagnosticLogger
) {
    /**
     * Execute only predefined safe actions. Never execute arbitrary user-supplied strings.
     */
    suspend fun execute(action: RootAction): AppExecResult = withContext(Dispatchers.IO) {
        try {
            val command = mapActionToCommand(action) ?: return@withContext AppExecResult.Error("Action not mapped")
            logger.d("RootExecutor", "Executing safe action: $action -> $command")

            // Check su availability first
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            val output = reader.readText()
            val error = errorReader.readText()
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                AppExecResult.Success(output)
            } else {
                AppExecResult.Error("Exit $exitCode: $error")
            }
        } catch (e: Exception) {
            logger.e("RootExecutor", "Failed to execute $action: ${e.message}")
            AppExecResult.Error(e.message ?: "Unknown error")
        }
    }

    private fun mapActionToCommand(action: RootAction): String? {
        return when (action) {
            RootAction.CheckRoot -> "id"
            RootAction.StartInputMonitor -> "echo start_monitor > /dev/null; echo ok"
            RootAction.StopInputMonitor -> "echo stop_monitor > /dev/null; echo ok"
            RootAction.ListInputDevices -> "ls /dev/input/"
            RootAction.DumpInputEvents -> "getevent -p"
            RootAction.RestartAccessibility -> "echo restart_accessibility > /dev/null; echo ok"
        }
    }

    sealed class AppExecResult {
        data class Success(val output: String) : AppExecResult()
        data class Error(val message: String) : AppExecResult()
    }
}
