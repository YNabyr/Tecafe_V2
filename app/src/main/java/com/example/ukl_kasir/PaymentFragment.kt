package com.example.ukl_kasir

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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

    private lateinit var tableAdapter: ArrayAdapter<String>


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

        // Inisialisasi Spinner untuk memilih meja
        setupTableSpinner()

        paymentAdapter = PaymentAdapter(paymentList, binding)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = paymentAdapter

        // Ambil data dari Firebase Firestore
        fetchDataFromFirestore()

        // Inisialisasi Spinner untuk memilih meja


        return binding.root
    }

    private fun setupTableSpinner() {
        // Inisialisasi adapter untuk Spinner
        tableAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item
        )
        tableAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spTable.adapter = tableAdapter

        // Ambil data meja dari Firebase Firestore
        fetchAndSortTableDataFromFirestore()
    }

    private fun fetchAndSortTableDataFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        val tableCollectionRef = db.collection("table")

        tableCollectionRef.whereEqualTo("status", "Tersedia") // Filter hanya "Tersedia"
            .get()
            .addOnSuccessListener { querySnapshot: QuerySnapshot? ->
                if (querySnapshot != null) {
                    val tableList = mutableListOf<TableModel>()
                    for (document in querySnapshot.documents) {
                        val tableData = document.toObject(TableModel::class.java)
                        if (tableData != null) {
                            tableList.add(tableData)
                        }
                    }

                    // Urutkan data meja berdasarkan tableNum
                    tableList.sortBy { it.tableNum }

                    // Tambahkan "Meja" di depan setiap nomor meja
                    val tableNumWithPrefix = tableList.map { "Meja ${it.tableNum}" }

                    // Update data pada Spinner
                    tableAdapter.clear()
                    tableAdapter.addAll(tableNumWithPrefix)
                    tableAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->
                // Penanganan kesalahan jika gagal mengambil data meja
            }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


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





    private fun calculateTotalSemuaItem() {
        totalSemuaItem = paymentList.sumBy { it.totalItem }
        updateTotalPriceTextView()
    }
    private fun updateTotalPriceTextView() {
        val formattedTotalPrice = String.format("Harga: Rp %.2f", totalHargaSemuaItem)
        binding.tvPrice.text = formattedTotalPrice
    }
}

