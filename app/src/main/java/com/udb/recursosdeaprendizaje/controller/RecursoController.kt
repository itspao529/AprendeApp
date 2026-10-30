package com.udb.recursosdeaprendizaje.controller

import com.udb.recursosdeaprendizaje.R
import com.udb.recursosdeaprendizaje.model.Recurso
import com.udb.recursosdeaprendizaje.network.RetrofitClient
import java.text.Normalizer
import retrofit2.Response

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

    // ---- CRUD del Docente (POST, PUT, DELETE) ----

    suspend fun crear(recurso: Recurso): RecursosResult = ejecutar {
        // id en null: el servidor asigna el id. Un recurso nuevo empieza sin calificaciones.
        api.crearRecurso(recurso.copy(id = null, ratingSuma = 0, ratingCantidad = 0))
    }

    suspend fun actualizar(recurso: Recurso): RecursosResult = ejecutar {
        val id = recurso.id.orEmpty()
        // Releemos las calificaciones actuales para no pisar lo que votaron los estudiantes
        val actual = api.obtenerRecurso(id).body()
        val nuevo = if (actual != null) {
            recurso.copy(ratingSuma = actual.ratingSuma, ratingCantidad = actual.ratingCantidad)
        } else recurso
        api.actualizarRecurso(id, nuevo)
    }

    suspend fun eliminar(id: String): RecursosResult = ejecutar {
        api.eliminarRecurso(id)
    }

    // ---- Calificación (PUT al backend) ----
    // previa = estrellas que este usuario ya había dado a este recurso (o null).
    // Primera vez: suma las estrellas y aumenta la cantidad de votos.
    // Cambio de voto: reemplaza el voto anterior sin aumentar la cantidad.
    suspend fun calificar(id: String, estrellas: Int, previa: Int?): RecursosResult {
        return try {
            // Leemos el valor más reciente del servidor para no pisar votos de otros
            val lectura = api.obtenerRecurso(id)
            val actual = lectura.body()
            if (!lectura.isSuccessful || actual == null) {
                return RecursosResult.Error(R.string.error_servidor)
            }
            val nuevo = if (previa == null) {
                actual.copy(
                    ratingSuma = actual.ratingSuma + estrellas,
                    ratingCantidad = actual.ratingCantidad + 1
                )
            } else {
                actual.copy(ratingSuma = actual.ratingSuma - previa + estrellas)
            }
            val respuesta = api.actualizarRecurso(id, nuevo)
            if (respuesta.isSuccessful) {
                val guardado = respuesta.body() ?: nuevo
                // Actualizamos también la copia local para que la tarjeta se refresque
                todos = todos.map { if (it.id == id) guardado else it }
                RecursosResult.Exito
            } else {
                RecursosResult.Error(R.string.error_servidor)
            }
        } catch (e: Exception) {
            RecursosResult.Error(R.string.error_red)
        }
    }

    private suspend fun <T> ejecutar(llamada: suspend () -> Response<T>): RecursosResult {
        return try {
            if (llamada().isSuccessful) RecursosResult.Exito
            else RecursosResult.Error(R.string.error_servidor)
        } catch (e: Exception) {
            RecursosResult.Error(R.string.error_red)
        }
    }

    // soloIds: si no es null, solo se muestran recursos cuyo id esté en ese conjunto (favoritos)
    fun filtrar(
        consulta: String,
        tipo: String?,
        ascendente: Boolean,
        soloIds: Set<String>? = null
    ): List<Recurso> {
        val q = normalizar(consulta)
        val filtrados = todos.filter { r ->
            val cumpleTipo = tipo == null || normalizar(r.tipo) == tipo
            val cumpleFavorito = soloIds == null || (r.id != null && r.id in soloIds)
            val cumpleBusqueda = q.isEmpty() ||
                r.id == q ||
                normalizar(r.titulo).contains(q) ||
                normalizar(r.tipo).contains(q)
            cumpleTipo && cumpleFavorito && cumpleBusqueda
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
