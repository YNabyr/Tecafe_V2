package com.example.ukl_kasir

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.ukl_kasir.databinding.ActivityAddFoodBinding
import com.example.ukl_kasir.databinding.ActivityAddTableBinding
import com.google.firebase.firestore.FirebaseFirestore

class AddTableActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityAddTableBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTableBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        binding.btnSave.setOnClickListener {
            saveTableData()
        }
    }

    private fun saveTableData() {
        val tableId = db.collection("table").document().id
        val tableNum = binding.edtTableNum.text.toString().toInt()

        val tableData = TableModel(tableId, tableNum)

        db.collection("table")
            .document(tableId)
            .set(tableData)
            .addOnSuccessListener {
                showToastMessage("Table data saved successfully.")
                navigateToAdminActivity()
            }
            .addOnFailureListener { exception ->
                showError("Error saving table data: ${exception.message}")
            }
    }

    private fun navigateToAdminActivity() {
        val intent = Intent(this, AdminActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showToastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}