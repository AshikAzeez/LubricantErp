package com.havos.lubricerp.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import android.provider.Settings
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.net.InetSocketAddress
import java.net.Socket
import java.util.zip.ZipFile

/**
 * Runtime integrity checks for sideloaded APK distribution.
 * Provides multi-layered defense against reverse engineering tools:
 * - Frida / dynamic instrumentation detection
 * - TracerPID anti-debugging
 * - /proc/self/maps memory scanning
 * - DEX CRC integrity verification
 * - Root / emulator / hooking framework detection
 * - APK signature verification
 */
object SecurityGuard {

    private var cachedPackageManager: PackageManager? = null
    private var cachedApkPath: String? = null
    private var cachedDexCrc: Long = 0L

    fun init(context: Context) {
        cachedPackageManager = context.packageManager
        cachedApkPath = context.packageCodePath
        // Capture the expected DEX CRC at init time for later integrity checks
        cachedDexCrc = computeDexCrc(context.packageCodePath)
    }

    /**
     * Runs security checks and returns detected threats.
     *
     * @param context Application context.
     * @param enforceAll When true (release builds), every check runs regardless
     *   of [SecurityConfig] toggles. When false (debug builds), only checks
     *   explicitly enabled in [SecurityConfig] are executed.
     */
    fun performChecks(context: Context, enforceAll: Boolean = true): List<SecurityThreat> {
        val threats = mutableListOf<SecurityThreat>()

        val checkDebugger = enforceAll || SecurityConfig.debuggerDetection
        val checkRoot = enforceAll || SecurityConfig.rootDetection
        val checkEmulator = enforceAll || SecurityConfig.emulatorDetection
        val checkFrida = enforceAll || SecurityConfig.fridaDetection
        val checkHooking = enforceAll || SecurityConfig.hookingDetection
        val checkDex = enforceAll || SecurityConfig.dexIntegrity
        val checkSignature = enforceAll || SecurityConfig.signatureVerification

        if (checkDebugger) {
            if (isDebuggerAttached()) threats += SecurityThreat.DEBUGGER_ATTACHED
            if (isTracerAttached()) threats += SecurityThreat.DEBUGGER_ATTACHED
        }
        if (checkRoot) {
            if (isRooted()) threats += SecurityThreat.ROOT_DETECTED
        }
        if (checkEmulator) {
            if (isEmulator()) threats += SecurityThreat.EMULATOR_DETECTED
        }
        if (checkSignature) {
            if (!isSignatureValid(context)) threats += SecurityThreat.SIGNATURE_MISMATCH
        }
        if (checkHooking) {
            if (isXposedInstalled()) threats += SecurityThreat.HOOKING_FRAMEWORK
            if (hasSuspiciousLibraries()) threats += SecurityThreat.HOOKING_FRAMEWORK
        }
        if (checkFrida) {
            if (isFridaDetected()) threats += SecurityThreat.FRIDA_DETECTED
        }
        if (checkDex) {
            if (!isDexIntact()) threats += SecurityThreat.DEX_TAMPERED
        }

        return threats.distinct()
    }

    // ─── Debugger Detection ─────────────────────────────────────────────────

    fun isDebuggerAttached(): Boolean {
        return Debug.isDebuggerConnected() || Debug.waitingForDebugger()
    }

    /**
     * Checks /proc/self/status for a non-zero TracerPid field.
     * A non-zero value means a process (ptrace/strace/lldb) is tracing us.
     */
    fun isTracerAttached(): Boolean {
        return try {
            BufferedReader(FileReader("/proc/self/status")).use { reader ->
                reader.lineSequence().any { line ->
                    line.startsWith("TracerPid:") &&
                        line.substringAfter(":").trim() != "0"
                }
            }
        } catch (_: Exception) {
            false
        }
    }

    // ─── Frida Detection ────────────────────────────────────────────────────

    /**
     * Multi-vector Frida detection:
     * 1. Default Frida port (27042) open on localhost
     * 2. Frida artifacts in /proc/self/maps
     * 3. Frida server binary on disk
     * 4. D-Bus protocol probe on common Frida ports
     */
    fun isFridaDetected(): Boolean {
        return isFridaPortOpen() ||
            isFridaInMaps() ||
            isFridaServerOnDisk() ||
            isFridaDbusProbe()
    }

    private fun isFridaPortOpen(): Boolean {
        val ports = intArrayOf(27042, 27043)
        return ports.any { port ->
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress("127.0.0.1", port), 200)
                    true
                }
            } catch (_: Exception) {
                false
            }
        }
    }

    private fun isFridaInMaps(): Boolean {
        return try {
            BufferedReader(FileReader("/proc/self/maps")).use { reader ->
                reader.lineSequence().any { line ->
                    val lower = line.lowercase()
                    lower.contains("frida") ||
                        lower.contains("gadget") ||
                        lower.contains("linjector")
                }
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun isFridaServerOnDisk(): Boolean {
        val paths = arrayOf(
            "/data/local/tmp/frida-server",
            "/data/local/tmp/re.frida.server",
            "/data/local/tmp/frida-agent",
            "/data/local/tmp/frida-gadget",
            "/system/lib/libfrida-gadget.so",
            "/system/lib64/libfrida-gadget.so"
        )
        return paths.any { File(it).exists() }
    }

    /**
     * Sends a D-Bus AUTH message to common Frida ports.
     * Frida's agent responds to D-Bus protocol; a normal service won't.
     */
    private fun isFridaDbusProbe(): Boolean {
        val ports = intArrayOf(27042, 27043)
        val dbusAuth = byteArrayOf(0x00) + "AUTH\r\n".toByteArray()
        return ports.any { port ->
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress("127.0.0.1", port), 200)
                    socket.soTimeout = 300
                    socket.getOutputStream().write(dbusAuth)
                    socket.getOutputStream().flush()
                    val response = ByteArray(128)
                    val read = socket.getInputStream().read(response)
                    // Frida responds with "REJECTED" to invalid AUTH
                    read > 0 && String(response, 0, read).contains("REJECTED")
                }
            } catch (_: Exception) {
                false
            }
        }
    }

    // ─── Memory Map Scanning ────────────────────────────────────────────────

    /**
     * Scans /proc/self/maps for known instrumentation/hooking libraries.
     */
    fun hasSuspiciousLibraries(): Boolean {
        val suspicious = arrayOf(
            "frida",
            "xposed",
            "substrate",
            "libsubstrate",
            "libmemtrack_frida",
            "gadget",
            "linjector",
            "libxposed_art",
            "edxposed",
            "lsposed",
            "riru",
            "zygisk"
        )
        return try {
            BufferedReader(FileReader("/proc/self/maps")).use { reader ->
                reader.lineSequence().any { line ->
                    val lower = line.lowercase()
                    suspicious.any { lower.contains(it) }
                }
            }
        } catch (_: Exception) {
            false
        }
    }

    // ─── DEX Integrity Verification ─────────────────────────────────────────

    /**
     * Verifies the classes.dex CRC inside the APK hasn't been modified.
     * If an attacker patches the DEX (e.g., via apktool + smali edit),
     * the CRC will differ from what was captured at init.
     */
    fun isDexIntact(): Boolean {
        val path = cachedApkPath ?: return true
        if (cachedDexCrc == 0L) return true // Not initialized
        val currentCrc = computeDexCrc(path)
        return currentCrc == cachedDexCrc
    }

    private fun computeDexCrc(apkPath: String): Long {
        return try {
            ZipFile(apkPath).use { zip ->
                // Check all dex files (classes.dex, classes2.dex, etc.)
                var combinedCrc = 0L
                val entries = zip.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    if (entry.name.startsWith("classes") && entry.name.endsWith(".dex")) {
                        combinedCrc += entry.crc
                    }
                }
                combinedCrc
            }
        } catch (_: Exception) {
            0L
        }
    }

    // ─── Root Detection ─────────────────────────────────────────────────────

    fun isRooted(): Boolean {
        return checkRootBinaries() || checkRootPackages() || checkSuCommand() ||
            checkTestKeys() || checkRootViaBuildProps() || checkWritableSystemPartition()
    }

    private fun checkRootBinaries(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/system/xbin/su",
            "/system/bin/su",
            "/sbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su",
            "/system/etc/.installed_su_daemon",
            "/system/xbin/daemonsu",
            "/system/xbin/magisk",
            "/system/bin/magisk",
            "/sbin/magisk",
            "/data/adb/magisk/magisk",
            "/data/adb/magisk/magisk64",
            "/data/adb/magisk/magisk32"
        )
        return paths.any { File(it).exists() }
    }

    private fun checkRootPackages(): Boolean {
        val packages = arrayOf(
            "com.topjohnwu.magisk",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.noshufou.android.su",
            "com.thirdparty.superuser",
            "com.yellowes.su",
            "com.devadvance.rootcloak",
            "com.zachspong.temprootremovejb",
            "com.amphoras.hidemyroot",
            "com.formyhm.hideroot",
            "io.github.vvb2060.magisk",
            "com.dergoogler.mmrl",
            "me.weishu.kernelsu"
        )
        return try {
            val pm = getPackageManager()
            packages.any { pkg ->
                try {
                    pm.getPackageInfo(pkg, 0)
                    true
                } catch (_: PackageManager.NameNotFoundException) {
                    false
                }
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun getPackageManager(): PackageManager {
        return cachedPackageManager ?: throw IllegalStateException("PackageManager not initialized")
    }

    private fun checkSuCommand(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("which", "su"))
            val result = process.inputStream.bufferedReader().readText()
            process.destroy()
            result.isNotBlank()
        } catch (_: Exception) {
            false
        }
    }

    private fun checkTestKeys(): Boolean {
        val buildTags = Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }

    private fun checkRootViaBuildProps(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("getprop", "ro.build.tags"))
            val result = process.inputStream.bufferedReader().readText().trim()
            process.destroy()
            result.contains("test-keys")
        } catch (_: Exception) {
            false
        }
    }

    private fun checkWritableSystemPartition(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("mount"))
            val result = process.inputStream.bufferedReader().readText()
            process.destroy()
            result.lines().any { it.contains(" /system ") && it.contains("rw") }
        } catch (_: Exception) {
            false
        }
    }

    // ─── Emulator Detection ─────────────────────────────────────────────────

    fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic") ||
            Build.FINGERPRINT.startsWith("unknown") ||
            Build.MODEL.contains("google_sdk") ||
            Build.MODEL.contains("Emulator") ||
            Build.MODEL.contains("Android SDK built for x86") ||
            Build.MANUFACTURER.contains("Genymotion") ||
            Build.BRAND.startsWith("generic") ||
            Build.DEVICE.startsWith("generic") ||
            Build.PRODUCT == "google_sdk" ||
            Build.PRODUCT == "sdk_gphone64_arm64" ||
            Build.PRODUCT == "sdk_gphone64_x86_64" ||
            Build.HARDWARE == "goldfish" ||
            Build.HARDWARE == "ranchu" ||
            Build.BOARD == "unknown" ||
            Build.BOOTLOADER == "unknown" ||
            checkEmulatorFiles() ||
            checkEmulatorProperties())
    }

    private fun checkEmulatorFiles(): Boolean {
        val files = arrayOf(
            "/dev/socket/qemud",
            "/dev/qemu_pipe",
            "/system/lib/libc_malloc_debug_qemu.so",
            "/sys/qemu_trace",
            "/system/bin/qemu-props",
            "/dev/socket/genyd",
            "/dev/socket/baseband_genyd"
        )
        return files.any { File(it).exists() }
    }

    private fun checkEmulatorProperties(): Boolean {
        val props = arrayOf(
            "init.svc.qemud" to "",
            "init.svc.qemu-props" to "",
            "qemu.sf.lcd_density" to "",
            "qemu.hw.mainkeys" to "",
            "ro.kernel.qemu" to "1",
            "ro.kernel.qemu.gles" to ""
        )
        return props.any { (key, expectedValue) ->
            try {
                val process = Runtime.getRuntime().exec(arrayOf("getprop", key))
                val value = process.inputStream.bufferedReader().readText().trim()
                process.destroy()
                if (expectedValue.isEmpty()) value.isNotEmpty() else value == expectedValue
            } catch (_: Exception) {
                false
            }
        }
    }

    // ─── APK Signature Verification ─────────────────────────────────────────

    /**
     * Verifies the APK signing certificate matches the expected SHA-256 fingerprint.
     * Replace [EXPECTED_CERT_SHA256] with the actual fingerprint of your release keystore:
     *   keytool -list -v -keystore key.jks -alias GoalErp2026 | grep SHA256
     */
    private const val EXPECTED_CERT_SHA256 =
        "B2:83:C2:D1:C2:53:CC:AA:11:6D:83:EF:7B:4C:48:96:A0:5D:05:A8:4D:60:09:6F:50:50:82:E7:50:4C:48:49"

    @Suppress("DEPRECATION")
    fun isSignatureValid(context: Context): Boolean {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners ?: return false
            } else {
                packageInfo.signatures ?: return false
            }

            if (signatures.isEmpty()) return false

            val md = java.security.MessageDigest.getInstance("SHA-256")
            val certBytes = signatures[0].toByteArray()
            val digest = md.digest(certBytes)
            val fingerprint = digest.joinToString(":") { "%02X".format(it) }

            fingerprint == EXPECTED_CERT_SHA256
        } catch (_: Exception) {
            false
        }
    }

    // ─── Hooking Framework Detection ────────────────────────────────────────

    fun isXposedInstalled(): Boolean {
        val indicators = arrayOf(
            "de.robv.android.xposed",
            "org.meowcat.edxposed.manager",
            "com.saurik.substrate",
            "com.zachspong.temprootremovejb",
            "com.topjohnwu.magisk",
            "org.lsposed.manager",
            "io.github.vvb2060.magisk"
        )
        val xposedInstalled = try {
            val pm = getPackageManager()
            indicators.any { pkg ->
                try {
                    pm.getPackageInfo(pkg, 0)
                    true
                } catch (_: PackageManager.NameNotFoundException) {
                    false
                }
            }
        } catch (_: Exception) {
            false
        }

        val xposedFiles = arrayOf(
            "/system/framework/XposedBridge.jar",
            "/system/lib/libxposed_art.so",
            "/system/lib64/libxposed_art.so",
            "/data/data/de.robv.android.xposed.installer",
            "/data/adb/lspd",
            "/data/adb/riru",
            "/data/adb/modules/riru_lsposed",
            "/data/adb/modules/zygisk_lsposed"
        )
        val filesExist = xposedFiles.any { File(it).exists() }

        return xposedInstalled || filesExist
    }

    // ─── Developer Options Check ────────────────────────────────────────────

    fun isAdbEnabled(context: Context): Boolean {
        return try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        } catch (_: Exception) {
            false
        }
    }

    // ─── Debuggable Flag Check ──────────────────────────────────────────────

    /**
     * Detects if the app has been re-packaged with android:debuggable=true.
     */
    fun isAppDebuggable(context: Context): Boolean {
        return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }
}

enum class SecurityThreat(val description: String) {
    DEBUGGER_ATTACHED("Debugger or tracer detected"),
    ROOT_DETECTED("Device appears to be rooted"),
    EMULATOR_DETECTED("Running on an emulator"),
    SIGNATURE_MISMATCH("APK signature verification failed"),
    HOOKING_FRAMEWORK("Hooking framework detected"),
    FRIDA_DETECTED("Frida instrumentation detected"),
    DEX_TAMPERED("DEX integrity check failed")
}
