package com.example.ukl_kasir

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    // Deklarasikan ViewModel
    private lateinit var binding: FragmentTableBinding

    private lateinit var firestore: FirebaseFirestore
    private lateinit var tableAdapter: TableAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: TableViewModel // Initialize the recyclerView


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

        recyclerView = binding.rvTable
        firestore = FirebaseFirestore.getInstance()

        // Inisialisasi ViewModel
        viewModel = ViewModelProvider(this).get(TableViewModel::class.java)

        // Fetch table data from Firestore and populate the tableList
        firestore.collection("table")
            .orderBy("tableNum") // Menambahkan orderBy ke sini untuk mengurutkan berdasarkan tableNum
            .get()
            .addOnSuccessListener { result ->
                val tableList = mutableListOf<TableModel>()
                for (document in result) {
                    val tableData = document.toObject(TableModel::class.java)
                    tableList.add(tableData)
                }

                // Set data tabel menggunakan ViewModel
                viewModel.setTableData(tableList)
            }
            .addOnFailureListener { exception ->
                showError("Error fetching table data: ${exception.message}")
            }

        tableAdapter = TableAdapter { clickedTable ->
            // Handle item click here
            viewModel.updateTableStatus(clickedTable)
        }

        recyclerView.adapter = tableAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Observe changes in tableData LiveData from ViewModel
        viewModel.tableData.observe(viewLifecycleOwner, Observer { tableData ->
            // Update RecyclerView adapter with the latest data
            tableAdapter.submitList(tableData)
        })

        // Get a reference to the FloatingActionButton from the binding
        val fabAddTable = binding.fabAddTable

        val isFromKasirActivity = requireActivity() is KasirActivity

        // Hide the FloatingActionButton if TableFragment is called from KasirActivity
        if (isFromKasirActivity) {
            fabAddTable.visibility = View.GONE
        } else {
            // Add OnClickListener to the FloatingActionButton
            fabAddTable.setOnClickListener {
                val intent = Intent(requireContext(), AddTableActivity::class.java)
                startActivity(intent)
            }
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



}