package com.example.ukl_kasir

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.ukl_kasir.databinding.ActivityKasirBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class KasirActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKasirBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKasirBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        // Set default fragment when the activity starts
        val defaultFragment = ProfileFragment()
        replaceFragment(defaultFragment)

        binding.bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.profile -> {
                    val profileFragment = ProfileFragment()
                    replaceFragment(profileFragment)
                    true
                }
                R.id.table -> {
                    val tableFragment = TableFragment()
                    replaceFragment(tableFragment)
                    true
                }
                R.id.payment -> {
                    val paymentFragment = PaymentFragment()
                    replaceFragment(paymentFragment)
                    true
                }
                R.id.transaction -> {
                    val transactionFragment = TransactionFragment()
                    replaceFragment(transactionFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }


    // Function to show Toast message
    fun showToastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Function to get the user ID
    fun getUserId(): String? {
        return auth.currentUser?.uid
    }
}