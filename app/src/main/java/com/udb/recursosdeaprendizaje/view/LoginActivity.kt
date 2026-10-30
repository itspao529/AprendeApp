package com.udb.recursosdeaprendizaje.view

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.udb.recursosdeaprendizaje.R
import com.udb.recursosdeaprendizaje.controller.AuthController
import com.udb.recursosdeaprendizaje.controller.AuthResult
import com.udb.recursosdeaprendizaje.databinding.ActivityLoginBinding
import com.udb.recursosdeaprendizaje.utils.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var session: SessionManager
    private val auth = AuthController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = SessionManager(this)

        // Si ya hay sesión guardada, saltamos directo a la pantalla principal
        if (session.estaLogueado()) {
            irAMain()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener { intentarLogin() }
        binding.btnIrRegistro.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
    }

    private fun intentarLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        var valido = true
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.error_email_invalido)
            valido = false
        } else {
            binding.tilEmail.error = null
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.error_password_vacia)
            valido = false
        } else {
            binding.tilPassword.error = null
        }
        if (!valido) return

        mostrarCargando(true)
        lifecycleScope.launch {
            when (val resultado = auth.login(email, password)) {
                is AuthResult.Exito -> {
                    session.guardarSesion(resultado.usuario)
                    Toast.makeText(this@LoginActivity, R.string.login_exitoso, Toast.LENGTH_SHORT).show()
                    irAMain()
                }
                is AuthResult.Error -> {
                    mostrarCargando(false)
                    Snackbar.make(binding.root, resultado.mensaje, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun mostrarCargando(cargando: Boolean) {
        binding.progress.isVisible = cargando
        binding.btnLogin.isEnabled = !cargando
        binding.btnIrRegistro.isEnabled = !cargando
    }

    private fun irAMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
