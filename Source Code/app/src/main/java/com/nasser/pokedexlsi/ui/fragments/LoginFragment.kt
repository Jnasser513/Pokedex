package com.nasser.pokedexlsi.ui.fragments

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nasser.pokedexlsi.databinding.LoginFragmentBinding

class LoginFragment : Fragment() {

    private lateinit var mBinding: LoginFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        mBinding = LoginFragmentBinding.inflate(inflater, container, false)

        return mBinding.root
    }

}