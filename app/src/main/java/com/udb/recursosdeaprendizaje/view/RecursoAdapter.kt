package com.udb.recursosdeaprendizaje.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.udb.recursosdeaprendizaje.R
import com.udb.recursosdeaprendizaje.databinding.ItemRecursoBinding
import com.udb.recursosdeaprendizaje.model.Recurso

class RecursoAdapter(
    private val onAbrirEnlace: (Recurso) -> Unit
) : ListAdapter<Recurso, RecursoAdapter.VH>(Diff) {

    class VH(val b: ItemRecursoBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemRecursoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val r = getItem(position)
        val ctx = holder.itemView.context

        val tipoRes = when (r.tipo.lowercase()) {
            "libro" -> R.string.tipo_libro
            "video" -> R.string.tipo_video
            "articulo" -> R.string.tipo_articulo
            else -> null
        }
        val tipoTexto = tipoRes?.let { ctx.getString(it) } ?: r.tipo

        with(holder.b) {
            tvTitulo.text = r.titulo
            tvDescripcion.text = r.descripcion
            tvTipo.text = ctx.getString(R.string.item_tipo, tipoTexto)
            tvId.text = ctx.getString(R.string.item_id, r.id ?: "-")
            tvRating.text =
                if (r.ratingCantidad == 0) ctx.getString(R.string.item_sin_calificar)
                else ctx.getString(R.string.item_promedio, r.promedio, r.ratingCantidad)

            Glide.with(ctx)
                .load(r.imagen)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .centerCrop()
                .into(imgRecurso)

            btnEnlace.setOnClickListener { onAbrirEnlace(r) }
        }
    }

    // DiffUtil compara listas y solo redibuja las tarjetas que cambiaron
    companion object Diff : DiffUtil.ItemCallback<Recurso>() {
        override fun areItemsTheSame(a: Recurso, b: Recurso) = a.id == b.id
        override fun areContentsTheSame(a: Recurso, b: Recurso) = a == b
    }
}
