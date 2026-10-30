package com.udb.recursosdeaprendizaje.controller

import android.content.Context

// Datos locales de cada usuario: sus favoritos y las calificaciones que ya dio.
// Se separan por id de usuario, así dos cuentas en el mismo teléfono no se mezclan.
class PreferenciasController(context: Context, usuarioId: String) {

    private val prefs = context.applicationContext
        .getSharedPreferences("preferencias_aprendeapp", Context.MODE_PRIVATE)

    private val claveFavoritos = "fav_$usuarioId"
    private val prefijoRating = "rating_${usuarioId}_"

    fun favoritos(): Set<String> =
        HashSet(prefs.getStringSet(claveFavoritos, emptySet()) ?: emptySet())

    fun esFavorito(idRecurso: String): Boolean = favoritos().contains(idRecurso)

    // Devuelve true si quedó como favorito, false si se quitó
    fun alternarFavorito(idRecurso: String): Boolean {
        val actuales = favoritos().toMutableSet()
        val ahoraEsFavorito = actuales.add(idRecurso)
        if (!ahoraEsFavorito) actuales.remove(idRecurso)
        prefs.edit().putStringSet(claveFavoritos, actuales).apply()
        return ahoraEsFavorito
    }

    fun calificacionPrevia(idRecurso: String): Int? {
        val valor = prefs.getInt(prefijoRating + idRecurso, 0)
        return if (valor == 0) null else valor
    }

    fun guardarCalificacion(idRecurso: String, estrellas: Int) {
        prefs.edit().putInt(prefijoRating + idRecurso, estrellas).apply()
    }
}
