package com.nasser.pokedexlsi.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.nasser.pokedexlsi.R
import com.nasser.pokedexlsi.databinding.AppBarActivityBinding
import com.nasser.pokedexlsi.ui.fragments.BookmarkFragment
import com.nasser.pokedexlsi.ui.fragments.HomeScreenFragment

class AppBarActivity: AppCompatActivity() {

    private lateinit var mBinding: AppBarActivityBinding
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = AppBarActivityBinding.inflate(layoutInflater)

        toggle = ActionBarDrawerToggle(this, mBinding.drawerLayout, R.string.open, R.string.close)
        mBinding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mBinding.navigationView.setNavigationItemSelectedListener {

            it.isChecked = true

            when(it.itemId) {
                R.id.home_pokedex -> {
                    replaceFragment(HomeScreenFragment(), it.title.toString())
                }
                R.id.bookmark_pokemon -> {
                    replaceFragment(BookmarkFragment(), it.title.toString())
                }
                R.id.sign_out -> {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    Toast.makeText(this, "Sesion cerrada", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }

        setContentView(mBinding.root)
    }

    private fun replaceFragment(fragment: Fragment, title: String) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.framelayout, fragment)
        fragmentTransaction.commit()
        mBinding.drawerLayout.closeDrawers()
        setTitle(title)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

}