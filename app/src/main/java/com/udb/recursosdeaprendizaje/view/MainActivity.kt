package com.udb.recursosdeaprendizaje.view

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.udb.recursosdeaprendizaje.R
import com.udb.recursosdeaprendizaje.controller.RecursoController
import com.udb.recursosdeaprendizaje.controller.RecursosResult
import com.udb.recursosdeaprendizaje.databinding.ActivityMainBinding
import com.udb.recursosdeaprendizaje.model.Recurso
import com.udb.recursosdeaprendizaje.utils.SessionManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var session: SessionManager
    private lateinit var adapter: RecursoAdapter
    private val controller = RecursoController()

    private var tipoSeleccionado: String? = null
    private var ascendente = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = SessionManager(this)

        // Protección: sin sesión no se entra a esta pantalla
        if (!session.estaLogueado()) {
            irALogin()
            return
        }

        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }

        configurarToolbar()
        configurarLista()
        configurarBusquedaYFiltros()
        cargarRecursos()
    }

    private fun configurarToolbar() {
        val nombreRol = if (session.esDocente()) R.string.rol_docente else R.string.rol_estudiante
        binding.toolbar.subtitle =
            getString(R.string.main_subtitulo, session.obtenerNombre(), getString(nombreRol))

        binding.toolbar.inflateMenu(R.menu.menu_main)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_cerrar_sesion -> {
                    session.cerrarSesion()
                    Toast.makeText(this, R.string.sesion_cerrada, Toast.LENGTH_SHORT).show()
                    irALogin()
                    true
                }
                else -> false
            }
        }
    }

    private fun configurarLista() {
        adapter = RecursoAdapter { recurso -> abrirEnlace(recurso) }
        binding.rvRecursos.layoutManager = LinearLayoutManager(this)
        binding.rvRecursos.adapter = adapter
        binding.btnReintentar.setOnClickListener { cargarRecursos() }
    }

    private fun configurarBusquedaYFiltros() {
        binding.etBuscar.doOnTextChanged { _, _, _, _ -> aplicarFiltros() }

        binding.chipGroup.setOnCheckedStateChangeListener { _, ids ->
            tipoSeleccionado = when (ids.firstOrNull()) {
                R.id.chipLibro -> "libro"
                R.id.chipVideo -> "video"
                R.id.chipArticulo -> "articulo"
                else -> null
            }
            aplicarFiltros()
        }

        binding.btnOrden.setOnClickListener {
            ascendente = !ascendente
            binding.btnOrden.setText(if (ascendente) R.string.orden_az else R.string.orden_za)
            aplicarFiltros()
        }
    }

    private fun cargarRecursos() {
        binding.layoutMensaje.isVisible = false
        binding.progress.isVisible = true
        lifecycleScope.launch {
            when (val resultado = controller.cargar()) {
                is RecursosResult.Exito -> {
                    binding.progress.isVisible = false
                    aplicarFiltros()
                }
                is RecursosResult.Error -> {
                    binding.progress.isVisible = false
                    adapter.submitList(emptyList())
                    binding.tvContador.text = ""
                    mostrarMensaje(resultado.mensaje, conReintento = true)
                    Snackbar.make(binding.root, resultado.mensaje, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun aplicarFiltros() {
        val lista = controller.filtrar(
            binding.etBuscar.text.toString(),
            tipoSeleccionado,
            ascendente
        )
        adapter.submitList(lista)
        binding.tvContador.text = getString(R.string.lista_contador, lista.size)
        if (lista.isEmpty()) mostrarMensaje(R.string.lista_vacia, conReintento = false)
        else binding.layoutMensaje.isVisible = false
    }

    private fun mostrarMensaje(@StringRes mensaje: Int, conReintento: Boolean) {
        binding.tvMensaje.setText(mensaje)
        binding.btnReintentar.isVisible = conReintento
        binding.layoutMensaje.isVisible = true
    }

    private fun abrirEnlace(recurso: Recurso) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(recurso.enlace)))
        } catch (e: ActivityNotFoundException) {
            Snackbar.make(binding.root, R.string.error_abrir_enlace, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun irALogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}
