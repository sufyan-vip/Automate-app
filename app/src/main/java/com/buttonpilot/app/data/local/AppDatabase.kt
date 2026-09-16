package com.buttonpilot.app.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordingDao {
    @Query("SELECT * FROM recordings ORDER BY createdAt DESC")
    fun getAll(): Flow<List<RecordingEntity>>

    @Query("SELECT * FROM recordings ORDER BY createdAt DESC")
    suspend fun getAllSync(): List<RecordingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RecordingEntity): Long

    @Delete
    suspend fun delete(entity: RecordingEntity)

    @Query("DELETE FROM recordings WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM recordings WHERE id = :id")
    suspend fun getById(id: Long): RecordingEntity?
}

@Dao
interface ShortcutRuleDao {
    @Query("SELECT * FROM shortcut_rules ORDER BY id ASC")
    fun getAll(): Flow<List<ShortcutRuleEntity>>

    @Query("SELECT * FROM shortcut_rules WHERE enabled = 1")
    suspend fun getEnabled(): List<ShortcutRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ShortcutRuleEntity): Long

    @Update
    suspend fun update(entity: ShortcutRuleEntity)

    @Delete
    suspend fun delete(entity: ShortcutRuleEntity)

    @Query("SELECT * FROM shortcut_rules WHERE id = :id")
    suspend fun getById(id: Long): ShortcutRuleEntity?
}

@Dao
interface ActionHistoryDao {
    @Query("SELECT * FROM action_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int = 100): Flow<List<ActionHistoryEntity>>

    @Insert
    suspend fun insert(entity: ActionHistoryEntity)

    @Query("DELETE FROM action_history")
    suspend fun clear()
}

@Database(
    entities = [RecordingEntity::class, ShortcutRuleEntity::class, ActionHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordingDao(): RecordingDao
    abstract fun shortcutRuleDao(): ShortcutRuleDao
    abstract fun actionHistoryDao(): ActionHistoryDao
}
