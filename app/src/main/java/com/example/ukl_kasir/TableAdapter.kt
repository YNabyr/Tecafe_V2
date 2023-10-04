package com.example.ukl_kasir

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class TableAdapter(private val itemClickListener: (TableModel) -> Unit) :
    ListAdapter<TableModel, TableAdapter.TableViewHolder>(TableDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TableViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_table, parent, false)
        return TableViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TableViewHolder, position: Int) {
        val currentItem = getItem(position)

        holder.bind(currentItem)

        holder.itemView.setOnClickListener {
            itemClickListener(currentItem)
        }
    }

    class TableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTableNum: TextView = itemView.findViewById(R.id.tvTableNum)
        private val btnDeleteTable: Button = itemView.findViewById(R.id.btnDeleteTable)
        private val statusTable: TextView = itemView.findViewById(R.id.statusTable)
        private val btnStatus: Button = itemView.findViewById(R.id.btnStatus)

        fun bind(table: TableModel) {
            tvTableNum.text = "Table ${table.tableNum}"
            statusTable.text = table.status

            btnStatus.setOnClickListener {
                val newStatus = if (table.status == "Tersedia") "Tidak Tersedia" else "Tersedia"
                updateStatusInFirestore(table.tableId, newStatus)
            }

            btnDeleteTable.setOnClickListener {
                deleteTableFromFirestore(table.tableId)
            }
        }

        private fun updateStatusInFirestore(tableId: String, newStatus: String) {
            val firestore = FirebaseFirestore.getInstance()
            val tableRef = firestore.collection("table").document(tableId)

            tableRef
                .update("status", newStatus)
                .addOnSuccessListener {
                    // Handle ketika pembaruan berhasil
                    statusTable.text = newStatus // Update status lokal
                }
                .addOnFailureListener { exception ->
                    // Handle ketika pembaruan gagal
                    // Anda dapat menampilkan pesan kesalahan atau mengambil tindakan lain yang diperlukan
                }
        }

        private fun deleteTableFromFirestore(tableId: String) {
            val firestore = FirebaseFirestore.getInstance()
            val tableRef = firestore.collection("table").document(tableId)

            tableRef
                .delete()
                .addOnSuccessListener {
                    // Handle ketika penghapusan berhasil
                }
                .addOnFailureListener { exception ->
                    // Handle ketika penghapusan gagal
                }
        }
    }
}

class TableDiffCallback : DiffUtil.ItemCallback<TableModel>() {
    override fun areItemsTheSame(oldItem: TableModel, newItem: TableModel): Boolean {
        return oldItem.tableId == newItem.tableId
    }

    override fun areContentsTheSame(oldItem: TableModel, newItem: TableModel): Boolean {
        return oldItem == newItem
    }
}