package com.example.ukl_kasir

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class TableViewModel : ViewModel() {
    // Tambahkan properti LiveData untuk data tabel
    private val _tableData = MutableLiveData<List<TableModel>>()
    val tableData: LiveData<List<TableModel>>
        get() = _tableData

    // Tambahkan fungsi untuk mengatur data tabel
    fun setTableData(data: List<TableModel>) {
        _tableData.value = data
    }

    fun updateTableStatus(table: TableModel) {
        // Update the status in Firestore
        val firestore = FirebaseFirestore.getInstance()
        val tableRef = firestore.collection("table").document(table.tableId)

        tableRef
            .update("status", table.status)
            .addOnSuccessListener {
                // Handle success
            }
            .addOnFailureListener { exception ->
                // Handle failure
            }
    }

}