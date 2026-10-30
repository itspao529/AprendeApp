package com.udb.recursosdeaprendizaje.model

import java.io.Serializable

data class Recurso(
    val id: String? = null,
    val titulo: String = "",
    val descripcion: String = "",
    val tipo: String = "",
    val enlace: String = "",
    val imagen: String = "",
    val ratingSuma: Int = 0,
    val ratingCantidad: Int = 0
) : Serializable {

    // El promedio se calcula a partir de lo guardado en el backend
    val promedio: Float
        get() = if (ratingCantidad == 0) 0f else ratingSuma.toFloat() / ratingCantidad
}
