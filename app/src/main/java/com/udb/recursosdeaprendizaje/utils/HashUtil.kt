package com.udb.recursosdeaprendizaje.utils

import java.security.MessageDigest

object HashUtil {

    // Usamos el correo como "sal" para que dos usuarios con la misma
    // contraseña no tengan el mismo hash.
    fun hashPassword(password: String, email: String): String {
        val entrada = email.trim().lowercase() + ":" + password
        val bytes = MessageDigest.getInstance("SHA-256").digest(entrada.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
