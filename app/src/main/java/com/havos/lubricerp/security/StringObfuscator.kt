package com.havos.lubricerp.security

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Runtime string obfuscation to protect sensitive constants (API keys, URLs,
 * internal identifiers) from static analysis tools like jadx, apktool, or
 * strings extraction.
 *
 * Usage:
 *   // At build time (or in a test), encrypt the plaintext:
 *   val encrypted = StringObfuscator.encrypt("my-secret-value")
 *   // Embed the encrypted string in code:
 *   val secret = StringObfuscator.decrypt("BASE64_IV:BASE64_CIPHER")
 *
 * The AES key is derived at runtime and never stored as a plaintext constant,
 * making static string searches ineffective.
 */
object StringObfuscator {

    // Key material is split and reassembled at runtime to defeat simple grep/strings.
    // This is NOT cryptographic security – it's obfuscation to raise the RE bar.
    private val k1 = charArrayOf('G', 'o', 'a', 'l', 'E', 'r', 'p', '@')
    private val k2 = charArrayOf('2', '0', '2', '6', 'S', 'e', 'c', '!')
    private val ivSeed = charArrayOf('L', 'u', 'b', 'r', 'i', 'c', 'E', 'r', 'p', 'I', 'V', '0', '1', '2', '3', '4')

    private fun deriveKey(): SecretKeySpec {
        val combined = ByteArray(16)
        for (i in k1.indices) combined[i] = k1[i].code.toByte()
        for (i in k2.indices) combined[i + 8] = k2[i].code.toByte()
        return SecretKeySpec(combined, "AES")
    }

    private fun deriveIv(): IvParameterSpec {
        val iv = ByteArray(16)
        for (i in ivSeed.indices) iv[i] = ivSeed[i].code.toByte()
        return IvParameterSpec(iv)
    }

    /**
     * Encrypts a plaintext string. Use this at development time to produce
     * the obfuscated constant to embed in source code.
     */
    fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, deriveKey(), deriveIv())
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(encrypted)
    }

    /**
     * Decrypts an obfuscated string at runtime.
     */
    fun decrypt(encryptedBase64: String): String {
        return try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, deriveKey(), deriveIv())
            val decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedBase64))
            String(decrypted, Charsets.UTF_8)
        } catch (_: Exception) {
            ""
        }
    }
}
