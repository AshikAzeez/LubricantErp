package com.havos.lubricerp.security

/**
 * Per-check toggle flags for debug builds.
 *
 * In release builds ALL checks are enforced regardless of these values.
 * In debug builds, only checks explicitly enabled here will run (log-only,
 * never fatal). This lets developers verify individual security measures
 * without triggering the full suite on every launch.
 *
 * Usage – flip a flag in your local build or via a debug-only settings screen:
 * ```
 * SecurityConfig.debuggerDetection = true   // test debugger detection
 * SecurityConfig.fridaDetection = true      // test Frida detection
 * ```
 */
object SecurityConfig {

    // ─── Individual check toggles (debug builds only) ───────────────────────

    /** Detect attached debuggers (Debug.isDebuggerConnected, TracerPID). */
    var debuggerDetection: Boolean = false

    /** Detect rooted devices (su binaries, Magisk, KernelSU, test-keys). */
    var rootDetection: Boolean = false

    /** Detect emulators (Build props, qemu files, hardware identifiers). */
    var emulatorDetection: Boolean = false

    /** Detect Frida instrumentation (ports, maps, D-Bus, server binary). */
    var fridaDetection: Boolean = false

    /** Detect hooking frameworks (Xposed, LSPosed, Substrate, Riru, Zygisk). */
    var hookingDetection: Boolean = false

    /** Verify DEX CRC integrity (detects smali patching / repackaging). */
    var dexIntegrity: Boolean = false

    /** Verify APK signing certificate fingerprint. */
    var signatureVerification: Boolean = false

    /** Run periodic background monitoring (IntegrityMonitor). */
    var periodicMonitoring: Boolean = false

    // ─── Convenience ────────────────────────────────────────────────────────

    /** Enable all checks (useful for a full security dry-run in debug). */
    fun enableAll() {
        debuggerDetection = true
        rootDetection = true
        emulatorDetection = true
        fridaDetection = true
        hookingDetection = true
        dexIntegrity = true
        signatureVerification = true
        periodicMonitoring = true
    }

    /** Disable all checks (default debug state – zero overhead). */
    fun disableAll() {
        debuggerDetection = false
        rootDetection = false
        emulatorDetection = false
        fridaDetection = false
        hookingDetection = false
        dexIntegrity = false
        signatureVerification = false
        periodicMonitoring = false
    }

    /** True if at least one check is enabled. */
    fun anyEnabled(): Boolean =
        debuggerDetection || rootDetection || emulatorDetection ||
            fridaDetection || hookingDetection || dexIntegrity ||
            signatureVerification || periodicMonitoring
}
