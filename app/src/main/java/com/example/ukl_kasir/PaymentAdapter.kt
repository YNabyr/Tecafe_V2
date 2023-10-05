package com.example.ukl_kasir

import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ukl_kasir.databinding.FragmentPaymentBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class PaymentAdapter(
    private val paymentList: List<PaymentModel>,
    private val fragmentBinding: FragmentPaymentBinding
) :
    RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder>() {
    private var totalHargaSemuaItem: Double = 0.0
    private lateinit var db: FirebaseFirestore
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
        holder.totalHargaItem.text =
            "Total Harga: Rp ${String.format("%.2f", totalHarga)}" // Format sesuai kebutuhan

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

        fragmentBinding.btnPesan.setOnClickListener {
            pesan()

        }
    }

    private fun pesan() {
        val selectedTable = fragmentBinding.spTable.selectedItem.toString() // Mendapatkan meja yang dipilih dari Spinner
        val tableNumber = selectedTable.removePrefix("Meja ").toInt() // Mengambil nomor meja dari teks yang dipilih

        // Mengelompokkan menuNamePayment berdasarkan menuTypePayment
        val menuTypeToMenuNames = paymentList.groupBy { it.menuType }

        // Simpan tanggal hari ini dalam format "yyyy-MM-dd"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        // Menghitung seluruh totalItem dari pesanan
        val allTotalItem = paymentList.sumBy { it.totalItem }

        // Simpan data selectedTable ke Firebase Firestore dalam koleksi "payment"
        val db = FirebaseFirestore.getInstance()
        val paymentData = hashMapOf(
            "tableNumber" to tableNumber,
            "totalHargaSemua" to totalHargaSemuaItem,
            "totalItem" to allTotalItem,// Simpan totalHargaSemua
            "tanggal" to currentDate,
            "statusPembayaran" to "Belum Dibayar",
            // Anda dapat menambahkan data lain yang ingin Anda simpan di sini.
        )

        db.collection("payment")
            .add(paymentData)
            .addOnSuccessListener { documentReference ->
                // Penanganan sukses saat data berhasil disimpan
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")

                // Reset totalHargaSemuaItem atau melakukan tindakan lain yang diperlukan setelah pesanan berhasil
                totalHargaSemuaItem = 0.0
                // Set ulang teks tvPrice menggunakan binding
                updateTotalPriceTextView()
                notifyDataSetChanged()

                // Panggil updateTableStatusToNotAvailable(tableNumber) setelah penyimpanan data berhasil
                updateTableStatusToNotAvailable(tableNumber)

                // Panggil saveMenuNamesToSubcollections() untuk menyimpan menuNamePayment ke subkoleksi yang sesuai
                saveMenuNamesToSubcollections(menuTypeToMenuNames, documentReference.id)
            }
            .addOnFailureListener { e ->
                // Penanganan kesalahan jika gagal menyimpan data
                Log.w(TAG, "Error adding document", e)
            }
    }

    private fun updateTableStatusToNotAvailable(tableNumber: Int) {
        val db = FirebaseFirestore.getInstance()
        val tableCollectionRef = db.collection("table")

        tableCollectionRef.whereEqualTo("tableNum", tableNumber)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.documents.isNotEmpty()) {
                    val tableDocument = querySnapshot.documents[0]
                    tableDocument.reference.update("status", "Tidak Tersedia")
                        .addOnSuccessListener {
                            // Penanganan sukses saat update berhasil
                            Log.d(TAG, "TableStatus updated to 'Tidak Tersedia' for Table $tableNumber")

                            // Reset totalHargaSemuaItem atau melakukan tindakan lain yang diperlukan setelah pesanan berhasil
                            totalHargaSemuaItem = 0.0
                            // Set ulang teks tvPrice menggunakan binding
                            updateTotalPriceTextView()
                            notifyDataSetChanged()
                        }
                        .addOnFailureListener { exception ->
                            // Penanganan kesalahan jika gagal melakukan update
                            Log.w(TAG, "Error updating tableStatus", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                // Penanganan kesalahan jika gagal mendapatkan data meja
                Log.w(TAG, "Error getting table data", exception)
            }
    }

    private fun saveMenuNamesToSubcollections(menuTypeToMenuNames: Map<String, List<PaymentModel>>, paymentId: String) {
        val db = FirebaseFirestore.getInstance()

        // Iterasi melalui map menuTypeToMenuNames
        for ((menuType, menuNames) in menuTypeToMenuNames) {
            val subcollectionRef = db.collection("payment").document(paymentId).collection(menuType)

            // Iterasi melalui list menuNames dalam setiap menuType
            for (payment in menuNames) {
                val menuData = hashMapOf(
                    "menuName" to payment.menuName,
                    "totalItem" to payment.totalItem, // Menyimpan totalItem
                    "totalHargaItem" to payment.totalHargaItem, // Menyimpan totalHargaItem
                    // Anda dapat menambahkan data lain yang ingin Anda simpan di sini.
                )

                subcollectionRef.add(menuData)
                    .addOnSuccessListener {
                        // Penanganan sukses saat data berhasil disimpan di subkoleksi
                        Log.d(TAG, "DocumentSnapshot added to subcollection: $menuType")
                    }
                    .addOnFailureListener { exception ->
                        // Penanganan kesalahan jika gagal menyimpan data di subkoleksi
                        Log.w(TAG, "Error adding document to subcollection", exception)
                    }
            }
        }
    }
    override fun getItemCount(): Int {
        return paymentList.size
    }

    inner class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val menuImagePayment: ImageView = itemView.findViewById(R.id.menuImagePayment)
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