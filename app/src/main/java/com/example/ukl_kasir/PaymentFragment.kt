package com.example.ukl_kasir

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ukl_kasir.databinding.FragmentPaymentBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class PaymentFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var binding: FragmentPaymentBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var paymentAdapter: PaymentAdapter
    private val paymentList = mutableListOf<PaymentModel>() // List untuk menyimpan data dari Firestore
    var totalHargaSemuaItem: Double = 0.0
    private var totalSemuaItem: Int = 0




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPaymentBinding.inflate(inflater, container, false)
        recyclerView = binding.rvPayment

        paymentAdapter = PaymentAdapter(paymentList, binding)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = paymentAdapter

        // Ambil data dari Firebase Firestore
        fetchDataFromFirestore()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        paymentAdapter = PaymentAdapter(paymentList, binding)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = paymentAdapter

        fetchDataFromFirestore()

        // Inisialisasi teks tvPrice
        updateTotalPriceTextView()

        // Hitung ulang totalHargaSemuaItem
        calculateTotalSemuaItem()



    }

    private fun fetchDataFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        val menuCollectionRef = db.collection("menu")

        menuCollectionRef.get()
            .addOnSuccessListener { querySnapshot: QuerySnapshot? ->
                if (querySnapshot != null) {
                    paymentList.clear()
                   // Reset totalHargaSemuaItem

                    for (document in querySnapshot.documents) {
                        val menuData = document.toObject(PaymentModel::class.java)
                        if (menuData != null) {
                            paymentList.add(menuData)
                            totalHargaSemuaItem += menuData.totalHargaItem
                        }
                    }

                    // Setelah totalHargaSemuaItem dihitung ulang, perbarui tampilan tvPrice
                    updateTotalPriceTextView()

                    paymentAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->
                // Penanganan kesalahan jika gagal mengambil data
            }
    }
    private fun pesan() {
        // Disini Anda dapat menyimpan data ke Firebase Firestore dalam collection 'payment'
        // dengan ID yang digenerate secara otomatis.

        val paymentData = hashMapOf(
            "totalHarga" to totalHargaSemuaItem
            // Anda dapat menambahkan data lainnya yang ingin Anda simpan di sini.
        )

        db.collection("payment")
            .add(paymentData)
            .addOnSuccessListener { documentReference ->
                // Penanganan sukses saat data berhasil disimpan
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                // Reset totalHargaSemuaItem atau melakukan tindakan lain yang diperlukan setelah pesanan berhasil
                totalHargaSemuaItem = 0.0
                paymentList.clear()
                // Set ulang teks tvPrice menggunakan binding
                updateTotalPriceTextView()
                paymentAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                // Penanganan kesalahan jika gagal menyimpan data
                Log.w(TAG, "Error adding document", e)
            }
    }

    // Metode lainnya seperti fetchDataFromFirestore, updateTotalPriceTextView, dsb...

    companion object {
        private const val TAG = "PaymentFragment"
    }
    private fun calculateTotalSemuaItem() {
        totalSemuaItem = paymentList.sumBy { it.totalItem }
        updateTotalPriceTextView()
    }
    private fun updateTotalPriceTextView() {
        val formattedTotalPrice = String.format("Harga: Rp %.2f", totalHargaSemuaItem)
        binding.tvPrice.text = formattedTotalPrice
    }
}

