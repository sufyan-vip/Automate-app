package com.buttonpilot.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shortcut_rules")
data class ShortcutRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val triggerKey: String,
    val triggerPattern: String,
    val actionType: String,
    val actionData: String?,
    val enabled: Boolean,
    val timeWindowMs: Long,
    val cooldownMs: Long
)
