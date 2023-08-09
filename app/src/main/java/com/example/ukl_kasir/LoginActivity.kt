package com.example.ukl_kasir

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.example.ukl_kasir.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogin.setOnClickListener {
            binding.pbLoading.visibility = View.VISIBLE
            val username = binding.edtUsername.text.toString()
            val pwd = binding.edtPassword.text.toString()

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(pwd)) {
                showError("Text Field cannot be empty")
            } else {
                mAuth.signInWithEmailAndPassword(username, pwd)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = mAuth.currentUser
                            if (user != null) {
                                getUserRole(user.uid)
                            } else {
                                showError("Failed to retrieve user ID")
                            }
                        } else {
                            showError("Failed to Login")
                        }
                    }
            }
        }
    }

    private fun showError(message: String) {
        binding.pbLoading.visibility = View.GONE
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun getUserRole(userId: String) {
        db.collection("User").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val role = document.getString("role")
                    if (!role.isNullOrEmpty()) {
                        when (role) {
                            "Kasir" -> goToKasirActivity()
                            "Manager" -> goToManagerActivity()
                            "Admin" -> goToAdminActivity()
                            else -> showError("Invalid Role")
                        }
                    } else {
                        showError("Failed to retrieve user role")
                    }
                } else {
                    showError("Failed to retrieve user role")
                }
            }
            .addOnFailureListener { exception ->
                showError("Failed to retrieve user role: ${exception.message}")
            }
    }

    private fun goToKasirActivity() {
        binding.pbLoading.visibility = View.GONE
        val intent = Intent(this, KasirActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun goToManagerActivity() {
        binding.pbLoading.visibility = View.GONE
        val intent = Intent(this, ManagerActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun goToAdminActivity() {
        binding.pbLoading.visibility = View.GONE
        val intent = Intent(this, AdminActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        val user: FirebaseUser? = mAuth.currentUser

        if (user != null) {
            getUserRole(user.uid)
        }
    }
}