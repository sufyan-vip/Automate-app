package com.buttonpilot.app.core.root

import com.buttonpilot.app.core.logging.DiagnosticLogger
import com.buttonpilot.app.core.storage.PreferencesManager
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RootControllerImpl @Inject constructor(
    private val executor: RootCommandExecutor,
    private val preferencesManager: PreferencesManager,
    private val logger: DiagnosticLogger
) : RootController {

    override suspend fun isRootAvailable(): Boolean {
        return try {
            val result = executor.execute(RootAction.CheckRoot)
            when (result) {
                is RootCommandExecutor.AppExecResult.Success -> {
                    logger.i("RootController", "Root check success: ${result.output}")
                    result.output.contains("uid=0")
                }
                is RootCommandExecutor.AppExecResult.Error -> {
                    logger.w("RootController", "Root not available: ${result.message}")
                    false
                }
            }
        } catch (e: Exception) {
            logger.e("RootController", "Root check exception: ${e.message}")
            false
        }
    }

    override suspend fun requestRoot(): RootResult {
        return try {
            val available = isRootAvailable()
            if (available) RootResult.Available else RootResult.NotAvailable
        } catch (e: Exception) {
            RootResult.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun executeSafe(action: RootAction): RootCommandExecutor.AppExecResult {
        if (!isRootEnhancedModeEnabled()) {
            return RootCommandExecutor.AppExecResult.Error("Root Enhanced Mode not enabled")
        }
        return executor.execute(action)
    }

    override fun isRootEnhancedModeEnabled(): Boolean {
        return try {
            kotlinx.coroutines.runBlocking {
                preferencesManager.rootEnhancedEnabled.first()
            }
        } catch (e: Exception) {
            false
        }
    }
}
