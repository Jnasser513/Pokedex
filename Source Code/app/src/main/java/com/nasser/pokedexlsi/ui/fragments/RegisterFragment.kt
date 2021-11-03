package com.nasser.pokedexlsi.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.nasser.pokedexlsi.databinding.RegisterFragmentBinding

class RegisterFragment : Fragment() {

    private lateinit var mBinding: RegisterFragmentBinding

    private lateinit var dbReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        mBinding = RegisterFragmentBinding.inflate(inflater, container, false)

        setUpListeners()

        return mBinding.root
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
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(requireActivity()) { task ->
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
        user?.sendEmailVerification()?.addOnCompleteListener(requireActivity()) { task ->
            if(task.isComplete) {
                Toast.makeText(requireContext(), "Correo enviado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Error al enviar el email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun actionComplete() {
        //startActivity(Intent(this, LoginFragment::class.java))
    }
}