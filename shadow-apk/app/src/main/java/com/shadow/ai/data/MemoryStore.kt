package com.shadow.ai.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// ─── Entities ──────────────────────────────────────────────────────────────

@Entity(tableName = "command_history")
data class CommandRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transcript: String,
    val resolvedAction: String,
    val success: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_preferences")
data class AppPreference(
    @PrimaryKey val packageName: String,
    val displayName: String,
    val usageCount: Int = 0,
    val lastUsed: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_routines")
data class CustomRoutine(
    @PrimaryKey val name: String,
    val displayName: String,
    val actionsJson: String,  // JSON array of ShadowAction
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_preferences")
data class UserPreference(
    @PrimaryKey val key: String,
    val value: String
)

// ─── DAOs ───────────────────────────────────────────────────────────────────

@Dao
interface CommandHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: CommandRecord)

    @Query("SELECT * FROM command_history ORDER BY timestamp DESC LIMIT 50")
    fun getRecentCommands(): Flow<List<CommandRecord>>

    @Query("SELECT transcript FROM command_history WHERE success = 1 GROUP BY transcript ORDER BY COUNT(*) DESC LIMIT 10")
    suspend fun getFrequentCommands(): List<String>

    @Query("DELETE FROM command_history WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)
}

@Dao
interface AppPreferenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(pref: AppPreference)

    @Query("SELECT * FROM app_preferences ORDER BY usageCount DESC LIMIT 20")
    suspend fun getTopApps(): List<AppPreference>

    @Query("UPDATE app_preferences SET usageCount = usageCount + 1, lastUsed = :ts WHERE packageName = :pkg")
    suspend fun incrementUsage(pkg: String, ts: Long = System.currentTimeMillis())
}

@Dao
interface CustomRoutineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(routine: CustomRoutine)

    @Delete
    suspend fun delete(routine: CustomRoutine)

    @Query("SELECT * FROM custom_routines ORDER BY createdAt DESC")
    fun getAllRoutines(): Flow<List<CustomRoutine>>

    @Query("SELECT * FROM custom_routines WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): CustomRoutine?
}

@Dao
interface UserPreferenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(pref: UserPreference)

    @Query("SELECT value FROM user_preferences WHERE `key` = :key LIMIT 1")
    suspend fun get(key: String): String?
}

// ─── Database ────────────────────────────────────────────────────────────────

@Database(
    entities = [CommandRecord::class, AppPreference::class, CustomRoutine::class, UserPreference::class],
    version = 1,
    exportSchema = false
)
abstract class ShadowDatabase : RoomDatabase() {
    abstract fun commandHistoryDao(): CommandHistoryDao
    abstract fun appPreferenceDao(): AppPreferenceDao
    abstract fun customRoutineDao(): CustomRoutineDao
    abstract fun userPreferenceDao(): UserPreferenceDao
}

// ─── Store ───────────────────────────────────────────────────────────────────

class MemoryStore(context: Context) {

    private val db = Room.databaseBuilder(
        context.applicationContext,
        ShadowDatabase::class.java,
        "shadow_memory"
    ).fallbackToDestructiveMigration().build()

    val commandHistory = db.commandHistoryDao()
    val appPreferences = db.appPreferenceDao()
    val routines = db.customRoutineDao()
    val preferences = db.userPreferenceDao()

    suspend fun recordCommand(transcript: String, resolvedAction: String, success: Boolean) {
        commandHistory.insert(CommandRecord(transcript = transcript, resolvedAction = resolvedAction, success = success))
    }

    suspend fun getWakeWord(): String {
        return preferences.get("wake_word") ?: "shadow"
    }

    suspend fun setWakeWord(word: String) {
        preferences.set(UserPreference("wake_word", word))
    }

    suspend fun isFirstLaunch(): Boolean {
        return preferences.get("onboarded") == null
    }

    suspend fun markOnboarded() {
        preferences.set(UserPreference("onboarded", "true"))
    }
}
