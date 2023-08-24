package com.example.ukl_kasir

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ukl_kasir.databinding.FragmentFoodBinding
import com.example.ukl_kasir.databinding.FragmentTableBinding
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TableFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TableFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentTableBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var tableAdapter: TableAdapter
    private lateinit var recyclerView: RecyclerView // Initialize the recyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTableBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the recyclerView
        recyclerView = binding.rvTable

        // Initialize other components and set up the adapter
        firestore = FirebaseFirestore.getInstance()

        // Fetch table data from Firestore and populate the tableList
        firestore.collection("table")
            .get()
            .addOnSuccessListener { result ->
                val tableList = mutableListOf<TableModel>()
                for (document in result) {
                    val tableData = document.toObject(TableModel::class.java)
                    tableList.add(tableData)
                }

                // Set up the adapter with the fetched table data
                tableAdapter = TableAdapter(tableList) { clickedTable ->
                    // Handle item click here
                    // You can open a new activity or perform any desired action
                    showToastMessage("Clicked on Table ${clickedTable.tableNum}")
                }
                recyclerView.adapter = tableAdapter
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
            }
            .addOnFailureListener { exception ->
                showError("Error fetching table data: ${exception.message}")
            }

        // Mendapatkan referensi ke FloatingActionButton dari binding
        val fabAddTable = binding.fabAddTable

        // Menambahkan OnClickListener ke FloatingActionButton
        fabAddTable.setOnClickListener {
            // Membuat Intent untuk pindah ke AddTableActivity
            val intent = Intent(requireContext(), AddTableActivity::class.java)
            startActivity(intent)
        }
    }

    // Add this function to show a toast message
    private fun showToastMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    // Add this function to show an error toast message
    private fun showError(message: String) {
        Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_SHORT).show()
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TableFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TableFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}