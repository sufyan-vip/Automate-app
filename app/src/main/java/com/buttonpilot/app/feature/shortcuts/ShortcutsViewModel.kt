package com.buttonpilot.app.feature.shortcuts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buttonpilot.app.data.local.AppDatabase
import com.buttonpilot.app.data.local.ShortcutRuleEntity
import com.buttonpilot.app.feature.automation.ShortcutAction
import com.buttonpilot.app.feature.automation.TriggerKey
import com.buttonpilot.app.feature.automation.TriggerPattern
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShortcutsViewModel @Inject constructor(
    private val database: AppDatabase
) : ViewModel() {

    val shortcuts = database.shortcutRuleDao().getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleEnabled(entity: ShortcutRuleEntity) {
        viewModelScope.launch {
            database.shortcutRuleDao().update(entity.copy(enabled = !entity.enabled))
        }
    }

    fun delete(entity: ShortcutRuleEntity) {
        viewModelScope.launch {
            database.shortcutRuleDao().delete(entity)
        }
    }

    fun createShortcut(name: String, triggerKey: TriggerKey, pattern: TriggerPattern, action: ShortcutAction) {
        viewModelScope.launch {
            val entity = ShortcutRuleEntity(
                name = name,
                triggerKey = triggerKey.name,
                triggerPattern = pattern.displayName(),
                actionType = action::class.simpleName ?: "Unknown",
                actionData = when (action) {
                    is ShortcutAction.LaunchApp -> action.packageName
                    is ShortcutAction.OpenDialer -> action.number
                    else -> null
                },
                enabled = true,
                timeWindowMs = 1100,
                cooldownMs = 1500
            )
            database.shortcutRuleDao().insert(entity)
        }
    }
}
