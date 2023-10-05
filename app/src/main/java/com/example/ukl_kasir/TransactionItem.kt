package com.example.ukl_kasir

data class TransactionItem(
    val tableNumber: Int = 0,            // Nomor meja (diubah menjadi tipe Int)
    val tanggalPayment: String = "",     // Tanggal pembayaran (diubah menjadi String)
    val statusTransaksi: String = "",    // Status transaksi
    val totalItemTransaksi: Int = 0 ,
    val totalHargaSemua: Int =0 ,
)