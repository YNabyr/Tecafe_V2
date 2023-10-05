package com.example.ukl_kasir

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ukl_kasir.databinding.FragmentTransactionBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class TransactionFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: FragmentTransactionBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionAdapter: TransactionAdapter
    private val itemList = mutableListOf<TransactionItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTransactionBinding.inflate(inflater, container, false)
        recyclerView = binding.rvTransaksi

        transactionAdapter = TransactionAdapter(itemList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = transactionAdapter
        // Ambil data dari Firestore dan atur adapter
        fetchDataFromFirestore()

        return binding.root
    }

    private fun fetchDataFromFirestore() {
        val paymentCollectionRef = db.collection("payment")

        paymentCollectionRef.get()
            .addOnSuccessListener { querySnapshot: QuerySnapshot? ->
                if (querySnapshot != null) {
                    itemList.clear()

                    for (document in querySnapshot.documents) {
                        val tableNumber = document.getLong("tableNumber")?.toInt() ?: 0
                        val tanggalPayment = document.getString("tanggal") ?: ""
                        val statusTransaksi = document.getString("statusPembayaran") ?: ""
                        val totalItemTransaksi = document.getLong("totalItem")?.toInt() ?: 0

                        val transactionItem = TransactionItem(
                            tableNumber,
                            tanggalPayment,
                            statusTransaksi,
                            totalItemTransaksi
                        )

                        itemList.add(transactionItem)
                    }

                    transactionAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->
                // Penanganan kesalahan jika gagal mengambil data
            }
    }
}
