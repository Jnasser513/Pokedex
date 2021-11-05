package com.nasser.pokedexlsi.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.nasser.pokedexlsi.databinding.LoginActivityBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var mBinding: LoginActivityBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
    }

    override fun onStart() {
        super.onStart()

        setUpListeners()
    }

    private fun setUpListeners() {
        setUp()
        goToRegister()
        forgotPassword()
        login()
    }

    private fun setUp() {
        auth = FirebaseAuth.getInstance()
    }

    fun forgotPassword() {

    }

    fun goToRegister() {
        mBinding.actionGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    fun login() {
        mBinding.actionLogin.setOnClickListener{loginUser()}
    }

    private fun loginUser() {
        val user: String = mBinding.loginInputEditUser.text.toString()
        val password: String = mBinding.loginInputEditPassword.text.toString()

        if(!TextUtils.isEmpty(user) && !TextUtils.isEmpty(password)) {
            auth.signInWithEmailAndPassword(user, password).addOnCompleteListener(this) { task ->
                if(task.isSuccessful) {
                    goHome()
                } else {
                    Toast.makeText(this, "Error en la autenticacion", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun goHome() {
        startActivity(Intent(this, AppBarActivity::class.java))
    }

}