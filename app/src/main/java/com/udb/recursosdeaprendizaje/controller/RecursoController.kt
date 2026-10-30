package com.udb.recursosdeaprendizaje.controller

import com.udb.recursosdeaprendizaje.R
import com.udb.recursosdeaprendizaje.model.Recurso
import com.udb.recursosdeaprendizaje.network.RetrofitClient
import java.text.Normalizer

sealed class RecursosResult {
    object Exito : RecursosResult()
    data class Error(val mensaje: Int) : RecursosResult()
}

class RecursoController {

    private val api = RetrofitClient.api

    // Copia local de lo que devolvió la API. Buscar, filtrar y ordenar
    // trabajan sobre esta lista para no llamar al servidor en cada tecla.
    private var todos: List<Recurso> = emptyList()

    suspend fun cargar(): RecursosResult {
        return try {
            val respuesta = api.obtenerRecursos()
            when {
                respuesta.code() == 404 -> {
                    todos = emptyList()
                    RecursosResult.Exito
                }
                respuesta.isSuccessful -> {
                    todos = respuesta.body().orEmpty()
                    RecursosResult.Exito
                }
                else -> RecursosResult.Error(R.string.error_servidor)
            }
        } catch (e: Exception) {
            RecursosResult.Error(R.string.error_red)
        }
    }

    fun filtrar(consulta: String, tipo: String?, ascendente: Boolean): List<Recurso> {
        val q = normalizar(consulta)
        val filtrados = todos.filter { r ->
            val cumpleTipo = tipo == null || normalizar(r.tipo) == tipo
            val cumpleBusqueda = q.isEmpty() ||
                r.id == q ||
                normalizar(r.titulo).contains(q) ||
                normalizar(r.tipo).contains(q)
            cumpleTipo && cumpleBusqueda
        }
        val porTitulo = compareBy<Recurso> { normalizar(it.titulo) }
        return if (ascendente) filtrados.sortedWith(porTitulo)
        else filtrados.sortedWith(porTitulo.reversed())
    }

    // Minúsculas y sin tildes: "Artículo" y "articulo" se tratan igual.
    private fun normalizar(texto: String): String {
        val sinTildes = Normalizer.normalize(texto.trim().lowercase(), Normalizer.Form.NFD)
        return sinTildes.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
    }
}
