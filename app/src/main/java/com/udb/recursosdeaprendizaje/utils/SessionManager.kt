package com.udb.recursosdeaprendizaje.utils

import android.content.Context
import com.udb.recursosdeaprendizaje.model.Rol
import com.udb.recursosdeaprendizaje.model.Usuario

class SessionManager(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("sesion_aprendeapp", Context.MODE_PRIVATE)

    fun guardarSesion(usuario: Usuario) {
        prefs.edit()
            .putString(KEY_ID, usuario.id)
            .putString(KEY_NOMBRE, usuario.nombre)
            .putString(KEY_EMAIL, usuario.email)
            .putString(KEY_ROL, usuario.rol)
            .apply()
    }

    fun estaLogueado(): Boolean = prefs.getString(KEY_ID, null) != null

    fun obtenerId(): String = prefs.getString(KEY_ID, "") ?: ""
    fun obtenerNombre(): String = prefs.getString(KEY_NOMBRE, "") ?: ""
    fun obtenerEmail(): String = prefs.getString(KEY_EMAIL, "") ?: ""

    fun obtenerRol(): Rol =
        runCatching { Rol.valueOf(prefs.getString(KEY_ROL, "") ?: "") }
            .getOrDefault(Rol.ESTUDIANTE)

    fun esDocente(): Boolean = obtenerRol() == Rol.DOCENTE

    fun cerrarSesion() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val KEY_ID = "id"
        const val KEY_NOMBRE = "nombre"
        const val KEY_EMAIL = "email"
        const val KEY_ROL = "rol"
    }
}
