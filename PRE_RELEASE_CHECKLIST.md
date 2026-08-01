# Pre-Release Checklist

## 1. CRITICAL: Production API URL uses HTTP (cleartext)

The `prod` flavor in `app/build.gradle.kts` currently has:

```kotlin
buildConfigField("String", "BASE_URL", "\"http://havostech-001-site2.atempurl.com/\"")
```

But the release `network_security_config.xml` blocks cleartext traffic (`cleartextTrafficPermitted="false"`).
**The prod release build will fail all network requests.**

**Action:** Change the prod `BASE_URL` to `https://` before building:

```kotlin
buildConfigField("String", "BASE_URL", "\"https://havostech-001-site2.atempurl.com/\"")
```

---

## 2. Signing Configuration

| Item | Status | Location |
|------|--------|----------|
| Keystore file (`key.jks`) exists at project root | Verify | `rootProject/key.jks` |
| `local.properties` has correct credentials | Configured | `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS=GoalErp2026`, `RELEASE_KEY_PASSWORD` |
| `local.properties` is in `.gitignore` | Verify | Must never be committed |
| `key.jks` is in `.gitignore` | Verify | Must never be committed |

**Action:** Run this to confirm the signing config resolves:

```bash
./gradlew :app:assembleProdRelease --dry-run
```

---

## 3. APK Signature Verification

`SecurityGuard.kt` has the fingerprint configured:

```
B2:83:C2:D1:C2:53:CC:AA:11:6D:83:EF:7B:4C:48:96:A0:5D:05:A8:4D:60:09:6F:50:50:82:E7:50:4C:48:49
```

**Action:** Verify it still matches your keystore:

```bash
keytool -list -v -keystore key.jks -alias GoalErp2026 | grep SHA256
```

If you ever rotate the keystore, update this constant.

---

## 4. Version Bump

Current values in `app/build.gradle.kts`:

```kotlin
versionCode = 2
versionName = "1.0.1"
```

**Action:** Increment `versionCode` (must be monotonically increasing) and update `versionName` for each release.

---

## 5. Build & Verify Release APK

```bash
# Clean build
./gradlew clean assembleProdRelease

# APK output location:
# app/build/outputs/apk/prod/release/app-prod-release.apk
```

**Post-build verification:**

- [ ] Install on a physical device (not emulator)
- [ ] Confirm the app does NOT crash on launch (signature check passes)
- [ ] Confirm login works over HTTPS
- [ ] Confirm the app kills itself if re-signed with a different key
- [ ] Confirm screenshots are blocked (FLAG_SECURE active in release)
- [ ] Confirm no logcat output from the app (Log calls stripped by R8)

---

## 6. Security Configuration Summary (no action needed, just verify)

| Feature | Release Behavior | File |
|---------|-----------------|------|
| Cleartext traffic | Blocked | `network_security_config.xml` |
| R8 minification + resource shrinking | Enabled | `build.gradle.kts` |
| Log stripping | All `Log.*` and `System.out` removed | `proguard-rules.pro` |
| FLAG_SECURE (no screenshots) | Active | `MainActivity.kt` |
| SecurityGuard checks | All enforced, fatal on critical | `LubricErpApplication.kt` |
| IntegrityMonitor (10s periodic) | Running | `IntegrityMonitor.kt` |
| Backup disabled | `allowBackup=false` | `AndroidManifest.xml` |
| TLS 1.2+ only | Enforced via OkHttp ConnectionSpec | `NetworkModule.kt` |

---

## 7. Optional Hardening (recommended before first public distribution)

- [ ] **Rotate StringObfuscator key material** in `StringObfuscator.kt` — the current key (`GoalErp@2026Sec!`) is guessable. Change `k1`, `k2`, and `ivSeed` to random character arrays.
- [ ] **Back up `key.jks`** in a secure location (password manager, hardware token). If lost, you can never update the app for existing users.
- [ ] **Consider certificate pinning** once your server has a stable certificate. Add a `<domain-config>` with `<pin-set>` to `network_security_config.xml`.

---

## 8. Git Hygiene Before Tagging

```bash
# Ensure these are NOT tracked:
git status local.properties key.jks

# Confirm .gitignore covers:
# - local.properties
# - key.jks
# - *.apk
# - app/prod/release/
```

---

## Priority Order

1. **Switch prod BASE_URL to HTTPS** (app won't work without this)
2. **Verify signing config** (build won't produce a signed APK without this)
3. **Bump version** (required for each release)
4. **Build, install, and test on physical device**
5. **Optional hardening items**
