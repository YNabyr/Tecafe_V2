package com.example.ukl_kasir

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ukl_kasir.databinding.FragmentPaymentBinding


class PaymentAdapter(private val paymentList: List<PaymentModel>,  private val fragmentBinding: FragmentPaymentBinding) :
    RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder>() {
    private var totalHargaSemuaItem: Double = 0.0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment, parent, false)
        return PaymentViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val payment = paymentList[position]
        holder.menuNamePayment.text = payment.menuName
        holder.menuTypePayment.text = payment.menuType
        holder.menuPricePayment.text = payment.menuPrice.toString()

        // Memuat gambar menggunakan Glide (ganti 'menuImage' sesuai dengan field yang sesuai)
        Glide.with(holder.itemView)
            .load(payment.menuImage)  // Mengambil URL gambar dari PaymentModel
            .placeholder(R.drawable.placeholder_profile)  // Gambar placeholder jika URL kosong atau tidak valid
            .error(R.drawable.placeholder_profile)  // Gambar error jika gagal memuat gambar
            .into(holder.menuImagePayment)

        // Menampilkan totalItem
        holder.totalItem.text = payment.totalItem.toString()

        // Menghitung dan menampilkan totalHargaItem
        val totalHarga = payment.totalHargaItem
        holder.totalHargaItem.text = "Total Harga: Rp ${String.format("%.2f", totalHarga)}" // Format sesuai kebutuhan

        holder.btnAdd.setOnClickListener {
            // Tambah 1 ke totalItem saat tombol "Tambah" ditekan
            payment.totalItem++

            // Perbarui totalHargaItem saat jumlah berubah
            payment.totalHargaItem = (payment.totalItem * payment.menuPrice).toDouble()

            // Hitung ulang totalHargaSemuaItem setelah perubahan
            calculateTotalHargaSemuaItem()

            notifyDataSetChanged() // Perbarui tampilan RecyclerView

            // Set ulang teks tvPrice menggunakan fragmentBinding
            val formattedTotalPrice = String.format("Harga: Rp %.2f", totalHargaSemuaItem)
            fragmentBinding.tvPrice.text = formattedTotalPrice
        }

        holder.btnKurang.setOnClickListener {
            // Kurangi 1 dari totalItem saat tombol "Kurang" ditekan, dengan batasan minimal 0
            if (payment.totalItem > 0) {
                payment.totalItem--

                // Perbarui totalHargaItem saat jumlah berubah
                payment.totalHargaItem = (payment.totalItem * payment.menuPrice).toDouble()

                // Hitung ulang totalHargaSemuaItem setelah perubahan
                calculateTotalHargaSemuaItem()

                notifyDataSetChanged() // Perbarui tampilan RecyclerView

                // Set ulang teks tvPrice menggunakan fragmentBinding
                val formattedTotalPrice = String.format("Harga: Rp %.2f", totalHargaSemuaItem)
                fragmentBinding.tvPrice.text = formattedTotalPrice
            }
        }
    }

    override fun getItemCount(): Int {
        return paymentList.size
    }

    inner class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val menuImagePayment: ImageView= itemView.findViewById(R.id.menuImagePayment)
        val menuNamePayment: TextView = itemView.findViewById(R.id.menuNamePayment)
        val menuTypePayment: TextView = itemView.findViewById(R.id.menuTypePayment)
        val menuPricePayment: TextView = itemView.findViewById(R.id.menuPricePayment)
        val totalHargaItem: TextView = itemView.findViewById(R.id.totalHargaItem)
        val btnAdd: ImageButton = itemView.findViewById(R.id.btnAdd)
        val totalItem: TextView = itemView.findViewById(R.id.totalItem)
        val btnKurang: ImageButton = itemView.findViewById(R.id.btnKurang)

        init {
            // Tambahkan listener untuk btnAdd dan btnKurang di sini jika diperlukan.
        }

        // Tambahkan referensi ke tampilan lain yang perlu Anda atur di sini.
    }
    private fun updateTotalPriceTextView() {
        val formattedTotalPrice = String.format("Harga: Rp %.2f", totalHargaSemuaItem)
       fragmentBinding.tvPrice.text = formattedTotalPrice
    }
    private fun calculateTotalHargaSemuaItem() {
        totalHargaSemuaItem = paymentList.sumByDouble { it.totalHargaItem }
        updateTotalPriceTextView()
    }
}