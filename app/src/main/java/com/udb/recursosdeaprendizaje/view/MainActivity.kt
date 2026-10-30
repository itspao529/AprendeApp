package com.udb.recursosdeaprendizaje.view

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.udb.recursosdeaprendizaje.R
import com.udb.recursosdeaprendizaje.controller.PreferenciasController
import com.udb.recursosdeaprendizaje.controller.RecursoController
import com.udb.recursosdeaprendizaje.controller.RecursosResult
import com.udb.recursosdeaprendizaje.databinding.ActivityMainBinding
import com.udb.recursosdeaprendizaje.databinding.DialogCalificarBinding
import com.udb.recursosdeaprendizaje.model.Recurso
import com.udb.recursosdeaprendizaje.utils.SessionManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var session: SessionManager
    private lateinit var preferencias: PreferenciasController
    private lateinit var adapter: RecursoAdapter
    private val controller = RecursoController()

    // Al volver de la pantalla del Docente se recarga la lista
    private val adminLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { cargarRecursos() }

    private var tipoSeleccionado: String? = null
    private var soloFavoritos = false
    private var ascendente = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = SessionManager(this)

        // Protección: sin sesión no se entra a esta pantalla
        if (!session.estaLogueado()) {
            irALogin()
            return
        }
        preferencias = PreferenciasController(this, session.obtenerId())

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
        // "Administrar recursos" solo existe para el Docente
        binding.toolbar.menu.findItem(R.id.action_administrar).isVisible = session.esDocente()
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_cerrar_sesion -> {
                    session.cerrarSesion()
                    Toast.makeText(this, R.string.sesion_cerrada, Toast.LENGTH_SHORT).show()
                    irALogin()
                    true
                }
                R.id.action_administrar -> {
                    adminLauncher.launch(Intent(this, AdminActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun configurarLista() {
        val esEstudiante = !session.esDocente()
        adapter = RecursoAdapter(
            esEstudiante = esEstudiante,
            esFavorito = { preferencias.esFavorito(it.id.orEmpty()) },
            onFavorito = { alternarFavorito(it) },
            onCalificar = { mostrarDialogoCalificar(it) },
            onAbrirEnlace = { abrirEnlace(it) }
        )
        binding.rvRecursos.layoutManager = LinearLayoutManager(this)
        binding.rvRecursos.adapter = adapter
        binding.btnReintentar.setOnClickListener { cargarRecursos() }
        // El chip de Favoritos solo lo ve el Estudiante
        binding.chipFavoritos.isVisible = esEstudiante
    }

    private fun configurarBusquedaYFiltros() {
        binding.etBuscar.doOnTextChanged { _, _, _, _ -> aplicarFiltros() }

        binding.chipGroup.setOnCheckedStateChangeListener { _, ids ->
            val elegido = ids.firstOrNull()
            soloFavoritos = elegido == R.id.chipFavoritos
            tipoSeleccionado = when (elegido) {
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
            ascendente,
            if (soloFavoritos) preferencias.favoritos() else null
        )
        adapter.submitList(lista)
        binding.tvContador.text = getString(R.string.lista_contador, lista.size)
        if (lista.isEmpty()) {
            mostrarMensaje(
                if (soloFavoritos) R.string.lista_vacia_favoritos else R.string.lista_vacia,
                conReintento = false
            )
        } else {
            binding.layoutMensaje.isVisible = false
        }
    }

    // ---- Favoritos ----
    private fun alternarFavorito(recurso: Recurso): Boolean {
        val ahora = preferencias.alternarFavorito(recurso.id.orEmpty())
        Snackbar.make(
            binding.root,
            if (ahora) R.string.fav_agregado else R.string.fav_quitado,
            Snackbar.LENGTH_SHORT
        ).show()
        // Si estamos viendo solo favoritos, la tarjeta quitada debe desaparecer
        if (soloFavoritos) aplicarFiltros()
        return ahora
    }

    // ---- Calificación ----
    private fun mostrarDialogoCalificar(recurso: Recurso) {
        val id = recurso.id ?: return
        val dialogo = DialogCalificarBinding.inflate(layoutInflater)
        val previa = preferencias.calificacionPrevia(id)
        dialogo.ratingBar.rating = (previa ?: 0).toFloat()

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.calificar_titulo, recurso.titulo))
            .setView(dialogo.root)
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.calificar_enviar) { _, _ ->
                val estrellas = dialogo.ratingBar.rating.toInt()
                if (estrellas < 1) {
                    Snackbar.make(binding.root, R.string.calificar_sin_estrellas, Snackbar.LENGTH_LONG).show()
                } else {
                    enviarCalificacion(id, estrellas, previa)
                }
            }
            .show()
    }

    private fun enviarCalificacion(id: String, estrellas: Int, previa: Int?) {
        binding.progress.isVisible = true
        lifecycleScope.launch {
            when (val resultado = controller.calificar(id, estrellas, previa)) {
                is RecursosResult.Exito -> {
                    preferencias.guardarCalificacion(id, estrellas)
                    binding.progress.isVisible = false
                    aplicarFiltros()
                    val mensaje =
                        if (previa == null) R.string.calificacion_enviada
                        else R.string.calificacion_actualizada
                    Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_SHORT).show()
                }
                is RecursosResult.Error -> {
                    binding.progress.isVisible = false
                    Snackbar.make(binding.root, resultado.mensaje, Snackbar.LENGTH_LONG).show()
                }
            }
        }
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
