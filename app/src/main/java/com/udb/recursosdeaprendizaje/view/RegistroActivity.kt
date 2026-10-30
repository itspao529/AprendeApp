package com.udb.recursosdeaprendizaje.view

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.udb.recursosdeaprendizaje.R
import com.udb.recursosdeaprendizaje.controller.AuthController
import com.udb.recursosdeaprendizaje.controller.AuthResult
import com.udb.recursosdeaprendizaje.databinding.ActivityRegistroBinding
import com.udb.recursosdeaprendizaje.model.Rol
import com.udb.recursosdeaprendizaje.utils.PasswordValidator
import kotlinx.coroutines.launch

class RegistroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroBinding
    private val auth = AuthController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Validación en vivo de la contraseña
        binding.etPassword.doOnTextChanged { texto, _, _, _ ->
            if (texto.isNullOrEmpty()) {
                binding.tilPassword.error = null
            } else {
                mostrarErroresPassword(texto.toString())
            }
        }

        binding.btnRegistrar.setOnClickListener { intentarRegistro() }
        binding.btnIrLogin.setOnClickListener { finish() }
    }

    private fun mostrarErroresPassword(password: String): Boolean {
        val errores = PasswordValidator.validar(password)
        binding.tilPassword.error =
            if (errores.isEmpty()) null
            else errores.joinToString("\n") { getString(it) }
        return errores.isEmpty()
    }

    private fun validarFormulario(): Boolean {
        val nombre = binding.etNombre.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmar = binding.etConfirmar.text.toString()
        var valido = true

        if (nombre.isEmpty()) {
            binding.tilNombre.error = getString(R.string.error_nombre_vacio)
            valido = false
        } else {
            binding.tilNombre.error = null
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.error_email_invalido)
            valido = false
        } else {
            binding.tilEmail.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.error_password_vacia)
            valido = false
        } else if (!mostrarErroresPassword(password)) {
            valido = false
        }

        if (confirmar != password) {
            binding.tilConfirmar.error = getString(R.string.error_passwords_no_coinciden)
            valido = false
        } else {
            binding.tilConfirmar.error = null
        }
        return valido
    }

    private fun intentarRegistro() {
        if (!validarFormulario()) return

        val nombre = binding.etNombre.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        val rol = if (binding.rbDocente.isChecked) Rol.DOCENTE else Rol.ESTUDIANTE

        mostrarCargando(true)
        lifecycleScope.launch {
            when (val resultado = auth.registrar(nombre, email, password, rol)) {
                is AuthResult.Exito -> {
                    Toast.makeText(this@RegistroActivity, R.string.registro_exitoso, Toast.LENGTH_LONG).show()
                    finish()
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
        binding.btnRegistrar.isEnabled = !cargando
        binding.btnIrLogin.isEnabled = !cargando
    }
}
