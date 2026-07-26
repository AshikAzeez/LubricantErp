package com.havos.lubricerp.core.database

import java.util.Base64

/**
 * Simple reversible fake crypto for JVM unit tests where AndroidKeyStore
 * is unavailable. NOT secure - test use only.
 */
class FakeCryptoManager : CryptoManager {

    override fun encrypt(plainText: String): String {
        if (plainText.isBlank()) return plainText
        val encoded = Base64.getEncoder().encodeToString(plainText.toByteArray(Charsets.UTF_8))
        return "$PREFIX$encoded"
    }

    override fun decrypt(cipherText: String): String {
        if (!cipherText.startsWith(PREFIX)) return cipherText
        val encoded = cipherText.removePrefix(PREFIX)
        return String(Base64.getDecoder().decode(encoded), Charsets.UTF_8)
    }

    private companion object {
        const val PREFIX = "fake:"
    }
}
