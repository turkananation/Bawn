package app.bawn.util

import java.security.MessageDigest

object SecurityUtils {
    fun hashPin(pin: String): String {
        val bytes = pin.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun verifyPin(inputPin: String, storedHash: String?): Boolean {
        if (storedHash == null) return false // No PIN set yet
        return hashPin(inputPin) == storedHash
    }
}