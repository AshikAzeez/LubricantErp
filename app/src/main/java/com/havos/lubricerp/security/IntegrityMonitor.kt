package com.havos.lubricerp.security

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Process
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Periodically re-runs security checks throughout the app lifecycle.
 * This catches attackers who attach Frida/debugger AFTER app startup,
 * or who modify the DEX at runtime via hot-patching frameworks.
 *
 * In release builds, critical threats terminate the process.
 * In debug builds, threats are logged only.
 */
object IntegrityMonitor {

    private var scheduler: ScheduledExecutorService? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private const val CHECK_INTERVAL_SECONDS = 10L
    private const val INITIAL_DELAY_SECONDS = 5L

    /**
     * Starts periodic integrity monitoring.
     *
     * @param context Application context.
     * @param forceStart When true (release builds), monitoring always starts.
     *   When false (debug builds), monitoring starts only if
     *   [SecurityConfig.periodicMonitoring] is enabled.
     */
    fun start(context: Context, forceStart: Boolean = true) {
        if (!forceStart && !SecurityConfig.periodicMonitoring) return

        val appContext = context.applicationContext
        scheduler = Executors.newSingleThreadScheduledExecutor { r ->
            Thread(r, "integrity-monitor").apply { isDaemon = true }
        }

        scheduler?.scheduleWithFixedDelay(
            { runPeriodicChecks(appContext) },
            INITIAL_DELAY_SECONDS,
            CHECK_INTERVAL_SECONDS,
            TimeUnit.SECONDS
        )
    }

    fun stop() {
        scheduler?.shutdownNow()
        scheduler = null
    }

    private fun runPeriodicChecks(context: Context) {
        try {
            // Lightweight checks that can run frequently without performance impact
            val threats = mutableListOf<SecurityThreat>()

            if (SecurityGuard.isDebuggerAttached()) threats += SecurityThreat.DEBUGGER_ATTACHED
            if (SecurityGuard.isTracerAttached()) threats += SecurityThreat.DEBUGGER_ATTACHED
            if (SecurityGuard.isFridaDetected()) threats += SecurityThreat.FRIDA_DETECTED
            if (SecurityGuard.hasSuspiciousLibraries()) threats += SecurityThreat.HOOKING_FRAMEWORK
            if (!SecurityGuard.isDexIntact()) threats += SecurityThreat.DEX_TAMPERED

            if (threats.isNotEmpty()) {
                handleThreats(threats)
            }
        } catch (_: Exception) {
            // Never crash the app due to a security check failure
        }
    }

    private fun handleThreats(threats: List<SecurityThreat>) {
        // In release builds, any periodic-check threat is fatal
        mainHandler.post {
            Process.killProcess(Process.myPid())
        }
    }
}
