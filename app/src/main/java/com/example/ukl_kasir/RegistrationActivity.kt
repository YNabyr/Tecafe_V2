package com.example.ukl_kasir

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import com.example.ukl_kasir.databinding.ActivityRegistrationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User

class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.btnRegister.setOnClickListener {
            binding.pbLoading.visibility = View.VISIBLE
            val username = binding.edtUsername.text.toString()
            val pwd = binding.edtPassword.text.toString()
            val cnfPwd = binding.edtCnfPwd.text.toString()

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(pwd) || TextUtils.isEmpty(cnfPwd) || binding.rgRole.checkedRadioButtonId == -1) {
                showError("Text Input cannot be empty")
            } else if (pwd != cnfPwd) {
                showError("Please check both passwords")
            } else {
                mAuth.createUserWithEmailAndPassword(username, pwd).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val selectedRoleId = binding.rgRole.checkedRadioButtonId
                        val selectedRoleRadio = findViewById<RadioButton>(selectedRoleId)
                        val role = selectedRoleRadio.text.toString()

                        val userId = mAuth.currentUser?.uid
                        if (userId != null) {
                            val userRole = UserModel(userId, username, role, pwd)
                            saveUserRole(userRole)
                        } else {
                            showError("Failed to retrieve user ID")
                        }
                    } else {
                        showError("Failed to Register")
                    }
                }
            }
        }
    }

    private fun showError(message: String) {
        binding.pbLoading.visibility = View.GONE
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun saveUserRole(userRole: UserModel) {
        val userId = mAuth.currentUser?.uid
        if (userId != null) {
            db.collection("User").document(userId).set(userRole)
                .addOnSuccessListener {
                    when (userRole.role) {
                        "Admin" -> goToAdminActivity()
                        "Manager" -> goToManagerActivity()
                        "Kasir" -> goToKasirActivity()
                        else -> showError("Unknown role: ${userRole.role}")
                    }
                }
                .addOnFailureListener { exception ->
                    showError("Failed to save user role: ${exception.message}")
                }
        } else {
            showError("Failed to retrieve user ID")
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
}