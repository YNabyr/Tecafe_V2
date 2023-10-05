package com.example.ukl_kasir

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(private val itemList: List<TransactionItem>) :
    RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    // Inner class ViewHolder
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tableNumTransaksi: TextView = itemView.findViewById(R.id.tableNumTransaksi)
        val tanggalPayment: TextView = itemView.findViewById(R.id.tanggalPayment)
        val statusTransaksi: TextView = itemView.findViewById(R.id.statusTransaksi)
        val totalItemTransaksi: TextView = itemView.findViewById(R.id.totalItemTransaksi)
        val btnUbahTransaksi: Button = itemView.findViewById(R.id.btnUbahTransaksi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = itemList[position]

        // Set data ke TextViews dan Button
        holder.tableNumTransaksi.text = "Meja ${currentItem.tableNumber}" // Tambahkan "Meja" ke nomor meja
        holder.tanggalPayment.text = currentItem.tanggalPayment
        holder.statusTransaksi.text = currentItem.statusTransaksi
        holder.totalItemTransaksi.text = currentItem.totalItemTransaksi.toString()
        holder.btnUbahTransaksi.setOnClickListener {
            // Handle klik tombol ubah status di sini
        }
    }

    override fun getItemCount() = itemList.size
}