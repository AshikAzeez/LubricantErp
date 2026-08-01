package com.havos.lubricerp

import android.app.Application
import android.util.Log
import com.havos.lubricerp.di.appModule
import com.havos.lubricerp.security.IntegrityMonitor
import com.havos.lubricerp.security.SecurityConfig
import com.havos.lubricerp.security.SecurityGuard
import com.havos.lubricerp.security.SecurityThreat
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class LubricErpApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val isDebug = (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0

        // Always init so DEX CRC baseline is captured (lightweight).
        SecurityGuard.init(this)

        if (isDebug) {
            // Debug: run only checks explicitly enabled via SecurityConfig.
            // Threats are logged, never fatal. Flip flags in SecurityConfig
            // (or call SecurityConfig.enableAll()) to test specific checks.
            if (SecurityConfig.anyEnabled()) {
                val threats = SecurityGuard.performChecks(this, enforceAll = false)
                threats.forEach { Log.w(TAG, "[debug] Threat detected: ${it.description}") }
            }
            IntegrityMonitor.start(this, forceStart = false)
        } else {
            // Release: all checks enforced, critical threats terminate the process.
            val threats = SecurityGuard.performChecks(this, enforceAll = true)
            if (threats.isNotEmpty()) {
                handleSecurityThreats(threats)
            }
            IntegrityMonitor.start(this, forceStart = true)
        }

        startKoin {
            androidContext(this@LubricErpApplication)
            modules(appModule)
        }
    }

    private fun handleSecurityThreats(threats: List<SecurityThreat>) {
        val critical = threats.filter {
            it == SecurityThreat.SIGNATURE_MISMATCH ||
                it == SecurityThreat.HOOKING_FRAMEWORK ||
                it == SecurityThreat.FRIDA_DETECTED ||
                it == SecurityThreat.DEX_TAMPERED
        }
        if (critical.isNotEmpty()) {
            android.os.Process.killProcess(android.os.Process.myPid())
        }
    }

    companion object {
        private const val TAG = "SecurityGuard"
    }
}
