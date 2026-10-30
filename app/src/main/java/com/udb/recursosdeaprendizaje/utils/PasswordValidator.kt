package com.udb.recursosdeaprendizaje.utils

import com.udb.recursosdeaprendizaje.R

object PasswordValidator {

    private val MAYUSCULA = Regex("[A-Z]")
    private val MINUSCULA = Regex("[a-z]")
    private val NUMERO = Regex("[0-9]")
    private val ESPECIAL = Regex("[!@#$%^&*]")

    // Devuelve los ids de los textos de error. Lista vacía = contraseña válida.
    fun validar(password: String): List<Int> {
        val errores = mutableListOf<Int>()
        if (password.length < 12) errores.add(R.string.error_password_longitud)
        if (!MAYUSCULA.containsMatchIn(password)) errores.add(R.string.error_password_mayuscula)
        if (!MINUSCULA.containsMatchIn(password)) errores.add(R.string.error_password_minuscula)
        if (!NUMERO.containsMatchIn(password)) errores.add(R.string.error_password_numero)
        if (!ESPECIAL.containsMatchIn(password)) errores.add(R.string.error_password_especial)
        return errores
    }
}
