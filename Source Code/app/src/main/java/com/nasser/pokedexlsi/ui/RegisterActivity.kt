package com.nasser.pokedexlsi.ui

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.nasser.pokedexlsi.databinding.RegisterActivityBinding
import com.nasser.pokedexlsi.ui.LoginActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var mBinding: RegisterActivityBinding

    /*private val pokemonFactory: PokedexViewModelFactory by lazy {
        val app = this.application as PokedexApplication
        PokedexViewModelFactory(app.pokemonRepository)
    }

    private val pokedexViewModel: PokedexViewModel by viewModels {
        pokemonFactory
    }*/

    private lateinit var dbReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = RegisterActivityBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        /*mBinding.apply {
            viewmodel = pokedexViewModel
            lifecycleOwner = this@RegisterActivity
        }*/

        setUpListeners()

    }

    private fun setUpListeners() {
        setUp()
        registerUser()
        returnToLogin()
    }

    private fun setUp() {
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        dbReference = database.reference.child("User")
    }

    fun registerUser() {
        mBinding.actionRegister.setOnClickListener {
            createNewAccount()
        }
    }

    private fun createNewAccount() {
        val name: String = mBinding.registerInputEditName.text.toString()
        val lastName: String = mBinding.registerInputEditLastName.text.toString()
        val email: String = mBinding.registerInputEditEmail.text.toString()
        val password: String = mBinding.registerInputEditPassword.text.toString()

        if(!TextUtils.isEmpty(name) &&
            !TextUtils.isEmpty(lastName) &&
            !TextUtils.isEmpty(email) &&
            !TextUtils.isEmpty(password)) {
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if(task.isComplete) {
                    val user: FirebaseUser? = auth.currentUser
                    verifyEmail(user)
                    val userBD = dbReference.child(user!!.uid)

                    userBD.child("Name").setValue(name)
                    userBD.child("lastName").setValue(lastName)
                    actionComplete()
                }
            }
        }
    }

    private fun verifyEmail(user: FirebaseUser?) {
        user?.sendEmailVerification()?.addOnCompleteListener(this) { task ->
            if(task.isComplete) {
                Toast.makeText(this, "Correo enviado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error al enviar el email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun actionComplete() {
        startActivity(Intent(this, LoginActivity::class.java))
    }

    private fun returnToLogin() {
        mBinding.actionReturn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}