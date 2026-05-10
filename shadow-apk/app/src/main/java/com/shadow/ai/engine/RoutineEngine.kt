package com.shadow.ai.engine

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.shadow.ai.ShadowApplication
import com.shadow.ai.data.CustomRoutine
import com.shadow.ai.models.ActionResult
import com.shadow.ai.models.DEFAULT_ROUTINES
import com.shadow.ai.models.Routine
import com.shadow.ai.models.ShadowAction
import kotlinx.coroutines.delay

/**
 * Manages and executes named multi-step routines (macros).
 * Built-in routines: Gaming Mode, Study Mode, Sleep Mode.
 * Custom routines are stored in Room DB and resolved by name.
 */
class RoutineEngine(private val context: Context) {

    private val tag = "RoutineEngine"
    private val gson = Gson()
    private val memoryStore = (context.applicationContext as ShadowApplication).memoryStore

    /**
     * Execute a routine by name.
     * The ActionExecutor is injected lazily to break the circular dependency.
     */
    private var executor: ActionExecutor? = null

    fun setExecutor(e: ActionExecutor) {
        executor = e
    }

    suspend fun runRoutine(routineName: String): ActionResult {
        val routine = resolveRoutine(routineName)
            ?: return ActionResult(false, "Routine not found: $routineName")

        Log.i(tag, "Running routine: ${routine.displayName} (${routine.actions.size} steps)")

        val results = mutableListOf<ActionResult>()
        for ((index, action) in routine.actions.withIndex()) {
            Log.d(tag, "Step ${index + 1}: ${action.action}")
            val result = executor?.execute(action)
                ?: ActionResult(false, "Executor not ready")
            results.add(result)
            delay(800) // small delay between steps for reliability
        }

        val allOk = results.all { it.success }
        return ActionResult(
            success = allOk,
            message = "${routine.displayName} ${if (allOk) "activated" else "partially completed"}"
        )
    }

    suspend fun saveCustomRoutine(name: String, displayName: String, actions: List<ShadowAction>) {
        val record = CustomRoutine(
            name = name.lowercase().replace(" ", "_"),
            displayName = displayName,
            actionsJson = gson.toJson(actions)
        )
        memoryStore.routines.upsert(record)
        Log.i(tag, "Saved custom routine: $displayName")
    }

    private suspend fun resolveRoutine(name: String): Routine? {
        val key = name.lowercase().replace(" ", "_").replace("-", "_")

        // Check built-in routines first
        DEFAULT_ROUTINES.firstOrNull { it.name == key || it.displayName.lowercase() == name.lowercase() }
            ?.let { return it }

        // Check custom routines from DB
        val custom = memoryStore.routines.getByName(key)
        if (custom != null) {
            return try {
                val actionType = object : com.google.gson.reflect.TypeToken<List<ShadowAction>>() {}.type
                val actions = gson.fromJson<List<ShadowAction>>(custom.actionsJson, actionType)
                Routine(custom.name, custom.displayName, actions)
            } catch (e: Exception) {
                Log.e(tag, "Failed to parse custom routine", e)
                null
            }
        }

        return null
    }

    fun listBuiltInRoutines(): List<Routine> = DEFAULT_ROUTINES
}
