package com.udb.recursosdeaprendizaje.view

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.udb.recursosdeaprendizaje.R
import com.udb.recursosdeaprendizaje.controller.RecursoController
import com.udb.recursosdeaprendizaje.controller.RecursosResult
import com.udb.recursosdeaprendizaje.databinding.ActivityRecursoFormBinding
import com.udb.recursosdeaprendizaje.model.Recurso
import com.udb.recursosdeaprendizaje.utils.SessionManager
import kotlinx.coroutines.launch

class RecursoFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecursoFormBinding
    private val controller = RecursoController()

    // null = estamos creando; con valor = estamos editando
    private var editando: Recurso? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!SessionManager(this).esDocente()) {
            Toast.makeText(this, R.string.admin_sin_permiso, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        binding = ActivityRecursoFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        editando = IntentCompat.getSerializableExtra(intent, EXTRA_RECURSO, Recurso::class.java)
        editando?.let { prellenar(it) }

        binding.btnGuardar.setOnClickListener { guardar() }
    }

    // Formulario prellenado para edición
    private fun prellenar(r: Recurso) {
        binding.toolbar.setTitle(R.string.form_titulo_editar)
        binding.etTitulo.setText(r.titulo)
        binding.etDescripcion.setText(r.descripcion)
        binding.etEnlace.setText(r.enlace)
        binding.etImagen.setText(r.imagen)
        binding.chipGroupTipo.check(
            when (r.tipo.lowercase()) {
                "video" -> R.id.chipFormVideo
                "articulo" -> R.id.chipFormArticulo
                else -> R.id.chipFormLibro
            }
        )
    }

    private fun tipoSeleccionado(): String = when (binding.chipGroupTipo.checkedChipId) {
        R.id.chipFormVideo -> "video"
        R.id.chipFormArticulo -> "articulo"
        else -> "libro"
    }

    private fun esUrlValida(texto: String): Boolean =
        (texto.startsWith("http://") || texto.startsWith("https://")) &&
            Patterns.WEB_URL.matcher(texto).matches()

    private fun validar(): Boolean {
        val titulo = binding.etTitulo.text.toString().trim()
        val descripcion = binding.etDescripcion.text.toString().trim()
        val enlace = binding.etEnlace.text.toString().trim()
        val imagen = binding.etImagen.text.toString().trim()
        var valido = true

        if (titulo.length < 3) {
            binding.tilTitulo.error = getString(R.string.error_titulo); valido = false
        } else binding.tilTitulo.error = null

        if (descripcion.length < 10) {
            binding.tilDescripcion.error = getString(R.string.error_descripcion); valido = false
        } else binding.tilDescripcion.error = null

        if (!esUrlValida(enlace)) {
            binding.tilEnlace.error = getString(R.string.error_url); valido = false
        } else binding.tilEnlace.error = null

        if (!esUrlValida(imagen)) {
            binding.tilImagen.error = getString(R.string.error_url); valido = false
        } else binding.tilImagen.error = null

        return valido
    }

    private fun guardar() {
        if (!validar()) return

        val base = editando ?: Recurso()
        val recurso = base.copy(
            titulo = binding.etTitulo.text.toString().trim(),
            descripcion = binding.etDescripcion.text.toString().trim(),
            tipo = tipoSeleccionado(),
            enlace = binding.etEnlace.text.toString().trim(),
            imagen = binding.etImagen.text.toString().trim()
        )
        val esNuevo = editando == null

        mostrarCargando(true)
        lifecycleScope.launch {
            val resultado =
                if (esNuevo) controller.crear(recurso) else controller.actualizar(recurso)
            when (resultado) {
                is RecursosResult.Exito -> {
                    val mensaje = if (esNuevo) R.string.recurso_creado else R.string.recurso_actualizado
                    Toast.makeText(this@RecursoFormActivity, mensaje, Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                is RecursosResult.Error -> {
                    mostrarCargando(false)
                    Snackbar.make(binding.root, resultado.mensaje, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun mostrarCargando(cargando: Boolean) {
        binding.progress.isVisible = cargando
        binding.btnGuardar.isEnabled = !cargando
    }

    companion object {
        const val EXTRA_RECURSO = "recurso"
    }
}
