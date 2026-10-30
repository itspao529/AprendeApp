package com.udb.recursosdeaprendizaje.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.udb.recursosdeaprendizaje.R
import com.udb.recursosdeaprendizaje.controller.RecursoController
import com.udb.recursosdeaprendizaje.controller.RecursosResult
import com.udb.recursosdeaprendizaje.databinding.ActivityAdminBinding
import com.udb.recursosdeaprendizaje.model.Recurso
import com.udb.recursosdeaprendizaje.utils.SessionManager
import kotlinx.coroutines.launch

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var adapter: AdminAdapter
    private val controller = RecursoController()

    // Cuando el formulario guarda algo, recargamos la lista
    private val formLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        if (resultado.resultCode == RESULT_OK) cargar()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Solo el Docente puede entrar a esta pantalla
        if (!SessionManager(this).esDocente()) {
            Toast.makeText(this, R.string.admin_sin_permiso, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = AdminAdapter(
            onEditar = { abrirFormulario(it) },
            onEliminar = { confirmarEliminar(it) }
        )
        binding.rvAdmin.layoutManager = LinearLayoutManager(this)
        binding.rvAdmin.adapter = adapter
        binding.fabAgregar.setOnClickListener { abrirFormulario(null) }

        cargar()
    }

    private fun cargar() {
        binding.progress.isVisible = true
        binding.tvVacio.isVisible = false
        lifecycleScope.launch {
            when (val resultado = controller.cargar()) {
                is RecursosResult.Exito -> {
                    binding.progress.isVisible = false
                    val lista = controller.filtrar("", null, true)
                    adapter.submitList(lista)
                    binding.tvVacio.isVisible = lista.isEmpty()
                }
                is RecursosResult.Error -> {
                    binding.progress.isVisible = false
                    Snackbar.make(binding.root, resultado.mensaje, Snackbar.LENGTH_LONG)
                        .setAction(R.string.lista_reintentar) { cargar() }
                        .show()
                }
            }
        }
    }

    private fun abrirFormulario(recurso: Recurso?) {
        val intent = Intent(this, RecursoFormActivity::class.java)
        if (recurso != null) intent.putExtra(RecursoFormActivity.EXTRA_RECURSO, recurso)
        formLauncher.launch(intent)
    }

    // Confirmación antes de eliminar (lo exige el desafío)
    private fun confirmarEliminar(recurso: Recurso) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.eliminar_titulo)
            .setMessage(getString(R.string.eliminar_mensaje, recurso.titulo))
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.eliminar_confirmar) { _, _ -> eliminar(recurso) }
            .show()
    }

    private fun eliminar(recurso: Recurso) {
        val id = recurso.id ?: return
        binding.progress.isVisible = true
        lifecycleScope.launch {
            when (val resultado = controller.eliminar(id)) {
                is RecursosResult.Exito -> {
                    Snackbar.make(binding.root, R.string.recurso_eliminado, Snackbar.LENGTH_SHORT).show()
                    cargar()
                }
                is RecursosResult.Error -> {
                    binding.progress.isVisible = false
                    Snackbar.make(binding.root, resultado.mensaje, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }
}
