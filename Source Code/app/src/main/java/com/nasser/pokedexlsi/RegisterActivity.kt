package com.nasser.pokedexlsi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.nasser.pokedexlsi.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityRegisterBinding

    private lateinit var dbReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setUpListeners()
    }

    private fun setUpListeners() {
        setUp()
        createNewAccount()
    }

    private fun setUp() {
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        dbReference = database.reference.child("User")
    }

    fun registerUser(view: View) {
        createNewAccount()
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
}