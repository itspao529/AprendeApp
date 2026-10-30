package com.udb.recursosdeaprendizaje.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.udb.recursosdeaprendizaje.R
import com.udb.recursosdeaprendizaje.databinding.ActivityMainBinding
import com.udb.recursosdeaprendizaje.utils.SessionManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var session: SessionManager

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

        val nombreRol = if (session.esDocente()) R.string.rol_docente else R.string.rol_estudiante
        binding.tvHola.text = getString(R.string.main_hola, session.obtenerNombre())
        binding.tvRol.text = getString(R.string.main_rol, getString(nombreRol))

        binding.btnCerrarSesion.setOnClickListener {
            session.cerrarSesion()
            Toast.makeText(this, R.string.sesion_cerrada, Toast.LENGTH_SHORT).show()
            irALogin()
        }
    }

    private fun irALogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}
