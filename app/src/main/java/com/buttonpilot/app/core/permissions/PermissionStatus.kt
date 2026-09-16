package com.buttonpilot.app.core.permissions

sealed class PermissionStatus {
    data object Granted : PermissionStatus()
    data object NotGranted : PermissionStatus()
    data object PermanentlyDenied : PermissionStatus()
    data object NotApplicable : PermissionStatus()
}

data class PermissionCardState(
    val name: String,
    val description: String,
    val status: PermissionStatus,
    val isRequired: Boolean,
    val isOptional: Boolean = false
)
