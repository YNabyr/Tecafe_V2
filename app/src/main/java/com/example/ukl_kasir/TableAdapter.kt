package com.example.ukl_kasir

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TableAdapter(private val tableList: List<TableModel>, private val itemClickListener: (TableModel) -> Unit) :
    RecyclerView.Adapter<TableAdapter.TableViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TableViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_table, parent, false)
        return TableViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TableViewHolder, position: Int) {
        val currentItem = tableList[position]

        holder.bind(currentItem)
        holder.itemView.setOnClickListener {
            itemClickListener(currentItem)
        }
    }

    override fun getItemCount() = tableList.size

    class TableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTableNum: TextView = itemView.findViewById(R.id.tvTableNum)
        val btnDeleteTable: Button = itemView.findViewById(R.id.btnDeleteTable)

        fun bind(table: TableModel) {
            tvTableNum.text = "Table ${table.tableNum}"

            // Set click listener for delete button
            btnDeleteTable.setOnClickListener {
                // Handle delete button click here
                // You can call a function to delete the table data from Firestore
                deleteTableFromFirestore(table.tableId)
            }
        }

        private fun deleteTableFromFirestore(tableId: String) {
            // Implement your code to delete table data from Firestore
            // You can use the FirebaseFirestore instance and delete the document
        }
    }
}