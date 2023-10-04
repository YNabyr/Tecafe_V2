package com.example.ukl_kasir

data class PaymentModel(
    val paymentId: String = "",
    val menuName: String = "",
    val menuType: String = "",
    val menuPrice: Int = 0,
    val menuImage: String = "",
    var totalHargaItem: Double = 0.0,
    var totalItem: Int = 0,



)

