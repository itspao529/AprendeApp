package com.udb.recursosdeaprendizaje.controller

import com.udb.recursosdeaprendizaje.R
import com.udb.recursosdeaprendizaje.model.Rol
import com.udb.recursosdeaprendizaje.model.Usuario
import com.udb.recursosdeaprendizaje.network.RetrofitClient
import com.udb.recursosdeaprendizaje.utils.HashUtil

sealed class AuthResult {
    data class Exito(val usuario: Usuario) : AuthResult()
    data class Error(val mensaje: Int) : AuthResult()
}

class AuthController {

    private val api = RetrofitClient.api

    suspend fun registrar(nombre: String, email: String, password: String, rol: Rol): AuthResult {
        return try {
            if (buscarPorEmail(email) != null) {
                return AuthResult.Error(R.string.registro_email_existente)
            }
            val nuevo = Usuario(
                nombre = nombre.trim(),
                email = email.trim().lowercase(),
                passwordHash = HashUtil.hashPassword(password, email),
                rol = rol.name
            )
            val respuesta = api.registrarUsuario(nuevo)
            val cuerpo = respuesta.body()
            if (respuesta.isSuccessful && cuerpo != null) AuthResult.Exito(cuerpo)
            else AuthResult.Error(R.string.error_servidor)
        } catch (e: Exception) {
            AuthResult.Error(R.string.error_red)
        }
    }

    suspend fun login(email: String, password: String): AuthResult {
        return try {
            val usuario = buscarPorEmail(email)
            val hash = HashUtil.hashPassword(password, email)
            if (usuario != null && usuario.passwordHash == hash) AuthResult.Exito(usuario)
            else AuthResult.Error(R.string.login_credenciales_invalidas)
        } catch (e: Exception) {
            AuthResult.Error(R.string.error_red)
        }
    }

    // MockAPI filtra por coincidencia parcial (si buscas "ana@x.com" también
    // trae "juana@x.com"), por eso comparamos el correo exacto nosotros.
    // Además responde 404 cuando no hay resultados: eso significa "no existe".
    private suspend fun buscarPorEmail(email: String): Usuario? {
        val limpio = email.trim().lowercase()
        val respuesta = api.buscarUsuarioPorEmail(limpio)
        if (respuesta.code() == 404) return null
        if (!respuesta.isSuccessful) throw RuntimeException("HTTP ${respuesta.code()}")
        return respuesta.body()?.firstOrNull { it.email.equals(limpio, ignoreCase = true) }
    }
}
