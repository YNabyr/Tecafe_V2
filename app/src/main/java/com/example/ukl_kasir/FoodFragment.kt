package com.example.ukl_kasir

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ukl_kasir.databinding.FragmentFoodBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FoodFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FoodFragment : Fragment() {
    private lateinit var viewModel: FoodViewModel
    private lateinit var binding: FragmentFoodBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var foodAdapter: FoodAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setupRecyclerView()
        fetchDataAndUpdateAdapter()
        setupClickListeners()
    }

    private fun initViews() {
        recyclerView = binding.rvMenu
        firestore = FirebaseFirestore.getInstance()
        viewModel = ViewModelProvider(this).get(FoodViewModel::class.java)
        foodAdapter = FoodAdapter(emptyList())
    }

    private fun setupRecyclerView() {
        foodAdapter = FoodAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = foodAdapter
    }

    private fun setupClickListeners() {
        foodAdapter.setOnItemClickListener(object : FoodAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {
                // Handle item click
            }
        })

        binding.fabAddFood.setOnClickListener {
            val intent = Intent(requireContext(), AddFoodActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchDataAndUpdateAdapter() {
        if (viewModel.getFoodList().isEmpty()) {
            // Ambil data dari Firestore hanya jika data belum ada di ViewModel
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val querySnapshot = firestore.collection("menu").get().await()
                    val foodList = querySnapshot.toObjects(FoodModel::class.java)
                    launch(Dispatchers.Main) {
                        foodAdapter.updateData(foodList)
                        viewModel.setFoodList(foodList) // Simpan data ke ViewModel
                    }
                } catch (e: Exception) {
                    showError("Error fetching data: ${e.message}")
                }
            }
        } else {
            // Gunakan data yang sudah ada di ViewModel
            val cachedFoodList = viewModel.getFoodList()
            foodAdapter.updateData(cachedFoodList)
        }
    }
    private fun showError(message: String) {
        // Show an error message, e.g., using Toast
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) = FoodFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }
    }
}


