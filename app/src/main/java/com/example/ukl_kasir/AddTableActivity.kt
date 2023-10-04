package com.example.ukl_kasir

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
        val selectedStatusId = binding.rgStatusTable.checkedRadioButtonId
        val selectedStatusRadio = findViewById<RadioButton>(selectedStatusId)
        val statusTable = selectedStatusRadio.text.toString()

        val tableData = TableModel(tableId, tableNum, statusTable )

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