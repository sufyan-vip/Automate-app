package com.buttonpilot.app.core.root

interface RootController {
    suspend fun isRootAvailable(): Boolean
    suspend fun requestRoot(): RootResult
    suspend fun executeSafe(action: RootAction): RootCommandExecutor.AppExecResult
    fun isRootEnhancedModeEnabled(): Boolean
}
