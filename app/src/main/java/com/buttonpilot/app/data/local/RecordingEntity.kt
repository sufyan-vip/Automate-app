package com.buttonpilot.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recordings")
data class RecordingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val filename: String,
    val path: String,
    val uri: String,
    val createdAt: Long,
    val duration: Long,
    val size: Long,
    val sourceShortcut: String
)
