package com.buttonpilot.app.core.root

sealed class RootResult {
    data object Available : RootResult()
    data object NotAvailable : RootResult()
    data class Error(val message: String) : RootResult()
    data object Denied : RootResult()
}
