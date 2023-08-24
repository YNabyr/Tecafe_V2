package com.example.ukl_kasir

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.ukl_kasir.databinding.ActivityAdminBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class AdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminBinding

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private val profileFragment = ProfileFragment()
    private val userFragment = UserFragment()
    private val foodFragment = FoodFragment()
    private val tableFragment = TableFragment()

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
        when (menuItem.itemId) {
            R.id.profile -> {
                replaceFragment(profileFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.user -> {
                replaceFragment(userFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.food -> {
                replaceFragment(foodFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.table -> {
                replaceFragment(tableFragment)
                return@OnNavigationItemSelectedListener true
            }
            else -> false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        // Set default fragment
        replaceFragment(profileFragment)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}