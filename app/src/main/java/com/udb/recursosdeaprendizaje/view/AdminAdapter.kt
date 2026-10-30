package com.udb.recursosdeaprendizaje.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.udb.recursosdeaprendizaje.R
import com.udb.recursosdeaprendizaje.databinding.ItemAdminRecursoBinding
import com.udb.recursosdeaprendizaje.model.Recurso

class AdminAdapter(
    private val onEditar: (Recurso) -> Unit,
    private val onEliminar: (Recurso) -> Unit
) : ListAdapter<Recurso, AdminAdapter.VH>(Diff) {

    class VH(val b: ItemAdminRecursoBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemAdminRecursoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val r = getItem(position)
        val ctx = holder.itemView.context
        with(holder.b) {
            tvTitulo.text = r.titulo
            tvTipo.text = ctx.getString(R.string.item_tipo, r.tipo)
            tvId.text = ctx.getString(R.string.item_id, r.id ?: "-")
            btnEditar.setOnClickListener { onEditar(r) }
            btnEliminar.setOnClickListener { onEliminar(r) }
        }
    }

    companion object Diff : DiffUtil.ItemCallback<Recurso>() {
        override fun areItemsTheSame(a: Recurso, b: Recurso) = a.id == b.id
        override fun areContentsTheSame(a: Recurso, b: Recurso) = a == b
    }
}
